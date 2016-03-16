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
package org.aw20.jettydesktop.controller;

import org.aw20.jettydesktop.util.Globals;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


/*
 * Class to control console operations, update, clear
 */
public class ConsoleController {


	public void updateConsole( int id, String line, Scene scene ) {
		final int currentId = id;
		Platform.runLater( () -> {
			// target correct console pane
			TextFlow consoleTextFlow = (TextFlow) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.CONSOLEID + currentId );
			Text t = new Text( line );

			t.setFont( Font.font( "Lucida Sans Typewriter" ) );
			// insert text
			consoleTextFlow.getChildren().add( t );
			consoleTextFlow.setVisible( true );
			consoleTextFlow.toFront();
			// scroll pane to the bottom
			( (ScrollPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + id ) ).setVvalue( 1.0 );
		} );
	}


	public void clearConsole( int serverId, Scene scene ) {
		StackPane consoleStackPane = (StackPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.CONSOLESTACKPANE );
		Platform.runLater( () -> {
			TextFlow textFlowContent = (TextFlow) consoleStackPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.CONSOLEID + serverId );
			textFlowContent.getChildren().clear();
		} );
	}
}
