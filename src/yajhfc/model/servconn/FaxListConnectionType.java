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

import yajhfc.Utils;
import yajhfc.model.servconn.directaccess.DirectAccessFaxListConnection;
import yajhfc.model.servconn.hylafax.HylaFaxListConnection;

/**
 * Connection types to access the fax lists
 * @author jonas
 *
 */
public enum FaxListConnectionType {
    HYLAFAX(Utils._("Using the HylaFAX protocol"), HylaFaxListConnection.class),
    DIRECTACCESS(Utils._("Directly accessing the spool area"), DirectAccessFaxListConnection.class);
    
    private final Class<? extends FaxListConnection> implementingClass;
    private final String description;
    
    private FaxListConnectionType(String description,
            Class<? extends FaxListConnection> implementingClass) {
        this.description = description;
        this.implementingClass = implementingClass;
    }
    
    /**
     * Returns a user readable description for this connection type
     * @return
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * The class implementing the FaxListConnection.
     * This class must have a constructor (like HylaFaxListConnection) with the signature 
     * public ImplementingClass(FaxOptions fo, Window parentWindow, ProgressUI progressUI)
     * @return
     */
    public Class<? extends FaxListConnection> getImplementingClass() {
        return implementingClass;
    }
    
    @Override
    public String toString() {
        return description;
    }
}
