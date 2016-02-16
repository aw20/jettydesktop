package org.aw20.jettydesktop.controller;

import java.util.Map.Entry;

import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.jettydesktop.ui.ServerWrapper;
import org.aw20.util.Globals;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class SettingsController {

	public void updateSettings( UIController uiController, Main main, ServerController serverController, Executor executor, int savedServerId, boolean newServer, Scene scene, Pane settings, String tempName, String tempIp, String tempPort, String tempWebFolder, String tempUri, String tempCustomJvm, boolean isCustomJvm, String tempJvmArgs, String tempMemory ) {

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

			HBox hbox = (HBox) scene.lookup( Globals.FXVariables.idSelector + id ).getParent();

			uiController.addCurrentClassToServer( hbox );

			// update info in server info pane
			( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFOWEBFOLDERID + id ) ).setText( '\n' + tempWebFolder );
			( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFONAMEID + id ) ).setText( tempName + " - " );
			( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFOURLID + id ) ).setText( tempIp + ":" + tempPort );
		} else {
			// add ids to list
			uiController.getServerConfigIdList().add( savedServerId );

			// get new server list item
			ServerSetup serverSetup = new ServerSetup();
			HBox hbox = serverSetup.addHBoxToList( savedServerId, scene, true, uiController, serverController, new ServerActions(), executor );

			// add console for server
			TextFlow newTextFlow = new TextFlow();
			newTextFlow.setId( Globals.FXVariables.CONSOLEID + savedServerId );
			newTextFlow.setVisible( true );
			ScrollPane scrollPane = new ScrollPane( newTextFlow );
			scrollPane.setId( Globals.FXVariables.SCROLLPANEID + savedServerId );
			uiController.getConsoleStackPane().getChildren().add( scrollPane );
			scrollPane.setVisible( true );

			uiController.getListViewAppList().getItems().add( hbox );
		}

		ServerInfoController serverInfoController = new ServerInfoController();

		serverInfoController.refreshServerList( uiController );

	}


	/*
	 * method to validate settings on save
	 */
	public boolean validateSettings( Scene scene, Main main, ServerController serverController ) {
		Pane tempSettings;
		// not new server
		if ( serverController.getSelectedServer() != 0 ) {
			tempSettings = ( (Pane) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.SETTINGSID + serverController.getSelectedServer() ) );
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
					Alert alert = main.createNewAlert( AlertType.WARNING, "Error", "Please choose a different name for the new server", null );
					alert.showAndWait();
					return false;
				} else {
					return true;
				}
			}
		}

		return true;
	}

}
