package org.aw20.jettydesktop.ui;

import java.util.HashMap;
import java.util.Map;


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
}
