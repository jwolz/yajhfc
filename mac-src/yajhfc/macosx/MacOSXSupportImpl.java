/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package yajhfc.macosx;

import java.awt.Frame;
import java.awt.Image;
import java.awt.PopupMenu;

import javax.swing.Action;

import yajhfc.launch.MainApplicationFrame;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

/**
 * @author jonas
 *
 */
public class MacOSXSupportImpl extends MacOSXSupport {
	private ApplicationListener appListener = null;
	
	protected MainApplicationFrame mainWindow;
	protected Action actPreferences;
	protected Action actAbout;
	protected Action actQuit;
	
	/* (non-Javadoc)
	 * @see yajhfc.macosx.MacOSXSupport#setApplicationMenuActions(javax.swing.Action, javax.swing.Action, javax.swing.Action)
	 */
	@Override
	public void setApplicationMenuActions(MainApplicationFrame mainWin, Action preferencesAction,
			Action aboutAction, Action quitAction) {
		Application app = Application.getApplication();
		if (appListener == null) {
			appListener = new ApplicationAdapter() {
				@Override
				public void handleAbout(ApplicationEvent arg0) {
					if (actAbout != null) {
						actAbout.actionPerformed(null);
						arg0.setHandled(true);
					}
				}
				
				@Override
				public void handleQuit(ApplicationEvent arg0) {
					if (actQuit != null) {
						actQuit.actionPerformed(null);
						//arg0.setHandled(true);
					}
				}
				
				@Override
				public void handlePreferences(ApplicationEvent arg0) {
					if (actPreferences != null) {
						actPreferences.actionPerformed(null);
						arg0.setHandled(true);
					}
				}
				
				@Override
				public void handleReOpenApplication(ApplicationEvent arg0) {
					if (mainWindow != null) {
						mainWindow.bringToFront();
					}
				}
			};
			app.addApplicationListener(appListener);
		}
		this.mainWindow = mainWin;
		this.actAbout = aboutAction;
		app.setEnabledAboutMenu(aboutAction != null);
		this.actPreferences = preferencesAction;
		app.setEnabledPreferencesMenu(preferencesAction != null);
		this.actQuit = quitAction;
	}

	@Override
	public PopupMenu getDockIconMenu() {
		return Application.getApplication().getDockMenu();
	}

	@Override
	public void setDockIconBadge(String badge) {
		Application.getApplication().setDockIconBadge(badge);
	}

	@Override
	public void setDockIconImage(Image image) {
		Application.getApplication().setDockIconImage(image);
	}

	@Override
	public void setDockIconMenu(PopupMenu menu) {
		Application.getApplication().setDockMenu(menu);
	}

}
