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
package yajhfc.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import yajhfc.Utils;

/**
 * A general-purpose cache implementation. Before invoking any of the writeToCache/readFromCache methods,
 * please fill the check data map (or leave it empty if you do not need to check cache validity).
 * The data inside this map is then written out to the cache, too.
 * For a cache to successfully load, the data inside this map must be equal to the check data found in the cache.
 * 
 * Format of the cache:
 * int    version
 * long   timestamp
 * Object checkData
 * Object cachedData
 * @author jonas
 *
 */
public class Cache {
	public static boolean useForNextLogin = true;
	
	static final Logger log = Logger.getLogger(Cache.class.getName());
	private static final int VERSION = 1;
	
	protected Map<String,Object> checkData;
	protected Map<String,Object> cachedData;
	
	public Map<String, Object> getCachedData() {
		if (cachedData == null) {
			cachedData = new HashMap<String,Object>();
		}
		return cachedData;
	}
	
	public Map<String, Object> getCheckData() {
		return checkData;
	}
	
	/**
	 * Writes the cache to the default location using the default check data
	 * @throws IOException
	 */
	public void writeToCache(int serverID)  throws IOException {
		writeToCache(getCacheLocation(serverID));
	}
	
	/**
	 * Writes the cache to the specified file
	 * @param outFile
	 * @throws IOException
	 */
	public void writeToCache(File outFile) throws IOException {
		log.fine("Writing cache to " + outFile);
		OutputStream outStream = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outFile)));
		writeToCache(outStream);
		outStream.flush();
		outStream.close();
	}
	
	/**
	 * Writes the cache (uncompressed) to the specified Stream
	 * @param out
	 * @throws IOException
	 */
	public void writeToCache(OutputStream targetStream) throws IOException {
		log.fine("Writing cache version " + VERSION);
		ObjectOutputStream out = new ObjectOutputStream(targetStream);
		out.writeInt(VERSION);
		out.writeLong(System.currentTimeMillis());
		out.writeObject(checkData);
		out.writeObject(cachedData);
		out.flush();
	}
	
	/**
	 * Reads the cache from the default location using the default check data
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public boolean readFromCache(int serverID)  throws IOException, ClassNotFoundException {
		return readFromCache(getCacheLocation(serverID));
	}
	
	/**
	 * Reads the cache from the specified file
	 * @param outFile
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public boolean readFromCache(File inFile) throws IOException, ClassNotFoundException {
	    log.fine("Reading cache from " + inFile);
	    if (!inFile.canRead()) {
	        log.info("Cache file " + inFile + " does not exist");
	        return false;
	    }
		InputStream inStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(inFile)));
		boolean rv = readFromCache(inStream);
		inStream.close();
		return rv;
	}
	
	/**
	 * Reads the cache (uncompressed) from the specified Stream
	 * @param out
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public boolean readFromCache(InputStream sourceStream) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(sourceStream);
		int version = in.readInt();
		if (version > VERSION) {
			log.info("Cache invalid: Found version " + version + ", expected " + VERSION);
			return false;
		}
		long timestamp = in.readLong();
		log.fine("Reading cache with timestamp " + timestamp + "; now = " + System.currentTimeMillis());
		Map<String,Object> cacheCheckData = (Map<String,Object>)in.readObject();
		if (!checkData.equals(cacheCheckData)) {
			log.info("Cache invalid: Check data differs");
			if (Utils.debugMode) {
			    log.fine("Check data in cache: " + cacheCheckData);
			    log.fine("Check data expected: " + checkData);
			}
			return false;
		}
		cachedData = (Map<String,Object>)in.readObject();
		log.fine("Cache successfully loaded.");
		return true;
	}
	
	public Cache() {
		checkData = new HashMap<String,Object>();
	}

	public static File getCacheLocation(int serverID) {
		return new File(Utils.getConfigDir(), "faxlists" + serverID + ".cache");
	}
}
