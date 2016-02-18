/* 
 *  JettyDesktop is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  JettyDesktop is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  If not, see http://www.gnu.org/licenses/
 *  
 *  Additional permission under GNU GPL version 3 section 7
 *  
 *  If you modify this Program, or any covered work, by linking or combining 
 *  it with any of the JARS listed in the README.txt (or a modified version of 
 *  (that library), containing parts covered by the terms of that JAR, the 
 *  licensors of this Program grant you additional permission to convey the 
 *  resulting work. 
 *  
 *  https://github.com/aw20/jettydesktop
 *  
 *  February 2016
 */
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
		if ( tempName.equals( "" ) ) {
			return false;
		}
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
