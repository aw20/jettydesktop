package org.aw20.jettydesktop.ui;

import java.io.File;


/*
 * Example class for non-log plugin
 * 
 */
public class JavaPlugin implements Plugin {

	private String name;
	private File html;
	private File css;
	private File javascript;
	private String serverId;


	public JavaPlugin( String _name, File _html, String _serverId, File _css, File _javascript ) {
		name = _name;
		html = _html;
		css = _css;
		javascript = _javascript;
		serverId = _serverId;
	}


	@Override
	public String getName() {
		return name;
	}


	@Override
	public File getHtml() {
		return html;
	}


	@Override
	public File getCss() {
		return css;
	}


	@Override
	public File getJavascript() {
		return javascript;
	}


	@Override
	public String getId() {
		return serverId;
	}
}
