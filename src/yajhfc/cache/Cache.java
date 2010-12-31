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

import yajhfc.FaxOptions;
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
	public void writeToCache(FaxOptions fo)  throws IOException {
		writeToCache(getDefaultCacheLocation());
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
	public boolean readFromCache(FaxOptions fo)  throws IOException, ClassNotFoundException {
		return readFromCache(getDefaultCacheLocation());
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

	public static File getDefaultCacheLocation() {
		return new File(Utils.getConfigDir(), "faxlists.cache");
	}
}
