package org.aw20.jettydesktop.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.jettydesktop.ui.ServerWrapper;
import org.aw20.util.Globals;


public class ServerActions {

	ServerWrapper serverWrapper = ServerWrapper.getInstance();


	public boolean startServer( Executor executor, UIController uiController, ServerController serverController, String serverId, Scene scene ) {

		Platform.runLater( ( ) -> {
			serverController.setSelectedServer( serverId );

			uiController.getSplashPane().setVisible( false );
			uiController.getSplashPane().toBack();
			uiController.getSplashAnchorPane().setVisible( false );
			uiController.getSplashAnchorPane().toBack();
			( (TabPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.TABPANEID ) ).setVisible( true );
			( (ScrollPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + serverId ) ).toFront();
			( (ScrollPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + serverId ) ).setVisible( true );

			scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.lastUpdatedTextFlow + serverId ).setVisible( true );
			scene.lookup( "#" + Globals.FXVariables.memoryTextFlow + serverId ).setVisible( true );

			uiController.updateConsole( serverId, Globals.ConsoleVariables.STARTING_SERVER, uiController.getConsoleStackPane() );
		} );
		ServerConfigMap scm = serverWrapper.getServer( serverId );

		try {
			if ( !scm.getPort().isEmpty() ) {

				executor = new Executor( serverId, uiController.getConsoleStackPane(), scene, uiController );

				serverWrapper.setRunning( serverId, true );

				uiController.updateRunningIcon( true, serverId );

				return true;
			}
			else {
				uiController.updateRunningIcon( false, serverId );
				return false;
			}
		} catch ( IOException e ) {
			return false;
		}

	}


	public boolean stopServer( UIController uiController, ServerController serverController, Executor executor, String id ) {
		uiController.updateConsole( id, Globals.ConsoleVariables.STOPPING_SERVER, uiController.getConsoleStackPane() );

		serverWrapper.setRunning( id, false );

		// get correct version of executor on exiting app and stopping all servers
		for ( Object ex : Executor.getAllInstances() ) {
			if ( ( (Executor) ex ).getCurrentServer().equals( id ) ) {
				executor = (Executor) ex;
			}
		}

		if ( executor != null ) {
			executor.exit();
		}
		// if executor shut down is successful
		if ( executor.isbRun() == false ) {
			uiController.updateConsole( id, Globals.ConsoleVariables.SERVER_STOPPED, uiController.getConsoleStackPane() );
			uiController.updateRunningIcon( false, id );
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
