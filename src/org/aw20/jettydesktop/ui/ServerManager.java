package org.aw20.jettydesktop.ui;

import java.util.Map;


public class ServerManager {

	public static Map<Integer, ServerWrapper> servers = null;


	public Map<Integer, ServerWrapper> getServers() {
		return servers;
	}


	public void setServers( Map<Integer, ServerWrapper> servers ) {
		this.servers = servers;
	}
}
