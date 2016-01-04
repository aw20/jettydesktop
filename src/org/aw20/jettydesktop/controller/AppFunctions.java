package org.aw20.jettydesktop.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.stage.DirectoryChooser;

import org.aw20.jettydesktop.ui.ConsoleLogWatcher;
import org.aw20.jettydesktop.ui.JavaPlugin;
import org.aw20.jettydesktop.ui.Plugin;
import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.util.PluginUtil;

import com.google.gson.Gson;


public class AppFunctions {

	private static Executor executor = null;
	private ServerConfigMap serverConfigMap;
	public List<ServerConfigMap> serverConfigList;
	private static Map<Integer, FileWatcher> fileWatcherList = new HashMap<Integer, FileWatcher>();
	private List<Plugin> plugins = new ArrayList<Plugin>();
	private List<String> pluginNamesList = new ArrayList<String>();

	private File tempFiles = Start.tempPluginFile;

	private static WebEngine webEngineSingleton = Start.getWebEngineInstance();

	// create Singleton instance
	private static AppFunctions instance = null;


	private AppFunctions() {
		// Exists only to defeat instantiation.
	}


	public static AppFunctions getInstance() {
		if ( instance == null ) {
			instance = new AppFunctions();
		}
		return instance;
	}


	// public methods
	public boolean getRunning( int id ) {
		for ( ServerConfigMap server : serverConfigList ) {
			if ( server.getId().equals( Integer.toString( id ) ) ) {
				if ( server.getRunning() == null ) {
					return false;
				}
				return Boolean.parseBoolean( server.getRunning() );
			}
		}
		return false;
	}


	public boolean getAllDeleted() {
		for ( ServerConfigMap server : serverConfigList ) {
			if ( server.getDeleted().equals( "false" ) ) {
				return false;
			}
		}
		return true;
	}


	public String onServerRestart( String serverId ) {
		if ( stopServer( serverId ) ) {
			if ( startServer( serverId ) ) {
				return "";
			}
			else {
				return "Server not restarted successfully";
			}
		}

		return serverId;

	}


	public String onServerStart( String serverId ) throws IOException {
		if ( startServer( serverId ) ) {
			return ( "" );
		}
		else {
			return ( "Server not started" );
		}
	}


	public String onServerStop( String serverId ) throws IOException {
		if ( stopServer( serverId ) ) {
			return "Server stopped.";
		}
		else {
			return "Server not stopped.";
		}
	}


	public String getServerConfigListAsJson() {
		boolean initialLoad = false;
		if ( serverConfigList == null ) {
			initialLoad = true;
		}
		loadSettings();
		Gson gson = new Gson();
		int count = 0;

		for ( ServerConfigMap server : serverConfigList ) {
			count++;
			server.setId( Integer.toString( count ) );
			if ( initialLoad ) {
				server.setRunning( "false" );
				server.setDeleted( "false" );
			}
		}

		return gson.toJson( serverConfigList );
	}


	public void log( String msg ) {
		System.out.println( msg );
	}


	public boolean deleteWebApp( String serverId ) { // delete server from settings
		for ( ServerConfigMap server : serverConfigList ) {
			if ( server.getId().equals( serverId ) ) {
				server.setDeleted( "true" );
				break;
			}
		}

		saveSettings();
		return true;
	}


	public void saveSettings( boolean newServer, String selectedServer, String name, String ip, String port, String webFolder, String uri, boolean defaultJvm, boolean customJvmBool, String customJvm, String jvmArgs, String memory ) {
		if ( newServer ) {
			// create new serverConfigMap instance
			ServerConfigMap scm = new ServerConfigMap();
			scm.setIP( ip );
			scm.setName( name );
			scm.setPort( port );
			scm.setWebFolder( webFolder );
			scm.setDeleted( "false" );
			scm.setRunning( "false" );

			if ( uri == "" ) {
				scm.setDefaultWebUri( uri );
			}
			else {
				scm.setDefaultWebUri( "/" );
			}

			if ( customJvmBool ) {
				scm.setCustomJVM( customJvm );
			}
			else {
				scm.setCurrentJVM();
			}

			if ( jvmArgs != "" ) {
				scm.setDefaultJVMArgs( jvmArgs );
			}

			if ( memory != null && !memory.isEmpty() ) {
				scm.setMemoryJVM( memory );
			}
			else {
				scm.setMemoryJVM( "64" );
			}

			// add to ServerConfigList
			serverConfigList.add( scm );
			saveSettings();
		}
		else {
			// find matching server
			for ( ServerConfigMap server : serverConfigList ) {
				if ( server.getId().equals( selectedServer ) ) {
					server.setId( selectedServer );
					server.setIP( ip );
					server.setName( name );
					server.setPort( port );
					server.setWebFolder( webFolder );
					server.setDeleted( "false" );
					server.setRunning( "false" );


					if ( uri == "" ) {
						server.setDefaultWebUri( uri );
					}
					else {
						server.setDefaultWebUri( "/" );
					}

					if ( customJvmBool ) {
						server.setCustomJVM( customJvm );
					}
					else {
						server.setCurrentJVM();
					}

					if ( jvmArgs != "" ) {
						server.setDefaultJVMArgs( jvmArgs );
					}

					if ( memory != null && !memory.isEmpty() ) {
						server.setMemoryJVM( memory );
					}
					else {
						server.setMemoryJVM( "64" );
					}
					break;
				}
			}

			saveSettings();
		}
		// addPluginOnNewWebapp();
	}


	public String getFolder( String dir ) {
		final DirectoryChooser directoryChooser =
				new DirectoryChooser();
		if ( !dir.isEmpty() ) {
			directoryChooser.setInitialDirectory( new File( dir ) );
		}
		final File selectedDirectory =
				directoryChooser.showDialog( Start.stage );
		if ( selectedDirectory != null ) {
			selectedDirectory.getAbsolutePath();
		}

		return selectedDirectory.toString();
	}


	public void openWebApp( String host, String defaultUri ) {
		if ( host.isEmpty() ) {
			host = "127.0.0.1";
		}

		URI uri = java.net.URI.create( "http://" + host + ":" + serverConfigMap.getPort() + defaultUri );
		goToWebpage( uri );
	}


	public void stopServers() {
		Vector v = Executor.getAllInstances();
		for ( Object executor : v ) {
			( (Executor) executor ).exit();
		}

		for ( ServerConfigMap server : serverConfigList ) {
			server.setRunning( "false" );
		}
	}


	public int getRunningApps() {
		int count = 0;

		for ( ServerConfigMap server : serverConfigList ) {
			if ( ( "true" ).equals( server.getRunning() ) ) {
				count++;
			}
		}

		return count;
	}


	public String getNameOfApp( String selectedServer ) {
		for ( ServerConfigMap server : serverConfigList ) {
			if ( server.getId().equals( selectedServer ) ) {
				return server.getName();
			}
		}
		return null;
	}


	public void openDialog() {
		int count = getRunningApps();

		String jsFunction = "window.closewindow(" + count + ");";
		webEngineSingleton.executeScript( jsFunction );
	}


	public void getButtonPressResponse( String res ) {
		if ( res.toString().equals( "yes" ) ) {
			stopServers();
			deleteServers();
			Platform.exit();
		}
	}


	public void deleteServers() {
		for ( int i = serverConfigList.size() - 1; i >= 0; --i ) {
			if ( serverConfigList.get( i ).getDeleted() == "true" ) {
				serverConfigList.remove( i );
			}
		}
		saveSettings();
	}


	public void onMemory( String line, String id ) {
		// call update memory usage on jetty desktop app
		String func = "window.memoryupdated('" + line.toString() + "', " + id + ");";
		webEngineSingleton.executeScript( func );
	}


	public void onLastUpdated( String line, String id ) {
		// call update last updated on jetty desktop app
		String jsFunction = "window.lastupdated('" + line.toString() + "', " + id + ");";
		webEngineSingleton.executeScript( jsFunction );
	}


	public String getJava() {
		return System.getProperty( "java.vm.name" ) + " " + System.getProperty( "java.version" ) + " " + System.getProperty( "java.vm.version" );
	}


	public String getJavaSystemProperties( List<String> args ) {
		// add args to String and return
		// eg return System.getProperty("java.version", "java.home");
		return System.getProperty( args.get( 0 ) );
	}


	public String getJettyVersion() {
		return Start.title;
	}


	public void goToGithub() {
		goToWebpage( java.net.URI.create( "https://github.com/aw20/jettydesktop" ) );
	}


	// private methods
	private boolean startServer( String serverId ) {
		serverConfigMap = get( serverId );
		try {
			if ( !serverConfigMap.getId().isEmpty() && !serverConfigMap.getPort().isEmpty() ) {
				executor = new Executor( serverConfigMap, this );
				serverConfigMap.setRunning( "true" );

				for ( ServerConfigMap server : serverConfigList ) {
					if ( server.getId().equals( serverId ) ) {
						server.setRunning( "true" );
					}
				}
				return true;
			}
			else {
				serverConfigMap.setRunning( "false" );
				return false;
			}
		} catch ( IOException e ) {
			serverConfigMap.setRunning( "false" );
			return false;
		}
	}


	private boolean stopServer( String serverId ) {
		for ( ServerConfigMap server : serverConfigList ) {
			if ( server.getId().equals( serverId ) ) {
				server.setRunning( "false" );
				serverConfigMap.setRunning( "false" );
			}
		}
		// get correct version of executor on exiting app and stopping all servers
		executor.exit();
		if ( executor.isbRun() == false )
			return true;
		else
			return false;
	}


	private ServerConfigMap get( String id ) {
		for ( ServerConfigMap server : serverConfigList ) {
			if ( server.getId().equals( id ) ) {
				return server;
			}
		}
		return null;
	}


	@SuppressWarnings( "unchecked" )
	private void loadSettings() {
		ObjectInputStream ois;
		try {
			FileInputStream in = new FileInputStream( new File( "jettydesktop.settings" ) );
			ois = new ObjectInputStream( in );
			serverConfigList = (java.util.List<org.aw20.jettydesktop.ui.ServerConfigMap>) ois.readObject();
			in.close();

		} catch ( Exception e ) {
			e.printStackTrace();
			serverConfigList = new ArrayList<ServerConfigMap>();
		}
	}


	private void saveSettings() {
		ObjectOutputStream OS;
		try {
			FileOutputStream out = new FileOutputStream( new File( "jettydesktop.settings" ) );
			OS = new ObjectOutputStream( out );
			OS.writeObject( serverConfigList );
			out.flush();
			out.close();
		} catch ( IOException e ) {}

	}


	private void goToWebpage( URI page ) {
		if ( Desktop.isDesktopSupported() ) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse( page );
			} catch ( IOException e ) {
				// do nothing
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec( "xdg-open " + page );
			} catch ( IOException e ) {}
		}
	}


	protected void setConsoleText( String id, String line ) {
		webEngineSingleton.executeScript( "document.getElementById('console_" + id + "').innerHTML += '<pre>" + line + "</pre>';" );
		webEngineSingleton.executeScript( "document.getElementById('console_" + id + "').scrollTop = document.getElementById('console_" + id + "').scrollHeight;" );
	}


	// PLUGINS
	private void setUpPlugins( List<ServerConfigMap> scm ) {
		for ( String plugin : pluginNamesList ) {
			for ( ServerConfigMap server : scm ) {
				setUpPluginForServer( server, plugin );
			}
		}
	}


	private void addFilesToHeader( Plugin p ) {
		if ( p.getCss() != null ) {
			String filePath = "plugins/" + p.getCss().getName().replace( "\\", "/" );
			String func = "window.addFileToHeader('" + filePath + "', 'css');";
			webEngineSingleton.executeScript( func );
		}
		if ( p.getJavascript() != null ) {
			String filePath = "plugins/" + p.getJavascript().getName().replace( "\\", "/" );
			String func = "window.addFileToHeader('" + filePath + "', 'js');";
			webEngineSingleton.executeScript( func );
		}
	}


	public boolean checkForPluginUpdate( String log, String selectedServer ) {
		for ( Map.Entry<Integer, FileWatcher> watcher : fileWatcherList.entrySet() ) {
			if ( selectedServer.equals( watcher.getKey() ) )
				if ( watcher.getValue().getNeedsUpdating().get() )
					return true;
		}
		return false;
	}


	// FOR LOG FILE PLUGINS
	public void findFile( String log, String selectedServer ) {
		String webFolder = "";

		for ( ServerConfigMap server : serverConfigList ) {
			if ( server.getId().equals( selectedServer ) ) {
				webFolder = server.getWebFolder();
			}
		}

		PluginUtil.listOfFiles.clear();
		PluginUtil.findFileByName( log, webFolder );

		if ( !PluginUtil.listOfFiles.isEmpty() ) {
			File latestLog = PluginUtil.getLatestFile();
			String content = PluginUtil.getPluginContent( latestLog );
			updatePlugin( latestLog.getName(), content, selectedServer );
		}

	}


	public static void updatePlugin( String file, String content, String filename ) {
		String jsFriendlyName = file.replace( ".", "dot" );
		String contentSplit[] = content.split( "\\r?\\n" );

		Platform.runLater( new Runnable() {

			public void run() {
				String serverId = "";
				if ( filename != null ) {
					serverId = filename;
				}
				else {
					for ( Map.Entry<Integer, FileWatcher> watcher : fileWatcherList.entrySet() ) {
						if ( file.equals( watcher.getValue().getFile() ) )
							serverId = Integer.toString( watcher.getKey() );
					}
				}
				for ( String s : contentSplit ) {
					s = s.replace( "'", "\\'" );
					String func = "window.pushToPluginView('" + jsFriendlyName + "', '" + s + "', '" + serverId + "');";
					try {
						webEngineSingleton.executeScript( func );
					} catch ( Throwable e ) {
						// e.printStackTrace();
					}
				}
			}
		} );
	}


	// PLUGIN METHODS TO UPDATE
	public void initialisePlugins() {
		if ( pluginNamesList.isEmpty() ) {
			try {
				// TODO: add plugin file name to pluginNamesList
				pluginNamesList.add( "plugin-java-details.java.html" );
				pluginNamesList.add( "plugin-bluedragon.log.html" );
			} catch ( Throwable e ) {
				e.printStackTrace();
			}
		}

		// empty plugins variable
		plugins.clear();
		setUpPlugins( serverConfigList );
		// add all plugins to html
		if ( !plugins.isEmpty() ) {
			addPluginsToJetty();
		}

		for ( Map.Entry<Integer, FileWatcher> watcher : fileWatcherList.entrySet() ) {
			watcher.getValue().start();
		}
	}


	private void setUpPluginForServer( ServerConfigMap server, String pluginName ) {
		PluginUtil.listOfFiles.clear();
		// initialise local variables`
		File blueDragonPlugin = PluginUtil.findFileByName( pluginName, tempFiles.getAbsolutePath() );
		String name = PluginUtil.getPluginName( blueDragonPlugin );
		File file = PluginUtil.getFile( blueDragonPlugin.getAbsolutePath() );
		String serverId = server.getId();
		File css = null;
		File js = null;

		// get css if exists
		if ( PluginUtil.getAdditionalFilesForPlugin( tempFiles, name, "css" ) != null ) {
			css = PluginUtil.getAdditionalFilesForPlugin( tempFiles, name, "css" ).get( 0 );
		}
		// get javascript if exists
		if ( PluginUtil.getAdditionalFilesForPlugin( tempFiles, name, "js" ) != null ) {
			js = PluginUtil.getAdditionalFilesForPlugin( tempFiles, name, "js" ).get( 0 );
		}

		// TODO: add branch here to update jetty view
		if ( name.contains( ".log" ) ) {
			// get plugin details for specific server
			String webFolder = server.getWebFolder(); // current webapp folder
			String plname = PluginUtil.getPluginName( blueDragonPlugin ); // plugin name eg. bluedragon.log
			PluginUtil.findFileByName( plname, webFolder ); // find bluedragon.log in webapp folder

			// add watcher to list of watchers if log exists
			if ( !PluginUtil.listOfFiles.isEmpty() ) {
				File latestLog = PluginUtil.getLatestFile();
				fileWatcherList.put( Integer.parseInt( server.getId() ), new FileWatcher( latestLog ) );
			}
			else {
				// add log file doesn't exist message
			}
			// TODO: use new class to add new plugin
			Plugin plugin = new ConsoleLogWatcher( name, file, serverId, css, js );
			plugins.add( plugin );
		}
		// TODO: use new class to add new plugin
		else if ( name.contains( ".java" ) ) {
			Plugin plugin = new JavaPlugin( name, file, serverId, css, js );
			plugins.add( plugin );
		}
	}


	public void addPluginsToJetty() {
		for ( Plugin p : plugins ) {

			String pContent = PluginUtil.getPluginContent( p.getHtml() );

			String jsFriendlyName = p.getName().replace( ".", "dot" );
			String func = "window.getPluginTab('" + p.getName() + "', '" + jsFriendlyName + "', '" + p.getId() + "');";
			webEngineSingleton.executeScript( func );

			func = "window.getPluginView('" + jsFriendlyName + "', '" + pContent + "', '" + p.getId() + "');";
			webEngineSingleton.executeScript( func );

			addFilesToHeader( p );

			// TODO: add branch here to update jetty view
			if ( p.getName().contains( "log" ) ) {
				// insert initial log into view
				findFile( p.getName(), p.getId() );
			}
			else if ( p.getName().contains( ".java" ) ) {
				List<String> javaArgs = new ArrayList<>();
				javaArgs.add( "java.version" );

				updatePlugin( p.getName(), getJavaSystemProperties( javaArgs ), p.getId() );
			}
		}
	}
}