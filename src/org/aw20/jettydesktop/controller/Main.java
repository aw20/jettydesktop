package org.aw20.jettydesktop.controller;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Vector;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.jettydesktop.ui.ServerWrapper;
import org.aw20.util.Globals;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application {

	private final String title = "Jetty Desktop v2.1.1";
	private final String currentJvm = System.getProperty( "java.vm.name" ) + " " + System.getProperty( "java.version" ) + " " + System.getProperty( "java.vm.version" );
	private final Font fontWebFolder = Font.font( "Arial", FontWeight.NORMAL, 13 );
	private final Font fontNameUrl = Font.font( "Arial", FontWeight.BOLD, 15 );

	private final String viewDir = "/org/aw20/jettydesktop/view/";

	private UIController uiController;
	private ServerController serverController = new ServerController();
	private ServerSetup serverSetup;

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

		uiController = new UIController();

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
		uiController.initialise();

		serverSetup.setUpSpalashScreen( this, serverController, uiController, title );
		try {
			serverSetup.setUpEmptySettings( settingsLoader, currentJvm, stage, uiController );
		} catch ( IOException e ) {
			Alert alert = createNewExceptionAlert( e, "Jetty Desktop has failed to initialise settings. Go on https://github.com/aw20/jettydesktop/ and report the following message." );

			alert.showAndWait();

			System.exit( -1 );
		}
		serverSetup.setUpSettings( serverController, uiController );
		serverSetup.setUpServerList( uiController, serverController, scene );
		serverSetup.setUpServerInfo( serverController, uiController, fontWebFolder, fontNameUrl );

		try {
			serverSetup.addSettingsToStackPane( serverController, uiController, settingsLoader, currentJvm, stage, this );
		} catch ( IOException e ) {
			Alert alert = createNewExceptionAlert( e, "Jetty Desktop has failed to initialise settings. Go on https://github.com/aw20/jettydesktop/ and report the following message." );

			alert.showAndWait();

			System.exit( -1 );
		}
		serverSetup.setUpConsoleInfo( serverController, uiController );

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
				// Platform.runLater( () -> {
				if ( uiController.getListViewAppList().getSelectionModel().getSelectedItem() != null ) {
					HBox hbox = uiController.getListViewAppList().getSelectionModel().getSelectedItem();

					// get selected server id
					String serverId = hbox.getId().replace( Globals.FXVariables.HBOXID, "" );

					Hyperlink h = (Hyperlink) hbox.getChildren().get( 0 );
					ServerConfigMap scm = ServerManager.getServers().get( Integer.parseInt( serverId ) ).getServerConfigMap();
					serverController.setSelectedServer( Integer.parseInt( serverId ) );

					uiController.handleListViewOnClick( hbox, scene, h, scm );
				}
				// } );

			}
		} );


		/**
		 * Method to handle click on arrow image
		 */
		uiController.getServerInfoImagePane().addEventHandler( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle( MouseEvent event ) {
				serverInfoArrowImageClick( event );
			}
		} );

		/**
		 * Method to handle click on add web app button
		 */
		uiController.getBtnAddWebApp().setOnAction( ( ActionEvent event ) -> {
			addWebAppBtnClick();
		} );

		/**
		 * Method to handle click on tab
		 */
		uiController.getTabPane().getSelectionModel().selectedItemProperty().addListener( ( ov, oldTab, newTab ) -> {
			tabPaneSelectionChange( newTab );
		} );


		/**
		 * Method to handle click on start button
		 */
		uiController.getStartBtn().setOnAction( ( ActionEvent e ) -> {
			uiController.startBtnClick( 0, scene );
		} );


		/**
		 * Method to handle click on stop button
		 */
		uiController.getStopBtn().setOnAction( ( ActionEvent e ) -> {
			uiController.stopBtnClick( 0, scene );
		} );


		/**
		 * Method to handle click on clear
		 */
		uiController.getClearBtn().setOnAction( ( ActionEvent e ) -> {
			uiController.clearConsole( ServerController.selectedServer );
		} );


		/**
		 * Method to handle click on open
		 */
		uiController.getOpenBtn().setOnAction( ( ActionEvent e ) -> {
			openBtnClick();
		} );


		/**
		 * Method to handle click on save
		 */
		uiController.getSaveBtn().setOnAction( ( ActionEvent e ) -> {
			try {
				if ( validateSettings() ) {
					saveBtnClick();
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
			String serverToBeDeleted = ServerManager.getServers().get( ServerController.selectedServer ).getServerConfigMap().getName();

			Optional<ButtonType> result = createNewAlert( AlertType.CONFIRMATION, "", "Delete " + serverToBeDeleted + "?", "Are you sure?" ).showAndWait();

			// OK button clicked
			if ( result.get() == ButtonType.OK ) {
				e.consume();
				// delete server console/settings panes
				serverController.setDeleted();
				uiController.updateServerOnDelete();

				// show splash screen
				uiController.getSplashPane().setVisible( true );
				uiController.getSplashPane().toFront();
				uiController.getSplashAnchorPane().setVisible( true );
				uiController.getSplashAnchorPane().toFront();

				uiController.getServerInfoImagePane().setVisible( false );
				uiController.getServerInfoPane().setVisible( false );

				// remove "current" from others
				List<HBox> listOfHboxes = uiController.getListViewAppList().getItems();
				for ( HBox item : listOfHboxes ) {
					item.getStyleClass().remove( Globals.StyleClasses.CURRENT );
					item.getParent().setStyle( Globals.StyleVariables.backgroundColourLighterGrey );
				}

				// hide buttons
				ButtonActions buttonActions = new ButtonActions();
				buttonActions.showNoButtons( uiController );

				// Cancel button clicked
			} else {
				e.consume();
			}


		} );

		// Exit jetty server
		Platform.runLater( new Runnable() {

			public void run() {
				// window close requested
				stage.setOnCloseRequest( new EventHandler<WindowEvent>() {

					public void handle( WindowEvent t ) {

						// if there are no server
						if ( ServerManager.getServers() == null || ServerManager.getServers().isEmpty() ) {
							// if ( ServerWrapper1.getInstance().getListOfServerConfigMap() == null ) {
							// appFunctions.deleteServers();
							Platform.exit();

							// if there are servers
						} else {
							// else count the running servers
							int runningServers = 0;
							for ( Entry<Integer, ServerWrapper> server : ServerManager.getServers().entrySet() ) {
								if ( server.getValue().isRunning() ) {
									runningServers++;
								}
							}
							// int runningServers = ServerWrapper1.getInstance().getNumberOfRunningServers();

							// if servers are running call js to run dialog
							if ( runningServers > 0 ) {
								Optional<ButtonType> result = createNewAlert( AlertType.CONFIRMATION, "", "Stop all apps (" + runningServers + ") running.", "Are you sure?" ).showAndWait();

								// if servers have started exit app - show confirm, hard delete servers, exit platform
								if ( result.get() == ButtonType.OK ) {
									stopServersOnExit();
									serverController.hardDeleteServersOnExit();
									Platform.exit();
								} else {
									// do nothing and close dialog
									t.consume();
								}
							}

							// if no servers have started exit app - hard delete servers, exit platform
							else {
								serverController.hardDeleteServersOnExit();
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


	private void serverInfoArrowImageClick( MouseEvent e ) {
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

			Pane serverInfoPane = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SERVERINFOID + ServerController.selectedServer );
			serverInfoPane.setVisible( true );
			serverInfoPane.toFront();

			e.consume();
		}
	}


	private void addWebAppBtnClick() {
		AnchorPane.setTopAnchor( uiController.getTabPaneMaster(), 0.0 );

		uiController.getServerInfoImagePane().setPrefHeight( 40.0 );
		uiController.getServerInfoImagePane().setMinHeight( 40.0 );
		uiController.getServerInfoImagePane().setMaxHeight( 40.0 );

		uiController.getArrowImage().setRotate( 180.0 );
		uiController.getServerInfoStackPaneMaster().setVisible( false );

		// hide splashPane and tabPane
		uiController.getSplashPane().setVisible( false );
		uiController.getSplashAnchorPane().setVisible( false );
		uiController.getSplashAnchorPane().toBack();
		uiController.getTabPane().setVisible( false );
		uiController.getServerInfoImagePane().setVisible( false );
		uiController.getServerInfoPane().setVisible( false );

		ButtonActions buttonActions = new ButtonActions();
		buttonActions.showButtonsOnNewWebApp( uiController );

		// remove "current" class from others
		List<HBox> listOfHboxes = uiController.getListViewAppList().getItems();
		for ( HBox item : listOfHboxes ) {
			item.getStyleClass().remove( Globals.StyleClasses.CURRENT );
			item.getParent().setStyle( Globals.StyleVariables.backgroundColourLighterGrey );
		}

		serverController.setSelectedServer( 0 );

		// show empty settings pane
		Pane p = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID );
		p.toFront();
		p.setVisible( true );
	}


	private void tabPaneSelectionChange( Tab newTab ) {
		uiController.setSelectedTabInstance( newTab );
		int selectedServer = ServerController.selectedServer;
		ButtonActions buttonActions = new ButtonActions();
		if ( newTab.getId().equals( "settingsTab" ) ) {
			// open settings tab and correct pane
			Pane pSettings = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + selectedServer );
			pSettings.setVisible( true );
			pSettings.toFront();

			// disable buttons if server running
			if ( ServerManager.getServers().get( selectedServer ).isRunning() ) {
				buttonActions.showSettingsButtonsOnRunning( uiController );
			} else {
				buttonActions.showSettingsButtonsOnNotRunning( uiController );
			}

		} else if ( newTab.getId().equals( "consoleTab" ) ) {
			// open console tab and correct pane
			uiController.getConsoleStackPane().setVisible( true );

			Iterator<Node> itConsole = uiController.getConsoleStackPane().getChildren().iterator();
			while ( itConsole.hasNext() ) {
				uiController.getSplashPane().setVisible( false );
				uiController.getSplashAnchorPane().setVisible( false );
				uiController.getSplashAnchorPane().toBack();
				ScrollPane consoleScrollPane = (ScrollPane) itConsole.next();

				Platform.runLater( () -> {
					ScrollPane sp1 = consoleScrollPane;
					sp1.setVisible( false );
					// show server info
					if ( sp1.getId().equals( Globals.FXVariables.SCROLLPANEID + selectedServer ) ) {
						sp1.setVisible( true );
						sp1.toFront();
					}
				} );
			}
			// show and disable correct buttons on console showing
			if ( ServerManager.getServers().get( selectedServer ).isRunning() ) {
				buttonActions.showConsoleButtonsOnRunning( uiController );
			} else {
				buttonActions.showConsoleButtonsOnNotRunning( uiController );
			}

		}
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


	private void saveBtnClick() throws IOException {
		boolean newServer = false;
		Pane tempSettings;
		// not new server
		if ( ServerController.selectedServer != 0 ) {
			tempSettings = ( (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + ServerController.selectedServer ) );
		} else {
			newServer = true;
			tempSettings = ( (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID ) );
		}

		// get all user input fields
		String tempName = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.nameTextBox ) ).getText();
		String tempIp = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.ipTextBox ) ).getText();
		String tempPort = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.portTextBox ) ).getText();
		String tempWebFolder = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).getText();
		String tempUri = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.uriTextBox ) ).getText();
		String tempCustomJvm = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).getText();
		String tempJvmArgs = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmArgsTextBox ) ).getText();
		String tempMemory = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryTextBox ) ).getText();
		boolean isCustomJvm = ( (RadioButton) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmRadioBtn ) ).selectedProperty().get();

		int savedServerId = serverController.saveServer( newServer, tempName, tempIp, tempPort, tempWebFolder, tempUri, tempCustomJvm, isCustomJvm, tempJvmArgs, tempMemory );

		if ( newServer ) {
			// add info to server info pane
			// smaller text for web folder
			Text text1 = new Text( '\n' + tempWebFolder );
			text1.setFill( Color.WHITE );
			text1.setFont( fontWebFolder );
			text1.setId( Globals.FXVariables.INFOWEBFOLDERID + savedServerId );
			// larger text for name and url
			Text text2 = new Text( tempName + " - " );
			text2.setFill( Color.WHITE );
			text2.setFont( fontNameUrl );
			text2.setId( Globals.FXVariables.INFONAMEID + savedServerId );
			Text text3 = new Text( tempIp + ":" + tempPort );
			text3.setFill( Color.WHITE );
			text3.setFont( fontNameUrl );
			text3.setId( Globals.FXVariables.INFOURLID + savedServerId );
			TextFlow tf = new TextFlow( text2, text3, text1 );
			tf.getStyleClass().add( Globals.StyleClasses.SERVERINFO );
			tf.setId( Globals.FXVariables.INFOTEXTFLOWID + savedServerId );
			tf.setPadding( new Insets( 5.0 ) );
			Pane newPane = new Pane();
			newPane.getStyleClass().add( Globals.StyleClasses.SERVERINFO );
			newPane.getChildren().add( tf );
			newPane.setVisible( true );
			newPane.setId( Globals.FXVariables.SERVERINFOID + savedServerId );

			// add server info pane to stack pane
			uiController.getServerInfoStackPane().getChildren().add( newPane );

			serverController.setSelectedServer( savedServerId );

			// add info to console info pane
			Text t = new Text();
			t.setId( Globals.FXVariables.lastUpdatedText + savedServerId );
			TextFlow tfLastUpdated = new TextFlow( t );
			tfLastUpdated.getStyleClass().add( Globals.StyleClasses.CONSOLEINFO );
			tfLastUpdated.setLayoutX( 20 );
			tfLastUpdated.setLayoutY( 18 );
			tfLastUpdated.setVisible( false );
			tfLastUpdated.setId( Globals.FXVariables.lastUpdatedTextFlow + savedServerId );

			uiController.getConsoleInfo().getChildren().add( tfLastUpdated );

			// add last updated and memory used info to console info pane
			Text t1 = new Text();
			t1.setId( Globals.FXVariables.memoryText + savedServerId );
			TextFlow tfMemory = new TextFlow( t1 );
			tfMemory.getStyleClass().add( Globals.StyleClasses.CONSOLEINFO );
			tfMemory.setLayoutX( 220 );
			tfMemory.setLayoutY( 18 );
			tfMemory.setVisible( false );
			tfMemory.setId( Globals.FXVariables.memoryTextFlow + savedServerId );

			uiController.getConsoleInfo().getChildren().add( tfMemory );
		}

		// update settings on user updated fields
		uiController.updateSettings( savedServerId, newServer, scene, tempSettings, tempName, tempIp, tempPort, tempWebFolder, tempUri, tempCustomJvm, isCustomJvm, tempJvmArgs, tempMemory );
		// close settings and open console
		tempSettings.setVisible( false );
		ScrollPane s = ( (ScrollPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + savedServerId ) );
		s.setVisible( true );
		s.toFront();

		ButtonActions buttonActions = new ButtonActions();
		buttonActions.showSettingsButtonsOnNotRunning( uiController );

		uiController.getTabPane().setVisible( true );

		// set console tab
		Tab tab = uiController.getTabPane().getTabs().get( 1 );
		uiController.getTabPane().getSelectionModel().select( tab );

		// show correct console buttons
		if ( ServerManager.getServers().get( ServerController.selectedServer ).isRunning() ) {
			buttonActions.showConsoleButtonsOnRunning( uiController );
		} else {
			buttonActions.showConsoleButtonsOnNotRunning( uiController );
		}
		// re display server info pane
		uiController.getServerInfoImagePane().setVisible( true );
		uiController.getServerInfoPane().setVisible( true );
		uiController.showCurrentServerInfoPane();

		if ( newServer ) {
			// change settingsEmpty id to new id
			Pane newSettings = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID );
			newSettings.setId( Globals.FXVariables.SETTINGSID + savedServerId );
			uiController.getSettingsStackPane().getChildren().add( newSettings );
			tempSettings.setVisible( false );
			// add new empty settings
			serverSetup.setUpEmptySettings( settingsLoader, currentJvm, stage, uiController );
		}

	}


	/*
	 * method to validate settings on save
	 */
	private boolean validateSettings() {
		Pane tempSettings;
		// not new server
		if ( ServerController.selectedServer != 0 ) {
			tempSettings = ( (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + ServerController.selectedServer ) );
		} else {
			tempSettings = ( (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID ) );
		}
		// get all user input fields
		String tempName = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.nameTextBox ) ).getText();
		String settingsId = tempSettings.getId().replace( Globals.FXVariables.SETTINGSID, "" );

		/*
		 * if name == server name that you're changing - ie remains unchanged, return true
		 * if name == server name in settings return false
		 * if name != server name in settings return true
		 */
		for ( Entry<Integer, ServerWrapper> server : ServerManager.getServers().entrySet() ) {
			if ( server.getValue().getServerConfigMap().getName().toLowerCase().equals( tempName.toLowerCase() ) ) {

				if ( !settingsId.equals( String.valueOf( server.getKey() ) ) ) {
					Alert alert = createNewAlert( AlertType.WARNING, "Error", "Please choose a different name for the new server", null );
					alert.showAndWait();
					return false;
				} else {
					return true;
				}
			}
		}

		return true;
	}


	/*
	 * button to open current web app - enabled when webapp is running
	 */
	private void openBtnClick() {
		int selectedServer = ServerController.selectedServer;
		String webFolder = "";
		String host = "";
		String port = "";
		String defaultUri = "";
		ServerWrapper serverWrapper = ServerManager.getServers().get( selectedServer );


		webFolder = serverWrapper.getServerConfigMap().getWebFolder();
		host = serverWrapper.getServerConfigMap().getIP();
		port = serverWrapper.getServerConfigMap().getPort();
		defaultUri = serverWrapper.getServerConfigMap().getDefaultWebUri();

		if ( !webFolder.isEmpty() && !webFolder.equals( "" ) ) {
			if ( host.isEmpty() ) {
				host = "127.0.0.1";
			}

			URI uri = java.net.URI.create( "http://" + host + ":" + port + defaultUri );
			ServerActions serverActions = new ServerActions();
			serverActions.goToWebpage( uri, this );
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
	public Alert createNewExceptionAlert( Exception e, String content ) {
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
