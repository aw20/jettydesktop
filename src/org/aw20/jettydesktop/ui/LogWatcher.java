package org.aw20.jettydesktop.ui;

import java.io.File;


/*
 * Example class for log plugin
 * 
 */
abstract class LogWatcher implements Plugin {

	private String name;
	private File html;
	private File css;
	private File javascript;
	private String serverId;


	LogWatcher( String _name, File _html, String _id, File _css, File _javascript ) {
		name = _name;
		html = _html;
		serverId = _id;
		css = _css;
		javascript = _javascript;
	}


	public File getHtml() {
		return html;
	}


	public File getCss() {
		return css;
	}


	public File getJavascript() {
		return javascript;
	}


	public String getName() {
		return name;
	}


	public String getServerId() {
		return serverId;
	}
}
