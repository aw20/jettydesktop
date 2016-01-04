package org.aw20.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PluginUtil {

	public static List<File> listOfFiles = new ArrayList<File>();


	public static File getFile( String path ) {
		return new File( path );
	}


	public static List<File> getPluginsFromDirectory( File dir, String ext ) {

		List<File> files = new ArrayList<File>();

		for ( final File fileEntry : dir.listFiles() ) {
			if ( fileEntry.getName().contains( "plugin-" ) && fileEntry.getName().contains( ext ) ) {
				files.add( fileEntry );
			}
		}

		if ( files.isEmpty() ) {
			return null;
		}

		return files;

	}


	public static List<File> getAdditionalFilesForPlugin( File dir, String plugin, String ext ) {
		List<File> files = new ArrayList<File>();

		for ( final File fileEntry : dir.listFiles() ) {
			if ( fileEntry.getName().contains( plugin ) && fileEntry.getName().contains( ext ) ) {
				files.add( fileEntry );
			}
		}

		if ( files.isEmpty() ) {
			return null;
		}

		return files;

	}


	public static String getPluginName( File file ) {
		String filename = file.getName();
		int indexOfDot = filename.lastIndexOf( '.' ); // 7 - indexOfDot

		return filename.substring( 7, indexOfDot );
	}


	public static String getPluginContent( File file ) {
		try {
			byte[] bytes = Files.readAllBytes( file.toPath() );
			return new String( bytes, "UTF-8" );
		} catch ( FileNotFoundException e ) {
			// e.printStackTrace();
		} catch ( IOException e ) {
			// e.printStackTrace();
		}
		return "";
	}


	public static File findFileByName( String name, String folder ) {
		File f = new File( folder );
		if ( !f.isHidden() ) {
			if ( f.getName().toLowerCase().contains( name.toLowerCase() ) ) {
				return f;
			}
			if ( f.isDirectory() ) {
				for ( String subFolder : f.list() ) {
					File ff = findFileByName( name, folder + File.separator + subFolder );
					if ( ff != null ) {
						listOfFiles.add( ff );
						return ff;
					}
				}
			}
		}
		return null;
	}


	public static File findFilesByName( String name, String folder ) {
		File f = new File( folder );
		if ( !f.isHidden() ) {
			if ( f.getName().toLowerCase().contains( name.toLowerCase() ) ) {
				listOfFiles.add( f );
				// return f;
			}
			if ( f.isDirectory() ) {
				for ( String subFolder : f.list() ) {
					File ff = findFileByName( name, folder + File.separator + subFolder );
					if ( ff != null ) {
						listOfFiles.add( ff );
						// return ff;
					}
				}
			}
		}
		return null;
	}


	public static File getLatestFile() {
		Collections.sort( listOfFiles );
		return listOfFiles.get( 0 );
	}


	public static void addFile( String path ) {

	}

}
