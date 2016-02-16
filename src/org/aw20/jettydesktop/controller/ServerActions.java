package org.aw20.jettydesktop.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.jettydesktop.ui.ServerWrapper;
import org.aw20.util.Globals;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;


public class ServerActions {

	// ServerWrapper1 serverWrapper = ServerWrapper1.getInstance();


	public boolean startServer( Executor executor, UIController uiController, ServerController serverController, int serverId, Scene scene ) {
		// run later on JavaFX thread
		Platform.runLater( () -> {
			serverController.setSelectedServer( serverId );
			// show console and hide splash screen if open
			uiController.getSplashPane().setVisible( false );
			uiController.getSplashPane().toBack();
			uiController.getSplashAnchorPane().setVisible( false );
			uiController.getSplashAnchorPane().toBack();
			( (TabPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.TABPANEID ) ).setVisible( true );
			( (ScrollPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + serverId ) ).toFront();
			( (ScrollPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + serverId ) ).setVisible( true );
			// show last updated and memory text
			scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.lastUpdatedTextFlow + serverId ).setVisible( true );
			scene.lookup( "#" + Globals.FXVariables.memoryTextFlow + serverId ).setVisible( true );

			uiController.updateConsole( serverId, Globals.ConsoleVariables.STARTING_SERVER, uiController.getConsoleStackPane() );
		} );

		ServerWrapper server = ServerManager.getServers().get( serverId );
		// ServerConfigMap scm = serverWrapper.getServer( serverId );

		try {
			if ( !server.getServerConfigMap().getPort().isEmpty() ) {
				// start server
				executor = new Executor( serverId, uiController.getConsoleStackPane(), scene, uiController );
				// set to running in settings
				server.setRunning( true );
				// update icon in server list
				uiController.updateRunningIcon( true, serverId );
				return true;
			} else {
				uiController.updateRunningIcon( false, serverId );
				return false;
			}
		} catch ( IOException e ) {
			return false;
		}

	}


	public boolean stopServer( UIController uiController, ServerController serverController, Executor executor, int serverId ) {
		uiController.updateConsole( serverId, Globals.ConsoleVariables.STOPPING_SERVER, uiController.getConsoleStackPane() );

		ServerWrapper server = ServerManager.getServers().get( serverId );
		server.setRunning( false );

		// get correct version of executor on exiting app and stopping all servers
		for ( Object ex : Executor.getAllInstances() ) {
			if ( ( (Executor) ex ).getCurrentServer() == serverId ) {
				executor = (Executor) ex;
			}
		}
		// shut down server
		if ( executor != null ) {
			executor.exit();
		}

		// if executor shut down is successful
		if ( executor.isbRun() == false ) {
			uiController.updateConsole( serverId, Globals.ConsoleVariables.SERVER_STOPPED, uiController.getConsoleStackPane() );
			uiController.updateRunningIcon( false, serverId );
			return true;
		} else {
			return false;
		}
	}


	public void goToWebpage( URI page, Main main ) {
		// if desktop is supported - ie Windows
		if ( Desktop.isDesktopSupported() ) {
			Desktop desktop = Desktop.getDesktop();
			try {
				// go to web page
				desktop.browse( page );
			} catch ( IOException e ) {
				Alert alert = main.createNewAlert( AlertType.ERROR, "Error", "An error has occurred while navigating to a web page", null );
				alert.showAndWait();
			}
			// if operating system is Linux
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec( "xdg-open " + page );
			} catch ( IOException e ) {}
		}
	}

}
