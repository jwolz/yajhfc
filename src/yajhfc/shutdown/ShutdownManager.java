/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc.shutdown;

/**
 * @author jonas
 *
 */
public class ShutdownManager {

    private static ShutdownManager INSTANCE = new ShutdownManager();
    /**
     * Returns the currently active shut down manager instance 
     * @return
     */
    public static ShutdownManager getInstance() {
        return INSTANCE;
    }
    
    public static void setInstance(ShutdownManager instance) {
        INSTANCE = instance;
    }
    
    /**
     * Registers the specified Runnable so that it is run on application shutdown
     * @param run
     */
    public void registerShutdownHook(Runnable run) {
        Runtime.getRuntime().addShutdownHook(new Thread(run));
    }
}
