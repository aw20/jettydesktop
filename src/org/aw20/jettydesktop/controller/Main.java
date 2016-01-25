package org.aw20.jettydesktop.controller;


import java.awt.Desktop;
import java.io.File;
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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.util.Globals;


public class Main extends Application {

	private final String title = "Jetty Desktop v2.1.1";
	private final String currentJvm = System.getProperty( "java.vm.name" ) + " " + System.getProperty( "java.version" ) + " " + System.getProperty( "java.vm.version" );

	private UIController uiController;
	private ServerController serverController;

	Stage stage;
	static Scene scene;
	Pane tempSettingsPane;

	FXMLLoader settingsLoader = null;


	/**
	 * @param primaryStage
	 */
	@Override
	public void start( Stage primaryStage ) {
		try {
			stage = primaryStage;
			FXMLLoader loader = new FXMLLoader( getClass().getResource( "JettyDesktopUI.fxml" ) );
			settingsLoader = new FXMLLoader( getClass().getResource( "settings.fxml" ) );

			uiController = new UIController( loader, primaryStage );
			serverController = ServerController.getInstance();

			loader.setController( uiController );

			AnchorPane root = loader.load();
			scene = new Scene( root, 889, 655 );

			// initialise
			Map<String, Pane> panes = new HashMap<String, Pane>();
			uiController.initialise();

			setUpSpalashScreen();
			setUpEmptySettings();
			setUpSettings();
			setUpServerList();
			setUpServerInfo();
			addSettingsToStackPane( panes );
			setUpConsoleInfo();

			scene.getStylesheets().add( getClass().getResource( "application.css" ).toExternalForm() );
			scene.getStylesheets().add( getClass().getResource( "alert.css" ).toExternalForm() );
			primaryStage.setScene( scene );
			primaryStage.setTitle( title );
			primaryStage.setResizable( true );
			primaryStage.getIcons().add( new Image( "/org/aw20/jettydesktop/view/logo.png" ) );
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
				saveBtnClick();
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
				Alert alert = new Alert( AlertType.CONFIRMATION );
				alert.setTitle( "" );
				alert.setHeaderText( "Are you sure?" );
				alert.setContentText( "Delete " + serverToBeDeleted + "?" );
				DialogPane dp = alert.getDialogPane();

				dp.getStylesheets().add( getClass().getResource( "alert.css" ).toExternalForm() );

				dp.getStyleClass().remove( "alert" );

				Optional<ButtonType> result = alert.showAndWait();

				if ( result.get() == ButtonType.OK ) {
					e.consume();
					alert.close();
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
					uiController.showNoButtons();
				} else {
					e.consume();
					alert.close();
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
									Alert alert = new Alert( AlertType.CONFIRMATION );
									alert.setTitle( "" );
									alert.setHeaderText( "Are you sure?" );
									alert.setContentText( "Stop all apps (" + runningServers + ") running." );

									DialogPane dp = alert.getDialogPane();

									dp.getStylesheets().add( getClass().getResource( "alert.css" ).toExternalForm() );

									dp.getStyleClass().remove( "alert" );

									Optional<ButtonType> result = alert.showAndWait();
									// if servers have started exit app - show confirm, hard delete servers, exit platform
									if ( result.get() == ButtonType.OK ) {
										stopServersOnExit();
										serverController.hardDeleteServersOnExit();
										Platform.exit();
									} else {
										// do nothing?
										t.consume();
										alert.close();
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

			Pane serverInfoPane = (Pane) scene.lookup( "#" + Globals.FXVariables.SERVERINFOID + serverController.getSelectedServerInstance() );
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

		uiController.showSettingsButtonsOnNotRunning();

		// remove "current" from others
		List<HBox> listOfHboxes = uiController.getListViewAppList().getItems();
		for ( HBox item : listOfHboxes ) {
			item.getStyleClass().remove( Globals.StyleClasses.CURRENT );
			item.getParent().setStyle( "-fx-background-color: #222d35;" );
		}

		serverController.setSelectedServer( null );

		Pane p = (Pane) scene.lookup( "#" + Globals.FXVariables.SETTINGSID + "Empty" );
		p.toFront();
		p.setVisible( true );
	}


	private void tabPaneSelectionChange( Tab newTab ) {
		uiController.setSelectedTabInstance( newTab );
		String ss = serverController.getSelectedServerInstance();
		if ( newTab.getId().equals( "settingsTab" ) ) {
			Pane pSettings = (Pane) scene.lookup( "#" + Globals.FXVariables.SETTINGSID + ss );
			pSettings.setVisible( true );
			pSettings.toFront();


			// disable buttons if server running
			for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
				if ( server.getId().equals( serverController.getSelectedServerInstance() ) ) {
					if ( server.getRunning().equals( "true" ) ) {
						uiController.showSettingsButtonsOnRunning();
					} else {
						uiController.showSettingsButtonsOnNotRunning();
					}
				}
			}
		} else if ( newTab.getId().equals( "consoleTab" ) ) {

			uiController.getConsoleStackPane().setVisible( true );

			Iterator itConsole = uiController.getConsoleStackPane().getChildren().iterator();
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
						uiController.showConsoleButtonsOnRunning();
					} else {
						uiController.showConsoleButtonsOnNotRunning();
					}
				}
			}
		}
	}


	private void stopServersOnExit() {
		Vector v = Executor.getAllInstances();
		for ( Object executor : v ) {
			( (Executor) executor ).exit();
		}

		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			server.setRunning( "false" );
		}
	}


	private void saveBtnClick() {
		boolean newServer = false;
		Pane tempSettings;
		// not new server
		if ( serverController.getSelectedServerInstance() != null ) {
			tempSettings = ( (Pane) scene.lookup( "#" + Globals.FXVariables.SETTINGSID + serverController.getSelectedServerInstance() ) );
		} else {
			newServer = true;
			tempSettings = ( (Pane) scene.lookup( "#" + Globals.FXVariables.SETTINGSEMPTYID ) );
		}

		String tempName = ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.nameTextBox ) ).getText();
		String tempIp = ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.ipTextBox ) ).getText();
		String tempPort = ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.portTextBox ) ).getText();
		String tempWebFolder = ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).getText();
		String tempUri = ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.uriTextBox ) ).getText();
		String tempCustomJvm = ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).getText();
		String tempJvmArgs = ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.jvmArgsTextBox ) ).getText();
		String tempMemory = ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.memoryTextBox ) ).getText();
		boolean isCustomJvm = ( (RadioButton) tempSettings.lookup( "#" + Globals.FXVariables.customJvmRadioBtn ) ).selectedProperty().get();

		String savedServerId = serverController.saveServer( newServer, tempName, tempIp, tempPort, tempWebFolder, tempUri, tempCustomJvm, isCustomJvm, tempJvmArgs, tempMemory );

		if ( newServer ) {
			// add info to server info pane
			Text text1 = new Text( '\n' + tempWebFolder );
			text1.setFill( Color.WHITE );
			text1.setFont( Font.font( "Arial", FontWeight.NORMAL, 13 ) );
			text1.setId( Globals.FXVariables.INFOWEBFOLDERID + savedServerId );
			Text text2 = new Text( tempName + " - " );
			text2.setFill( Color.WHITE );
			text2.setFont( Font.font( "Arial", FontWeight.BOLD, 15 ) );
			text2.setId( Globals.FXVariables.INFONAMEID + savedServerId );
			Text text3 = new Text( tempIp + ":" + tempPort );
			text3.setFill( Color.WHITE );
			text3.setFont( Font.font( "Arial", FontWeight.BOLD, 15 ) );
			text3.setId( Globals.FXVariables.INFOURLID + savedServerId );
			TextFlow tf = new TextFlow( text2, text3, text1 );
			tf.getStyleClass().add( "serverInfo" );
			tf.setId( Globals.FXVariables.INFOTEXTFLOWID + savedServerId );
			tf.setPadding( new Insets( 5.0 ) );
			Pane newPane = new Pane();
			newPane.getStyleClass().add( "serverInfo" );
			newPane.getChildren().add( tf );
			newPane.setVisible( true );
			newPane.setId( Globals.FXVariables.SERVERINFOID + savedServerId );

			uiController.getServerInfoStackPane().getChildren().add( newPane );

			serverController.setSelectedServer( savedServerId );
		}

		uiController.updateSettings( savedServerId, newServer, scene, tempSettings, tempName, tempIp, tempPort, tempWebFolder, tempUri, tempCustomJvm, isCustomJvm, tempJvmArgs, tempMemory );
		tempSettings.setVisible( false );
		ScrollPane s = ( (ScrollPane) scene.lookup( "#" + Globals.FXVariables.SCROLLPANEID + savedServerId ) );
		s.setVisible( true );
		s.toFront();

		uiController.showSettingsButtonsOnNotRunning();
		uiController.getTabPane().setVisible( true );

		// set console tab
		Tab tab = uiController.getTabPane().getTabs().get( 1 );
		uiController.getTabPane().getSelectionModel().select( tab );

		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getId().equals( serverController.getSelectedServerInstance() ) ) {
				if ( server.getRunning().equals( "true" ) ) {
					uiController.showConsoleButtonsOnRunning();
				} else {
					uiController.showConsoleButtonsOnNotRunning();
				}
			}
		}

		uiController.getServerInfoImagePane().setVisible( true );
		uiController.getServerInfoPane().setVisible( true );
		uiController.showCurrentServerInfoPane();


		if ( newServer ) {
			// change settingsEmpty id to new id

			Pane newSettings = (Pane) scene.lookup( "#" + Globals.FXVariables.SETTINGSEMPTYID );
			newSettings.setId( Globals.FXVariables.SETTINGSID + savedServerId );
			uiController.getSettingsStackPane().getChildren().add( newSettings );
			tempSettings.setVisible( false ); // add new empty settings
			setUpEmptySettings();
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
			goToWebpage( uri );
		}
	}


	private void setUpEmptySettings() {
		try {
			// TODO: THIS IS NOT RECOMMENDED - http://stackoverflow.com/questions/21424843/exception-has-occuredroot-value-already-specified-in-javafx-when-loading-fxml-p
			settingsLoader.setRoot( null ); // set to null to reinitialise
			tempSettingsPane = settingsLoader.load();

			Pane tempPane = tempSettingsPane;
			tempPane.getStyleClass().add( "settingsPane" );
			tempPane.setVisible( false );
			tempPane.toFront();

			GridPane.setHalignment( ( (Label) tempPane.lookup( "#lblName" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempPane.lookup( "#lblIp" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempPane.lookup( "#lblPort" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempPane.lookup( "#lblFolder" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempPane.lookup( "#lblUri" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempPane.lookup( "#lblArgs" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempPane.lookup( "#lblRuntime" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempPane.lookup( "#lblMemory" ) ), HPos.RIGHT );

			Button btnGetFolder = (Button) tempPane.lookup( "#" + Globals.FXVariables.BTNGETFOLDERID );
			btnGetFolder.getStyleClass().add( "browseBtn" );
			Button btnBrowse = (Button) tempPane.lookup( "#" + Globals.FXVariables.BTNBROWSEID );
			btnBrowse.getStyleClass().add( "browseBtn" );
			btnBrowse.setMinSize( Button.USE_PREF_SIZE, Button.USE_PREF_SIZE );

			( (RadioButton) tempPane.lookup( "#" + Globals.FXVariables.defaultJvmRadioBtn ) ).setSelected( true );

			( (Label) tempPane.lookup( "#" + Globals.FXVariables.jvmLabel ) ).setText( currentJvm );
			( (Label) tempPane.lookup( "#" + Globals.FXVariables.jvmLabel ) ).setMinSize( Label.USE_PREF_SIZE, Label.USE_PREF_SIZE );


			// set buttons to open directory chooser
			btnGetFolder.setOnAction( event -> {
				final DirectoryChooser directoryChooser = new DirectoryChooser();
				if ( ( (TextField) tempPane.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).getText() != null ) {
					if ( !( (TextField) tempPane.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).getText().isEmpty() ) {
						directoryChooser.setInitialDirectory( new File( ( (TextField) tempPane.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).getText() ) );
					}
				}
				final File selectedDirectory = directoryChooser.showDialog( stage );
				if ( selectedDirectory != null ) {
					selectedDirectory.getAbsolutePath();
					( (TextField) tempPane.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).setText( selectedDirectory.getAbsolutePath() );
				}
			} );

			btnBrowse.setOnAction( event -> {
				final DirectoryChooser directoryChooser = new DirectoryChooser();
				if ( ( (TextField) tempPane.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).getText() != null ) {
					if ( !( (TextField) tempPane.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).getText().isEmpty() ) {
						directoryChooser.setInitialDirectory( new File( ( (TextField) tempPane.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).getText() ) );
					}
				}
				final File selectedDirectory = directoryChooser.showDialog( stage );
				if ( selectedDirectory != null ) {
					selectedDirectory.getAbsolutePath();
					( (TextField) tempPane.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).setText( selectedDirectory.getAbsolutePath() );
				}
			} );

			uiController.getTabPaneMaster().getChildren().add( tempPane );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}


	private void setUpSettings() {
		// load settings into server config map
		serverController.loadSettings();
		for ( ServerConfigMap scm : serverController.getServerConfigListInstance() ) {
			// add ids to list
			uiController.getServerConfigIdList().add( scm.getId() );

			// add console for each server
			TextFlow newTextFlow = new TextFlow();
			newTextFlow.setId( Globals.FXVariables.CONSOLEID + scm.getId() );
			newTextFlow.setVisible( true );
			ScrollPane scrollPane = new ScrollPane( newTextFlow );
			scrollPane.setId( Globals.FXVariables.SCROLLPANEID + scm.getId() );
			uiController.getConsoleStackPane().getChildren().add( scrollPane );
		}
	}


	private void setUpServerInfo() {
		// set server info hidden initially
		AnchorPane.setTopAnchor( uiController.getTabPaneMaster(), 0.0 );

		uiController.getArrowImage().setRotate( 180.0 );
		uiController.getServerInfoStackPaneMaster().setVisible( false );

		if ( serverController.getSelectedServerInstance() == null ) {
			uiController.getServerInfoImagePane().setVisible( false );
		}

		for ( ServerConfigMap scm : serverController.getServerConfigListInstance() ) {
			// add info to server info pane
			Text text1 = new Text( '\n' + scm.getWebFolder() );
			text1.setFill( Color.WHITE );
			text1.setFont( Font.font( "Arial", FontWeight.NORMAL, 13 ) );
			text1.setId( Globals.FXVariables.INFOWEBFOLDERID + scm.getId() );
			Text text2 = new Text( scm.getName() + " - " );
			text2.setFill( Color.WHITE );
			text2.setFont( Font.font( "Arial", FontWeight.BOLD, 15 ) );
			text2.setId( Globals.FXVariables.INFONAMEID + scm.getId() );
			Text text3 = new Text( scm.getIP() + ":" + scm.getPort() );
			text3.setFill( Color.WHITE );
			text3.setFont( Font.font( "Arial", FontWeight.BOLD, 15 ) );
			text3.setId( Globals.FXVariables.INFOURLID + scm.getId() );
			TextFlow tf = new TextFlow( text2, text3, text1 );
			tf.getStyleClass().add( "serverInfo" );
			tf.setId( Globals.FXVariables.INFOTEXTFLOWID + scm.getId() );
			tf.setPadding( new Insets( 5.0 ) );
			Pane newPane = new Pane();
			newPane.getStyleClass().add( "serverInfo" );
			newPane.getChildren().add( tf );
			newPane.setVisible( true );
			newPane.setId( Globals.FXVariables.SERVERINFOID + scm.getId() );

			uiController.getServerInfoStackPane().getChildren().add( newPane );
		}

	}


	private void setUpConsoleInfo() {
		// set server info hidden initially

		for ( ServerConfigMap scm : serverController.getServerConfigListInstance() ) {
			// add info to console info pane
			Text t = new Text();
			t.setId( Globals.FXVariables.lastUpdatedText + scm.getId() );
			TextFlow tfLastUpdated = new TextFlow( t );
			tfLastUpdated.getStyleClass().add( "consoleInfo" );
			tfLastUpdated.setLayoutX( 20 );
			tfLastUpdated.setLayoutY( 18 );
			tfLastUpdated.setVisible( false );
			tfLastUpdated.setId( Globals.FXVariables.lastUpdatedTextFlow + scm.getId() );

			uiController.getConsoleInfo().getChildren().add( tfLastUpdated );

			// add info to console info pane
			Text t1 = new Text();
			t1.setId( Globals.FXVariables.memoryText + scm.getId() );
			TextFlow tfMemory = new TextFlow( t1 );
			tfMemory.getStyleClass().add( "consoleInfo" );
			tfMemory.setLayoutX( 220 );
			tfMemory.setLayoutY( 18 );
			tfMemory.setVisible( false );
			tfMemory.setId( Globals.FXVariables.memoryTextFlow + scm.getId() );

			uiController.getConsoleInfo().getChildren().add( tfMemory );
		}

	}


	private void setUpServerList() {
		// load server list
		uiController.getListViewAppList().setPadding( new Insets( 0.0 ) );

		// set action for on click server
		for ( ServerConfigMap scm : serverController.getServerConfigListInstance() ) {
			uiController.addHBoxToList( scm, scene, false );
		}

		for ( Map.Entry<String, AnchorPane> h : uiController.serversForList.entrySet() ) {
			AnchorPane hbox = h.getValue();
			uiController.getListViewAppList().getItems().add( (HBox) hbox.getChildren().get( 0 ) );
		}

		uiController.refreshServerList();

	}


	private void addSettingsToStackPane( Map<String, Pane> panes ) {
		// set up settings
		try {
			for ( ServerConfigMap scm : serverController.getServerConfigListInstance() ) {
				// add to settings stack pane

				// TODO: THIS IS NOT RECOMMENDED - http://stackoverflow.com/questions/21424843/exception-has-occuredroot-value-already-specified-in-javafx-when-loading-fxml-p
				settingsLoader.setRoot( null ); // set to null to reinitialise
				Pane tempSettings = settingsLoader.load();
				tempSettings.setId( Globals.FXVariables.SETTINGSID + scm.getId() );

				GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblName" ) ), HPos.RIGHT );
				GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblIp" ) ), HPos.RIGHT );
				GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblPort" ) ), HPos.RIGHT );
				GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblFolder" ) ), HPos.RIGHT );
				GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblUri" ) ), HPos.RIGHT );
				GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblArgs" ) ), HPos.RIGHT );
				GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblRuntime" ) ), HPos.RIGHT );
				GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblMemory" ) ), HPos.RIGHT );

				( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.nameTextBox ) ).setText( scm.getName() );
				( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.ipTextBox ) ).setText( scm.getIP() );
				( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.portTextBox ) ).setText( scm.getPort() );
				( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).setText( scm.getWebFolder() );
				( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.uriTextBox ) ).setText( scm.getDefaultWebUri() );
				( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).setText( scm.getCustomJVM() );
				( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.jvmArgsTextBox ) ).setText( scm.getDefaultJVMArgs() );
				( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.memoryTextBox ) ).setText( scm.getMemoryJVM() );
				( (Label) tempSettings.lookup( "#" + Globals.FXVariables.jvmLabel ) ).setText( currentJvm );
				( (Label) tempSettings.lookup( "#" + Globals.FXVariables.jvmLabel ) ).setMinSize( Label.USE_PREF_SIZE, Label.USE_PREF_SIZE );


				if ( scm.getCurrentJVM() != null ) {
					( (RadioButton) tempSettings.lookup( "#" + Globals.FXVariables.defaultJvmRadioBtn ) ).setSelected( true );
				} else {
					( (RadioButton) tempSettings.lookup( "#" + Globals.FXVariables.customJvmRadioBtn ) ).setSelected( true );
				}

				tempSettings.getStyleClass().add( "settingsPane" );

				Button tempBtnGetFolder = (Button) tempSettings.lookup( "#" + Globals.FXVariables.BTNGETFOLDERID );
				tempBtnGetFolder.setId( Globals.FXVariables.BTNGETFOLDERID + scm.getId() );
				tempBtnGetFolder.getStyleClass().add( "browseBtn" );

				Button tempBtnBrowse = (Button) tempSettings.lookup( "#" + Globals.FXVariables.BTNBROWSEID );
				tempBtnBrowse.setId( Globals.FXVariables.BTNBROWSEID + scm.getId() );
				tempBtnBrowse.getStyleClass().add( "browseBtn" );
				tempBtnBrowse.setMinSize( Button.USE_PREF_SIZE, Button.USE_PREF_SIZE );


				// set buttons to open directory chooser
				tempBtnGetFolder.setOnAction( event -> {
					final DirectoryChooser directoryChooser = new DirectoryChooser();
					if ( ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).getText() != null ) {
						if ( !( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).getText().isEmpty() ) {
							directoryChooser.setInitialDirectory( new File( ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).getText() ) );
						}
					}
					final File selectedDirectory = directoryChooser.showDialog( stage );
					if ( selectedDirectory != null ) {
						selectedDirectory.getAbsolutePath();
						( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).setText( selectedDirectory.getAbsolutePath() );
					}
				} );

				tempBtnBrowse.setOnAction( event -> {
					final DirectoryChooser directoryChooser = new DirectoryChooser();
					if ( ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).getText() != null ) {
						if ( !( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).getText().isEmpty() ) {
							directoryChooser.setInitialDirectory( new File( ( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).getText() ) );
						}
					}
					final File selectedDirectory = directoryChooser.showDialog( stage );
					if ( selectedDirectory != null ) {
						selectedDirectory.getAbsolutePath();
						( (TextField) tempSettings.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).setText( selectedDirectory.getAbsolutePath() );
					}
				} );

				tempSettings.setVisible( true );
				panes.put( scm.getId(), tempSettings );


			}
			uiController.getSettingsStackPane().getChildren().addAll( panes.values() );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}


	private void setUpSpalashScreen() {
		// set up both screens:

		Hyperlink hpl = new Hyperlink( "Jetty on Github" );
		hpl.setOnAction( event -> {
			goToWebpage( java.net.URI.create( "https://github.com/aw20/jettydesktop" ) );
		} );
		TextFlow textFlow = null;

		if ( !serverController.getServerConfigListInstance().isEmpty() ) {
			textFlow = new TextFlow( new Text( title ), new Text( "\nView " ), hpl, new Text( " for more information" ), new Text( "\nClick an app to start" ) );
			textFlow.getStyleClass().add( "splashScreenText" );
			uiController.getSplashPane().add( textFlow, 1, 1 );
		}

		else {
			textFlow = new TextFlow( new Text( title ), new Text( "\nView " ), hpl, new Text( " for more information" ), new Text( "\nAdd an app to start" ) );
			textFlow.getStyleClass().add( "splashScreenText" );
			uiController.getSplashPane().add( textFlow, 1, 1 );
		}
	}


	private void goToWebpage( URI page ) {
		if ( Desktop.isDesktopSupported() ) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse( page );
			} catch ( IOException e ) {
				// do nothing
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec( "xdg-open " + page );
			} catch ( IOException e ) {}
		}
	}
}
