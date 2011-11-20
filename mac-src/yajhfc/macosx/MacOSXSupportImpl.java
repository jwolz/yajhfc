/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz <info@yajhfc.de>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Linking YajHFC statically or dynamically with other modules is making 
 *  a combined work based on YajHFC. Thus, the terms and conditions of 
 *  the GNU General Public License cover the whole combination.
 *  In addition, as a special exception, the copyright holders of YajHFC 
 *  give you permission to combine YajHFC with modules that are loaded using
 *  the YajHFC plugin interface as long as such plugins do not attempt to
 *  change the application's name (for example they may not change the main window title bar 
 *  and may not replace or change the About dialog).
 *  You may copy and distribute such a system following the terms of the
 *  GNU GPL for YajHFC and the licenses of the other code concerned,
 *  provided that you include the source code of that other code when 
 *  and as the GNU GPL requires distribution of source code.
 *  
 *  Note that people who make modified versions of YajHFC are not obligated to grant 
 *  this special exception for their modified versions; it is their choice whether to do so.
 *  The GNU General Public License gives permission to release a modified 
 *  version without this exception; this exception also makes it possible 
 *  to release a modified version which carries forward this exception.
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
