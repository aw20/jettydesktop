package org.aw20.jettydesktop.controller;

import org.aw20.util.Globals;

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
