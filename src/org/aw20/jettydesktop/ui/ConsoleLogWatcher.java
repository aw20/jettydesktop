package org.aw20.jettydesktop.ui;

import java.io.File;


/*
 * Example class for log plugin
 * 
 */
public class ConsoleLogWatcher extends LogWatcher {

	public ConsoleLogWatcher( String _name, File _html, String _serverId, File _css, File _javascript ) {
		super( _name, _html, _serverId, _css, _javascript );
	}


	@Override
	public String getName() {
		return super.getName();
	}


	@Override
	public File getHtml() {
		return super.getHtml();
	}


	@Override
	public String getId() {
		return super.getServerId();
	}


	@Override
	public File getCss() {
		return super.getCss();
	}


	@Override
	public File getJavascript() {
		return super.getJavascript();
	}
}
