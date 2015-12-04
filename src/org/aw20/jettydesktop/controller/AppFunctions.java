package org.aw20.jettydesktop.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
		loadSettings();
		Gson gson = new Gson();
		int count = 0;
		for ( Iterator<ServerConfigMap> iter = serverConfigList.iterator(); iter.hasNext(); ) {
			count++;
			ServerConfigMap element = iter.next();
			element.setId( Integer.toString( count ) );

		}

		return gson.toJson( serverConfigList );
	}


	public void outputToEclipse( String msg ) {
		System.out.println( msg );
	}


	public void deleteWebApp( String serverId ) {
		// delete server from settings
		ServerConfigMap serverConfigMap = get( serverId );// should be current open tab in console

		// Remove from the list
		for ( int x = 0; x < serverConfigList.size(); x++ ) {
			if ( serverConfigList.get( x ).getName().equals( serverConfigMap.getName() ) ) {
				serverConfigList.remove( x );
				break;
			}
		}

		saveSettings();
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


	public String getFolder() {
		final DirectoryChooser directoryChooser =
				new DirectoryChooser();
		final File selectedDirectory =
				directoryChooser.showDialog( Start.stage );
		if ( selectedDirectory != null ) {
			selectedDirectory.getAbsolutePath();
		}

		return selectedDirectory.toString();
	}


	public void openWebApp( String host, String defaultUri ) {
		try {
			Desktop.getDesktop().browse( java.net.URI.create( "http://" + host + ":" + serverConfigMap.getPort() + defaultUri ) );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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


	// TODO
	public void onMemory( String line ) {
		// call update memory usage on jetty desktop app
		// Platform
		 webEngineSingleton.executeScript("$('#id1:first-child').text = '" + line + "'");
	}


	// TODO
	public void onLastUpdated( String line ) {
		// call update last updated on jetty desktop app
		webEngineSingleton.executeScript( "$('#id1:first-child').text = 'Last Updated: " + line + "'" );
		//webEngineSingleton.executeScript( "app.outputToEclipse(document.documentElement.innerHTML);" );
	}


	// private methods
	private boolean startServer( String serverId ) {
		serverConfigMap = get( serverId );
		try {
			executor = new Executor( serverConfigMap, this );
			serverConfigMap.setRunning( "true" );

			for ( int i = 0; i < serverConfigList.size(); ++i ) {
				if ( serverConfigList.get( i ).getId().equals( serverId ) ) {
					serverConfigList.get( i ).setRunning( "true" );
				}
			}
			return true;

		} catch ( IOException e ) {
			// webEngineSingleton.executeScript("document.getElementById('console_" + serverConfigMap.getId() + "').innerHTML += 'Port#" + serverConfigMap.getPort() + " appears to be in use already';");
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