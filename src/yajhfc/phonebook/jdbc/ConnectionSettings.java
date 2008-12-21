package yajhfc.phonebook.jdbc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2006 Jonas Wolz
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

import yajhfc.Password;
import yajhfc.phonebook.GeneralConnectionSettings;

public class ConnectionSettings extends GeneralConnectionSettings {
    public String driver = "";
    public String dbURL = "jdbc:";
    public String user = "";
    public final Password pwd = new Password();
    public boolean askForPWD = false;
    public String table = "";
    public boolean readOnly = false;
    public String displayCaption = "";
    
    public ConnectionSettings(String serialized) {
        super();
        loadFromString(serialized);
    }
    
    public ConnectionSettings() {
        super();
    }
    
    public ConnectionSettings(ConnectionSettings src) {
        super();
        copyFrom(src);
    }
}
