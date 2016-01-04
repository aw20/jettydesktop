package org.aw20.jettydesktop.ui;

import java.io.File;


public interface Plugin {

	String getName();


	String getId();


	File getHtml();


	File getCss();


	File getJavascript();
}
