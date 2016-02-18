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
package org.aw20.util;

import java.util.Comparator;


public class StringUtil implements Comparator<String> {

	public static Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>() {

		public int compare( String str1, String str2 ) {
			int res = String.CASE_INSENSITIVE_ORDER.compare( str1, str2 );
			if ( res == 0 ) {
				res = str1.compareTo( str2 );
			}
			return res;
		}
	};


	@Override
	public int compare( String o1, String o2 ) {
		// TODO Auto-generated method stub
		return 0;
	}

}