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
package org.aw20.jettydesktop.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.aw20.jettydesktop.rte.JettyRunTime;
import org.aw20.util.SocketUtil;

public class Executor extends Thread {
	
	/**
	 * 
	 *  "C:\Program Files (x86)\Java\jre6\bin\javaw.exe" -agentlib:jdwp=transport=dt_socket,suspend=y,address=localhost:55560 
	 *  -Dfile.encoding=Cp1252 -classpath C:\github\jettydesktop\bin org.aw20.jettydesktop.ui.Executor
	*/


	private Process process;
	private BufferedReader		bis;
	private ExecutorInterface executorI;
	private boolean bRun = true;
	
	public Executor( ServerConfigMap options, ExecutorInterface executorI ) throws IOException{
		this.executorI	= executorI;
		
		// Check to see if this server is already running
		if ( SocketUtil.isRemotePortAlive(options.getIP(), Integer.parseInt(options.getPort())) ){
			throw new IOException("Port#" + options.getPort() + " appears to be in use already" );
		}
		
		
		// Start up the server
		String USR_HOME = System.getProperty("user.dir") + File.separator;

		String JDK_HOME;
		if ( options.getCurrentJVM() == null )		
			JDK_HOME = options.getCustomJVM() + File.separator + "bin" + File.separator;
		else
			JDK_HOME = System.getProperty("java.home") + File.separator + "bin" + File.separator;

		if ( new File(JDK_HOME, "javaw.exe").exists() )
			JDK_HOME += "javaw.exe";
		else
			JDK_HOME += "javaw";
		
		List<String>	programArgs	= new ArrayList<String>();
		programArgs.add( JDK_HOME );
		
		if ( options.getMemoryJVM() != null )
			programArgs.add( "-Xmx" + options.getMemoryJVM() + "m" );
		
		programArgs.add( "-classpath" );
		programArgs.add( getClasspath(USR_HOME) );
		
		programArgs.add( JettyRunTime.class.getName() );
	
		if ( options.getIP().length() > 0 )
			programArgs.add( options.getIP() );
		
		programArgs.add( options.getPort() );
		programArgs.add( options.getWebFolder() );
		
		ProcessBuilder	pb	= new ProcessBuilder(programArgs); 

		// Start the process
		process	= pb.start();

		start();
	}
	
	public boolean isWebAppRunning(){
		return true;
	}
	
	public void exit(){
		process.destroy();
		bRun = false;
		interrupt();
	}
	
	public void run(){
		if (executorI != null)
			executorI.onServerStart();

		bis	= new BufferedReader( new InputStreamReader( process.getInputStream() ) );
		
		while (bRun){

			String line;
			try {
				while ( (line=bis.readLine()) != null ){
					if ( executorI != null )
						executorI.onConsole(line);
				}		
			} catch (IOException e) {
				break;
			}
			
		}
		

		if (executorI != null){
			executorI.onConsole( "Exited" );
			executorI.onServerExit();
		}
	}

	private String getClasspath(String usrdir){
		StringBuilder	sb = new StringBuilder(64);
		
		if ( new File(usrdir, "jettydesktop.jar").isFile() ){
			sb.append( usrdir + "jettydesktop.jar" );
		}else{
			sb.append( usrdir + "bin" )
			.append( File.pathSeparator + usrdir + "lib" + File.separator + "jetty-all-8.1.0.RC5.jar" )
			.append( File.pathSeparator + usrdir + "lib" + File.separator + "servlet-api-3.0.jar" );
		}
		
		return sb.toString();
	}
	
}
