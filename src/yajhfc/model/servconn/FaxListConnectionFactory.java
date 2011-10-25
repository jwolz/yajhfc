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
package yajhfc.model.servconn;

import java.lang.reflect.Constructor;

import yajhfc.server.ServerOptions;
import yajhfc.ui.YajOptionPane;

public class FaxListConnectionFactory {
    public static FaxListConnection getFaxListConnection(ServerOptions options, YajOptionPane dialogUI) throws Exception {
        Class <? extends FaxListConnection> implClass = options.faxListConnectionType.getImplementingClass();
        Constructor<? extends FaxListConnection> constructor = implClass.getConstructor(ServerOptions.class, YajOptionPane.class);
        return constructor.newInstance(options, dialogUI);
    }
    
    public static boolean isConnectionTypeStillValid(FaxListConnection connection, ServerOptions newOptions) {
        return (connection.getClass() == newOptions.faxListConnectionType.getImplementingClass());
    }
}
