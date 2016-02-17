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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.jettydesktop.ui.ServerWrapper;


/*
 * Class to control server actions such as save, delete, load. 
 * Contains current selected server in app
 */
public class ServerController {

	private int selectedServer;
	private ServerManager serverManager;


	public ServerController( ServerManager _serverManager ) {
		serverManager = _serverManager;
	}


	public void setSelectedServer( int savedServerId ) {
		selectedServer = savedServerId;
	}


	public int getSelectedServer() {
		return selectedServer;
	}


	// deleted ServerWrapper from server list and saves settings
	public void setDeleted() {
		serverManager.removeServer( selectedServer );
		saveSettings();
	}


	// produces ID for new server
	public int getNewId() {
		Set<Integer> ids = serverManager.getServers().keySet();
		if ( ids.isEmpty() ) {
			return 1;
		} else {
			return Collections.max( serverManager.getServers().keySet() ) + 1;
		}
	}


	public int saveServer( boolean newServer, Map<String, String> tempSettingsVariables, boolean isCustomJvm ) {
		int id;
		if ( newServer ) {
			// get new id
			id = getNewId();
			ServerConfigMap scm = new ServerConfigMap();

			scm.setIP( tempSettingsVariables.get( "tempIp" ) );
			scm.setName( tempSettingsVariables.get( "tempName" ) );
			scm.setPort( tempSettingsVariables.get( "tempPort" ) );
			scm.setWebFolder( tempSettingsVariables.get( "tempWebFolder" ) );

			if ( tempSettingsVariables.get( "tempUri" ) == "" ) {
				scm.setDefaultWebUri( tempSettingsVariables.get( "tempUri" ) );
			} else {
				scm.setDefaultWebUri( "/" );
			}

			if ( isCustomJvm ) {
				scm.setCustomJVM( tempSettingsVariables.get( "tempCustomJvm" ) );
			} else {
				scm.setCurrentJVM();
			}

			if ( tempSettingsVariables.get( "tempJvmArgs" ) != "" ) {
				scm.setDefaultJVMArgs( tempSettingsVariables.get( "tempJvmArgs" ) );
			}

			if ( tempSettingsVariables.get( "tempMemory" ) != null && !tempSettingsVariables.get( "tempMemory" ).isEmpty() ) {
				scm.setMemoryJVM( tempSettingsVariables.get( "tempMemory" ) );
			} else {
				scm.setMemoryJVM( "64" );
			}
			ServerWrapper serverWrapper = new ServerWrapper( id, scm );
			serverManager.getServers().put( id, serverWrapper );

		} else {
			id = selectedServer;
			ServerWrapper serverWrapper = serverManager.getServers().get( id );

			// overwrite existing data
			serverWrapper.getServerConfigMap().setIP( tempSettingsVariables.get( "tempIp" ) );
			serverWrapper.getServerConfigMap().setName( tempSettingsVariables.get( "tempName" ) );
			serverWrapper.getServerConfigMap().setPort( tempSettingsVariables.get( "tempPort" ) );
			serverWrapper.getServerConfigMap().setWebFolder( tempSettingsVariables.get( "tempWebFolder" ) );

			if ( tempSettingsVariables.get( "tempUri" ) == "" ) {
				serverWrapper.getServerConfigMap().setDefaultWebUri( tempSettingsVariables.get( "tempUri" ) );
			} else {
				serverWrapper.getServerConfigMap().setDefaultWebUri( "/" );
			}

			if ( isCustomJvm ) {
				serverWrapper.getServerConfigMap().setCustomJVM( tempSettingsVariables.get( "tempCustomJvm" ) );
			} else {
				serverWrapper.getServerConfigMap().setCurrentJVM();
			}

			if ( tempSettingsVariables.get( "tempJvmArgs" ) != "" ) {
				serverWrapper.getServerConfigMap().setDefaultJVMArgs( tempSettingsVariables.get( "tempJvmArgs" ) );
			}

			if ( tempSettingsVariables.get( "tempMemory" ) != null && !tempSettingsVariables.get( "tempMemory" ).isEmpty() ) {
				serverWrapper.getServerConfigMap().setMemoryJVM( tempSettingsVariables.get( "tempMemory" ) );
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

		serverManager.setServers( new HashMap<Integer, ServerWrapper>() );

		int count = 1;
		for ( ServerConfigMap scm : serverConfigList ) {
			serverManager.addServer( count, new ServerWrapper( count, scm ) );
			count++;
		}
	}


	private void saveSettings() {
		List<ServerConfigMap> scmList = new ArrayList<ServerConfigMap>();
		for ( Entry<Integer, ServerWrapper> serverWrapper : serverManager.getServers().entrySet() ) {
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
