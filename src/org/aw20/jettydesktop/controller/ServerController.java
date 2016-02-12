package org.aw20.jettydesktop.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.jettydesktop.ui.ServerWrapper;


public class ServerController {

	static int selectedServer;
	private static ServerController instance = null;


	public void setSelectedServer( int savedServerId ) {
		selectedServer = savedServerId;
	}


	public void hardDeleteServersOnExit() {
		for ( Entry<Integer, ServerWrapper> entry : ServerManager.servers.entrySet() ) {
			if ( entry.getValue().isDeleted() ) {
				ServerManager.servers.remove( entry );
			}
		}
		saveSettings();
	}


	// sets soft delete in ServerWrapper
	public void setDeleted() {
		ServerManager.servers.get( selectedServer ).setDeleted( true );
		saveSettings();
	}


	// produces ID for new server
	public int getNewId() {
		Set<Integer> ids = ServerManager.servers.keySet();
		if ( ids.isEmpty() ) {
			return 1;
		} else {
			return Collections.max( ServerManager.servers.keySet() ) + 1;
		}
	}


	public int saveServer( boolean newServer, String tempName, String tempIp, String tempPort, String tempWebFolder, String tempUri, String tempCustomJvm, boolean isCustomJvm, String tempJvmArgs, String tempMemory ) {
		int id;
		if ( newServer ) {
			// get new id
			id = getNewId();
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
			ServerWrapper serverWrapper = new ServerWrapper( id, scm );
			ServerManager.servers.put( id, serverWrapper );

		} else {
			id = selectedServer;
			ServerWrapper serverWrapper = ServerManager.servers.get( id );

			// overwrite existing data
			serverWrapper.getServerConfigMap().setIP( tempIp );
			serverWrapper.getServerConfigMap().setName( tempName );
			serverWrapper.getServerConfigMap().setPort( tempPort );
			serverWrapper.getServerConfigMap().setWebFolder( tempWebFolder );

			if ( tempUri == "" ) {
				serverWrapper.getServerConfigMap().setDefaultWebUri( tempUri );
			} else {
				serverWrapper.getServerConfigMap().setDefaultWebUri( "/" );
			}

			if ( isCustomJvm ) {
				serverWrapper.getServerConfigMap().setCustomJVM( tempCustomJvm );
			} else {
				serverWrapper.getServerConfigMap().setCurrentJVM();
			}

			if ( tempJvmArgs != "" ) {
				serverWrapper.getServerConfigMap().setDefaultJVMArgs( tempJvmArgs );
			}

			if ( tempMemory != null && !tempMemory.isEmpty() ) {
				serverWrapper.getServerConfigMap().setMemoryJVM( tempMemory );
			} else {
				serverWrapper.getServerConfigMap().setMemoryJVM( "64" );
			}
		}
		saveSettings();

		return id;
	}


	public void loadSettings() {
		List<ServerConfigMap> serverConfigList = new ArrayList<ServerConfigMap>();
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

		ServerManager.servers = new HashMap<Integer, ServerWrapper>();

		int count = 1;
		for ( ServerConfigMap scm : serverConfigList ) {
			ServerManager.servers.put( count, new ServerWrapper( count, scm ) );
			count++;
		}
	}


	public void saveSettings() {
		List<ServerConfigMap> scmList = new ArrayList<ServerConfigMap>();
		for ( Entry<Integer, ServerWrapper> serverWrapper : ServerManager.servers.entrySet() ) {
			scmList.add( serverWrapper.getValue().getServerConfigMap() );
		}
		ObjectOutputStream OS;
		try {
			FileOutputStream out = new FileOutputStream( new File( "jettydesktop.settings" ) );
			OS = new ObjectOutputStream( out );
			OS.writeObject( scmList );
			out.flush();
			out.close();
		} catch ( IOException e ) {}
	}
}
