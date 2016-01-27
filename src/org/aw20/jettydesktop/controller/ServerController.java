package org.aw20.jettydesktop.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.jettydesktop.ui.ServerWrapper;


public class ServerController {

	private String selectedServer = null;
	private List<ServerConfigMap> serverConfigList;
	private static ServerController instance = null;


	// create Singleton instance
	public static ServerController getInstance() {
		if ( instance == null ) {
			synchronized ( ServerController.class ) {
				if ( instance == null ) {
					instance = new ServerController();
				}
			}
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


	public void hardDeleteServersOnExit() {

		List<String> listToBeDeleted = ServerWrapper.getInstance().getAllDeleted();
		for ( String itemToBeDeleted : listToBeDeleted ) {
			ServerWrapper.getInstance().getServerConfigObject().remove( itemToBeDeleted );
		}

		saveSettings();
	}


	// sets soft delete in ServerWrapper
	public void setDeleted() {

		ServerWrapper.getInstance().setDeleted( getSelectedServerInstance(), true );

		saveSettings();
	}


	public String saveServer( boolean newServer, String tempName, String tempIp, String tempPort, String tempWebFolder, String tempUri, String tempCustomJvm, boolean isCustomJvm, String tempJvmArgs, String tempMemory ) {
		String id = null;
		if ( newServer ) {
			// get new id
			id = ServerWrapper.getInstance().getNewId();
			ServerConfigMap scm = new ServerConfigMap();

			scm.setIP( tempIp );
			scm.setName( tempName );
			scm.setPort( tempPort );
			scm.setWebFolder( tempWebFolder );

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
			// add server id, server, running = false and deleted = false to ServerWrapper
			ServerWrapper.getInstance().setDeleted( id, false );
			ServerWrapper.getInstance().setRunning( id, false );
			ServerWrapper.getInstance().setServer( id, scm );

		} else {
			id = selectedServer;
			ServerConfigMap server = ServerWrapper.getInstance().getServerConfigObject().get( id );

			// overwrite existing data
			server.setIP( tempIp );
			server.setName( tempName );
			server.setPort( tempPort );
			server.setWebFolder( tempWebFolder );
			// set running = false and deleted = false in ServerWrapper - should already be false
			ServerWrapper.getInstance().setDeleted( id, false );
			ServerWrapper.getInstance().setRunning( id, false );

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

			ServerWrapper.getInstance().setServer( id, server );
		}
		saveSettings();

		return id;
	}


	private void saveSettings() {
		ObjectOutputStream OS;
		try {
			FileOutputStream out = new FileOutputStream( new File( "jettydesktop.settings" ) );
			OS = new ObjectOutputStream( out );
			OS.writeObject( ServerWrapper.getInstance().getListOfServerConfigMap() );
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
			serverConfigList = (java.util.List<org.aw20.jettydesktop.ui.ServerConfigMap>) ois.readObject();
			// remove settings from previous implementations
			for ( ServerConfigMap server : serverConfigList ) {
				server.remove( "DELETED" );
				server.remove( "RUNNING" );
				server.remove( "SERVER_ID" );
			}
			in.close();

		} catch ( Exception e ) {
			serverConfigList = new ArrayList<org.aw20.jettydesktop.ui.ServerConfigMap>();
		}
		ServerWrapper.getInstance().loadSettingsIntoServerConfig( serverConfigList );
	}
}
