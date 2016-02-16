package org.aw20.jettydesktop.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aw20.util.Globals;
import org.aw20.util.StringUtil;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


/*
 * Class to control server info operations, running status, last updated/memory text, server info pane
 */
public class ServerInfoController {

	/*
	 * updates icon in server list
	 */
	public void updateRunningIcon( boolean running, int serverId, UIController uiController ) {
		final int selectedServer = serverId;

		Circle c = (Circle) uiController.getListViewAppList().lookup( Globals.FXVariables.idSelector + Globals.FXVariables.RUNNINGID + selectedServer );

		Platform.runLater( () -> {
			if ( running == true ) {
				c.setFill( Color.LIGHTSEAGREEN );
			} else {
				c.setFill( Color.GREY );
			}
		} );
	}


	public void updateLastUpdated( String lastUpdated, int currentServer, Scene scene ) {
		final int selectedServer = currentServer;
		Platform.runLater( () -> {
			Text textContent = (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.lastUpdatedText + selectedServer );
			textContent.setText( lastUpdated );
		} );
	}


	public void updateMemory( String memory, int serverId, Scene scene ) {
		final int selectedServer = serverId;
		Platform.runLater( () -> {
			Text textContent = (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryText + selectedServer );
			textContent.setText( memory );
		} );
	}


	// show correct text flows with last updated and memory info
	public void showCurrentConsoleInfo( UIController uiController, ServerController serverController ) {
		// show correct textflows with last updated and memory
		Iterator<Node> itConsoleInfo = uiController.getConsoleInfo().getChildren().iterator();
		while ( itConsoleInfo.hasNext() ) {
			TextFlow consoleInfoTextFlow = (TextFlow) itConsoleInfo.next();
			Platform.runLater( () -> {
				TextFlow textFlow = consoleInfoTextFlow;
				textFlow.setVisible( false );
				if ( textFlow.getId().contains( Integer.toString( serverController.getSelectedServer() ) ) ) {
					textFlow.setVisible( true );
				}
			} );
		}
	}


	public void showCurrentServerInfoPane( UIController uiController, int selectedServerId ) {
		final int selectedServer = selectedServerId;
		// show correct server info
		Iterator<Node> itInfo = uiController.getServerInfoStackPane().getChildren().iterator();
		while ( itInfo.hasNext() ) {
			uiController.getSplashPane().setVisible( false );
			uiController.getSplashPane().toBack();
			uiController.getSplashAnchorPane().setVisible( false );
			uiController.getSplashAnchorPane().toBack();
			Pane serverInfoPane = (Pane) itInfo.next();
			Platform.runLater( () -> {
				serverInfoPane.setVisible( false );
				// show server info
				if ( serverInfoPane.getId().equals( Globals.FXVariables.SERVERINFOID + selectedServer ) ) {
					serverInfoPane.setVisible( true );
					serverInfoPane.toFront();
				}
			} );
		}
	}


	/*
	 * get all server names and order alphabetically
	 */
	public void refreshServerList( UIController uiController ) {
		// get all names
		List<String> names = new ArrayList<String>();
		ObservableList<HBox> hboxs = (ObservableList<HBox>) uiController.getListViewAppList().getItems();
		ObservableList<HBox> newHboxs = FXCollections.observableArrayList( hboxs );
		for ( HBox node : hboxs ) {
			HBox hbox = node;
			ObservableList<Node> hboxChild = (ObservableList<Node>) hbox.getChildren();
			for ( Node nodeChild : hboxChild ) {
				if ( nodeChild instanceof javafx.scene.control.Hyperlink ) {
					names.add( ( (Hyperlink) nodeChild ).getText() );
				}
			}
		}

		// sort servers
		Collections.sort( names, StringUtil.ALPHABETICAL_ORDER );

		uiController.getListViewAppList().getItems().clear();

		for ( String name : names ) {
			for ( Node node : newHboxs ) {
				HBox hbox = (HBox) node;
				ObservableList<Node> hboxChild = (ObservableList<Node>) hbox.getChildren();
				for ( Node nodeChild : hboxChild ) {
					if ( nodeChild instanceof javafx.scene.control.Hyperlink ) {
						if ( ( (Hyperlink) nodeChild ).getText().equals( name ) ) {
							Platform.runLater( () -> {
								hbox.setPrefWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
								hbox.setMaxWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
								hbox.setMinWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
								VBox.setMargin( hbox, new Insets( 0, 0, 0, 0 ) );

								uiController.getListViewAppList().getItems().add( hbox );
							} );
						}
					}
				}
			}
		}
	}

}
