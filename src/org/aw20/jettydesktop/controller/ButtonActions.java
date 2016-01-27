package org.aw20.jettydesktop.controller;


public class ButtonActions {

	/**
	 * Method to show no buttons
	 * 
	 * @param uiController
	 */
	public void showNoButtons( UIController uiController ) {
		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );

		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( false );
		uiController.getClearBtn().setVisible( false );
	}


	/**
	 * Method to show disabled delete and disabled save buttons
	 * 
	 * @param uiController
	 */
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


	/**
	 * Method to show no delete and save buttons
	 * 
	 * @param uiController
	 */
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


	/**
	 * Method to show stop, open and clear buttons
	 * 
	 * @param uiController
	 */
	public void showConsoleButtonsOnRunning( UIController uiController ) {
		uiController.getStartBtn().setVisible( false );
		uiController.getStopBtn().setVisible( true );
		uiController.getOpenBtn().setVisible( true );
		uiController.getClearBtn().setVisible( true );

		uiController.getOpenBtn().setDisable( false );

		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );
	}


	/**
	 * Method to show start, disabled open and clear buttons
	 * 
	 * @param uiController
	 */
	public void showConsoleButtonsOnNotRunning( UIController uiController ) {
		uiController.getStartBtn().setVisible( true );
		uiController.getStopBtn().setVisible( false );
		uiController.getOpenBtn().setVisible( true );
		uiController.getClearBtn().setVisible( true );

		uiController.getOpenBtn().setDisable( true );

		uiController.getDeleteBtn().setVisible( false );
		uiController.getSaveBtn().setVisible( false );
	}


	/**
	 * Method to show save button
	 * 
	 * @param uiController
	 */
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
