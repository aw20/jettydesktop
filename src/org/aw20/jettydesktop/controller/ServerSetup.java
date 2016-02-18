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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.jettydesktop.ui.ServerWrapper;
import org.aw20.util.Globals;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;


/*
 * Class to setup fxml elements for saved servers
 * Contains instance of uiController
 */
public class ServerSetup {

	private UIController uiController;
	private ServerManager serverManager;

	private ObservableList<HBox> listOfServers = FXCollections.observableArrayList();


	static class HBoxCell extends ListCell<HBox> {

		@Override
		public void updateItem( HBox item, boolean empty ) {
			super.updateItem( item, empty );
			HBox hbox = new HBox();
			if ( item != null ) {
				hbox = item;
				setGraphic( hbox );
			}

		}
	}


	public void initialise( UIController uiController, ServerController serverController, ServerManager serverManager, Scene scene, ServerActions serverActions, Executor executor ) {
		this.uiController = uiController;
		this.serverManager = serverManager;
		serverController.loadSettings();
		// on start up, because no web app is selected, don't show console/settings tabs
		if ( serverController.getSelectedServer() == 0 ) {
			uiController.getTabPane().setVisible( false );
		}

		uiController.getTabPane().getSelectionModel().select( 1 );

		ButtonController buttonController = new ButtonController();
		buttonController.showNoButtons( uiController );

		// set initial size of server list
		uiController.getVboxAppListScrollPane().setFitToWidth( true );
		uiController.getVboxAppListScrollPane().setFitToHeight( true );

		setUpServerList( scene, serverManager, uiController, serverController, new ServerActions(), null );


		uiController.getListViewAppList().setItems( listOfServers );

		uiController.getListViewAppList().setCellFactory( new Callback<ListView<HBox>, ListCell<HBox>>() {

			public ListCell<HBox> call( ListView<HBox> list ) {
				return new HBoxCell();
			}
		} );

		uiController.getBtnAddWebApp().setText( "+ add webapp" );

		uiController.getListViewAppList().getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
		uiController.getListViewAppList().setFixedCellSize( 40 );

		// re order server list
		ServerInfoController serverInfoController = new ServerInfoController();
		serverInfoController.refreshServerList( uiController );
	}


	public HBox addHBoxToList( int serverId, Scene scene, boolean newServer, ServerManager serverManager, UIController uiController, ServerController serverController, ServerActions serverActions, Executor executor ) {

		// running circle

		Circle c = new Circle( 5.0f, Color.GREY );
		c.setId( Globals.FXVariables.RUNNINGID + serverId );
		c.setTranslateY( 16.0 );
		c.setTranslateX( 8.0 );

		Pane pane = new Pane( c );
		pane.setMinWidth( 24 );

		// play/stop polygon
		Polygon p = new Polygon();

		p.getPoints().setAll(
				0d, 0d,
				12d, 6d,
				0d, 12d );

		p.setId( "polygon" + serverId );
		p.setFill( Color.GREEN );

		p.setVisible( false );

		// initialises at 0, 0
		p.setTranslateY( 11.0 );
		p.setTranslateX( 12.0 );

		ButtonController buttonController = new ButtonController();

		// play/stop button click
		p.setOnMouseClicked( event -> {
			if ( serverManager.getServers().get( serverId ).isRunning() ) {
				buttonController.stopBtnClick( serverId, scene, serverController, serverActions, executor, uiController, serverManager );
			} else {
				buttonController.startBtnClick( serverId, scene, serverController, serverActions, executor, uiController, serverManager );
				uiController.setSelectedTabInstance( uiController.getTabPane().getTabs().get( 1 ) );
				if ( !uiController.getServerInfoImagePane().isVisible() ) {
					uiController.getServerInfoPane().setVisible( true );
					uiController.getServerInfoImagePane().setVisible( true );
				}
				ServerInfoController serverInfoController = new ServerInfoController();
				serverInfoController.showCurrentServerInfoPane( uiController, serverId );
			}
		} );

		Pane polygonPane = new Pane( p );
		polygonPane.setPrefWidth( Globals.StyleVariables.polygonPaneWidth );
		polygonPane.setMinWidth( Globals.StyleVariables.polygonPaneWidth );
		polygonPane.setMaxWidth( Globals.StyleVariables.polygonPaneWidth );

		// label = server name
		Label labelServerName = new Label( serverManager.getServers().get( serverId ).getServerConfigMap().getName() );

		Font font = Font.font( "Arial", FontWeight.BOLD, 15 );

		labelServerName.setFont( font );
		labelServerName.setTextFill( Color.WHITESMOKE );
		labelServerName.setPadding( new Insets( 0, 0, 0, 10 ) );
		labelServerName.setId( Integer.toString( serverId ) );
		labelServerName.getStyleClass().add( "serverListHyperlink" );
		labelServerName.setPrefWidth( Globals.StyleVariables.hyperlinkWidth - 18 - 24 );
		labelServerName.setMinWidth( Globals.StyleVariables.hyperlinkWidth - 18 - 24 );
		labelServerName.setMaxWidth( Globals.StyleVariables.hyperlinkWidth - 18 - 24 );

		HBox hbox = new HBox( pane, labelServerName, polygonPane );

		hbox.getStyleClass().add( "hboxServer" );
		hbox.setPrefWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
		hbox.setMaxWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
		hbox.setMinWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
		hbox.setPrefHeight( 28 );
		hbox.setPrefHeight( 28 );
		hbox.setPrefHeight( 28 );
		hbox.setId( Globals.FXVariables.HBOXID + serverId );

		AnchorPane a = new AnchorPane();

		a.getChildren().add( hbox );

		AnchorPane.setLeftAnchor( hbox, 0.0 );
		AnchorPane.setRightAnchor( hbox, 0.0 );

		hbox.setOnMouseEntered( event -> {
			p.setVisible( true );
		} );

		hbox.setOnMouseExited( event -> {
			p.setVisible( false );
		} );

		uiController.getServersForAppList().put( serverId, a );

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
		// align labels to right of grid column
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
		// add empty settings for on click "add web app" button
		uiController.getTabPaneMaster().getChildren().add( tempPane );
	}


	public void setUpSettings( ServerController serverController ) {
		// load settings into server config map

		for ( ServerWrapper serverWrapper : serverManager.getServers().values() ) {
			// add ids to list
			int id = serverWrapper.getId();
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


	public void setUpServerInfo( ServerController serverController, Font fontWebFolder, Font fontNameUrl ) {
		// set server info hidden initially
		AnchorPane.setTopAnchor( uiController.getTabPaneMaster(), 0.0 );

		uiController.getArrowImage().setRotate( 180.0 );
		uiController.getServerInfoStackPaneMaster().setVisible( false );

		if ( serverController.getSelectedServer() == 0 ) {
			uiController.getServerInfoImagePane().setVisible( false );
		}

		for ( ServerWrapper serverWrapper : serverManager.getServers().values() ) {

			String id = String.valueOf( serverWrapper.getId() );

			// add info to server info pane
			Text text1 = new Text( '\n' + serverWrapper.getServerConfigMap().getWebFolder() );
			text1.setFill( Color.WHITE );
			text1.setFont( fontWebFolder );
			text1.setId( Globals.FXVariables.INFOWEBFOLDERID + id );
			Text text2 = new Text( serverWrapper.getServerConfigMap().getName() + " - " );
			text2.setFill( Color.WHITE );
			text2.setFont( fontNameUrl );
			text2.setId( Globals.FXVariables.INFONAMEID + id );
			Text text3 = new Text( serverWrapper.getServerConfigMap().getIP() + ":" + serverWrapper.getServerConfigMap().getPort() );
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


	public void setUpConsoleInfo() {
		// set server info hidden initially
		for ( ServerWrapper serverWrapper : serverManager.getServers().values() ) {

			String id = String.valueOf( serverWrapper.getId() );

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


	public void setUpServerList( Scene scene, ServerManager serverManager, UIController uiController, ServerController serverController, ServerActions serverActions, Executor exector ) {
		// load server list
		uiController.getListViewAppList().setPadding( new Insets( 0.0 ) );

		// set action for on click server
		for ( Entry<Integer, ServerWrapper> server : serverManager.getServers().entrySet() ) {
			listOfServers.add( addHBoxToList( server.getKey(), scene, false, serverManager, uiController, serverController, serverActions, exector ) );
			// addHBoxToList( server.getKey(), scene, false, serverManager, uiController, serverController, serverActions, exector );
		}
	}


	public void addSettingsToStackPane( FXMLLoader settingsLoader, String currentJvm, Stage stage, Main main ) throws IOException {
		Map<String, Pane> panes = new HashMap<String, Pane>();
		for ( ServerWrapper serverWrapper : serverManager.getServers().values() ) {

			String id = String.valueOf( serverWrapper.getId() );

			// TODO: THIS IS NOT RECOMMENDED - http://stackoverflow.com/questions/21424843/exception-has-occuredroot-value-already-specified-in-javafx-when-loading-fxml-p
			settingsLoader.setRoot( null ); // set to null to reinitialise
			Pane tempSettings = settingsLoader.load();
			tempSettings.setId( Globals.FXVariables.SETTINGSID + id );
			// align labels to right of grid column
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblName" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblIp" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblPort" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblFolder" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblUri" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblArgs" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblRuntime" ) ), HPos.RIGHT );
			GridPane.setHalignment( ( (Label) tempSettings.lookup( "#lblMemory" ) ), HPos.RIGHT );

			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.nameTextBox ) ).setText( serverWrapper.getServerConfigMap().getName() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.ipTextBox ) ).setText( serverWrapper.getServerConfigMap().getIP() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.portTextBox ) ).setText( serverWrapper.getServerConfigMap().getPort() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).setText( serverWrapper.getServerConfigMap().getWebFolder() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.uriTextBox ) ).setText( serverWrapper.getServerConfigMap().getDefaultWebUri() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).setText( serverWrapper.getServerConfigMap().getCustomJVM() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmArgsTextBox ) ).setText( serverWrapper.getServerConfigMap().getDefaultJVMArgs() );
			( (TextField) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryTextBox ) ).setText( serverWrapper.getServerConfigMap().getMemoryJVM() );
			( (Label) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmLabel ) ).setText( currentJvm );
			( (Label) tempSettings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmLabel ) ).setMinSize( Label.USE_PREF_SIZE, Label.USE_PREF_SIZE );


			if ( serverWrapper.getServerConfigMap().getCurrentJVM() != null ) {
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
				} catch ( IllegalArgumentException e ) {
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


	public void setUpSpalashScreen( Main main, String title ) {
		// set up both screens:

		Hyperlink hpl = new Hyperlink( "Jetty on Github" );
		hpl.setOnAction( event -> {
			ServerActions serverActions = new ServerActions();
			serverActions.goToWebpage( java.net.URI.create( "https://github.com/aw20/jettydesktop" ), main );
		} );
		TextFlow textFlow = null;
		// if there are no webapps - instruct to add one
		if ( serverManager.getServers() == null || serverManager.getServers().isEmpty() ) {
			textFlow = new TextFlow( new Text( title ), new Text( "\nView " ), hpl, new Text( " for more information" ), new Text( "\nClick an app to start" ) );
			textFlow.getStyleClass().add( "splashScreenText" );
			uiController.getSplashPane().add( textFlow, 1, 1 );
		}
		// if there are webapps - instruct to click one
		else {
			textFlow = new TextFlow( new Text( title ), new Text( "\nView " ), hpl, new Text( " for more information" ), new Text( "\nAdd an app to start" ) );
			textFlow.getStyleClass().add( "splashScreenText" );
			uiController.getSplashPane().add( textFlow, 1, 1 );
		}
	}
}
