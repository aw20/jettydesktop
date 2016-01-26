package org.aw20.jettydesktop.controller;


import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.util.Globals;


public class Main extends Application {

	private final String title = "Jetty Desktop v2.1.1";
	private final String currentJvm = System.getProperty( "java.vm.name" ) + " " + System.getProperty( "java.version" ) + " " + System.getProperty( "java.vm.version" );
	private final Font fontWebFolder = Font.font( "Arial", FontWeight.NORMAL, 13 );
	private final Font fontNameUrl = Font.font( "Arial", FontWeight.BOLD, 15 );

	private final String viewDir = "/org/aw20/jettydesktop/view/";

	private UIController uiController;
	private ServerController serverController;
	private ServerSetup serverSetup;

	private Stage stage;
	private Scene scene;
	private FXMLLoader settingsLoader = null;


	/**
	 * @param primaryStage
	 */
	@Override
	public void start( Stage primaryStage ) {
		try {
			stage = primaryStage;

			FXMLLoader loader = new FXMLLoader( getClass().getResource( viewDir + "JettyDesktopUI.fxml" ) );
			settingsLoader = new FXMLLoader( getClass().getResource( viewDir + "settings.fxml" ) );

			uiController = UIController.getInstance();
			serverController = ServerController.getInstance();

			serverSetup = new ServerSetup();

			loader.setController( uiController );

			AnchorPane root = loader.load();
			scene = new Scene( root, 889, 655 );

			// initialise
			Map<String, Pane> panes = new HashMap<String, Pane>();

			uiController.initialise();

			serverSetup.setUpSpalashScreen( this, serverController, uiController, title );
			serverSetup.setUpEmptySettings( settingsLoader, currentJvm, stage, uiController );
			serverSetup.setUpSettings( serverController, uiController );
			serverSetup.setUpServerList( uiController, serverController, scene );
			serverSetup.setUpServerInfo( serverController, uiController, fontWebFolder, fontNameUrl );
			serverSetup.addSettingsToStackPane( panes, serverController, uiController, settingsLoader, currentJvm, stage, this );
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
					if ( uiController.getListViewAppList().getSelectionModel().getSelectedItem() != null ) {
						HBox hbox = uiController.getListViewAppList().getSelectionModel().getSelectedItem();

						// get selected server id
						String serverId = hbox.getId().replace( "hbox", "" );

						Hyperlink h = (Hyperlink) hbox.getChildren().get( 0 );
						ServerConfigMap scm = null;

						for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
							if ( server.getId().equals( serverId ) ) {
								serverController.setSelectedServer( server.getId() );
								scm = server;
								break;
							}
						}

						uiController.handleListViewOnClick( hbox, scene, h, scm );
					}
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
				uiController.startBtnClick( null, scene );
			} );


			/**
			 * Method to handle click on stop button
			 */
			uiController.getStopBtn().setOnAction( ( ActionEvent e ) -> {
				uiController.stopBtnClick( null, scene );
			} );


			/**
			 * Method to handle click on clear
			 */
			uiController.getClearBtn().setOnAction( ( ActionEvent e ) -> {
				uiController.clearConsole( serverController.getSelectedServerInstance() );
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
					saveBtnClick();
				} catch ( Exception e1 ) {
					Alert alert = createNewAlert( AlertType.ERROR, "Error", "An error has occurred while saving a web app", null );
					alert.showAndWait();
				}
			} );


			/**
			 * Method to handle click on delete
			 */
			uiController.getDeleteBtn().setOnAction( ( ActionEvent e ) -> {

				String serverToBeDeleted = null;
				for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
					if ( server.getId().equals( serverController.getSelectedServerInstance() ) ) {
						serverToBeDeleted = server.getName();
					}
				}

				Optional<ButtonType> result = createNewAlert( AlertType.CONFIRMATION, "", "Delete " + serverToBeDeleted + "?", "Are you sure?" ).showAndWait();

				if ( result.get() == ButtonType.OK ) {
					e.consume();
					serverController.setDeleted();
					uiController.updateServerOnDelete();

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
						item.getParent().setStyle( "-fx-background-color: #222d35;" );
					}

					// hide buttons
					ButtonActions buttonActions = new ButtonActions();
					buttonActions.showNoButtons( uiController );
				} else {
					e.consume();
				}


			} );

			// Exit jetty server
			Platform.runLater( new Runnable() {

				public void run() {

					stage.setOnCloseRequest( new EventHandler<WindowEvent>() {

						public void handle( WindowEvent t ) {

							// if there are no server
							if ( serverController.getServerConfigListInstance() == null ) {
								// appFunctions.deleteServers();
								Platform.exit();

								// if there are servers
							} else {
								// else count the running servers
								int runningServers = getRunningApps();

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
		} catch ( Exception e ) {
			// Alert alert = createNewAlert( AlertType.CONFIRMATION, "", "An error has occurred while starting Jetty Desktop", null );
			// alert.showAndWait();
			e.printStackTrace();
		}
	}


	public static void main( String[] args ) {
		launch( args );
	}


	private int getRunningApps() {
		int running = 0;
		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getRunning().equals( "true" ) ) {
				running++;
			}
		}
		return running;
	}


	private void serverInfoArrowImageClick( MouseEvent e ) {
		if ( AnchorPane.getTopAnchor( uiController.getTabPaneMaster() ) != 0.0 ) {
			AnchorPane.setTopAnchor( uiController.getTabPaneMaster(), 0.0 );

			uiController.getServerInfoImagePane().setPrefHeight( 40.0 );
			uiController.getServerInfoImagePane().setMinHeight( 40.0 );
			uiController.getServerInfoImagePane().setMaxHeight( 40.0 );

			uiController.getArrowImage().setRotate( 180.0 );
			uiController.getServerInfoStackPaneMaster().setVisible( false );
		} else {
			AnchorPane.setTopAnchor( uiController.getTabPaneMaster(), 47.0 );

			uiController.getArrowImage().setRotate( 0.0 );
			uiController.getServerInfoPane().setMaxHeight( 47.0 );
			uiController.getServerInfoPane().setMinHeight( 47.0 );
			uiController.getServerInfoPane().setPrefHeight( 47.0 );

			uiController.getServerInfoImagePane().setPrefHeight( 47.0 );
			uiController.getServerInfoImagePane().setMinHeight( 47.0 );
			uiController.getServerInfoImagePane().setMaxHeight( 47.0 );

			uiController.getServerInfoStackPaneMaster().setVisible( true );

			Pane serverInfoPane = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SERVERINFOID + serverController.getSelectedServerInstance() );
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

		// remove "current" from others
		List<HBox> listOfHboxes = uiController.getListViewAppList().getItems();
		for ( HBox item : listOfHboxes ) {
			item.getStyleClass().remove( Globals.StyleClasses.CURRENT );
			item.getParent().setStyle( "-fx-background-color: #222d35;" );
		}

		serverController.setSelectedServer( null );

		Pane p = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + "Empty" );
		p.toFront();
		p.setVisible( true );
	}


	private void tabPaneSelectionChange( Tab newTab ) {
		uiController.setSelectedTabInstance( newTab );
		String ss = serverController.getSelectedServerInstance();
		ButtonActions buttonActions = new ButtonActions();
		if ( newTab.getId().equals( "settingsTab" ) ) {
			Pane pSettings = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + ss );
			pSettings.setVisible( true );
			pSettings.toFront();


			// disable buttons if server running
			for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
				if ( server.getId().equals( serverController.getSelectedServerInstance() ) ) {
					if ( server.getRunning().equals( "true" ) ) {
						buttonActions.showSettingsButtonsOnRunning( uiController );
					} else {
						buttonActions.showSettingsButtonsOnNotRunning( uiController );
					}
				}
			}
		} else if ( newTab.getId().equals( "consoleTab" ) ) {

			uiController.getConsoleStackPane().setVisible( true );

			Iterator<Node> itConsole = uiController.getConsoleStackPane().getChildren().iterator();
			while ( itConsole.hasNext() ) {
				uiController.getSplashPane().setVisible( false );
				uiController.getSplashAnchorPane().setVisible( false );
				uiController.getSplashAnchorPane().toBack();
				ScrollPane consoleScrollPane = (ScrollPane) itConsole.next();

				Platform.runLater( ( ) -> {
					ScrollPane sp1 = consoleScrollPane;
					sp1.setVisible( false );
					// show server info
					if ( sp1.getId().equals( Globals.FXVariables.SCROLLPANEID + serverController.getSelectedServerInstance() ) ) {
						sp1.setVisible( true );
						sp1.toFront();
					}
				} );
			}

			for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
				if ( server.getId().equals( serverController.getSelectedServerInstance() ) ) {
					if ( server.getRunning().equals( "true" ) ) {
						buttonActions.showConsoleButtonsOnRunning( uiController );
					} else {
						buttonActions.showConsoleButtonsOnNotRunning( uiController );
					}
				}
			}
		}
	}


	private void stopServersOnExit() {
		@SuppressWarnings( "rawtypes" )
		Vector v = Executor.getAllInstances();
		for ( Object executor : v ) {
			( (Executor) executor ).exit();
		}

		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			server.setRunning( "false" );
		}
	}


	private void saveBtnClick() throws IOException {
		boolean newServer = false;
		Pane tempSettings;
		// not new server
		if ( serverController.getSelectedServerInstance() != null ) {
			tempSettings = ( (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + serverController.getSelectedServerInstance() ) );
		} else {
			newServer = true;
			tempSettings = ( (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID ) );
		}

		String tempName = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.nameTextBox ) ).getText();
		String tempIp = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.ipTextBox ) ).getText();
		String tempPort = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.portTextBox ) ).getText();
		String tempWebFolder = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).getText();
		String tempUri = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.uriTextBox ) ).getText();
		String tempCustomJvm = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).getText();
		String tempJvmArgs = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmArgsTextBox ) ).getText();
		String tempMemory = ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryTextBox ) ).getText();
		boolean isCustomJvm = ( (RadioButton) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmRadioBtn ) ).selectedProperty().get();

		String savedServerId = serverController.saveServer( newServer, tempName, tempIp, tempPort, tempWebFolder, tempUri, tempCustomJvm, isCustomJvm, tempJvmArgs, tempMemory );

		if ( newServer ) {
			// add info to server info pane
			Text text1 = new Text( '\n' + tempWebFolder );
			text1.setFill( Color.WHITE );
			text1.setFont( fontWebFolder );
			text1.setId( Globals.FXVariables.INFOWEBFOLDERID + savedServerId );
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

			// add info to console info pane
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

		uiController.updateSettings( savedServerId, newServer, scene, tempSettings, tempName, tempIp, tempPort, tempWebFolder, tempUri, tempCustomJvm, isCustomJvm, tempJvmArgs, tempMemory );
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

		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getId().equals( serverController.getSelectedServerInstance() ) ) {
				if ( server.getRunning().equals( "true" ) ) {
					buttonActions.showConsoleButtonsOnRunning( uiController );
				} else {
					buttonActions.showConsoleButtonsOnNotRunning( uiController );
				}
			}
		}

		uiController.getServerInfoImagePane().setVisible( true );
		uiController.getServerInfoPane().setVisible( true );
		uiController.showCurrentServerInfoPane();


		if ( newServer ) {
			// change settingsEmpty id to new id

			Pane newSettings = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID );
			newSettings.setId( Globals.FXVariables.SETTINGSID + savedServerId );
			uiController.getSettingsStackPane().getChildren().add( newSettings );
			tempSettings.setVisible( false ); // add new empty settings
			serverSetup.setUpEmptySettings( settingsLoader, currentJvm, stage, uiController );
		}

	}


	private void openBtnClick() {
		String selectedServer = serverController.getSelectedServerInstance();
		String webFolder = "";
		String host = "";
		String port = "";
		String defaultUri = "";
		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getId().equals( selectedServer ) ) {
				webFolder = server.getWebFolder();
				host = server.getIP();
				port = server.getPort();
				defaultUri = server.getDefaultWebUri();
				break;
			}
		}

		if ( !webFolder.isEmpty() && !webFolder.equals( "" ) ) {
			if ( host.isEmpty() ) {
				host = "127.0.0.1";
			}

			URI uri = java.net.URI.create( "http://" + host + ":" + port + defaultUri );
			ServerActions serverActions = new ServerActions();
			serverActions.goToWebpage( uri, this );
		}
	}


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
}
