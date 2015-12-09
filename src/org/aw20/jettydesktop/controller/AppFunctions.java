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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.web.WebEngine;
import javafx.stage.DirectoryChooser;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.util.StringUtils;

import com.google.gson.Gson;


public class AppFunctions {

	private static Executor executor = null;
	private ServerConfigMap serverConfigMap;
	public List<ServerConfigMap> serverConfigList;
	StringUtils comparator = new StringUtils();

	private WebEngine webEngineSingleton = Start.getWebEngineInstance();

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
		int count = 0;
		for ( Iterator<ServerConfigMap> iter = serverConfigList.iterator(); iter.hasNext(); ) {
			if ( serverConfigList.get( count ).getId().equals( Integer.toString( id ) ) ) {
				if ( serverConfigList.get( count ).getRunning() == null ) {
					return false;
				}
				boolean running = Boolean.parseBoolean( serverConfigList.get( count ).getRunning() );
				return running;
			}
			count++;
		}
		return false;
	}


	public String onServerRestart( String serverId ) {
		// boolean stop = stopServer( serverId );
		boolean started;
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
		for ( Iterator<ServerConfigMap> iter = serverConfigList.iterator(); iter.hasNext(); ) {
			count++;
			ServerConfigMap element = iter.next();
			element.setId( Integer.toString( count ) );
			if ( initialLoad ) {
				element.setRunning( "false" );
				element.setDeleted( "false" );
			}
		}

		return gson.toJson( serverConfigList );
	}


	public void outputToEclipse( String msg ) {
		System.out.println( msg );
	}


	public boolean deleteWebApp( String serverId ) {
		// delete server from settings

		Alert alert = new Alert( AlertType.CONFIRMATION, "Are you sure you want to delete this webapp?", ButtonType.YES, ButtonType.NO );
		Optional<ButtonType> result = alert.showAndWait();

		if ( result.get() == ButtonType.YES ) {
			ServerConfigMap serverConfigMap = get( serverId );// should be current open tab in console

			// Remove from the list
			for ( int x = 0; x < serverConfigList.size(); x++ ) {
				if ( serverConfigList.get( x ).getName().equals( serverConfigMap.getName() ) ) {
					// serverConfigList.remove( x );
					serverConfigList.get( x ).setDeleted( "true" );
					break;
				}
			}

			saveSettings();
			return true;
		}

		return false;
		// else do nothing

	}


	public void saveSettings( boolean newServer, String selectedServer, String name, String ip, String port, String webFolder, String uri, boolean defaultJvm, boolean customJvmBool, String customJvm, String jvmArgs, String memory ) {
		if ( newServer ) {
			// create new serverConfigMap instance
			ServerConfigMap scm = new ServerConfigMap();
			scm.setIP( ip );
			scm.setName( name );
			scm.setPort( port );
			scm.setWebFolder( webFolder );

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
			Iterator<ServerConfigMap> it = serverConfigList.iterator();
			while ( it.hasNext() ) {
				ServerConfigMap currentServer = it.next();
				if ( currentServer.getId().equals( selectedServer ) ) {
					currentServer.setId( selectedServer );
					currentServer.setIP( ip );
					currentServer.setName( name );
					currentServer.setPort( port );
					currentServer.setWebFolder( webFolder );

					if ( uri == "" ) {
						currentServer.setDefaultWebUri( uri );
					}
					else {
						currentServer.setDefaultWebUri( "/" );
					}

					if ( customJvmBool ) {
						currentServer.setCustomJVM( customJvm );
					}
					else {
						currentServer.setCurrentJVM();
					}

					if ( jvmArgs != "" ) {
						currentServer.setDefaultJVMArgs( jvmArgs );
					}

					if ( memory != null && !memory.isEmpty() ) {
						currentServer.setMemoryJVM( memory );
					}
					else {
						currentServer.setMemoryJVM( "64" );
					}
					break;
				}
			}
			saveSettings();
		}
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


	public void openWebApp( String ip, String defaultUri ) {
		String host = ip;
		if ( ip.isEmpty() ) {
			host = "127.0.0.1";
		}

		if ( Desktop.isDesktopSupported() ) {
			Desktop desktop = Desktop.getDesktop();
			try {
				URI uri = java.net.URI.create( "http://" + host + ":" + serverConfigMap.getPort() + defaultUri );
				desktop.browse( uri );
			} catch ( IOException e ) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec( "xdg-open " + java.net.URI.create( "http://" + host + ":" + serverConfigMap.getPort() + defaultUri ) );
			} catch ( IOException e ) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

	}


	// protected methods to access from JettyDesktopApp
	public void stopServers() {
		Vector v = Executor.getAllInstances();
		for ( Object e : v ) {
			( (Executor) e ).exit();
		}

		for ( int i = 0; i < serverConfigList.size(); ++i ) {
			serverConfigMap = serverConfigList.get( i );
			serverConfigMap.setRunning( "false" );
		}
	}


	public void exit() {
		for ( int i = serverConfigList.size() - 1; i >= 0; --i ) {
			if ( serverConfigList.get( i ).getDeleted() == "true" ) {
				serverConfigList.remove( i );
			}
		}
		saveSettings();
	}


	// TODO
	public void onMemory( String line, String id ) {
		// call update memory usage on jetty desktop app
		String func = "window.memoryupdated('" + line.toString() + "', " + id + ");";
		webEngineSingleton.executeScript( func );
	}


	// TODO
	public void onLastUpdated( String line, String id ) {
		// call update last updated on jetty desktop app
		String func = "window.lastupdated('" + line.toString() + "', " + id + ");";
		webEngineSingleton.executeScript( func );
	}


	// private methods
	private boolean startServer( String serverId ) {
		serverConfigMap = get( serverId );
		try {
			if ( !serverConfigMap.getId().isEmpty() && !serverConfigMap.getPort().isEmpty() ) {
				executor = new Executor( serverConfigMap, this );
				serverConfigMap.setRunning( "true" );

				for ( int i = 0; i < serverConfigList.size(); ++i ) {
					if ( serverConfigList.get( i ).getId().equals( serverId ) ) {
						serverConfigList.get( i ).setRunning( "true" );
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
		for ( int i = 0; i < serverConfigList.size(); ++i ) {
			if ( serverConfigList.get( i ).getId().equals( serverId ) ) {
				serverConfigMap = serverConfigList.get( i );
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
		// loadSettings();
		int count = 0;
		for ( Iterator<ServerConfigMap> iter = serverConfigList.iterator(); iter.hasNext(); ) {
			count++;
			ServerConfigMap element = iter.next();
			element.setId( Integer.toString( count ) );
			// element.setRunning("false");

		}
		count = 0;
		for ( Iterator<ServerConfigMap> iter = serverConfigList.iterator(); iter.hasNext(); ) {
			if ( serverConfigList.get( count ).getId().equals( id ) ) {
				return serverConfigList.get( count );
			}
			count++;
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
			serverConfigList = new ArrayList<ServerConfigMap>();
		}
		// Collections.sort(serverConfigList, comparator);
	}


	private void saveSettings() {
		// TODO Auto-generated method stub
		ObjectOutputStream OS;
		try {
			FileOutputStream out = new FileOutputStream( new File( "jettydesktop.settings" ) );
			OS = new ObjectOutputStream( out );
			OS.writeObject( serverConfigList );
			out.flush();
			out.close();
		} catch ( IOException e ) {}

	}


	public String getJava() {
		String text = System.getProperty( "java.vm.name" ) + " " + System.getProperty( "java.version" ) + " " + System.getProperty( "java.vm.version" );
		return text;
	}

}