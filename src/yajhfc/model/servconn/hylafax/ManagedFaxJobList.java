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
package yajhfc.model.servconn.hylafax;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.IOException;
import java.util.Map;

import yajhfc.FaxOptions;
import yajhfc.model.FmtItem;
import yajhfc.model.servconn.FaxJobList;

/**
 * A fax job list with some management methods needed for the list connection
 * @author jonas
 *
 */
public interface ManagedFaxJobList<T extends FmtItem> extends FaxJobList<T> {
    /**
     * Poll for new jobs using the specified HylaFAXClient.
     * The client may be null if it is not needed for the implementing class.
     * @param hyfc
     * @throws IOException
     * @throws ServerResponseException
     */
    void pollForNewJobs(HylaFAXClient hyfc) throws IOException, ServerResponseException;
    
    /**
     * Reloads the settings retrieved from the fax options
     */
    public void reloadSettings(FaxOptions fo);
    
    /**
     * Does some cleanup (especially clears the job list) when the connection disconnects
     */
    void disconnectCleanup();

    /**
     * Save the current jobs into the specified cache
     * @param cache the cache to use
     * @param keyPrefix a unique prefix to use for the objects to save
     */
    public void saveJobsToCache(Map<String,Object> cache, String keyPrefix);
    
    /**
     * Load the current jobs from the specified cache
     * @param cache the cache to use
     * @param keyPrefix a unique prefix to use for the objects to save
     */
    public void loadJobsFromCache(Map<String,Object> cache, String keyPrefix);
}
