package org.aw20.jettydesktop.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aw20.jettydesktop.ui.ServerConfigMap;


public class ServerController {

	private String selectedServer = null;

	private static List<ServerConfigMap> serverConfigList = null;

	// create Singleton instance
	private static ServerController instance = null;


	public List<ServerConfigMap> getServerConfigListInstance() {
		if ( serverConfigList == null ) {
			serverConfigList = new ArrayList<ServerConfigMap>();
		}
		return serverConfigList;
	}


	public static ServerController getInstance() {
		if ( instance == null ) {
			instance = new ServerController();
		}
		return instance;
	}


	public String getSelectedServerInstance() {
		if ( selectedServer == null ) {
			return null;
		} else {
			return selectedServer;
		}
	}


	public void setSelectedServer( String server ) {
		selectedServer = server;
	}


	public List<ServerConfigMap> getServerConfigList() {
		return serverConfigList;
	}


	public void hardDeleteServersOnExit() {
		for ( int i = serverConfigList.size() - 1; i >= 0; --i ) {
			if ( serverConfigList.get( i ).getDeleted() == "true" ) {
				serverConfigList.remove( i );
			}
		}
		saveSettings();
	}


	public void setDeleted() {
		for ( ServerConfigMap server : getServerConfigListInstance() ) {
			if ( server.getId().equals( getSelectedServerInstance() ) ) {
				server.setDeleted( "true" );
				break;
			}
		}

		saveSettings();
	}


	public String saveServer( boolean newServer, String tempName, String tempIp, String tempPort, String tempWebFolder, String tempUri, String tempCustomJvm, boolean isCustomJvm, String tempJvmArgs, String tempMemory ) {
		String id = null;
		if ( newServer ) {
			List<Integer> ids = new ArrayList<>();
			for ( ServerConfigMap server : serverConfigList ) {
				ids.add( Integer.parseInt( server.getId() ) );
			}
			Integer i;
			if ( ids.isEmpty() ) {
				i = 0;
			} else {
				i = Collections.max( ids );
			}
			id = Integer.toString( i + 1 );
			ServerConfigMap scm = new ServerConfigMap();
			scm.setId( id );
			scm.setIP( tempIp );
			scm.setName( tempName );
			scm.setPort( tempPort );
			scm.setWebFolder( tempWebFolder );
			scm.setDeleted( "false" );
			scm.setRunning( "false" );

			if ( tempUri == "" ) {
				scm.setDefaultWebUri( tempUri );
			} else {
				scm.setDefaultWebUri( "/" );
			}

			if ( isCustomJvm ) {
				scm.setCustomJVM( tempCustomJvm );
			} else {
				scm.setCurrentJVM();
			}

			if ( tempJvmArgs != "" ) {
				scm.setDefaultJVMArgs( tempJvmArgs );
			}

			if ( tempMemory != null && !tempMemory.isEmpty() ) {
				scm.setMemoryJVM( tempMemory );
			} else {
				scm.setMemoryJVM( "64" );
			}

			// add to ServerConfigList
			serverConfigList.add( scm );
		} else {
			for ( ServerConfigMap server : serverConfigList ) {
				if ( server.getId().equals( selectedServer ) ) {
					id = server.getId();
					server.setId( selectedServer );
					server.setIP( tempIp );
					server.setName( tempName );
					server.setPort( tempPort );
					server.setWebFolder( tempWebFolder );
					server.setDeleted( "false" );
					server.setRunning( "false" );


					if ( tempUri == "" ) {
						server.setDefaultWebUri( tempUri );
					} else {
						server.setDefaultWebUri( "/" );
					}

					if ( isCustomJvm ) {
						server.setCustomJVM( tempCustomJvm );
					} else {
						server.setCurrentJVM();
					}

					if ( tempJvmArgs != "" ) {
						server.setDefaultJVMArgs( tempJvmArgs );
					}

					if ( tempMemory != null && !tempMemory.isEmpty() ) {
						server.setMemoryJVM( tempMemory );
					} else {
						server.setMemoryJVM( "64" );
					}
					break;
				}
			}
		}
		saveSettings();
		serverConfigList = null;
		loadSettings();
		return id;
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


	@SuppressWarnings( "unchecked" )
	void loadSettings() {
		ObjectInputStream ois;
		try {
			FileInputStream in = new FileInputStream( new File( "jettydesktop.settings" ) );
			ois = new ObjectInputStream( in );
			setServerConfigList( (java.util.List<org.aw20.jettydesktop.ui.ServerConfigMap>) ois.readObject() );
			in.close();

		} catch ( Exception e ) {
			setServerConfigList( new ArrayList<org.aw20.jettydesktop.ui.ServerConfigMap>() );
		}
	}


	public void setServerConfigList( List<ServerConfigMap> serverConfigList ) {
		this.serverConfigList = serverConfigList;
	}


}
