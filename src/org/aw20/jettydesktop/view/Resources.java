package org.aw20.jettydesktop.view;

import java.io.File;
import java.net.URL;

public class Resources {
	
	public URL getHtmlResource(){
		Class<? extends Resources> class1 = getClass();
		URL res1 = class1.getResource("index.html");
		return getClass().getResource( "index.html" );
	}
	
	public URL getJSResource(){
		return getClass().getResource( "jetty.js" );
	}
	
	public URL getJQueryResource(){
		return getClass().getResource( "jquery-1.11.3.min.js" );
	}
	
	public URL getCSSResource(){
		return getClass().getResource( "jetty.css" );
	}
	
	public URL getPNGResource(){
		return getClass().getResource( "logo.png" );
	}
}
