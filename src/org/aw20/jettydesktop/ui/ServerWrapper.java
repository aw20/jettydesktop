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
