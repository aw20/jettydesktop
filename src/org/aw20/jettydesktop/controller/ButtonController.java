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

import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.jettydesktop.util.Globals;

import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;


/*
 * Class to control button operations - hiding/showing/disabling, start/stop button actions and show/hide server info button
 * doesn't contain buttons as they are part of the JettyDesktopUI.fxml and the UIController is the controller specified for that particular view
 */
public class ButtonController {

	/**
	 * Method to show no buttons
	 * 
	 * @param uiController
	 */
	public void showNoButtons( UIController uiController ) {
		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );

		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );
	}


	/**
	 * Method to show disabled delete and disabled save buttons
	 * 
	 * @param uiController
	 */
	public void showSettingsButtonsOnRunning( UIController uiController ) {
		uiController.getDeleteBtn().setVisible( true );
		uiController.getSaveBtn().setVisible( true );

		uiController.getDeleteBtn().setDisable( true );
		uiController.getSaveBtn().setDisable( true );

		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );
	}


	/**
	 * Method to show no delete and save buttons
	 * 
	 * @param uiController
	 */
	public void showSettingsButtonsOnNotRunning( UIController uiController ) {
		uiController.getDeleteBtn().setVisible( true );
		uiController.getSaveBtn().setVisible( true );

		uiController.getDeleteBtn().setDisable( false );
		uiController.getSaveBtn().setDisable( false );

		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );
	}


	/**
	 * Method to show stop, open and clear buttons
	 * 
	 * @param uiController
	 */
	public void showConsoleButtonsOnRunning( UIController uiController ) {
		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( true );
		uiController.getOpenBtn().setVisible( true );
		uiController.getClearBtn().setVisible( true );

		uiController.getOpenBtn().setDisable( false );

		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );
	}


	/**
	 * Method to show start, disabled open and clear buttons
	 * 
	 * @param uiController
	 */
	public void showConsoleButtonsOnNotRunning( UIController uiController ) {
		uiController.getStartBtn().setVisible( true );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( true );
		uiController.getClearBtn().setVisible( true );

		uiController.getOpenBtn().setDisable( true );

		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );
	}


	/**
	 * Method to show save button
	 * 
	 * @param uiController
	 */
	public void showButtonsOnNewWebApp( UIController uiController ) {
		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );

		uiController.getOpenBtn().setDisable( false );

		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( true );
	}


	public void startBtnClick( int id, Scene scene, ServerController serverController, ServerActions serverActions, Executor executor, UIController uiController, ServerManager serverManager ) {
		int selectedServer = id;
		if ( selectedServer == 0 ) {
			selectedServer = serverController.getSelectedServer();
		}


		final int finalSelectedServer = selectedServer;

		final Scene finalScene = scene;
		// on separate thread due to UI not updating until Executor process finished.
		Thread t1 = new Thread( new Runnable() {

			public void run() {

				serverActions.startServer( executor, uiController, serverController, finalSelectedServer, finalScene, serverManager );


				Platform.runLater( () -> {
					final Executor currentExecutor = Executor.getExecutor( finalSelectedServer );
					if ( currentExecutor != null ) {
						if ( ( (Executor) currentExecutor ).isWebAppRunning() ) {// executor is running
							showConsoleButtonsOnRunning( uiController );

							serverManager.getServers().get( finalSelectedServer ).setRunning( true );

							// target the correct play polygon
							Polygon p = ( (Polygon) finalScene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.POLYGONID + finalSelectedServer ) );
							// transform it to a square
							p.getPoints().setAll(
									0d, 0d, // (x, y)
									0d, 12d,
									12d, 12d,
									12d, 0d );
							// colour transition from green to red
							FillTransition ft = new FillTransition( Duration.millis( 4000 ), p, Color.GREEN, Color.RED );
							ft.play();


							// open the console tab and correct console pane
							Tab tab = uiController.getTabPane().getTabs().get( 1 );
							uiController.getTabPane().getSelectionModel().select( tab );
							uiController.setSelectedTabInstance( tab );

							( (ScrollPane) finalScene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + finalSelectedServer ) ).setVisible( true );

							// update server info
							Pane serverInfoPane = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SERVERINFOID + finalSelectedServer );
							serverInfoPane.setVisible( true );
							serverInfoPane.toFront();

						}
					} else {
						// update console with server not started message
						ConsoleController consoleController = new ConsoleController();
						consoleController.updateConsole( finalSelectedServer, "Server not started.", scene );
					}
				} );
			}

		} );
		t1.start();

	}


	public void stopBtnClick( int id, Scene scene, ServerController serverController, ServerActions serverActions, Executor executor, UIController uiController, ServerManager serverManager ) {
		int selectedServer = id;
		if ( selectedServer == 0 ) {
			selectedServer = serverController.getSelectedServer();
		}

		if ( uiController.getSelectedTabInstance() == null ) {
			Tab tab = uiController.getTabPane().getTabs().get( 1 );
			uiController.setSelectedTabInstance( tab );
		}
		// target stop polygon
		Polygon p = ( (Polygon) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.POLYGONID + selectedServer ) );
		// transform into a play polygon
		p.getPoints().setAll(
				0d, 0d, // (x, y)
				12d, 6d,
				0d, 12d );
		// colour transition red to green
		FillTransition ft = new FillTransition( Duration.millis( 2000 ), p, Color.RED, Color.GREEN );
		ft.play();

		serverActions.stopServer( uiController, serverController, executor, selectedServer, scene, serverManager );

		serverManager.getServers().get( selectedServer ).setRunning( false );

		// enable and disable correct buttons depending on which tab is selected
		if ( uiController.getSelectedTabInstance().getId().contains( "console" ) ) {
			showConsoleButtonsOnNotRunning( uiController );
		} else {
			showSettingsButtonsOnNotRunning( uiController );
		}
	}


	public void serverInfoArrowImageClick( MouseEvent e, Scene scene, UIController uiController, int selectedServerId ) {
		if ( AnchorPane.getTopAnchor( uiController.getTabPaneMaster() ) != 0.0 ) {
			AnchorPane.setTopAnchor( uiController.getTabPaneMaster(), 0.0 );

			// hide server info in blue pane
			uiController.getServerInfoImagePane().setPrefHeight( 40.0 );
			uiController.getServerInfoImagePane().setMinHeight( 40.0 );
			uiController.getServerInfoImagePane().setMaxHeight( 40.0 );

			uiController.getArrowImage().setRotate( 180.0 );
			uiController.getServerInfoStackPaneMaster().setVisible( false );
		} else {
			AnchorPane.setTopAnchor( uiController.getTabPaneMaster(), 47.0 );
			// show server info in blue pane
			uiController.getArrowImage().setRotate( 0.0 );
			uiController.getServerInfoPane().setMaxHeight( 47.0 );
			uiController.getServerInfoPane().setMinHeight( 47.0 );
			uiController.getServerInfoPane().setPrefHeight( 47.0 );

			uiController.getServerInfoImagePane().setPrefHeight( 47.0 );
			uiController.getServerInfoImagePane().setMinHeight( 47.0 );
			uiController.getServerInfoImagePane().setMaxHeight( 47.0 );

			uiController.getServerInfoStackPaneMaster().setVisible( true );

			Pane serverInfoPane = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SERVERINFOID + selectedServerId );
			serverInfoPane.setVisible( true );
			serverInfoPane.toFront();

			e.consume();
		}
	}

}
