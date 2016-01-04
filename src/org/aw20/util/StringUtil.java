package org.aw20.util;

import java.util.Comparator;

import org.aw20.jettydesktop.ui.ServerConfigMap;

public class StringUtil implements Comparator<ServerConfigMap> {
	//for ordered list in servers list - ignore casing
    public int compare(ServerConfigMap obj1, ServerConfigMap obj2) {
        return obj1.getName().toLowerCase().compareTo(obj2.getName().toLowerCase());
    }
    
    public static boolean isWhitespace(String s) {
        int length = s.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}