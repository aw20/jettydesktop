package org.aw20.jettydesktop.controller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.jettydesktop.ui.ServerWrapper;
import org.aw20.util.Globals;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


/*
 * Class to control UI operations, such as updating console, display settings/console/splash screen, update UI on server selection
 * Contains all FXML variables
 */
public class UIController {

	@FXML
	private ListView<HBox> listViewAppList;

	@FXML
	private ScrollPane vboxAppListScrollPane;

	@FXML
	private Pane serverInfoImagePane;

	@FXML
	private AnchorPane serverInfoPane;

	@FXML
	private AnchorPane leftSplit;

	@FXML
	private Pane serverInfoStackPaneMaster;

	@FXML
	private GridPane splashPane;

	@FXML
	private AnchorPane splashAnchorPane;

	@FXML
	private AnchorPane mainPane;

	@FXML
	private StackPane serverInfoStackPane;

	@FXML
	private StackPane consoleStackPane;

	@FXML
	private StackPane settingsStackPane;

	@FXML
	private Button btnAddWebApp;

	@FXML
	private TabPane tabPane;

	@FXML
	private StackPane tabPaneMaster;

	@FXML
	private ImageView arrowImage;

	@FXML
	private Tab settingsTab;

	@FXML
	private Tab consoleTab;

	@FXML
	private Button startBtn;

	@FXML
	private Button stopBtn;

	@FXML
	private Button clearBtn;

	@FXML
	private Button saveBtn;

	@FXML
	private Button deleteBtn;

	@FXML
	private Button openBtn;

	@FXML
	private Button btnGetFolder;

	@FXML
	private Button btnBrowse;

	@FXML
	private Pane consoleInfo;

	@FXML
	private SplitPane splitPane;

	private List<Integer> serverConfigIdList = new ArrayList<Integer>();
	private ServerSetup serverSetup = new ServerSetup();

	private Tab selectedTabInstance = null;
	private ServerController serverController = new ServerController();
	private Map<Integer, AnchorPane> serversForAppList = new HashMap<Integer, AnchorPane>();
	private ButtonController buttonController = new ButtonController();
	private ServerInfoController serverInfoController = new ServerInfoController();
	private SettingsController settingsController = new SettingsController();


	public Tab getSelectedTabInstance() {
		return selectedTabInstance;
	}


	public void setSelectedTabInstance( Tab _selectedTab ) {
		selectedTabInstance = _selectedTab;
	}


	public Map<Integer, AnchorPane> getServersForAppList() {
		return serversForAppList;
	}


	public void setServersForAppList( Map<Integer, AnchorPane> _serversForAppList ) {
		serversForAppList = _serversForAppList;
	}


	// settings tab should stay open if it's open when a user click on a new server, as should the console tab
	private void showCurrentTab() {
		if ( getSelectedTabInstance() != null && getSelectedTabInstance().getId().contains( "settings" ) ) {
			Iterator<Node> itSettings = getSettingsStackPane().getChildren().iterator();
			while ( itSettings.hasNext() ) {
				getSplashPane().setVisible( false );
				getSplashPane().toBack();
				getSplashAnchorPane().setVisible( false );
				getSplashAnchorPane().toBack();
				// bring correct settings pane to the front
				Pane settingsPane = (Pane) itSettings.next();
				if ( settingsPane.getId().equals( Globals.FXVariables.SETTINGSID + serverController.getSelectedServer() ) ) {
					Platform.runLater( () -> {
						Pane tempSettingsPane = settingsPane;
						tempSettingsPane.setVisible( true );
						tempSettingsPane.toFront();
					} );
				}
			}
		} else if ( getSelectedTabInstance() != null && getSelectedTabInstance().getId().contains( "console" ) ) {
			getConsoleStackPane().setVisible( true );

			Iterator<Node> itConsole = getConsoleStackPane().getChildren().iterator();
			while ( itConsole.hasNext() ) {
				getSplashPane().setVisible( false );
				getSplashPane().toBack();
				getSplashAnchorPane().setVisible( false );
				getSplashAnchorPane().toBack();
				// bring correct console pane to the front
				ScrollPane consoleScrollPane = (ScrollPane) itConsole.next();
				if ( consoleScrollPane.getId().equals( Globals.FXVariables.SCROLLPANEID + serverController.getSelectedServer() ) ) {
					Platform.runLater( () -> {
						ScrollPane tempScrollPane = consoleScrollPane;
						tempScrollPane.setVisible( true );
						tempScrollPane.toFront();
					} );

				}
			}
		}
	}


	public void handleListViewOnClick( HBox hbox, Scene scene, Hyperlink h, int selectedServerId ) {
		serverController.setSelectedServer( selectedServerId );
		scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID ).setVisible( false );
		getTabPane().setVisible( true );
		serverController.setSelectedServer( Integer.parseInt( h.getId() ) );

		getServerInfoPane().setVisible( true );
		getServerInfoImagePane().setVisible( true );

		serverInfoController.showCurrentServerInfoPane( this, selectedServerId );
		ConsoleController consoleController = new ConsoleController();
		consoleController.showCurrentConsoleInfo( this, serverController );
		showCurrentTab();

		if ( ServerManager.getServers().get( serverController.getSelectedServer() ).isRunning() ) {
			buttonController.showConsoleButtonsOnRunning( this );
		} else {
			buttonController.showConsoleButtonsOnNotRunning( this );
		}

		addCurrentClassToServer( hbox );

		// console is selected
		String consoleId = ( Globals.FXVariables.CONSOLEID + serverController.getSelectedServer() );
		TextFlow textFlowContent = (TextFlow) scene.lookup( Globals.FXVariables.idSelector + consoleId );
		String settingsId = ( Globals.FXVariables.SETTINGSID + serverController.getSelectedServer() );
		Pane settings = (Pane) scene.lookup( Globals.FXVariables.idSelector + settingsId );

		if ( getSelectedTabInstance() != null ) {
			if ( getSelectedTabInstance().getId().contains( "console" ) ) {
				textFlowContent.setVisible( true );
				textFlowContent.toFront();
				if ( ServerManager.getServers().get( serverController.getSelectedServer() ).isRunning() ) {
					buttonController.showConsoleButtonsOnRunning( this );
				} else {
					buttonController.showConsoleButtonsOnNotRunning( this );
				}
			} // settings is selected
			else {
				settings.setVisible( true );
				settings.toFront();
				if ( ServerManager.getServers().get( serverController.getSelectedServer() ).isRunning() ) {
					buttonController.showSettingsButtonsOnRunning( this );
				} else {
					buttonController.showSettingsButtonsOnNotRunning( this );
				}
			}
		} else {
			// show correct console
			settings.setVisible( false );
			settings.toBack();
			textFlowContent.setVisible( true );
			textFlowContent.toFront();
		}
		// get list cell of hbox
		// apply css to it on selection
		if ( hbox.getStyleClass().contains( Globals.StyleClasses.CURRENT ) ) {
			hbox.getParent().setStyle( Globals.StyleVariables.backgroundColourDarkerGrey );
		} else {
			hbox.getParent().setStyle( Globals.StyleVariables.backgroundColourLighterGrey );
		}
	}


	public void tabPaneSelectionChange( Tab newTab, Scene scene ) {
		setSelectedTabInstance( newTab );
		int selectedServer = serverController.getSelectedServer();
		if ( newTab.getId().equals( "settingsTab" ) ) {
			// open settings tab and correct pane
			Pane pSettings = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + selectedServer );
			pSettings.setVisible( true );
			pSettings.toFront();

			// disable buttons if server running
			if ( ServerManager.getServers().get( selectedServer ).isRunning() ) {
				buttonController.showSettingsButtonsOnRunning( this );
			} else {
				buttonController.showSettingsButtonsOnNotRunning( this );
			}

		} else if ( newTab.getId().equals( "consoleTab" ) ) {
			// open console tab and correct pane
			getConsoleStackPane().setVisible( true );

			Iterator<Node> itConsole = getConsoleStackPane().getChildren().iterator();
			while ( itConsole.hasNext() ) {
				getSplashPane().setVisible( false );
				getSplashAnchorPane().setVisible( false );
				getSplashAnchorPane().toBack();
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
				buttonController.showConsoleButtonsOnRunning( this );
			} else {
				buttonController.showConsoleButtonsOnNotRunning( this );
			}

		}
	}


	private void updateServerOnDelete() {
		// hide hyperlink
		HBox poly = (HBox) listViewAppList.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.HBOXID + serverController.getSelectedServer() );
		listViewAppList.getItems().remove( poly );

		// hide console
		ScrollPane sp = (ScrollPane) consoleStackPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + serverController.getSelectedServer() );
		consoleStackPane.getChildren().remove( sp );

		// hide settings
		Pane p = (Pane) settingsStackPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + serverController.getSelectedServer() );
		settingsStackPane.getChildren().remove( p );

		// hide server info
		TextFlow tf = (TextFlow) serverInfoStackPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFOTEXTFLOWID + serverController.getSelectedServer() );
		serverInfoStackPane.getChildren().remove( tf );

		// hide console info
		Pane tf2 = (Pane) consoleInfo.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryTextFlow + serverController.getSelectedServer() );
		consoleInfo.getChildren().remove( tf2 );

		Pane tf3 = (Pane) consoleInfo.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.lastUpdatedTextFlow + serverController.getSelectedServer() );
		consoleInfo.getChildren().remove( tf3 );

		serversForAppList.remove( serverController.getSelectedServer() );

		serverController.setSelectedServer( 0 );

		serverInfoController.refreshServerList( this );
	}


	public void addCurrentClassToServer( HBox hbox ) {

		// remove "current" from others
		List<HBox> listOfHboxes = listViewAppList.getItems();
		for ( HBox item : listOfHboxes ) {
			item.getStyleClass().remove( Globals.StyleClasses.CURRENT );
			item.getParent().setStyle( Globals.StyleVariables.backgroundColourLighterGrey );
		}

		// set app to "current"
		hbox.getStyleClass().add( Globals.StyleClasses.CURRENT );
	}


	public void addWebAppBtnClick( Scene scene ) {
		AnchorPane.setTopAnchor( getTabPaneMaster(), 0.0 );

		getServerInfoImagePane().setPrefHeight( 40.0 );
		getServerInfoImagePane().setMinHeight( 40.0 );
		getServerInfoImagePane().setMaxHeight( 40.0 );

		getArrowImage().setRotate( 180.0 );
		getServerInfoStackPaneMaster().setVisible( false );

		// hide splashPane and tabPane
		getSplashPane().setVisible( false );
		getSplashAnchorPane().setVisible( false );
		getSplashAnchorPane().toBack();
		getTabPane().setVisible( false );
		getServerInfoImagePane().setVisible( false );
		getServerInfoPane().setVisible( false );

		buttonController.showButtonsOnNewWebApp( this );

		// remove "current" class from others
		List<HBox> listOfHboxes = getListViewAppList().getItems();
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


	public void saveBtnClick( Scene scene, Main main, Executor executor, Font fontWebFolder, Font fontNameUrl, String currentJvm, Stage stage, FXMLLoader settingsLoader, UIController uiController ) throws IOException {
		boolean newServer = false;
		Pane tempSettings;
		// not new server
		if ( serverController.getSelectedServer() != 0 ) {
			tempSettings = ( (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + serverController.getSelectedServer() ) );
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
			getServerInfoStackPane().getChildren().add( newPane );

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

			getConsoleInfo().getChildren().add( tfLastUpdated );

			// add last updated and memory used info to console info pane
			Text t1 = new Text();
			t1.setId( Globals.FXVariables.memoryText + savedServerId );
			TextFlow tfMemory = new TextFlow( t1 );
			tfMemory.getStyleClass().add( Globals.StyleClasses.CONSOLEINFO );
			tfMemory.setLayoutX( 220 );
			tfMemory.setLayoutY( 18 );
			tfMemory.setVisible( false );
			tfMemory.setId( Globals.FXVariables.memoryTextFlow + savedServerId );

			getConsoleInfo().getChildren().add( tfMemory );
		}

		// update settings on user updated fields

		settingsController.updateSettings( this, main, serverController, executor, savedServerId, newServer, scene, tempSettings, tempName, tempIp, tempPort, tempWebFolder, tempUri, tempCustomJvm, isCustomJvm, tempJvmArgs, tempMemory );
		// close settings and open console
		tempSettings.setVisible( false );
		ScrollPane s = ( (ScrollPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + savedServerId ) );
		s.setVisible( true );
		s.toFront();

		buttonController.showSettingsButtonsOnNotRunning( this );

		getTabPane().setVisible( true );

		// set console tab
		Tab tab = getTabPane().getTabs().get( 1 );
		getTabPane().getSelectionModel().select( tab );

		// show correct console buttons
		if ( ServerManager.getServers().get( serverController.getSelectedServer() ).isRunning() ) {
			buttonController.showConsoleButtonsOnRunning( this );
		} else {
			buttonController.showConsoleButtonsOnNotRunning( this );
		}
		// re display server info pane
		getServerInfoImagePane().setVisible( true );
		getServerInfoPane().setVisible( true );

		serverInfoController.showCurrentServerInfoPane( this, savedServerId );

		if ( newServer ) {
			// change settingsEmpty id to new id
			Pane newSettings = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID );
			newSettings.setId( Globals.FXVariables.SETTINGSID + savedServerId );
			getSettingsStackPane().getChildren().add( newSettings );
			tempSettings.setVisible( false );
			// add new empty settings
			serverSetup.setUpEmptySettings( settingsLoader, currentJvm, stage, uiController );
		}

	}


	public void deleteServer( Main main, ActionEvent e ) {
		List<HBox> listOfHboxes = getListViewAppList().getItems();
		String serverToBeDeleted = ServerManager.getServers().get( serverController.getSelectedServer() ).getServerConfigMap().getName();

		Optional<ButtonType> result = main.createNewAlert( AlertType.CONFIRMATION, "", "Delete " + serverToBeDeleted + "?", "Are you sure?" ).showAndWait();

		// OK button clicked
		if ( result.get() == ButtonType.OK ) {
			e.consume();


			// show splash screen
			getSplashPane().setVisible( true );
			getSplashPane().toFront();
			getSplashAnchorPane().setVisible( true );
			getSplashAnchorPane().toFront();

			getServerInfoImagePane().setVisible( false );
			getServerInfoPane().setVisible( false );

			// remove "current" from others

			for ( HBox item : listOfHboxes ) {
				item.getStyleClass().remove( Globals.StyleClasses.CURRENT );
				item.getParent().setStyle( Globals.StyleVariables.backgroundColourLighterGrey );
			}

			// hide buttons
			buttonController.showNoButtons( this );

			// delete server console/settings panes
			serverController.setDeleted();
			updateServerOnDelete();

			// Cancel button clicked
		} else {
			e.consume();
		}
	}


	public boolean validateSettings( Scene scene, Main main ) {
		return settingsController.validateSettings( scene, main, serverController );
	}


	public void serverInfoArrowImageClick( MouseEvent e, Scene scene ) {
		serverInfoController.serverInfoArrowImageClick( e, scene, this, serverController.getSelectedServer() );
	}


	public void handleStartBtnClick( Scene scene, Executor executor ) {
		buttonController.startBtnClick( 0, scene, serverController, new ServerActions(), executor, this );
	}


	public void handleStopBtnClick( Scene scene, Executor executor ) {
		buttonController.stopBtnClick( 0, scene, serverController, new ServerActions(), executor, this );
	}


	/*
	 * button to open current web app - enabled when webapp is running
	 */
	public void openBtnClick( Main main ) {
		int selectedServer = serverController.getSelectedServer();
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
			serverActions.goToWebpage( uri, main );
		}
	}


	public StackPane getServerInfoStackPane() {
		return serverInfoStackPane;
	}


	public Pane getServerInfoImagePane() {
		return serverInfoImagePane;
	}


	public ListView<HBox> getListViewAppList() {
		return listViewAppList;
	}


	public TabPane getTabPane() {
		return tabPane;
	}


	public ImageView getArrowImage() {
		return arrowImage;
	}


	public Pane getServerInfoStackPaneMaster() {
		return serverInfoStackPaneMaster;
	}


	public Button getBtnAddWebApp() {
		return btnAddWebApp;
	}


	public Button getStartBtn() {
		return startBtn;
	}


	public Button getStopBtn() {
		return stopBtn;
	}


	public Button getClearBtn() {
		return clearBtn;
	}


	public Button getSaveBtn() {
		return saveBtn;
	}


	public Button getDeleteBtn() {
		return deleteBtn;
	}


	public StackPane getConsoleStackPane() {
		return consoleStackPane;
	}


	public StackPane getSettingsStackPane() {
		return settingsStackPane;
	}


	public Tab getSettingsTab() {
		return settingsTab;
	}


	public StackPane getTabPaneMaster() {
		return tabPaneMaster;
	}


	public AnchorPane getServerInfoPane() {
		return serverInfoPane;
	}


	public GridPane getSplashPane() {
		return splashPane;
	}


	public Button getBtnGetFolder() {
		return btnGetFolder;
	}


	public Button getBtnBrowse() {
		return btnBrowse;
	}


	public Pane getConsoleInfo() {
		return consoleInfo;
	}


	public Button getOpenBtn() {
		return openBtn;
	}


	public ScrollPane getVboxAppListScrollPane() {
		return vboxAppListScrollPane;
	}


	public AnchorPane getLeftSplit() {
		return leftSplit;
	}


	public AnchorPane getSplashAnchorPane() {
		return splashAnchorPane;
	}


	public List<Integer> getServerConfigIdList() {
		return serverConfigIdList;
	}

}
