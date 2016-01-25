package org.aw20.jettydesktop.ui;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gson.Gson;


public class ServerConfigMap extends HashMap<String, String> implements Serializable {

	private static final long serialVersionUID = 1L;


	public ServerConfigMap() {}


	ServerConfigMap( String name, String ip, String port, String folder, String jvm, String memory ) {
		this.setName( name );
		this.setIP( ip );
		this.setPort( port );
		this.setWebFolder( folder );
		this.setMemoryJVM( memory );
		if ( jvm != null ) {
			this.setCustomJVM( jvm );
		}
		else {
			this.setCurrentJVM();
		}
	}


	@SuppressWarnings( "serial" )
	public static ServerConfigMap getDefault() {
		return new ServerConfigMap() {

			{
				setId( "" );
				setName( "" );
				setIP( "127.0.0.1" );
				setPort( "80" );
				setWebFolder( "" );
				setCurrentJVM();
				setMemoryJVM( "64" );
			}
		};
	}


	public void setName( String name ) {
		put( "SERVER_NAME", name );
	}


	public String getName() {
		return get( "SERVER_NAME" );
	}


	public void setDeleted( String del ) {
		put( "DELETED", del );
	}


	public String getDeleted() {
		return get( "DELETED" );
	}


	public void setIP( String ip ) {
		put( "SERVER_IP", ip );
	}


	public String getIP() {
		return get( "SERVER_IP" );
	}


	public void setPort( String port ) {
		put( "SERVER_PORT", port );
	}


	public String getPort() {
		return get( "SERVER_PORT" );
	}


	public void setWebFolder( String webfolder ) {
		put( "WEBFOLDER", webfolder );
	}


	public String getWebFolder() {
		return get( "WEBFOLDER" );
	}


	public void setCustomJVM( String customFolder ) {
		put( "CUSTOMJVM", customFolder );
		remove( "CURRENTJVM" );
	}


	public String getCustomJVM() {
		return get( "CUSTOMJVM" );
	}


	public void setCurrentJVM() {
		put( "CURRENTJVM", "1" );
		remove( "CUSTOMJVM" );
	}


	public String getCurrentJVM() {
		return get( "CURRENTJVM" );
	}


	public void setMemoryJVM( String memory ) {
		put( "MEMORYJVM", memory );
	}


	public String getMemoryJVM() {
		return get( "MEMORYJVM" );
	}


	public String getDefaultJVMArgs() {
		return get( "DEFAULTJVM" );
	}


	public String getDefaultWebUri() {
		return get( "DEFAULTURI" );
	}


	public void setDefaultJVMArgs( String args ) {
		put( "DEFAULTJVM", args );
	}


	public void setDefaultWebUri( String args ) {
		put( "DEFAULTURI", args.trim() );
	}


	public void setId( String args ) {
		put( "SERVER_ID", args );
	}


	public String getId() {
		return get( "SERVER_ID" );
	}


	public String getRunning() {
		return get( "RUNNING" );
	}


	public void setRunning( String args ) {
		put( "RUNNING", args );
	}


	public String toJson() {
		Gson gson = new Gson();
		return ( gson.toJson( this ) );
	}
}