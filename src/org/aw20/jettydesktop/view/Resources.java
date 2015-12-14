package org.aw20.jettydesktop.view;

import java.net.URL;


public class Resources {

	public URL getHtmlResource() {
		return getClass().getResource( "index.html" );
	}


	public URL getJSResource() {
		return getClass().getResource( "jetty.js" );
	}


	public URL getJQueryResource() {
		return getClass().getResource( "jquery-1.11.3.min.js" );
	}


	public URL getTooltipResource() {
		return getClass().getResource( "jquery.tooltipster.min.js" );
	}


	public URL getCSSResource() {
		return getClass().getResource( "jetty.css" );
	}


	public URL getPNGResource() {
		return getClass().getResource( "jquery-ui.js" );
	}


	public URL getUICSSResource() {
		return getClass().getResource( "jquery-ui.css" );
	}


	public URL getUIResource() {
		return getClass().getResource( "jquery-ui.js" );
	}
}
