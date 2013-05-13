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
package org.aw20.jettydesktop.rte;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyRunTime extends Object {

	public static void main(String[] args) {
		System.out.println( "Jetty Version: " + Server.getVersion() );
		
		if ( args.length == 2 )
			System.out.println( "http://*:" + args[0] );
		else
			System.out.println( "http://" + args[0] + ":" + args[1] );

		try {
			if ( args.length == 2 )
				new JettyRunTime( null, args[0], args[1] );
			else
				new JettyRunTime( args[0], args[1], args[2] );
		} catch (Exception e) {
			System.out.println( e.getMessage() );
		}
	}

	private Server server;
	
	public JettyRunTime( String ip, String port, String webapp ) throws Exception{
		
		if ( ip == null )
			server = new Server( Integer.valueOf(port) );
		else
			server = new Server( InetSocketAddress.createUnresolved(ip, Integer.valueOf(port)) );
		
		WebAppContext context = new WebAppContext();
		
		context.setDescriptor( webapp + "/WEB-INF/web.xml");
		context.setResourceBase( webapp );
		context.setContextPath("/");
		context.setParentLoaderPriority(true);
		context.setDefaultsDescriptor("org/aw20/jettydesktop/rte/webdefault.xml");
		
		server.setHandler(context);
		server.start();
		
		System.out.println( "Jetty has started." );
	}
	
}