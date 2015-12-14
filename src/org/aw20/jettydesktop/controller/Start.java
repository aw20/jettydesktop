package org.aw20.jettydesktop.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import netscape.javascript.JSObject;

import org.aw20.jettydesktop.view.Resources;


public class Start extends Application {

	private String title = "Jetty Desktop";

	protected Scene primaryScene;
	private static Resources res = new Resources();

	public static Stage stage;
	static WebView webView = new WebView();
	public WebEngine webEngineSingleton = getWebEngineInstance();

	public static File temp = new File( System.getProperty( "java.io.tmpdir" ) );

	private static File dest;


	public static void main( String[] args ) {
		File f = new File( "resources/" ).getAbsoluteFile();
		if ( f.exists() ) {
			temp = f;
		}

		dest = new File( temp + "/jettystyle" );
		if ( !dest.exists() ) {
			dest.mkdir();
		}

		// if ( !f1.exists() ) //this line is for Tony
		try ( InputStream htmlBytes = res.getHtmlResource().openStream();
				InputStream jsBytes = res.getJSResource().openStream();
				InputStream jqueryBytes = res.getJQueryResource().openStream();
				InputStream cssBytes = res.getCSSResource().openStream();
				InputStream logoBytes = res.getPNGResource().openStream();
				InputStream jqueryuiBytes = res.getUIResource().openStream();
				InputStream jqueryuicssBytes = res.getUICSSResource().openStream(); ) {

			Files.copy( htmlBytes, new File( dest, "index.html" ).toPath(), StandardCopyOption.REPLACE_EXISTING );
			Files.copy( jsBytes, new File( dest, "jetty.js" ).toPath(), StandardCopyOption.REPLACE_EXISTING );
			Files.copy( jqueryBytes, new File( dest, "jquery-1.11.3.min.js" ).toPath(), StandardCopyOption.REPLACE_EXISTING );
			Files.copy( cssBytes, new File( dest, "jetty.css" ).toPath(), StandardCopyOption.REPLACE_EXISTING );
			Files.copy( logoBytes, new File( dest, "logo.png" ).toPath(), StandardCopyOption.REPLACE_EXISTING );
			Files.copy( jqueryuicssBytes, new File( dest, "jquery-ui.css" ).toPath(), StandardCopyOption.REPLACE_EXISTING );
			Files.copy( jqueryuiBytes, new File( dest, "jquery-ui.js" ).toPath(), StandardCopyOption.REPLACE_EXISTING );
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		launch( args );
	}


	@Override
	public void start( Stage primaryStage ) {

		AnchorPane anchorPane = new AnchorPane();
		stage = primaryStage;
		primaryStage.setTitle( title );
		primaryStage.setHeight( 750 );
		primaryStage.setWidth( 1000 );

		final AppFunctions appFunctions = AppFunctions.getInstance();
		URL index = null;
		try {
			index = new File( dest, "index.html" ).toURL();

		} catch ( MalformedURLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSObject win = (JSObject) webEngine.executeScript( "window" );
		win.setMember( "java", appFunctions );


		webView.setContextMenuEnabled( false );

		webEngine.load( index.toExternalForm() );

		// Set Layout Constraint
		AnchorPane.setTopAnchor( webView, 0.0 );
		AnchorPane.setBottomAnchor( webView, 0.0 );
		AnchorPane.setLeftAnchor( webView, 0.0 );
		AnchorPane.setRightAnchor( webView, 0.0 );

		// Add WebView to AnchorPane
		anchorPane.getChildren().add( webView );

		primaryScene = new Scene( anchorPane, 300, 250 );

		primaryStage.setScene( primaryScene );
		primaryStage.setResizable( true );
		primaryStage.getIcons().add( new Image( "/org/aw20/jettydesktop/view/logo.png" ) );

		primaryStage.show();

		Platform.runLater( new Runnable() {

			public void run() {


				Start.stage.setOnCloseRequest( new EventHandler<WindowEvent>() {

					public void handle( WindowEvent t ) {
						// if no servers have started exit app - hard delete servers, exit platform
						if ( appFunctions.serverConfigList == null ) {
							appFunctions.deleteServers();
							Platform.exit();
						} else {
							// else count the running servers
							int count = 0;

							for ( int i = 0; i < appFunctions.serverConfigList.size(); ++i ) {
								if ( ( "true" ).equals( appFunctions.serverConfigList.get( i ).getRunning() ) ) {
									count++;
								}
							}
							// if servers are running call js to run dialog
							if ( count > 0 ) {
								appFunctions.openDialog();
								t.consume();// no button pressed
							}
							// no servers running - hard delete servers, exit platform
							else {
								appFunctions.deleteServers();
								Platform.exit();
							}
						}
					}
				} );
			}
		} );
	}

	// create Singleton instance
	private static WebEngine webEngine = null;


	public static WebEngine getWebEngineInstance() {
		if ( webEngine == null ) {
			webEngine = webView.getEngine();
		}
		return webEngine;
	}
}