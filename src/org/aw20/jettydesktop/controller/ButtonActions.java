package org.aw20.jettydesktop.controller;


public class ButtonActions {

	// BUTTON HIDING/SHOWING FUNCTIONS

	public void showNoButtons( UIController uiController ) {
		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );

		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );
	}


	public void showSettingsButtonsOnRunning( UIController uiController ) {
		uiController.getDeleteBtn().setVisible( true );
		uiController.getSaveBtn().setVisible( true );

		uiController.getDeleteBtn().setDisable( true );
		uiController.getSaveBtn().setDisable( true );

		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );
	}


	public void showSettingsButtonsOnNotRunning( UIController uiController ) {
		uiController.getDeleteBtn().setVisible( true );
		uiController.getSaveBtn().setVisible( true );

		uiController.getDeleteBtn().setDisable( false );
		uiController.getSaveBtn().setDisable( false );

		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );
	}


	public void showConsoleButtonsOnRunning( UIController uiController ) {
		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( true );
		uiController.getOpenBtn().setVisible( true );
		uiController.getClearBtn().setVisible( true );

		uiController.getOpenBtn().setDisable( false );

		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );
	}


	public void showConsoleButtonsOnNotRunning( UIController uiController ) {
		uiController.getStartBtn().setVisible( true );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( true );
		uiController.getClearBtn().setVisible( true );

		uiController.getOpenBtn().setDisable( true );

		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );
	}


	public void showButtonsOnNewWebApp( UIController uiController ) {
		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );

		uiController.getOpenBtn().setDisable( false );

		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( true );
	}

}
