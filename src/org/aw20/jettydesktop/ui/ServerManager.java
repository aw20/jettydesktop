package org.aw20.jettydesktop.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/*
 * Class to add, remove and get ServerWrappers
 * Contains servers map
 */
public class ServerManager {

	private Map<Integer, ServerWrapper> servers = null;


	public ServerManager() {
		servers = new HashMap<Integer, ServerWrapper>();
	}


	public Map<Integer, ServerWrapper> getServers() {
		return servers;
	}


	public void setServers( Map<Integer, ServerWrapper> _servers ) {
		servers = _servers;
	}


	public void addServer( int id, ServerWrapper server ) {
		servers.put( id, server );
	}


	public void removeServer( int id ) {
		servers.remove( id );
	}


	public int getNumberOfRunningServers() {
		int count = 0;
		for ( Entry<Integer, ServerWrapper> serverWrapper : servers.entrySet() ) {
			if ( serverWrapper.getValue().isRunning() ) {
				count++;
			}
		}

		return count;
	}


	public boolean isValidServerName( String tempName, String settingsId ) {
		/*
		 * if name == server name that you're changing - ie remains unchanged, return true
		 * if name == server name in settings return false
		 * if name != server name in settings return true
		 */
		for ( Entry<Integer, ServerWrapper> server : getServers().entrySet() ) {
			if ( server.getValue().getServerConfigMap().getName().toLowerCase().equals( tempName.toLowerCase() ) ) {

				if ( !settingsId.equals( String.valueOf( server.getKey() ) ) ) {
					return false;
				}
			}
		}

		return true;
	}
}
