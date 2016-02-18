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


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Vector;

import org.aw20.jettydesktop.ui.ServerManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application {

	private final String title = "Jetty Desktop v2.1.1";
	private final String currentJvm = System.getProperty( "java.vm.name" ) + " " + System.getProperty( "java.version" ) + " " + System.getProperty( "java.vm.version" );
	private final Font fontWebFolder = Font.font( "Arial", FontWeight.NORMAL, 13 );
	private final Font fontNameUrl = Font.font( "Arial", FontWeight.BOLD, 15 );

	private final String viewDir = "/org/aw20/jettydesktop/view/";

	private UIController uiController;
	private ServerSetup serverSetup;
	private Executor executor = null;
	private ServerManager serverManager = new ServerManager();
	private ServerController serverController = new ServerController( serverManager );

	private Stage stage;
	private Scene scene;
	private FXMLLoader settingsLoader = null;


	/**
	 * @param primaryStage
	 */
	@Override
	public void start( Stage primaryStage ) {
		stage = primaryStage;

		FXMLLoader loader = new FXMLLoader( getClass().getResource( viewDir + "JettyDesktopUI.fxml" ) );
		settingsLoader = new FXMLLoader( getClass().getResource( viewDir + "settings.fxml" ) );

		uiController = new UIController( serverManager );

		serverSetup = new ServerSetup();

		loader.setController( uiController );

		AnchorPane root = null;
		try {
			root = loader.load();
		} catch ( IOException e ) {
			Alert alert = createNewExceptionAlert( e, "Jetty Desktop has failed to load initial resources. Go on https://github.com/aw20/jettydesktop/ and report the following message." );

			alert.showAndWait();

			System.exit( -1 );
		}
		scene = new Scene( root, 889, 655 );

		// set up JavaFX variables and window

		serverSetup.initialise( uiController, serverController, serverManager, scene, new ServerActions(), null );

		serverSetup.setUpSpalashScreen( this, title );
		try {
			serverSetup.setUpEmptySettings( settingsLoader, currentJvm, stage, uiController );
		} catch ( IOException e ) {
			Alert alert = createNewExceptionAlert( e, "Jetty Desktop has failed to initialise settings. Go on https://github.com/aw20/jettydesktop/ and report the following message." );
			alert.showAndWait();
			System.exit( -1 );
		}
		serverSetup.setUpSettings( serverController );

		serverSetup.setUpServerInfo( serverController, fontWebFolder, fontNameUrl );

		try {
			serverSetup.addSettingsToStackPane( settingsLoader, currentJvm, stage, this );
		} catch ( IOException e ) {
			Alert alert = createNewExceptionAlert( e, "Jetty Desktop has failed to initialise settings. Go on https://github.com/aw20/jettydesktop/ and report the following message." );
			alert.showAndWait();
			System.exit( -1 );
		}
		serverSetup.setUpConsoleInfo();

		scene.getStylesheets().add( getClass().getResource( viewDir + "application.css" ).toExternalForm() );
		scene.getStylesheets().add( getClass().getResource( viewDir + "alert.css" ).toExternalForm() );

		primaryStage.setScene( scene );
		primaryStage.setTitle( title );
		primaryStage.setResizable( true );
		primaryStage.getIcons().add( new Image( viewDir + "logo.png" ) );
		primaryStage.show();

		// event handlers

		/**
		 * Method to handle click on list view
		 */
		uiController.getListViewAppList().addEventHandler( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle( MouseEvent event ) {
				if ( uiController.getListViewAppList().getSelectionModel().getSelectedItem() != null ) {
					HBox hbox = uiController.getListViewAppList().getSelectionModel().getSelectedItem();

					// get selected server id
					Label label = (Label) hbox.getChildren().get( 1 );

					serverController.setSelectedServer( Integer.parseInt( label.getId() ) );

					uiController.handleListViewOnClick( hbox, scene, Integer.parseInt( label.getId() ) );
				}
			}
		} );


		/**
		 * Method to handle click on arrow image
		 */
		uiController.getServerInfoImagePane().addEventHandler( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle( MouseEvent event ) {
				uiController.serverInfoArrowImageClick( event, scene );
			}
		} );

		/**
		 * Method to handle click on add web app button
		 */
		uiController.getBtnAddWebApp().setOnAction( ( ActionEvent event ) -> {
			uiController.addWebAppBtnClick( scene );
		} );

		/**
		 * Method to handle click on tab
		 */
		uiController.getTabPane().getSelectionModel().selectedItemProperty().addListener( ( ov, oldTab, newTab ) -> {
			uiController.tabPaneSelectionChange( newTab, scene );
		} );


		/**
		 * Method to handle click on start button
		 */
		uiController.getStartBtn().setOnAction( ( ActionEvent e ) -> {
			uiController.handleStartBtnClick( scene, executor );
		} );


		/**
		 * Method to handle click on stop button
		 */
		uiController.getStopBtn().setOnAction( ( ActionEvent e ) -> {
			uiController.handleStopBtnClick( scene, executor );
		} );


		/**
		 * Method to handle click on clear
		 */
		uiController.getClearBtn().setOnAction( ( ActionEvent e ) -> {
			uiController.clearConsole( scene );
		} );


		/**
		 * Method to handle click on open
		 */
		uiController.getOpenBtn().setOnAction( ( ActionEvent e ) -> {
			uiController.openBtnClick( this );
		} );


		/**
		 * Method to handle click on save
		 */
		uiController.getSaveBtn().setOnAction( ( ActionEvent e ) -> {
			try {
				if ( uiController.validateSettings( scene, this ) ) {
					uiController.saveBtnClick( scene, this, executor, fontWebFolder, fontNameUrl, currentJvm, stage, settingsLoader, uiController );
				}
			} catch ( Exception e1 ) {
				Alert alert = createNewExceptionAlert( e1, "An error has occurred while saving a web app" );// ( AlertType.ERROR, "Error", "An error has occurred while saving a web app", null );
				alert.showAndWait();
			}
		} );


		/**
		 * Method to handle click on delete
		 */
		uiController.getDeleteBtn().setOnAction( ( ActionEvent e ) -> {
			uiController.deleteServer( this, e );
		} );

		// Exit jetty server
		Platform.runLater( new Runnable() {

			public void run() {
				// window close requested
				stage.setOnCloseRequest( new EventHandler<WindowEvent>() {

					public void handle( WindowEvent t ) {

						// if there are no server
						if ( serverManager.getServers() == null || serverManager.getServers().isEmpty() ) {
							// appFunctions.deleteServers();
							Platform.exit();

							// if there are servers
						} else {
							// else count the running servers
							int runningServers = serverManager.getNumberOfRunningServers();

							// if servers are running call js to run dialog
							if ( runningServers > 0 ) {
								Optional<ButtonType> result = createNewAlert( AlertType.CONFIRMATION, "", "Stop all apps (" + runningServers + ") running.", "Are you sure?" ).showAndWait();

								// if servers have started exit app - show confirm, hard delete servers, exit platform
								if ( result.get() == ButtonType.OK ) {
									stopServersOnExit();
									Platform.exit();
								} else {
									// do nothing and close dialog
									t.consume();
								}
							}

							// if no servers have started exit app - hard delete servers, exit platform
							else {
								Platform.exit();
							}
						}
					}
				} );
			}
		} );
	}


	public static void main( String[] args ) {
		launch( args );
	}


	/*
	 * Method to stop all servers running on application close
	 */
	private void stopServersOnExit() {
		@SuppressWarnings( "rawtypes" )
		Vector v = Executor.getAllInstances();
		for ( Object executor : v ) {
			( (Executor) executor ).exit();
		}
	}


	/*
	 * create alerts for exceptions, deleting and closing the application
	 */
	public Alert createNewAlert( AlertType type, String title, String content, String header ) {
		Alert alert = new Alert( type );
		alert.setTitle( title );
		alert.setHeaderText( header );
		alert.setContentText( content );

		DialogPane dp = alert.getDialogPane();

		dp.getStylesheets().add( getClass().getResource( viewDir + "alert.css" ).toExternalForm() );

		dp.getStyleClass().remove( "alert" );

		return alert;
	}


	/*
	 * create alerts for exceptions, deleting and closing the application
	 */
	private Alert createNewExceptionAlert( Exception e, String content ) {
		Alert alert = new Alert( AlertType.ERROR );
		alert.setTitle( "Exception Dialog" );
		alert.setHeaderText( null );
		alert.setContentText( content );

		DialogPane dp = alert.getDialogPane();

		dp.getStylesheets().add( getClass().getResource( viewDir + "alert.css" ).toExternalForm() );

		dp.getStyleClass().remove( "alert" );

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter( sw );
		e.printStackTrace( pw );
		String exceptionText = sw.toString();

		Label label = new Label( "The exception stacktrace was:" );

		TextArea textArea = new TextArea( exceptionText );
		textArea.setEditable( false );
		textArea.setWrapText( true );

		textArea.setMaxWidth( Double.MAX_VALUE );
		textArea.setMaxHeight( Double.MAX_VALUE );
		GridPane.setVgrow( textArea, Priority.ALWAYS );
		GridPane.setHgrow( textArea, Priority.ALWAYS );

		GridPane expContent = new GridPane();
		expContent.setMaxWidth( Double.MAX_VALUE );
		expContent.add( label, 0, 0 );
		expContent.add( textArea, 0, 1 );

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent( expContent );

		return alert;
	}
}
