package org.aw20.jettydesktop.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.util.Globals;


public class ServerActions {

	public boolean startServer( Executor executor, UIController uiController, ServerController serverController, ServerConfigMap serverConfigMap, String serverId, Scene scene ) {

		serverController.setSelectedServer( serverId );

		uiController.getSplashPane().setVisible( false );
		uiController.getSplashPane().toBack();
		uiController.getSplashAnchorPane().setVisible( false );
		uiController.getSplashAnchorPane().toBack();
		( (TabPane) scene.lookup( "#" + Globals.FXVariables.TABPANEID ) ).setVisible( true );
		( (ScrollPane) scene.lookup( "#" + Globals.FXVariables.SCROLLPANEID + serverId ) ).toFront();
		( (ScrollPane) scene.lookup( "#" + Globals.FXVariables.SCROLLPANEID + serverId ) ).setVisible( true );

		scene.lookup( "#" + Globals.FXVariables.lastUpdatedTextFlow + serverId ).setVisible( true );
		scene.lookup( "#" + Globals.FXVariables.memoryTextFlow + serverId ).setVisible( true );

		uiController.updateConsole( serverId, Globals.ConsoleVariables.STARTING_SERVER, uiController.getConsoleStackPane() );

		try {
			if ( !serverConfigMap.getId().isEmpty() && !serverConfigMap.getPort().isEmpty() ) {

				executor = new Executor( serverConfigMap, uiController.getConsoleStackPane(), scene, uiController );
				serverConfigMap.setRunning( "true" );

				for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
					if ( server.getId().equals( serverId ) ) {
						server.setRunning( "true" );
					}
				}

				uiController.updateRunningIcon( true, serverId );

				return true;
			}
			else {
				serverConfigMap.setRunning( "false" );
				uiController.updateRunningIcon( false, serverId );
				return false;
			}
		} catch ( IOException e ) {
			serverConfigMap.setRunning( "false" );
			return false;
		}
	}


	public boolean stopServer( UIController uiController, ServerController serverController, Executor executor, ServerConfigMap serverConfigMap ) {
		uiController.updateConsole( serverConfigMap.getId(), Globals.ConsoleVariables.STOPPING_SERVER, uiController.getConsoleStackPane() );
		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getId().equals( serverConfigMap.getId() ) ) {
				server.setRunning( "false" );
				serverConfigMap.setRunning( "false" );

			}
		}
		// get correct version of executor on exiting app and stopping all servers
		for ( Object ex : Executor.getAllInstances() ) {
			if ( ( (Executor) ex ).getCurrentServerConfigMap().getId().equals( serverConfigMap.getId() ) ) {
				executor = (Executor) ex;
			}
		}

		if ( executor != null ) {
			executor.exit();
		}
		// if executor shut down is successful
		if ( executor.isbRun() == false ) {
			uiController.updateConsole( serverConfigMap.getId(), Globals.ConsoleVariables.SERVER_STOPPED, uiController.getConsoleStackPane() );
			uiController.updateRunningIcon( false, serverConfigMap.getId() );
			return true;
		}
		else {
			return false;
		}
	}


	public void goToWebpage( URI page, Main main ) {
		if ( Desktop.isDesktopSupported() ) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse( page );
			} catch ( IOException e ) {
				Alert alert = main.createNewAlert( AlertType.ERROR, "Error", "An error has occurred while navigating to a web page", null );
				alert.showAndWait();
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				// if operating system is Linux
				runtime.exec( "xdg-open " + page );
			} catch ( IOException e ) {}
		}
	}

}
