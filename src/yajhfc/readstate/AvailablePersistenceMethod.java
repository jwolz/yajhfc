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
 */
package yajhfc.readstate;

import java.awt.Window;

/**
 * @author jonas
 *
 */
public interface AvailablePersistenceMethod {
    /**
     * Returns a key uniquely identifying this method
     * @return
     */
    public String getKey();
    
    /**
     * Returns a user visible description for this method
     * @return
     */
    public String getDescription();
    
    /**
     * Returns true if this read state persister can be configured.
     * @return
     */
    public boolean canConfigure();

    /**
     * Shows the configuration dialog if canConfigure() == true and returns
     * a configuration String.
     * @param parent
     * @param oldConfig An old configuration to use as base. May be null to use a default configuration.
     * @return A valid configuration for createInstance or null if the user selected cancel
     */
    public String showConfigDialog(Window parent, String oldConfig);
    
    /**
     * Creates a new instance for this persistence method.
     * @param config
     * @return
     */
    public PersistentReadState createInstance(String config, int serverID);
}
