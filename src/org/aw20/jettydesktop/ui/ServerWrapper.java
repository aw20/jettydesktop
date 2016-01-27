package org.aw20.jettydesktop.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class ServerWrapper {

	private Map<String, ServerConfigMap> serverConfigObject = new HashMap<String, ServerConfigMap>();
	private Map<String, Boolean> serverRunningObject = new HashMap<String, Boolean>();
	private Map<String, Boolean> serverDeletedObject = new HashMap<String, Boolean>();

	private static ServerWrapper instance = null;


	public static ServerWrapper getInstance() {
		if ( instance == null ) {
			synchronized ( ServerWrapper.class ) {
				if ( instance == null ) {
					instance = new ServerWrapper();
				}
			}
		}
		return instance;
	}


	public Map<String, ServerConfigMap> getServerConfigObject() {
		return serverConfigObject;

	}


	public void loadSettingsIntoServerConfig( List<ServerConfigMap> list ) {
		serverConfigObject.clear();
		int count = 1;
		for ( ServerConfigMap server : list ) {
			serverConfigObject.put( Integer.toString( count ), server );
			serverRunningObject.put( Integer.toString( count ), false );
			serverDeletedObject.put( Integer.toString( count ), false );
			count++;
		}
	}


	public String getIdOfServer( ServerConfigMap scm ) {
		Iterator it = serverConfigObject.entrySet().iterator();

		while ( it.hasNext() ) {
			Map.Entry<String, ServerConfigMap> item = (Entry<String, ServerConfigMap>) it.next();
			if ( scm.equals( item.getValue() ) ) {
				return item.getKey();
			}
		}
		return null;
	}


	public ServerConfigMap getServer( String serverId ) {
		return serverConfigObject.get( serverId );
	}


	public void setServer( String serverId, ServerConfigMap scm ) {
		serverConfigObject.put( serverId, scm );
	}


	public Boolean getDeleted( String serverId ) {
		return serverDeletedObject.get( serverId );

	}


	public void setDeleted( String serverId, Boolean deleted ) {
		serverDeletedObject.put( serverId, deleted );

	}


	public Boolean getRunning( String serverId ) {
		return serverRunningObject.get( serverId );

	}


	public Boolean setRunning( String serverId, Boolean running ) {
		return serverRunningObject.put( serverId, running );

	}


	public List<String> getAllDeleted() {
		List<String> list = new ArrayList<>();

		Iterator it = serverDeletedObject.entrySet().iterator();

		while ( it.hasNext() ) {
			Map.Entry<String, Boolean> item = (Entry<String, Boolean>) it.next();
			if ( item.getValue() ) {
				list.add( item.getKey() );
			}
		}

		return list;
	}


	/*
	 * produces ID for new server
	 */
	public String getNewId() {
		Iterator it = serverConfigObject.entrySet().iterator();
		List<Integer> ids = new ArrayList<Integer>();
		Integer i;

		while ( it.hasNext() ) {
			Map.Entry<String, Boolean> item = (Entry<String, Boolean>) it.next();
			ids.add( Integer.parseInt( item.getKey() ) );
		}
		if ( ids.isEmpty() ) {
			i = 0;
		} else {
			i = Collections.max( ids );
		}

		return Integer.toString( i + 1 );
	}


	public List<ServerConfigMap> getListOfServerConfigMap() {
		List<ServerConfigMap> list = new ArrayList<>();

		Iterator it = serverConfigObject.entrySet().iterator();

		while ( it.hasNext() ) {
			Map.Entry<String, ServerConfigMap> item = (Entry<String, ServerConfigMap>) it.next();
			list.add( item.getValue() );
		}

		return list;
	}


	public Integer getNumberOfRunningServers() {
		Integer count = 0;
		Iterator it = serverRunningObject.entrySet().iterator();

		while ( it.hasNext() ) {
			Map.Entry<String, Boolean> item = (Entry<String, Boolean>) it.next();
			if ( item.getValue() ) {
				count++;
			}
		}
		return count;
	}
}
