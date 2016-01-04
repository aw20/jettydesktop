package org.aw20.jettydesktop.controller;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aw20.util.PluginUtil;


public class FileWatcher extends Thread {

	private final File file;
	private AtomicBoolean stop = new AtomicBoolean( false );

	private AtomicBoolean needToUpdate = new AtomicBoolean( false );


	public FileWatcher( File file ) {
		this.file = file;
	}


	public boolean isStopped() {
		return stop.get();
	}


	public int getServerId() {
		return 0;// serverId;
	}


	public File getFile() {
		return this.file;
	}


	public void stopThread() {
		stop.set( true );
	}


	public void doOnChange() {
		AppFunctions.updatePlugin( file.getName(), PluginUtil.getPluginContent( file ), null );
		needToUpdate.set( false );
	}


	public AtomicBoolean getNeedsUpdating() {
		return needToUpdate;
	}


	@Override
	public void run() {
		try ( WatchService watcher = FileSystems.getDefault().newWatchService() ) {
			Path path = file.toPath().getParent();
			path.register( watcher, StandardWatchEventKinds.ENTRY_MODIFY );
			while ( !isStopped() ) {
				WatchKey key;
				try {
					key = watcher.poll( 25, TimeUnit.MILLISECONDS );
				} catch ( InterruptedException e ) {
					return;
				}
				if ( key == null ) {
					Thread.yield();
					continue;
				}

				for ( WatchEvent<?> event : key.pollEvents() ) {
					WatchEvent.Kind<?> kind = event.kind();

					@SuppressWarnings( "unchecked" )
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();

					if ( kind == StandardWatchEventKinds.OVERFLOW ) {
						Thread.yield();
						continue;
					} else if ( kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY && filename.toString().equals( file.getName() ) ) {
						needToUpdate.set( true );
						doOnChange();
					}
					boolean valid = key.reset();
					if ( !valid ) {
						break;
					}
				}
				Thread.yield();
			}
		} catch ( Throwable e ) {
			// Log or rethrow the error
		}
	}
}