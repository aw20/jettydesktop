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
package org.aw20.util;

import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketUtil extends Object {

	public static boolean isRemotePortAlive( String ip, int port ){
		return isRemotePortAlive( ip, port, 3000 );
	}
	
	public static boolean isRemotePortAlive( String ip, int port, int timeoutMs ){
		try{
			Socket s = new Socket();
			s.connect( new InetSocketAddress( ip, port ), timeoutMs );
			s.close();
			return true;
			
		}catch(Exception e){
			//e.printStackTrace();
			return false;
		}
	}
	
}
