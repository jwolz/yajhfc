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
package yajhfc.model.servconn;

import java.awt.Window;
import java.lang.reflect.Constructor;

import yajhfc.FaxOptions;
import yajhfc.util.ProgressWorker.ProgressUI;

public class FaxListConnectionFactory {
    public static FaxListConnection getFaxListConnection(FaxOptions options, Window parentWindow, ProgressUI progressUI) throws Exception {
        Class <? extends FaxListConnection> implClass = options.faxListConnectionType.getImplementingClass();
        Constructor<? extends FaxListConnection> constructor = implClass.getConstructor(FaxOptions.class, Window.class, ProgressUI.class);
        return constructor.newInstance(options, parentWindow, progressUI);
    }
    
    public static boolean isConnectionTypeStillValid(FaxListConnection connection, FaxOptions newOptions) {
        return (connection.getClass() == newOptions.faxListConnectionType.getImplementingClass());
    }
}
