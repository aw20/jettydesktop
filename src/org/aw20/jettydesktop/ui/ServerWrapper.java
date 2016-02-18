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

/*
 * Class to set and get server attributes 
 * Contains id, running boolean and ServerConfigMap
 */
public class ServerWrapper {

	private Integer id;
	private boolean running;
	private ServerConfigMap serverConfigMap;


	public ServerWrapper( Integer id, ServerConfigMap serverConfigMap ) {
		this.setId( id );
		this.setRunning( false );
		this.setServerConfigMap( serverConfigMap );
	}


	public ServerConfigMap getServerConfigMap() {
		return serverConfigMap;
	}


	public void setServerConfigMap( ServerConfigMap serverConfigMap ) {
		this.serverConfigMap = serverConfigMap;
	}


	public boolean isRunning() {
		return running;
	}


	public void setRunning( boolean running ) {
		this.running = running;
	}


	public Integer getId() {
		return id;
	}


	public void setId( Integer id ) {
		this.id = id;
	}
}
