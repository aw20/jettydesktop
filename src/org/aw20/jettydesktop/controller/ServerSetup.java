package org.aw20.jettydesktop.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.jettydesktop.ui.ServerWrapper;
import org.aw20.util.Globals;


public class ServerSetup {

	public static void initialise( UIController uiController, ServerController serverController ) {
		// on start up, because no web app is selected, don't show console/settings tabs
		if ( serverController.getSelectedServerInstance() == null ) {
			uiController.getTabPane().setVisible( false );
		}

		uiController.getTabPane().getSelectionModel().select( 1 );

		ButtonActions buttonActions = new ButtonActions();
		buttonActions.showNoButtons( uiController );

		uiController.getVboxAppListScrollPane().setFitToWidth( true );
		uiController.getVboxAppListScrollPane().setFitToHeight( true );

		uiController.getBtnAddWebApp().setText( "+ add webapp" );
	}


	public HBox addHBoxToList( UIController uiController, ServerConfigMap scm, Scene scene, boolean newServer ) {

		String id = ServerWrapper.getInstance().getIdOfServer( scm );

		// RUNNING CIRCLE
		Circle c = new Circle( 5.0f, Color.GREY );
		c.setId( Globals.FXVariables.RUNNINGID + id );

		// POLYGON
		Polygon p = new Polygon();

		p.getPoints().setAll(
				0d, 0d,
				12d, 6d,
				0d, 12d
				);

		p.setId( "polygon" + id );
		p.setFill( Color.GREEN );

		p.setVisible( false );

		p.setTranslateY( 10.0 );
		p.setTranslateX( 12.0 );

		// play/stop button click
		p.setOnMouseClicked( event -> {
			if ( ServerWrapper.getInstance().getRunning( id ) ) {
				uiController.stopBtnClick( id, scene );
			}
				else {
					uiController.startBtnClick( id, scene );
					uiController.setSelectedTabInstance( uiController.getTabPane().getTabs().get( 1 ) );
					if ( !uiController.getServerInfoImagePane().isVisible() ) {
						uiController.getServerInfoPane().setVisible( true );
						uiController.getServerInfoImagePane().setVisible( true );
					}
					uiController.showCurrentServerInfoPane();
				}
			} );

		Pane polygonPane = new Pane( p );
		polygonPane.setPrefWidth( Globals.StyleVariables.polygonPaneWidth );
		polygonPane.setMinWidth( Globals.StyleVariables.polygonPaneWidth );
		polygonPane.setMaxWidth( Globals.StyleVariables.polygonPaneWidth );


		// HYPERLINK
		Hyperlink h = new Hyperlink( scm.getName(), c );

		h.setPadding( new Insets( 0, 0, 0, 10 ) );
		h.setId( id );
		h.getStyleClass().add( "serverListHyperlink" );
		h.setPrefWidth( Globals.StyleVariables.hyperlinkWidth - 18 );
		h.setMinWidth( Globals.StyleVariables.hyperlinkWidth - 18 );
		h.setMaxWidth( Globals.StyleVariables.hyperlinkWidth - 18 );

		HBox hbox = new HBox( h, polygonPane );

		hbox.getStyleClass().add( "hboxServer" );
		hbox.setPrefWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
		hbox.setMaxWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
		hbox.setMinWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
		hbox.setId( Globals.FXVariables.HBOXID + id );


		AnchorPane a = new AnchorPane();
		a.getChildren().add( hbox );

		AnchorPane.setLeftAnchor( hbox, 0.0 );
		AnchorPane.setRightAnchor( hbox, 0.0 );

		uiController.getServersForListInstance().put( id, a );

		polygonPane.setOnMouseEntered( event -> {
			p.setVisible( true );
		} );

		polygonPane.setOnMouseExited( event -> {
			p.setVisible( false );
		} );

		h.setOnMouseEntered( event -> {
			p.setVisible( true );
		} );

		h.setOnMouseExited( event -> {
			p.setVisible( false );
		} );

		h.setOnAction( event -> {
			uiController.handleListViewOnClick( hbox, scene, h, scm );
		} );
		return hbox;
	}


	public void setUpEmptySettings( FXMLLoader settingsLoader, String currentJvm, Stage stage, UIController uiController ) throws IOException {
		// TODO: THIS IS NOT RECOMMENDED - http://stackoverflow.com/questions/21424843/exception-has-occuredroot-value-already-specified-in-javafx-when-loading-fxml-p
		settingsLoader.setRoot( null ); // set to null to reinitialise
		Pane tempSettingsPane = settingsLoader.load();

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

		Button btnGetFolder = (Button) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.BTNGETFOLDERID );
		btnGetFolder.getStyleClass().add( "browseBtn" );
		Button btnBrowse = (Button) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.BTNBROWSEID );
		btnBrowse.getStyleClass().add( "browseBtn" );
		btnBrowse.setMinSize( Button.USE_PREF_SIZE, Button.USE_PREF_SIZE );

		( (RadioButton) tempPane.lookup( "#" + Globals.FXVariables.defaultJvmRadioBtn ) ).setSelected( true );

		( (Label) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmLabel ) ).setText( currentJvm );
		( (Label) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmLabel ) ).setMinSize( Label.USE_PREF_SIZE, Label.USE_PREF_SIZE );


		// set buttons to open directory chooser
		btnGetFolder.setOnAction( event -> {
			final DirectoryChooser directoryChooser = new DirectoryChooser();
			if ( ( (TextField) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).getText() != null ) {
				if ( !( (TextField) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).getText().isEmpty() ) {
					directoryChooser.setInitialDirectory( new File( ( (TextField) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).getText() ) );
				}
			}
			final File selectedDirectory = directoryChooser.showDialog( stage );
			if ( selectedDirectory != null ) {
				selectedDirectory.getAbsolutePath();
				( (TextField) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).setText( selectedDirectory.getAbsolutePath() );
			}
		} );

		btnBrowse.setOnAction( event -> {
			final DirectoryChooser directoryChooser = new DirectoryChooser();
			if ( ( (TextField) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).getText() != null ) {
				if ( !( (TextField) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).getText().isEmpty() ) {
					directoryChooser.setInitialDirectory( new File( ( (TextField) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).getText() ) );
				}
			}
			final File selectedDirectory = directoryChooser.showDialog( stage );
			if ( selectedDirectory != null ) {
				selectedDirectory.getAbsolutePath();
				( (TextField) tempPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).setText( selectedDirectory.getAbsolutePath() );
			}
		} );

		uiController.getTabPaneMaster().getChildren().add( tempPane );
	}


	public void setUpSettings( ServerController serverController, UIController uiController ) {
		// load settings into server config map
		serverController.loadSettings();
		for ( ServerConfigMap scm : ServerWrapper.getInstance().getListOfServerConfigMap() ) {
			// add ids to list
			String id = ServerWrapper.getInstance().getIdOfServer( scm );
			uiController.getServerConfigIdList().add( id );

			// add console for each server
			TextFlow newTextFlow = new TextFlow();
			newTextFlow.setId( Globals.FXVariables.CONSOLEID + id );
			newTextFlow.setVisible( true );
			ScrollPane scrollPane = new ScrollPane( newTextFlow );
			scrollPane.setId( Globals.FXVariables.SCROLLPANEID + id );
			uiController.getConsoleStackPane().getChildren().add( scrollPane );
		}
	}


	public void setUpServerInfo( ServerController serverController, UIController uiController, Font fontWebFolder, Font fontNameUrl ) {
		// set server info hidden initially
		AnchorPane.setTopAnchor( uiController.getTabPaneMaster(), 0.0 );

		uiController.getArrowImage().setRotate( 180.0 );
		uiController.getServerInfoStackPaneMaster().setVisible( false );

		if ( serverController.getSelectedServerInstance() == null ) {
			uiController.getServerInfoImagePane().setVisible( false );
		}

		for ( ServerConfigMap scm : ServerWrapper.getInstance().getListOfServerConfigMap() ) {
			String id = ServerWrapper.getInstance().getIdOfServer( scm );

			// add info to server info pane
			Text text1 = new Text( '\n' + scm.getWebFolder() );
			text1.setFill( Color.WHITE );
			text1.setFont( fontWebFolder );
			text1.setId( Globals.FXVariables.INFOWEBFOLDERID + id );
			Text text2 = new Text( scm.getName() + " - " );
			text2.setFill( Color.WHITE );
			text2.setFont( fontNameUrl );
			text2.setId( Globals.FXVariables.INFONAMEID + id );
			Text text3 = new Text( scm.getIP() + ":" + scm.getPort() );
			text3.setFill( Color.WHITE );
			text3.setFont( fontNameUrl );
			text3.setId( Globals.FXVariables.INFOURLID + id );
			TextFlow tf = new TextFlow( text2, text3, text1 );
			tf.getStyleClass().add( Globals.StyleClasses.SERVERINFO );
			tf.setId( Globals.FXVariables.INFOTEXTFLOWID + id );
			tf.setPadding( new Insets( 5.0 ) );
			Pane newPane = new Pane();
			newPane.getStyleClass().add( Globals.StyleClasses.SERVERINFO );
			newPane.getChildren().add( tf );
			newPane.setVisible( true );
			newPane.setId( Globals.FXVariables.SERVERINFOID + id );

			uiController.getServerInfoStackPane().getChildren().add( newPane );
		}

	}


	public void setUpConsoleInfo( ServerController serverController, UIController uiController ) {
		// set server info hidden initially
		for ( ServerConfigMap scm : ServerWrapper.getInstance().getListOfServerConfigMap() ) {
			String id = ServerWrapper.getInstance().getIdOfServer( scm );
			// add info to console info pane
			Text t = new Text();
			t.setId( Globals.FXVariables.lastUpdatedText + id );
			TextFlow tfLastUpdated = new TextFlow( t );
			tfLastUpdated.getStyleClass().add( Globals.StyleClasses.CONSOLEINFO );
			tfLastUpdated.setLayoutX( 20 );
			tfLastUpdated.setLayoutY( 18 );
			tfLastUpdated.setVisible( false );
			tfLastUpdated.setId( Globals.FXVariables.lastUpdatedTextFlow + id );

			uiController.getConsoleInfo().getChildren().add( tfLastUpdated );

			// add info to console info pane
			Text t1 = new Text();
			t1.setId( Globals.FXVariables.memoryText + id );
			TextFlow tfMemory = new TextFlow( t1 );
			tfMemory.getStyleClass().add( Globals.StyleClasses.CONSOLEINFO );
			tfMemory.setLayoutX( 220 );
			tfMemory.setLayoutY( 18 );
			tfMemory.setVisible( false );
			tfMemory.setId( Globals.FXVariables.memoryTextFlow + id );

			uiController.getConsoleInfo().getChildren().add( tfMemory );
		}

	}


	public void setUpServerList( UIController uiController, ServerController serverController, Scene scene ) {
		// load server list
		uiController.getListViewAppList().setPadding( new Insets( 0.0 ) );

		// set action for on click server
		for ( ServerConfigMap scm : ServerWrapper.getInstance().getListOfServerConfigMap() ) {
			addHBoxToList( uiController, scm, scene, false );
		}

		for ( Map.Entry<String, AnchorPane> h : uiController.getServersForListInstance().entrySet() ) {
			AnchorPane hbox = h.getValue();
			uiController.getListViewAppList().getItems().add( (HBox) hbox.getChildren().get( 0 ) );
		}

		uiController.refreshServerList();

	}


	public void addSettingsToStackPane( Map<String, Pane> panes, ServerController serverController, UIController uiController,
			FXMLLoader settingsLoader, String currentJvm, Stage stage, Main main ) throws IOException {
		for ( ServerConfigMap scm : ServerWrapper.getInstance().getListOfServerConfigMap() ) {
			// add to settings stack pane
			String id = ServerWrapper.getInstance().getIdOfServer( scm );

			// TODO: THIS IS NOT RECOMMENDED - http://stackoverflow.com/questions/21424843/exception-has-occuredroot-value-already-specified-in-javafx-when-loading-fxml-p
			settingsLoader.setRoot( null ); // set to null to reinitialise
			Pane tempSettings = settingsLoader.load();
			tempSettings.setId( Globals.FXVariables.SETTINGSID + id );

			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblName" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblIp" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblPort" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblFolder" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblUri" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblArgs" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblRuntime" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblMemory" ) ), HPos.RIGHT );

			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.nameTextBox ) ).setText( scm.getName() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.ipTextBox ) ).setText( scm.getIP() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.portTextBox ) ).setText( scm.getPort() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).setText( scm.getWebFolder() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.uriTextBox ) ).setText( scm.getDefaultWebUri() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).setText( scm.getCustomJVM() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmArgsTextBox ) ).setText( scm.getDefaultJVMArgs() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryTextBox ) ).setText( scm.getMemoryJVM() );
			( (Label) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmLabel ) ).setText( currentJvm );
			( (Label) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmLabel ) ).setMinSize( Label.USE_PREF_SIZE, Label.USE_PREF_SIZE );


			if ( scm.getCurrentJVM() != null ) {
				( (RadioButton) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.defaultJvmRadioBtn ) ).setSelected( true );
			} else {
				( (RadioButton) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmRadioBtn ) ).setSelected( true );
			}

			tempSettings.getStyleClass().add( "settingsPane" );

			Button tempBtnGetFolder = (Button) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.BTNGETFOLDERID );
			tempBtnGetFolder.setId( Globals.FXVariables.BTNGETFOLDERID + id );
			tempBtnGetFolder.getStyleClass().add( "browseBtn" );

			Button tempBtnBrowse = (Button) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.BTNBROWSEID );
			tempBtnBrowse.setId( Globals.FXVariables.BTNBROWSEID + id );
			tempBtnBrowse.getStyleClass().add( "browseBtn" );
			tempBtnBrowse.setMinSize( Button.USE_PREF_SIZE, Button.USE_PREF_SIZE );


			// set buttons to open directory chooser
			tempBtnGetFolder.setOnAction( event -> {
				final DirectoryChooser directoryChooser = new DirectoryChooser();
				if ( ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).getText() != null ) {
					if ( !( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).getText().isEmpty() ) {
						directoryChooser.setInitialDirectory( new File( ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).getText() ) );
					}
				}
				try {
					final File selectedDirectory = directoryChooser.showDialog( stage );
					if ( selectedDirectory != null ) {
						selectedDirectory.getAbsolutePath();
						( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).setText( selectedDirectory.getAbsolutePath() );
					}
				}
					catch ( IllegalArgumentException e ) {
						Alert alert = main.createNewAlert( AlertType.ERROR, "Error", "The web folder field must either be empty or contain a valid path e.g. C:/Program Files", null );
						alert.showAndWait();
					}
				} );

			tempBtnBrowse.setOnAction( event -> {
				final DirectoryChooser directoryChooser = new DirectoryChooser();
				if ( ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).getText() != null ) {
					if ( !( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).getText().isEmpty() ) {
						directoryChooser.setInitialDirectory( new File( ( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).getText() ) );
					}
				}
				final File selectedDirectory = directoryChooser.showDialog( stage );
				if ( selectedDirectory != null ) {
					selectedDirectory.getAbsolutePath();
					( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).setText( selectedDirectory.getAbsolutePath() );
				}
			} );

			tempSettings.setVisible( true );
			panes.put( id, tempSettings );


		}
		uiController.getSettingsStackPane().getChildren().addAll( panes.values() );
	}


	public void setUpSpalashScreen( Main main, ServerController serverController, UIController uiController, String title ) {
		// set up both screens:

		Hyperlink hpl = new Hyperlink( "Jetty on Github" );
		hpl.setOnAction( event -> {
			ServerActions serverActions = new ServerActions();
			serverActions.goToWebpage( java.net.URI.create( "https://github.com/aw20/jettydesktop" ), main );
		} );
		TextFlow textFlow = null;

		if ( !ServerWrapper.getInstance().getListOfServerConfigMap().isEmpty() ) {
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
}
