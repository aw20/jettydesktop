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
package org.aw20.jettydesktop.util;


public class Globals {

	public class ConsoleVariables {

		// Console variables
		public final static String SERVER_STOPPED = "Server stopped.\n";
		public final static String STOPPING_SERVER = "Stopping server...\n";
		public final static String STARTING_SERVER = "Server starting...\n";
	}

	public class StyleClasses {

		public final static String CURRENT = "current";
		public final static String SERVERINFO = "serverInfo";
		public final static String CONSOLEINFO = "consoleInfo";
	}


	public class FXVariables {

		// FX ID variables
		public final static String TABPANEID = "tabPane";
		public final static String SCROLLPANEID = "scrollPane";
		public final static String CONSOLEID = "console";
		public final static String RUNNINGID = "running";
		public final static String SERVERINFOID = "serverInfoId";
		public final static String HBOXID = "hbox";
		public final static String SETTINGSID = "settings";
		public final static String SETTINGSEMPTYID = "settingsEmpty";
		public final static String POLYGONID = "polygon";
		public final static String INFOWEBFOLDERID = "infoWebFolder";
		public final static String INFONAMEID = "infoName";
		public final static String INFOURLID = "infoUrl";
		public final static String INFOTEXTFLOWID = "infoTextFlow";
		public final static String BTNGETFOLDERID = "btnGetFolder";
		public final static String BTNBROWSEID = "btnBrowseJvm";
		public final static String CONSOLESTACKPANE = "consoleStackPane";

		public final static String nameTextBox = "txtName";
		public final static String ipTextBox = "txtIp";
		public final static String portTextBox = "txtPort";
		public final static String webFolderTextBox = "txtWebFolder";
		public final static String uriTextBox = "txtUri";
		public final static String customJvmTextBox = "txtCustomJvm";
		public final static String jvmArgsTextBox = "txtJvmArgs";
		public final static String memoryTextBox = "txtMemory";
		public final static String jvmLabel = "lblJvm";

		public final static String defaultJvmRadioBtn = "radioBtnDefaultJvm";
		public final static String customJvmRadioBtn = "radioBtnCustomJvm";

		public final static String lastUpdatedTextFlow = "tfLastUpdated";
		public final static String memoryTextFlow = "tfMemory";

		public final static String memoryText = "memory";
		public final static String lastUpdatedText = "lastUpdated";

		public final static String idSelector = "#";
	}

	public static class StyleVariables {

		public final static Double polygonPaneWidth = 40.0;
		public final static Double hyperlinkWidth = 185.0;
		public final static String backgroundColourDarkerGrey = "-fx-background-color: #1c252c;";
		public final static String backgroundColourLighterGrey = "-fx-background-color: #222d35;";
	}

}
