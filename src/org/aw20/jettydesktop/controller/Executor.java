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
 *  May 2013
 */
package org.aw20.jettydesktop.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.aw20.jettydesktop.rte.JettyRunTime;
import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.util.SocketUtil;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;


@SuppressWarnings( "rawtypes" )
public class Executor extends Object {

	/**
	 * 
	 * "C:\Program Files (x86)\Java\jre6\bin\javaw.exe" -agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:55560 -Dfile.encoding=Cp1252 -classpath C:\github\jettydesktop\bin org.aw20.jettydesktop.ui.Executor
	 */

	private Process process;
	private boolean bRun = true;
	private ioConsumer ioconsumers[];
	private adminPortWatcher AdminPortWatcher = null;
	private int adminPort;
	private int currentServer;
	ServerConfigMap currentServerConfigMap = null;

	private StackPane currentStackPaneConsole;
	private Scene scene;

	private DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM, FormatStyle.MEDIUM );

	private static Vector allInstances;


	static {
		allInstances = new Vector();
	}


	public static synchronized Vector getAllInstances() {
		return ( (Vector) allInstances.clone() );
	}


	protected void finalize() {
		allInstances.removeElement( this );
	}


	@SuppressWarnings( "unchecked" )
	public Executor( int serverId, StackPane _currentStackPaneConsole, Scene _scene, UIController _uiController ) throws IOException {
		allInstances.add( this );

		currentServer = serverId;
		currentStackPaneConsole = _currentStackPaneConsole;
		scene = _scene;
		currentServerConfigMap = ServerManager.servers.get( currentServer ).getServerConfigMap();

		// Check to see if this server is already running
		if ( SocketUtil.isRemotePortAlive( currentServerConfigMap.getIP(), Integer.parseInt( currentServerConfigMap.getPort() ) ) ) {
			_uiController.updateConsole( currentServer, "Port #" + currentServerConfigMap.getPort() + " appears to be in use already.\n", currentStackPaneConsole );
			throw new IOException( "Port#" + currentServerConfigMap.getPort() + " appears to be in use already" );
		}

		findFreePort( _uiController, currentServer );

		// Start up the server
		String USR_HOME = System.getProperty( "user.dir" ) + File.separator;

		String JDK_HOME;
		if ( currentServerConfigMap.getCurrentJVM() == null )
			JDK_HOME = currentServerConfigMap.getCustomJVM() + File.separator + "bin" + File.separator;
		else
			JDK_HOME = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator;

		if ( new File( JDK_HOME, "javaw.exe" ).exists() )
			JDK_HOME += "javaw.exe";
		else if ( new File( JDK_HOME, "javaw" ).exists() )
			JDK_HOME += "javaw";
		else
			JDK_HOME += "java";

		List<String> programArgs = new ArrayList<String>();
		programArgs.add( JDK_HOME );

		if ( currentServerConfigMap.getMemoryJVM() != null )
			programArgs.add( "-Xmx" + currentServerConfigMap.getMemoryJVM() + "m" );

		if ( currentServerConfigMap.getDefaultJVMArgs() != null )
			programArgs.add( currentServerConfigMap.getDefaultJVMArgs() );

		programArgs.add( "-classpath" );
		programArgs.add( getClasspath( USR_HOME ) );

		programArgs.add( JettyRunTime.class.getName() );

		if ( currentServerConfigMap.getIP().length() > 0 )
			programArgs.add( currentServerConfigMap.getIP() );

		programArgs.add( currentServerConfigMap.getPort() );
		programArgs.add( currentServerConfigMap.getWebFolder() );
		programArgs.add( String.valueOf( adminPort ) );


		ProcessBuilder pb = new ProcessBuilder( programArgs );

		// Start the process

		process = pb.start();
		// this is where the JNI error occurs
		ioconsumers = new ioConsumer[3];
		ioconsumers[0] = new ioConsumer( process.getErrorStream(), _uiController, currentServer );
		ioconsumers[1] = new ioConsumer( process.getInputStream(), _uiController, currentServer );

		if ( adminPort > 0 ) {
			try {
				AdminPortWatcher = new adminPortWatcher( _uiController, currentServer );
			} catch ( IOException ioe ) {
				AdminPortWatcher = null;
			}
		}
	}


	private void findFreePort( UIController _uiController, int _currentServer ) {
		try {
			adminPort = 34000;

			for ( int x = 0; x < 1000; x++ ) {
				adminPort += x;

				Socket s = new Socket();
				s.connect( new InetSocketAddress( "127.0.0.1", adminPort ), 1000 );
				s.close();
			}

			adminPort = -1;

		} catch ( Exception e ) {
			_uiController.updateConsole( _currentServer, "Using Free Admin Port: " + adminPort + "\n", currentStackPaneConsole );
			return;
		}
	}


	class adminPortWatcher extends Thread {

		BufferedReader br;
		Socket s;

		UIController uiController;
		int currentServer;


		public adminPortWatcher( UIController _uiController, int _currentServer ) throws IOException {
			uiController = _uiController;
			currentServer = _currentServer;
			s = new Socket();
			s.connect( new InetSocketAddress( "127.0.0.1", adminPort ), 1000 );
			br = new BufferedReader( new InputStreamReader( s.getInputStream() ) );
			start();
		}


		public void run() {
			try {
				Thread.sleep( 5000 );
			} catch ( InterruptedException e ) {
				return;
			}

			while ( isbRun() ) {
				String line;
				try {
					while ( ( line = br.readLine() ) != null ) {
						try {
							Thread.sleep( 1000 );
						} catch ( InterruptedException ignored ) {}

						final String l = line;
						// update memory
						uiController.updateMemory( l, currentServer, scene );
					}
				} catch ( IOException e ) {
					break;
				}
			}

			try {
				br.close();
				s.close();
			} catch ( IOException e ) {}

		}
	}


	class ioConsumer extends Thread {

		BufferedReader br;
		UIController uiController;
		int currentServer;


		public ioConsumer( InputStream io, UIController _uiController, int _currentServer ) {
			uiController = _uiController;
			currentServer = _currentServer;
			br = new BufferedReader( new InputStreamReader( io ) );
			start();
		}


		public void run() {

			while ( isbRun() ) {
				String line;
				try {
					while ( ( line = br.readLine() ) != null ) {
						final String l = line;
						// update console
						uiController.updateConsole( currentServer, l + "\n", currentStackPaneConsole );
						// update last updated text
						uiController.updateLastUpdated( "last updated: " + LocalDateTime.now().format( formatter ).toString(), currentServer, scene );
					}
				} catch ( IOException e ) {
					break;
				}
			}

			try {
				br.close();
			} catch ( IOException e ) {}
		}
	}


	public boolean isWebAppRunning() {
		return true;
	}


	public void exit() {
		if ( process != null ) {
			process.destroy();
			setbRun( false );
			ioconsumers[0].interrupt();
			ioconsumers[1].interrupt();
		}

		if ( AdminPortWatcher != null )
			AdminPortWatcher.interrupt();
	}


	private String getClasspath( String usrdir ) {
		StringBuilder sb = new StringBuilder( 64 );

		if ( new File( usrdir, "jettydesktop.jar" ).isFile() ) {
			sb.append( usrdir + "jettydesktop.jar" );
		} else {
			sb.append( usrdir + "bin" ).append( File.pathSeparator + usrdir + "lib" + File.separator + "jetty-all-9.3.6.v20151106-uber.jar" ).append( File.pathSeparator + usrdir + "lib" + File.separator + "servlet-api-3.1.0.jar" );
		}

		return sb.toString();
	}


	public boolean isbRun() {
		return bRun;
	}


	public void setbRun( boolean bRun ) {
		this.bRun = bRun;
	}


	public int getCurrentServer() {
		return currentServer;
	}
}
