package org.aw20.jettydesktop.ui;

import java.util.HashMap;
import java.util.Map;


/*
 * Class to add, remove and get ServerWrappers
 * Contains servers map
 */
public class ServerManager {

	private static Map<Integer, ServerWrapper> servers = null;


	public ServerManager() {
		servers = new HashMap<Integer, ServerWrapper>();
	}


	public static Map<Integer, ServerWrapper> getServers() {
		return servers;
	}


	public static void setServers( Map<Integer, ServerWrapper> servers ) {
		ServerManager.servers = servers;
	}


	public static void addServer( int id, ServerWrapper server ) {
		servers.put( id, server );
	}


	public static void removeServer( int id ) {
		servers.remove( id );
	}
}
