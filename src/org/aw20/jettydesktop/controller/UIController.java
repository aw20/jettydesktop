package org.aw20.jettydesktop.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import org.aw20.jettydesktop.ui.ServerConfigMap;
import org.aw20.util.Globals;
import org.aw20.util.StringUtil;


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

	private List<String> serverConfigIdList = new ArrayList<String>();
	private ServerConfigMap serverConfigMap;
	private static Executor executor = null;
	private static FXMLLoader loader;
	private static Stage stage;

	private static Tab selectedTab = null;

	final ServerController serverController;

	public static Map<String, AnchorPane> serversForList = new HashMap<String, AnchorPane>();

	final private Double polygonPaneWidth = 40.0;
	final private Double hyperlinkWidth = 185.0;

	// create Singleton instance
	private static UIController instance = null;


	public UIController( FXMLLoader _loader, Stage _stage ) {
		serverController = ServerController.getInstance();
		loader = _loader;
		stage = _stage;
	}


	public static UIController getInstance() {
		if ( instance == null ) {
			instance = new UIController( loader, stage );
		}
		return instance;
	}


	public Tab getSelectedTabInstance() {
		return selectedTab;
	}


	public void setSelectedTabInstance( Tab _selectedTab ) {
		selectedTab = _selectedTab;
	}


	void initialise() {
		// on start up, because no web app is selected, don't show console/settings tabs
		if ( serverController.getSelectedServerInstance() == null ) {
			getTabPane().setVisible( false );
		}

		getTabPane().getSelectionModel().select( 1 );

		showNoButtons();

		getVboxAppListScrollPane().setFitToWidth( true );
		getVboxAppListScrollPane().setFitToHeight( true );

		// text="+ add webapp"
		getBtnAddWebApp().setText( "+ add webapp" );
	}


	boolean startServer( String serverId, Scene scene ) {

		serverController.setSelectedServer( serverId );

		getSplashPane().setVisible( false );
		getSplashPane().toBack();
		getSplashAnchorPane().setVisible( false );
		getSplashAnchorPane().toBack();
		( (TabPane) scene.lookup( "#" + Globals.FXVariables.TABPANEID ) ).setVisible( true );
		( (ScrollPane) scene.lookup( "#" + Globals.FXVariables.SCROLLPANEID + serverId ) ).toFront();
		( (ScrollPane) scene.lookup( "#" + Globals.FXVariables.SCROLLPANEID + serverId ) ).setVisible( true );

		scene.lookup( "#" + Globals.FXVariables.lastUpdatedTextFlow + serverId ).setVisible( true );
		scene.lookup( "#" + Globals.FXVariables.memoryTextFlow + serverId ).setVisible( true );

		updateConsole( serverId, Globals.ConsoleVariables.STARTING_SERVER, consoleStackPane );
		serverConfigMap = get( serverId );
		try {
			if ( !serverConfigMap.getId().isEmpty() && !serverConfigMap.getPort().isEmpty() ) {

				executor = new Executor( serverConfigMap, getConsoleStackPane(), scene );
				serverConfigMap.setRunning( "true" );

				for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
					if ( server.getId().equals( serverId ) ) {
						server.setRunning( "true" );
					}
				}

				updateRunningIcon( true, serverId );

				return true;
			}
			else {
				serverConfigMap.setRunning( "false" );
				updateRunningIcon( false, serverId );
				return false;
			}
		} catch ( IOException e ) {
			serverConfigMap.setRunning( "false" );
			return false;
		}
	}


	boolean stopServer( String serverId ) {
		updateConsole( serverId, Globals.ConsoleVariables.STOPPING_SERVER, consoleStackPane );
		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getId().equals( serverId ) ) {
				server.setRunning( "false" );
				serverConfigMap.setRunning( "false" );

			}
		}
		// get correct version of executor on exiting app and stopping all servers
		executor.exit();
		if ( executor.isbRun() == false ) {
			updateConsole( serverId, Globals.ConsoleVariables.SERVER_STOPPED, consoleStackPane );
			updateRunningIcon( false, serverId );
			return true;
		}
		else {
			return false;
		}
	}


	private ServerConfigMap get( String id ) {
		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getId().equals( id ) ) {
				return server;
			}
		}
		return null;
	}


	public void updateConsole( String id, String l, StackPane sp ) {
		Platform.runLater( ( ) -> {
			TextFlow consoleTextFlow = (TextFlow) sp.lookup( "#" + Globals.FXVariables.CONSOLEID + id );
			Text t = new Text( l );

			t.setFont( Font.font( "Lucida Sans Typewriter" ) );
			consoleTextFlow.getChildren().add( t ); // adds to pane, not textflow
			consoleTextFlow.setVisible( true );
			consoleTextFlow.toFront();

			( (ScrollPane) sp.lookup( "#" + Globals.FXVariables.SCROLLPANEID + id ) ).setVvalue( 1.0 );
		} );
	}


	public void updateRunningIcon( boolean running, String serverId ) {
		final String selectedServer = serverId;

		Circle c = (Circle) listViewAppList.lookup( "#" + Globals.FXVariables.RUNNINGID + selectedServer );

		Platform.runLater( ( ) -> {
			if ( running == true ) {
				c.setFill( Color.LIGHTSEAGREEN );
			}
				else {
					c.setFill( Color.GREY );
				}
			} );
	}


	public void updateLastUpdated( String lastUpdated, String serverId, Scene scene ) {
		Platform.runLater( ( ) -> {
			Text textContent = (Text) scene.lookup( "#" + Globals.FXVariables.lastUpdatedText + serverId );
			textContent.setText( lastUpdated );
		} );
	}


	public void updateMemory( String memory, String serverId, Scene scene ) {
		Platform.runLater( ( ) -> {
			Text textContent = (Text) scene.lookup( "#" + Globals.FXVariables.memoryText + serverId );
			textContent.setText( memory );
		} );
	}


	public void clearConsole( String serverId ) {
		StackPane sp = consoleStackPane;

		Platform.runLater( ( ) -> {
			TextFlow textFlowContent = (TextFlow) sp.lookup( "#" + Globals.FXVariables.CONSOLEID + serverId );
			textFlowContent.getChildren().clear();
		} );
	}


	public void handleListViewOnClick( HBox hbox, Scene scene, Hyperlink h, ServerConfigMap scm ) {
		scene.lookup( "#" + Globals.FXVariables.SETTINGSEMPTYID ).setVisible( false );
		getTabPane().setVisible( true );
		serverController.setSelectedServer( h.getId() );

		getServerInfoPane().setVisible( true );
		getServerInfoImagePane().setVisible( true );
		showCurrentServerInfoPane();

		// show correct textflows with last updated and memory
		Iterator itConsoleInfo = getConsoleInfo().getChildren().iterator();
		while ( itConsoleInfo.hasNext() ) {
			TextFlow consoleInfoTextFlow = (TextFlow) itConsoleInfo.next();
			Platform.runLater( ( ) -> {
				TextFlow textFlow = consoleInfoTextFlow;
				textFlow.setVisible( false );
				if ( textFlow.getId().contains( serverController.getSelectedServerInstance() ) ) {
					textFlow.setVisible( true );
				}
			} );
		}

		if ( getSelectedTabInstance() != null && getSelectedTabInstance().getId().contains( "settings" ) ) {
			Iterator itSettings = getSettingsStackPane().getChildren().iterator();
			while ( itSettings.hasNext() ) {
				getSplashPane().setVisible( false );
				getSplashPane().toBack();
				getSplashAnchorPane().setVisible( false );
				getSplashAnchorPane().toBack();
				Pane settingsPane = (Pane) itSettings.next();

				Platform.runLater( ( ) -> {
					Pane sp1 = settingsPane;
					sp1.setVisible( false );
					// show settings
					if ( sp1.getId().equals( Globals.FXVariables.SETTINGSID + serverController.getSelectedServerInstance() ) ) {
						sp1.setVisible( true );
						sp1.toFront();
					}
				} );
			}
		}
		else if ( getSelectedTabInstance() != null && getSelectedTabInstance().getId().contains( "console" ) ) {
			getConsoleStackPane().setVisible( true );

			Iterator itConsole = getConsoleStackPane().getChildren().iterator();
			while ( itConsole.hasNext() ) {
				getSplashPane().setVisible( false );
				getSplashPane().toBack();
				getSplashAnchorPane().setVisible( false );
				getSplashAnchorPane().toBack();
				ScrollPane consoleScrollPane = (ScrollPane) itConsole.next();

				Platform.runLater( ( ) -> {
					ScrollPane sp1 = consoleScrollPane;
					sp1.setVisible( false );
					// show console
					if ( sp1.getId().equals( Globals.FXVariables.SCROLLPANEID + serverController.getSelectedServerInstance() ) ) {
						sp1.setVisible( true );
						sp1.toFront();
					}
				} );
			}
		}

		if ( scm.getRunning().equals( "true" ) ) {
			showConsoleButtonsOnRunning();
		}
		else {
			showConsoleButtonsOnNotRunning();
		}

		addCurrentClassToServer( hbox );

		Platform.runLater( ( ) -> {
			// console is selected
			String consoleId = ( Globals.FXVariables.CONSOLEID + serverController.getSelectedServerInstance() );
			TextFlow textFlowContent = (TextFlow) scene.lookup( "#" + consoleId );
			String settingsId = ( Globals.FXVariables.SETTINGSID + serverController.getSelectedServerInstance() );
			Pane settings = (Pane) scene.lookup( "#" + settingsId );

			if ( getSelectedTabInstance() != null ) {
				if ( getSelectedTabInstance().getId().contains( "console" ) ) {
					textFlowContent.setVisible( true );
					textFlowContent.toFront();
					if ( scm.getRunning().equals( "true" ) ) {
						showConsoleButtonsOnRunning();
					}
					else {
						showConsoleButtonsOnNotRunning();
					}
				}// settings is selected
				else {
					settings.setVisible( true );
					settings.toFront();
					if ( scm.getRunning().equals( "true" ) ) {
						showSettingsButtonsOnRunning();
					}
					else {
						showSettingsButtonsOnNotRunning();
					}
				}
			}
				else {
					settings.setVisible( false );
					settings.toBack();
					textFlowContent.setVisible( true );
					textFlowContent.toFront();
				}
			} );

		// get list cell of hbox
		// apply css to it on selection
		if ( hbox.getStyleClass().contains( Globals.StyleClasses.CURRENT ) ) {
			hbox.getParent().setStyle( "-fx-background-color: #1c252c;" );
		}
		else {
			hbox.getParent().setStyle( "-fx-background-color: #222d35;" );
		}
	}


	public void updateSettings( String savedServerId, boolean newServer, Scene scene, Pane settings, String tempName, String tempIp, String tempPort, String tempWebFolder, String tempUri, String tempCustomJvm, boolean isCustomJvm, String tempJvmArgs, String tempMemory ) {

		if ( !newServer ) {
			// update settings
			( (TextField) settings.lookup( "#" + Globals.FXVariables.nameTextBox ) ).setText( tempName );
			( (TextField) settings.lookup( "#" + Globals.FXVariables.ipTextBox ) ).setText( tempIp );
			( (TextField) settings.lookup( "#" + Globals.FXVariables.portTextBox ) ).setText( tempPort );
			( (TextField) settings.lookup( "#" + Globals.FXVariables.webFolderTextBox ) ).setText( tempWebFolder );
			( (TextField) settings.lookup( "#" + Globals.FXVariables.uriTextBox ) ).setText( tempUri );
			( (TextField) settings.lookup( "#" + Globals.FXVariables.customJvmTextBox ) ).setText( tempCustomJvm );
			( (TextField) settings.lookup( "#" + Globals.FXVariables.jvmArgsTextBox ) ).setText( tempJvmArgs );
			( (TextField) settings.lookup( "#" + Globals.FXVariables.memoryTextBox ) ).setText( tempMemory );
			( (RadioButton) settings.lookup( "#" + Globals.FXVariables.customJvmRadioBtn ) ).selectedProperty().set( isCustomJvm );

			String id = settings.getId().replace( Globals.FXVariables.SETTINGSID, "" );

			// update hyperlink in vboxAppList
			( (Hyperlink) scene.lookup( "#" + id ) ).setText( tempName );

			// update info in server info pane
			( (Text) scene.lookup( "#" + Globals.FXVariables.INFOWEBFOLDERID + id ) ).setText( '\n' + tempWebFolder );
			( (Text) scene.lookup( "#" + Globals.FXVariables.INFONAMEID + id ) ).setText( tempName + " - " );
			( (Text) scene.lookup( "#" + Globals.FXVariables.INFOURLID + id ) ).setText( tempIp + ":" + tempPort );
		}
		else {
			// add ids to list
			getServerConfigIdList().add( savedServerId );

			ServerConfigMap scm = null;

			for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
				if ( server.getId().equals( savedServerId ) ) {
					scm = server;
				}
			}


			HBox hbox = addHBoxToList( scm, scene, true );

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


	HBox addHBoxToList( ServerConfigMap scm, Scene scene, boolean newServer ) {

		// RUNNING CIRCLE
		Circle c = new Circle( 5.0f, Color.GREY );
		c.setId( Globals.FXVariables.RUNNINGID + scm.getId() );

		// POLYGON
		Polygon p = new Polygon();

		p.getPoints().setAll(
				0d, 0d,
				12d, 6d,
				0d, 12d
				);

		p.setId( "polygon" + scm.getId() );
		p.setFill( Color.GREEN );

		p.setVisible( false );

		p.setTranslateY( 10.0 );
		p.setTranslateX( 12.0 );

		// play/stop button click
		p.setOnMouseClicked( event -> {
			if ( scm.getRunning().equals( "true" ) ) {
				stopBtnClick( scm.getId(), scene );
			}
				else {
					startBtnClick( scm.getId(), scene );
					setSelectedTabInstance( getTabPane().getTabs().get( 1 ) );
					if ( !getServerInfoImagePane().isVisible() ) {
						getServerInfoPane().setVisible( true );
						getServerInfoImagePane().setVisible( true );
					}
					showCurrentServerInfoPane();
				}
			} );

		Pane polygonPane = new Pane( p );
		polygonPane.setPrefWidth( polygonPaneWidth );
		polygonPane.setMinWidth( polygonPaneWidth );
		polygonPane.setMaxWidth( polygonPaneWidth );


		// HYPERLINK
		Hyperlink h = new Hyperlink( scm.getName(), c );

		h.setPadding( new Insets( 0, 0, 0, 10 ) );
		h.setId( scm.getId() );
		h.getStyleClass().add( "serverListHyperlink" );
		h.setPrefWidth( hyperlinkWidth - 18 );
		h.setMinWidth( hyperlinkWidth - 18 );
		h.setMaxWidth( hyperlinkWidth - 18 );

		HBox hbox = new HBox( h, polygonPane );

		hbox.getStyleClass().add( "hboxServer" );
		hbox.setPrefWidth( polygonPaneWidth + hyperlinkWidth - 18 );
		hbox.setMaxWidth( polygonPaneWidth + hyperlinkWidth - 18 );
		hbox.setMinWidth( polygonPaneWidth + hyperlinkWidth - 18 );
		hbox.setId( Globals.FXVariables.HBOXID + scm.getId() );


		AnchorPane a = new AnchorPane();
		a.getChildren().add( hbox );

		AnchorPane.setLeftAnchor( hbox, 0.0 );
		AnchorPane.setRightAnchor( hbox, 0.0 );

		serversForList.put( scm.getId(), a );

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
			handleListViewOnClick( hbox, scene, h, scm );
		} );
		return hbox;
	}


	void startBtnClick( String id, Scene scene ) {
		String selectedServer = id;
		if ( selectedServer == null ) {
			selectedServer = serverController.getSelectedServerInstance();
		}

		Polygon p = ( (Polygon) scene.lookup( "#" + Globals.FXVariables.POLYGONID + selectedServer ) );

		p.getPoints().setAll(
				0d, 0d, // (x, y)
				0d, 12d,
				12d, 12d,
				12d, 0d
				);

		FillTransition ft = new FillTransition( Duration.millis( 4000 ), p, Color.GREEN, Color.RED );
		ft.play();

		final String ss = selectedServer;
		Thread t1 = new Thread( new Runnable() {

			public void run()
			{
				startServer( ss, scene );
			}
		} );
		t1.start();


		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getId().equals( selectedServer ) ) {
				server.setRunning( "true" );
			}
		}

		Tab tab = getTabPane().getTabs().get( 1 );
		getTabPane().getSelectionModel().select( tab );
		setSelectedTabInstance( tab );

		( (ScrollPane) scene.lookup( "#" + Globals.FXVariables.SCROLLPANEID + selectedServer ) ).setVisible( true );
		showConsoleButtonsOnRunning();
		// update server info
		Pane serverInfoPane = (Pane) scene.lookup( "#" + Globals.FXVariables.SERVERINFOID + selectedServer );
		serverInfoPane.setVisible( true );
		serverInfoPane.toFront();
	}


	void stopBtnClick( String id, Scene scene ) {
		String selectedServer = serverController.getSelectedServerInstance();
		if ( selectedServer == null ) {
			selectedServer = id;
		}

		if ( getSelectedTabInstance() == null ) {
			Tab tab = getTabPane().getTabs().get( 1 );
			setSelectedTabInstance( tab );
		}

		Polygon p = ( (Polygon) scene.lookup( "#" + Globals.FXVariables.POLYGONID + selectedServer ) );

		p.getPoints().setAll(
				0d, 0d, // (x, y)
				12d, 6d,
				0d, 12d
				);

		FillTransition ft = new FillTransition( Duration.millis( 2000 ), p, Color.RED, Color.GREEN );
		ft.play();

		stopServer( selectedServer );
		for ( ServerConfigMap server : serverController.getServerConfigListInstance() ) {
			if ( server.getId().equals( selectedServer ) ) {
				server.setRunning( "false" );
			}
		}

		if ( getSelectedTabInstance().getId().contains( "console" ) ) {
			showConsoleButtonsOnNotRunning();
		}
		else {
			showSettingsButtonsOnNotRunning();
		}
	}


	public void updateServerOnDelete() {
		// hide hyperlink
		HBox poly = (HBox) listViewAppList.lookup( "#" + Globals.FXVariables.HBOXID + serverController.getSelectedServerInstance() );
		listViewAppList.getItems().remove( poly );

		// hide console
		ScrollPane sp = (ScrollPane) consoleStackPane.lookup( "#" + Globals.FXVariables.SCROLLPANEID + serverController.getSelectedServerInstance() );
		consoleStackPane.getChildren().remove( sp );

		// hide settings
		Pane p = (Pane) settingsStackPane.lookup( "#" + Globals.FXVariables.SETTINGSID + serverController.getSelectedServerInstance() );
		settingsStackPane.getChildren().remove( p );

		// hide server info
		TextFlow tf = (TextFlow) serverInfoStackPane.lookup( "#" + Globals.FXVariables.INFOTEXTFLOWID + serverController.getSelectedServerInstance() );
		serverInfoStackPane.getChildren().remove( tf );

		// hide console info
		Pane tf2 = (Pane) consoleInfo.lookup( "#" + Globals.FXVariables.memoryTextFlow + serverController.getSelectedServerInstance() );
		consoleInfo.getChildren().remove( tf2 );

		Pane tf3 = (Pane) consoleInfo.lookup( "#" + Globals.FXVariables.lastUpdatedTextFlow + serverController.getSelectedServerInstance() );
		consoleInfo.getChildren().remove( tf3 );

		serversForList.remove( serverController.getSelectedServerInstance() );

		serverController.setSelectedServer( null );

		refreshServerList();
	}


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
							Platform.runLater( ( ) -> {
								hbox.setPrefWidth( polygonPaneWidth + hyperlinkWidth - 18 );
								hbox.setMaxWidth( polygonPaneWidth + hyperlinkWidth - 18 );
								hbox.setMinWidth( polygonPaneWidth + hyperlinkWidth - 18 );
								VBox.setMargin( hbox, new Insets( 0, 0, 0, 0 ) );

								getListViewAppList().getItems().add( hbox );
							} );
						}
					}
				}
			}
		}
	}


	protected void showCurrentServerInfoPane() {

		// show correct server info
		Iterator itInfo = getServerInfoStackPane().getChildren().iterator();
		while ( itInfo.hasNext() ) {
			getSplashPane().setVisible( false );
			getSplashPane().toBack();
			getSplashAnchorPane().setVisible( false );
			getSplashAnchorPane().toBack();
			Pane serverInfoPane = (Pane) itInfo.next();
			Platform.runLater( ( ) -> {
				serverInfoPane.setVisible( false );
				// show server info
				if ( serverInfoPane.getId().equals( Globals.FXVariables.SERVERINFOID + serverController.getSelectedServerInstance() ) ) {
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
			item.getParent().setStyle( "-fx-background-color: #222d35;" );
		}

		// set app to "current"
		hbox.getStyleClass().add( Globals.StyleClasses.CURRENT );
	}


	// BUTTON HIDING/SHOWING FUNCTIONS

	protected void showNoButtons() {
		getDeleteBtn().setVisible( false );
		getSaveBtn().setVisible( false );

		getStartBtn().setVisible( false );
		getStopBtn().setVisible( false );
		getOpenBtn().setVisible( false );
		getClearBtn().setVisible( false );
	}


	protected void showSettingsButtonsOnRunning() {
		getDeleteBtn().setVisible( true );
		getSaveBtn().setVisible( true );

		getDeleteBtn().setDisable( true );
		getSaveBtn().setDisable( true );

		getStartBtn().setVisible( false );
		getStopBtn().setVisible( false );
		getOpenBtn().setVisible( false );
		getClearBtn().setVisible( false );
	}


	protected void showSettingsButtonsOnNotRunning() {
		getDeleteBtn().setVisible( true );
		getSaveBtn().setVisible( true );

		getDeleteBtn().setDisable( false );
		getSaveBtn().setDisable( false );

		getStartBtn().setVisible( false );
		getStopBtn().setVisible( false );
		getOpenBtn().setVisible( false );
		getClearBtn().setVisible( false );
	}


	protected void showConsoleButtonsOnRunning() {
		getStartBtn().setVisible( false );
		getStopBtn().setVisible( true );
		getOpenBtn().setVisible( true );
		getClearBtn().setVisible( true );

		getOpenBtn().setDisable( false );

		getDeleteBtn().setVisible( false );
		getSaveBtn().setVisible( false );
	}


	protected void showConsoleButtonsOnNotRunning() {
		getStartBtn().setVisible( true );
		getStopBtn().setVisible( false );
		getOpenBtn().setVisible( true );
		getClearBtn().setVisible( true );

		getOpenBtn().setDisable( true );

		getDeleteBtn().setVisible( false );
		getSaveBtn().setVisible( false );
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


	public void setConsoleInfo( Pane consoleInfo ) {
		this.consoleInfo = consoleInfo;
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


	public List<String> getServerConfigIdList() {
		return serverConfigIdList;
	}


	public void setServerConfigIdList( List<String> serverConfigIdList ) {
		this.serverConfigIdList = serverConfigIdList;
	}
}
