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

import java.awt.Image;
import java.awt.PopupMenu;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public abstract class MacOSXSupport {
	private static MacOSXSupport instance;
	private static final String IMPL_CLASS_NAME = "yajhfc.macosx.MacOSXSupportImpl";

	public static MacOSXSupport getInstance() {
		if (!Utils.IS_MACOSX)
			return null;
		
		if (instance == null) {
			try {
				instance = (MacOSXSupport)Class.forName(IMPL_CLASS_NAME).newInstance();
			} catch (Exception e) {
				instance = null;
				Logger.getLogger(MacOSXSupport.class.getName()).log(Level.WARNING, "Mac OS X support not available:", e);
			}
		}
		return instance;
	}
	
	/**
	 * Adjusts System properties to make the UI feel more Mac native
	 */
	public static void setUIProperties() {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", Utils.AppShortName);
	}
	
	/**
	 * Sets the actions to use for the application menu entries
	 * @param actPreferences
	 * @param actAbout
	 * @param actQuit
	 */
	public abstract void setApplicationMenuActions(Action actPreferences, Action actAbout, Action actQuit);
	
	/**
	 * Sets the dock icon image
	 * @param image
	 */
	public abstract void setDockIconImage(Image image);
	
	/**
	 * Sets the dock icon badge
	 * @param badge
	 */
	public abstract void setDockIconBadge(String badge);
	
	/**
	 * Sets the dock icon menu
	 * @param menu
	 */
	public abstract void setDockIconMenu(PopupMenu menu);
	
	/**
	 * Returns the currently set dock icon menu
	 * @return
	 */
	public abstract PopupMenu getDockIconMenu();
}
