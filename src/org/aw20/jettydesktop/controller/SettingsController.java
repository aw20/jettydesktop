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

import java.util.Map;

import org.aw20.jettydesktop.ui.ServerManager;
import org.aw20.util.Globals;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


/*
 * Class to control settings operations, updating and validating
 */
public class SettingsController {

	public void createSettings( UIController uiController, ServerManager serverManager, ServerController serverController, Executor executor, int savedServerId, Scene scene ) {

		// add ids to list
		uiController.getServerConfigIdList().add( savedServerId );

		// get new server list item
		ServerSetup serverSetup = new ServerSetup();
		HBox hbox = serverSetup.addHBoxToList( savedServerId, scene, true, serverManager, uiController, serverController, new ServerActions(), executor );

		// add console for server
		TextFlow newTextFlow = new TextFlow();
		newTextFlow.setId( Globals.FXVariables.CONSOLEID + savedServerId );
		newTextFlow.setVisible( true );
		ScrollPane scrollPane = new ScrollPane( newTextFlow );
		scrollPane.setId( Globals.FXVariables.SCROLLPANEID + savedServerId );
		uiController.getConsoleStackPane().getChildren().add( scrollPane );
		scrollPane.setVisible( true );

		uiController.getListViewAppList().getItems().add( hbox );


		ServerInfoController serverInfoController = new ServerInfoController();

		serverInfoController.refreshServerList( uiController );


	}


	public void updateSettings( Pane settings, Scene scene, UIController uiController, Map<String, String> tempSettingsVariables, boolean isCustomJvm ) {
		// update settings
		( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.nameTextBox ) ).setText( tempSettingsVariables.get( "tempName" ) );
		( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.ipTextBox ) ).setText( tempSettingsVariables.get( "tempIp" ) );
		( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.portTextBox ) ).setText( tempSettingsVariables.get( "tempPort" ) );
		( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.webFolderTextBox ) ).setText( tempSettingsVariables.get( "tempWebFolder" ) );
		( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.uriTextBox ) ).setText( tempSettingsVariables.get( "tempUri" ) );
		( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmTextBox ) ).setText( tempSettingsVariables.get( "tempCustomJvm" ) );
		( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.jvmArgsTextBox ) ).setText( tempSettingsVariables.get( "tempJvmArgs" ) );
		( (TextField) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.memoryTextBox ) ).setText( tempSettingsVariables.get( "tempMemory" ) );
		( (RadioButton) settings.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.customJvmRadioBtn ) ).selectedProperty().set( isCustomJvm );

		String id = settings.getId().replace( Globals.FXVariables.SETTINGSID, "" );

		// update label in listViewAppList with name
		( (Label) scene.lookup( Globals.FXVariables.idSelector + id ) ).setText( tempSettingsVariables.get( "tempName" ) );

		// update info in server info pane
		( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFOWEBFOLDERID + id ) ).setText( '\n' + tempSettingsVariables.get( "tempWebFolder" ) );
		( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFONAMEID + id ) ).setText( tempSettingsVariables.get( "tempName" ) + " - " );
		( (Text) scene.lookup( Globals.FXVariables.idSelector + Globals.FXVariables.INFOURLID + id ) ).setText( tempSettingsVariables.get( "tempIp" ) + ":" + tempSettingsVariables.get( "tempPort" ) );

		ServerInfoController serverInfoController = new ServerInfoController();

		serverInfoController.refreshServerList( uiController );
	}


	/*
	 * method to validate settings on save
	 */
	public boolean validateSettings( Scene scene, Main main, ServerController serverController, ServerManager serverManager ) {
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

		if ( !serverManager.isValidServerName( tempName, settingsId ) ) {
			Alert alert = main.createNewAlert( AlertType.WARNING, "Error", "Please choose a different name for the new server", null );
			alert.showAndWait();
			return false;
		}

		return true;
	}

}
