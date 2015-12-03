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


	public URL getCSSResource() {
	return getClass().getResource( "jetty.css" );
	}


	public URL getCSSAwesomeResource() {
	return getClass().getResource( "font-awesome.css" );
	}


	public URL getPNGResource() {
	return getClass().getResource( "logo.png" );
	}


	public URL getFA1Resource() {
	return getClass().getResource( "FontAwesome.otf" );
	}


	public URL getFA2Resource() {
	return getClass().getResource( "fontawesome-webfont.eot" );
	}


	public URL getFA3Resource() {
	return getClass().getResource( "fontawesome-webfont.svg" );
	}


	public URL getFA4Resource() {
	return getClass().getResource( "fontawesome-webfont.ttf" );
	}


	public URL getFA5Resource() {
	return getClass().getResource( "fontawesome-webfont.woff" );
	}


	public URL getFA6Resource() {
	return getClass().getResource( "fontawesome-webfont.woff2" );
	}

}
