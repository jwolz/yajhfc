/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.options;

import yajhfc.FaxOptions;

/**
 * @author jonas
 *
 */
public interface OptionsPage {
    /**
     * Loads the settings from the options
     * @param foEdit
     */
    public void loadSettings(FaxOptions foEdit);
    /**
     * Saves the settings to the options.
     * @param foEdit
     */
    public void saveSettings(FaxOptions foEdit);
    /**
     * Validates the user's settings
     * @return true if settings are valid, false otherwise
     */
    public boolean validateSettings();
    /**
     * Validates the user's settings
     * @param optionsWin
     * @return true if settings are valid, false otherwise
     */
    public boolean validateSettings(OptionsWin optionsWin);
}
