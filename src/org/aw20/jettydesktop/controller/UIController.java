package org.aw20.jettydesktop.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.util.Globals;
import org.aw20.util.StringUtil;

import javafx.animation.FillTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;


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
	private Executor executor = null;
	private ServerActions serverActions = new ServerActions();
	private ServerSetup serverSetup = new ServerSetup();

	private static FXMLLoader loader;
	private static Stage stage;
	private static Tab selectedTabInstance = null;

	private ServerController serverController = new ServerController();

	private Map<Integer, AnchorPane> serversForListInstance = new HashMap<Integer, AnchorPane>();


	public UIController( FXMLLoader _loader, Stage _stage ) {
		loader = _loader;
		stage = _stage;
	}


	public Tab getSelectedTabInstance() {
		return selectedTabInstance;
	}


	public void setSelectedTabInstance( Tab _selectedTab ) {
		selectedTabInstance = _selectedTab;
	}


	public Map<Integer, AnchorPane> getServersForListInstance() {
		return serversForListInstance;
	}


	public void setServersForListInstance( Map<Integer, AnchorPane> _serversForListInstance ) {
		serversForListInstance = _serversForListInstance;
	}


	public void initialise() {
		ServerSetup.initialise( this, serverController );
	}


	public void updateConsole( int id, String line, StackPane sp ) {
		Platform.runLater( () -> {
			// target correct console pane
			TextFlow consoleTextFlow = (TextFlow) sp.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.CONSOLEID + id );
			Text t = new Text( line );

			t.setFont( Font.font( "Lucida Sans Typewriter" ) );
			// insert text
			consoleTextFlow.getChildren().add( t );
			consoleTextFlow.setVisible( true );
			consoleTextFlow.toFront();
			// scroll pane to the bottom
			( (ScrollPane) sp.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + id ) ).setVvalue( 1.0 );
		} );
	}


	/*
	 * updates icon in server list
	 */
	public void updateRunningIcon( boolean running, int serverId ) {
		final int selectedServer = serverId;

		Circle c = (Circle) listViewAppList.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.RUNNINGID + selectedServer );

		Platform.runLater( () -> {
			if ( running == true ) {
				c.setFill( Color.LIGHTSEAGREEN );
			} else {
				c.setFill( Color.GREY );
			}
		} );
	}


	public void updateLastUpdated( String lastUpdated, int currentServer, Scene scene ) {
		Platform.runLater( () -> {
			Text textContent = (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.lastUpdatedText + currentServer );
			textContent.setText( lastUpdated );
		} );
	}


	public void updateMemory( String memory, int serverId, Scene scene ) {
		Platform.runLater( () -> {
			Text textContent = (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryText + serverId );
			textContent.setText( memory );
		} );
	}


	public void clearConsole( int serverId ) {
		StackPane sp = consoleStackPane;

		Platform.runLater( () -> {
			TextFlow textFlowContent = (TextFlow) sp.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.CONSOLEID + serverId );
			textFlowContent.getChildren().clear();
		} );
	}


	// show correct text flows with last updated and memory info
	private void showCurrentConsoleInfo() {
		// show correct textflows with last updated and memory
		Iterator<Node> itConsoleInfo = getConsoleInfo().getChildren().iterator();
		while ( itConsoleInfo.hasNext() ) {
			TextFlow consoleInfoTextFlow = (TextFlow) itConsoleInfo.next();
			Platform.runLater( () -> {
				TextFlow textFlow = consoleInfoTextFlow;
				textFlow.setVisible( false );
				if ( textFlow.getId().contains( Integer.toString( ServerController.selectedServer ) ) ) {
					textFlow.setVisible( true );
				}
			} );
		}
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
				if ( settingsPane.getId().equals( Globals.FXVariables.SETTINGSID + ServerController.selectedServer ) ) {
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
				if ( consoleScrollPane.getId().equals( Globals.FXVariables.SCROLLPANEID + ServerController.selectedServer ) ) {
					Platform.runLater( () -> {
						ScrollPane tempScrollPane = consoleScrollPane;
						tempScrollPane.setVisible( true );
						tempScrollPane.toFront();
					} );

				}
			}
		}
	}


	public void startBtnClick( int id, Scene scene ) {
		int selectedServer = id;
		if ( selectedServer == 0 ) {
			selectedServer = selectedServer;
		}
		// target the correct play polygon
		Polygon p = ( (Polygon) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.POLYGONID + selectedServer ) );
		// transform it to a square
		p.getPoints().setAll(
				0d, 0d, // (x, y)
				0d, 12d,
				12d, 12d,
				12d, 0d );
		// colour transition from green to red
		FillTransition ft = new FillTransition( Duration.millis( 4000 ), p, Color.GREEN, Color.RED );
		ft.play();

		final int ss = selectedServer;
		// on separate thread due to UI not updating until Executor process finished.
		Thread t1 = new Thread( new Runnable() {

			public void run() {
				serverActions.startServer( executor, UIController.this, serverController, ss, scene );
			}
		} );
		t1.start();

		ServerManager.servers.get( selectedServer ).setRunning( true );

		// open the console tab and correct console pane
		Tab tab = getTabPane().getTabs().get( 1 );
		getTabPane().getSelectionModel().select( tab );
		setSelectedTabInstance( tab );

		( (ScrollPane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + selectedServer ) ).setVisible( true );
		ButtonActions buttonActions = new ButtonActions();
		buttonActions.showConsoleButtonsOnRunning( this );
		// update server info
		Pane serverInfoPane = (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SERVERINFOID + selectedServer );
		serverInfoPane.setVisible( true );
		serverInfoPane.toFront();
	}


	public void stopBtnClick( int id, Scene scene ) {
		int selectedServer = ServerController.selectedServer;
		if ( selectedServer == 0 ) {
			selectedServer = id;
		}

		if ( getSelectedTabInstance() == null ) {
			Tab tab = getTabPane().getTabs().get( 1 );
			setSelectedTabInstance( tab );
		}
		// target stop polygon
		Polygon p = ( (Polygon) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.POLYGONID + selectedServer ) );
		// transform into a play polygon
		p.getPoints().setAll(
				0d, 0d, // (x, y)
				12d, 6d,
				0d, 12d );
		// colour transition red to green
		FillTransition ft = new FillTransition( Duration.millis( 2000 ), p, Color.RED, Color.GREEN );
		ft.play();

		serverActions.stopServer( this, serverController, executor, selectedServer );

		ServerManager.servers.get( selectedServer ).setRunning( false );

		ButtonActions buttonActions = new ButtonActions();
		// enable and disable correct buttons depending on which tab is selected
		if ( getSelectedTabInstance().getId().contains( "console" ) ) {
			buttonActions.showConsoleButtonsOnNotRunning( this );
		} else {
			buttonActions.showSettingsButtonsOnNotRunning( this );
		}
	}


	public void handleListViewOnClick( HBox hbox, Scene scene, Hyperlink h, ServerConfigMap scm ) {
		ButtonActions buttonActions = new ButtonActions();
		scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSEMPTYID ).setVisible( false );
		getTabPane().setVisible( true );
		serverController.setSelectedServer( Integer.parseInt( h.getId() ) );

		getServerInfoPane().setVisible( true );
		getServerInfoImagePane().setVisible( true );
		showCurrentServerInfoPane();
		showCurrentConsoleInfo();
		showCurrentTab();

		if ( ServerManager.servers.get( ServerController.selectedServer ).isRunning() ) {
			buttonActions.showConsoleButtonsOnRunning( this );
		} else {
			buttonActions.showConsoleButtonsOnNotRunning( this );
		}

		addCurrentClassToServer( hbox );

		Platform.runLater( () -> {
			// console is selected
			String consoleId = ( Globals.FXVariables.CONSOLEID + ServerController.selectedServer );
			TextFlow textFlowContent = (TextFlow) scene.lookup( Globals.FXVariables.idSelector + consoleId );
			String settingsId = ( Globals.FXVariables.SETTINGSID + ServerController.selectedServer );
			Pane settings = (Pane) scene.lookup( Globals.FXVariables.idSelector + settingsId );

			if ( getSelectedTabInstance() != null ) {
				if ( getSelectedTabInstance().getId().contains( "console" ) ) {
					textFlowContent.setVisible( true );
					textFlowContent.toFront();
					if ( ServerManager.servers.get( ServerController.selectedServer ).isRunning() ) {
						buttonActions.showConsoleButtonsOnRunning( this );
					} else {
						buttonActions.showConsoleButtonsOnNotRunning( this );
					}
				} // settings is selected
				else {
					settings.setVisible( true );
					settings.toFront();
					if ( ServerManager.servers.get( ServerController.selectedServer ).isRunning() ) {
						buttonActions.showSettingsButtonsOnRunning( this );
					} else {
						buttonActions.showSettingsButtonsOnNotRunning( this );
					}
				}
			} else {
				// show correct console
				settings.setVisible( false );
				settings.toBack();
				textFlowContent.setVisible( true );
				textFlowContent.toFront();
			}
		} );

		// get list cell of hbox
		// apply css to it on selection
		if ( hbox.getStyleClass().contains( Globals.StyleClasses.CURRENT ) ) {
			hbox.getParent().setStyle( Globals.StyleVariables.backgroundColourDarkerGrey );
		} else {
			hbox.getParent().setStyle( Globals.StyleVariables.backgroundColourLighterGrey );
		}
	}


	public void updateSettings( int savedServerId, boolean newServer, Scene scene, Pane settings, String tempName, String tempIp, String tempPort, String tempWebFolder, String tempUri, String tempCustomJvm, boolean isCustomJvm, String tempJvmArgs, String tempMemory ) {

		if ( !newServer ) {
			// update settings
			( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.nameTextBox ) ).setText( tempName );
			( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.ipTextBox ) ).setText( tempIp );
			( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.portTextBox ) ).setText( tempPort );
			( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).setText( tempWebFolder );
			( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.uriTextBox ) ).setText( tempUri );
			( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).setText( tempCustomJvm );
			( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmArgsTextBox ) ).setText( tempJvmArgs );
			( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryTextBox ) ).setText( tempMemory );
			( (RadioButton) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmRadioBtn ) ).selectedProperty().set( isCustomJvm );

			String id = settings.getId().replace( Globals.FXVariables.SETTINGSID, "" );

			// update hyperlink in listViewAppList with name
			( (Hyperlink) scene.lookup( Globals.FXVariables.idSelector + id ) ).setText( tempName );

			// update info in server info pane
			( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFOWEBFOLDERID + id ) ).setText( '\n' + tempWebFolder );
			( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFONAMEID + id ) ).setText( tempName + " - " );
			( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFOURLID + id ) ).setText( tempIp + ":" + tempPort );
		} else {
			// add ids to list
			getServerConfigIdList().add( savedServerId );

			// get new server list item
			HBox hbox = serverSetup.addHBoxToList( this, savedServerId, scene, true );

			// add console for server
			TextFlow newTextFlow = new TextFlow();
			newTextFlow.setId( Globals.FXVariables.CONSOLEID + savedServerId );
			newTextFlow.setVisible( true );
			ScrollPane scrollPane = new ScrollPane( newTextFlow );
			scrollPane.setId( Globals.FXVariables.SCROLLPANEID + savedServerId );
			getConsoleStackPane().getChildren().add( scrollPane );
			scrollPane.setVisible( true );

			getListViewAppList().getItems().add( hbox );
		}


		refreshServerList();

	}


	public void updateServerOnDelete() {
		// hide hyperlink
		HBox poly = (HBox) listViewAppList.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.HBOXID + ServerController.selectedServer );
		listViewAppList.getItems().remove( poly );

		// hide console
		ScrollPane sp = (ScrollPane) consoleStackPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SCROLLPANEID + ServerController.selectedServer );
		consoleStackPane.getChildren().remove( sp );

		// hide settings
		Pane p = (Pane) settingsStackPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + ServerController.selectedServer );
		settingsStackPane.getChildren().remove( p );

		// hide server info
		TextFlow tf = (TextFlow) serverInfoStackPane.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFOTEXTFLOWID + ServerController.selectedServer );
		serverInfoStackPane.getChildren().remove( tf );

		// hide console info
		Pane tf2 = (Pane) consoleInfo.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryTextFlow + ServerController.selectedServer );
		consoleInfo.getChildren().remove( tf2 );

		Pane tf3 = (Pane) consoleInfo.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.lastUpdatedTextFlow + ServerController.selectedServer );
		consoleInfo.getChildren().remove( tf3 );

		serversForListInstance.remove( ServerController.selectedServer );

		serverController.setSelectedServer( 0 );

		refreshServerList();
	}


	/*
	 * get all server names and order alphabetically
	 */
	public void refreshServerList() {
		// get all names
		List<String> names = new ArrayList<String>();
		ObservableList<HBox> hboxs = (ObservableList<HBox>) listViewAppList.getItems();
		ObservableList<HBox> newHboxs = FXCollections.observableArrayList( hboxs );
		for ( HBox node : hboxs ) {
			HBox hbox = node;
			ObservableList<Node> hboxChild = (ObservableList<Node>) hbox.getChildren();
			for ( Node nodeChild : hboxChild ) {
				if ( nodeChild instanceof javafx.scene.control.Hyperlink ) {
					names.add( ( (Hyperlink) nodeChild ).getText() );
				}
			}
		}

		// sort servers
		Collections.sort( names, StringUtil.ALPHABETICAL_ORDER );

		listViewAppList.getItems().clear();

		for ( String name : names ) {
			for ( Node node : newHboxs ) {
				HBox hbox = (HBox) node;
				ObservableList<Node> hboxChild = (ObservableList<Node>) hbox.getChildren();
				for ( Node nodeChild : hboxChild ) {
					if ( nodeChild instanceof javafx.scene.control.Hyperlink ) {
						if ( ( (Hyperlink) nodeChild ).getText().equals( name ) ) {
							Platform.runLater( () -> {
								hbox.setPrefWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
								hbox.setMaxWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
								hbox.setMinWidth( Globals.StyleVariables.polygonPaneWidth + Globals.StyleVariables.hyperlinkWidth - 18 );
								VBox.setMargin( hbox, new Insets( 0, 0, 0, 0 ) );

								getListViewAppList().getItems().add( hbox );
							} );
						}
					}
				}
			}
		}
	}


	public void showCurrentServerInfoPane() {

		// show correct server info
		Iterator<Node> itInfo = getServerInfoStackPane().getChildren().iterator();
		while ( itInfo.hasNext() ) {
			getSplashPane().setVisible( false );
			getSplashPane().toBack();
			getSplashAnchorPane().setVisible( false );
			getSplashAnchorPane().toBack();
			Pane serverInfoPane = (Pane) itInfo.next();
			Platform.runLater( () -> {
				serverInfoPane.setVisible( false );
				// show server info
				if ( serverInfoPane.getId().equals( Globals.FXVariables.SERVERINFOID + ServerController.selectedServer ) ) {
					serverInfoPane.setVisible( true );
					serverInfoPane.toFront();
				}
			} );
		}
	}


	private void addCurrentClassToServer( HBox hbox ) {

		// remove "current" from others
		List<HBox> listOfHboxes = listViewAppList.getItems();
		for ( HBox item : listOfHboxes ) {
			item.getStyleClass().remove( Globals.StyleClasses.CURRENT );
			item.getParent().setStyle( Globals.StyleVariables.backgroundColourLighterGrey );
		}

		// set app to "current"
		hbox.getStyleClass().add( Globals.StyleClasses.CURRENT );
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
