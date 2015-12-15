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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import org.aw20.jettydesktop.rte.JettyRunTime;
import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.util.SocketUtil;


public class Executor extends Object {

	/**
	 * 
	 * "C:\Program Files (x86)\Java\jre6\bin\javaw.exe" -agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:55560 -Dfile.encoding=Cp1252 -classpath C:\github\jettydesktop\bin org.aw20.jettydesktop.ui.Executor
	 */

	private Process process;
	private AppFunctions appFunctions;
	private boolean bRun = true;
	private ioConsumer ioconsumers[];
	private adminPortWatcher AdminPortWatcher = null;
	private int adminPort;
	private ServerConfigMap scm = null;

	private DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM, FormatStyle.MEDIUM );

	private WebEngine webEngineSingleton = Start.getWebEngineInstance();

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


	public Executor( ServerConfigMap serverConfigMap, AppFunctions appFunctions ) throws IOException {
		allInstances.add( this );

		scm = serverConfigMap;
		this.appFunctions = appFunctions;

		// Check to see if this server is already running
		if ( SocketUtil.isRemotePortAlive( serverConfigMap.getIP(), Integer.parseInt( serverConfigMap.getPort() ) ) ) {
			throw new IOException( "Port#" + serverConfigMap.getPort() + " appears to be in use already" );
		}

		findFreePort();

		// Start up the server
		String USR_HOME = System.getProperty( "user.dir" ) + File.separator;

		String JDK_HOME;
		if ( serverConfigMap.getCurrentJVM() == null )
			JDK_HOME = serverConfigMap.getCustomJVM() + File.separator + "bin" + File.separator;
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
		// programArgs.add( null );

		if ( serverConfigMap.getMemoryJVM() != null )
			programArgs.add( "-Xmx" + serverConfigMap.getMemoryJVM() + "m" );

		if ( serverConfigMap.getDefaultJVMArgs() != null )
			programArgs.add( serverConfigMap.getDefaultJVMArgs() );

		programArgs.add( "-classpath" );
		programArgs.add( getClasspath( USR_HOME ) );

		programArgs.add( JettyRunTime.class.getName() );

		if ( serverConfigMap.getIP().length() > 0 )
			programArgs.add( serverConfigMap.getIP() );

		programArgs.add( serverConfigMap.getPort() );
		programArgs.add( serverConfigMap.getWebFolder() );
		programArgs.add( String.valueOf( adminPort ) );


		ProcessBuilder pb = new ProcessBuilder( programArgs );

		// what is the error stream below outputting?

		// Start the process

		process = pb.start();
		// this is where the JNI error occurs
		ioconsumers = new ioConsumer[3];
		ioconsumers[0] = new ioConsumer( process.getErrorStream() );
		ioconsumers[1] = new ioConsumer( process.getInputStream() );
		ioconsumers[2] = new ioConsumer( process.getOutputStream() );


		if ( adminPort > 0 ) {
			try {
				AdminPortWatcher = new adminPortWatcher();
			} catch ( IOException ioe ) {
				AdminPortWatcher = null;
			}
		}
	}


	private void findFreePort() {
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
			webEngineSingleton.executeScript( "document.getElementById('console_" + scm.getId() + "').innerHTML += '<pre>Using Free AdminPort=" + adminPort + "</pre>';" );
			return;
		}
	}


	class adminPortWatcher extends Thread {

		BufferedReader br;
		Socket s;


		public adminPortWatcher() throws IOException {
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
						if ( appFunctions != null )
							Platform.runLater( new Runnable() {

								public void run() {
									appFunctions.onMemory( l, scm.getId() );
								}
							} );
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


		public ioConsumer( InputStream io ) {
			br = new BufferedReader( new InputStreamReader( io ) );
			start();
		}


		public ioConsumer( OutputStream outputStream ) {
			// TODO Auto-generated constructor stub
		}


		public void run() {

			while ( isbRun() ) {
				String line;
				try {
					while ( ( line = br.readLine() ) != null ) {
						final String l = line;
						if ( appFunctions != null )
							Platform.runLater( new Runnable() {

								public void run() {
									// webEngineSingleton.executeScript( "$('console_" + scm.getId() + "').find('pre').text += '" + l + "';" );
									webEngineSingleton.executeScript( "document.getElementById('console_" + scm.getId() + "').innerHTML += '<pre>" + l + "</pre>';" );
									webEngineSingleton.executeScript( "document.getElementById('console_" + scm.getId() + "').scrollTop = document.getElementById('console_" + scm.getId() + "').scrollHeight;" );
									appFunctions.onLastUpdated( "Last Updated: " + LocalDateTime.now().format( formatter ).toString(), scm.getId() );
								}
							} );
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
			ioconsumers[2].interrupt();
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

}
