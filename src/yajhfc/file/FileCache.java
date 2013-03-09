/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2013 Jonas Wolz <info@yajhfc.de>
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
package yajhfc.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;

/**
 * A cache for converted files
 * 
 * @author jonas
 *
 */
public class FileCache {
    static final Logger log = Logger.getLogger(FileCache.class.getName());

    protected final Map<List<FormattedFile>, ConvFileInfo> cache = new HashMap<List<FormattedFile>, FileCache.ConvFileInfo>();

    /**
     * Creates a cache entry
     * @param source
     * @param targetName
     * @param paperSize
     */
    public synchronized void addToCache(List<FormattedFile> source, File targetName, PaperSize paperSize) {
        cache.put(source, new ConvFileInfo(source, targetName, paperSize));
        Cleaner.checkInstall();
    }

    /**
     * Checks if there is a valid cache entry for the specified source files.
     * @param source
     * @param targetName
     * @param paperSize
     * @return a cached file or null if nothing was found
     */
    public File checkCache(List<FormattedFile> source, PaperSize paperSize) {
        ConvFileInfo cfi = cache.get(source);
        if (cfi != null) {
            log.fine("Found file in cache...");
            if (paperSize.equals(cfi.paperSize) && cfi.checkValid()) {
                log.fine("File is valid.");
                return cfi.file;
            } else {
                log.fine("File is invalid, removing it.");
                synchronized (this) {
                    cache.remove(source);
                }
                return null;
            }
        } else {
            log.fine("Found nothing in cache.");
            return null;
        }
    }

    /**
     * Revalidates all entries in the cache
     */
    public synchronized void validateCache() {
        if (cache.size() > 0) {
            Iterator<Map.Entry<List<FormattedFile>, ConvFileInfo>> it = cache.entrySet().iterator();
            Map.Entry<List<FormattedFile>, ConvFileInfo> entry;
            while (it.hasNext()) {
                entry = it.next();
                if (!entry.getValue().checkValid()) {
                    it.remove();
                }
            }
        }
    }

    public static class ConvFileInfo  extends FileInfo {
        public final List<FileInfo> sourceFiles = new ArrayList<FileInfo>();
        public final PaperSize paperSize;

        @Override
        public boolean checkValid() {
            if (!super.checkValid())
                return false;
            for (FileInfo fi : sourceFiles) {
                if (!fi.checkValid())
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return sourceFiles.hashCode() ^ paperSize.hashCode() ^ file.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj==this)
                return true;
            if (obj instanceof ConvFileInfo) {
                final ConvFileInfo convFileInfo = (ConvFileInfo)obj;
                return paperSize.equals(convFileInfo.paperSize)
                        && file.equals(convFileInfo.file)
                        && sourceFiles.equals(convFileInfo.sourceFiles);
            }
            return false;
        }

        public ConvFileInfo(List<FormattedFile> sourceFiles, File targetFile, PaperSize paperSize) {
            super(targetFile);
            this.paperSize = paperSize;
            for (FormattedFile f : sourceFiles) {
                this.sourceFiles.add(new FileInfo(f.file));
            }
        } 
    }

    public static class FileInfo {
        public final File file;
        public long size;
        public long timestamp;

        protected void readFileInfo() {
            this.size = file.length();
            this.timestamp = file.lastModified();
        }

        public boolean checkValid() {
            return (file.exists() &&
                    file.length() == size &&
                    file.lastModified() == timestamp);
        }

        @Override
        public int hashCode() {
            return file.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj==this)
                return true;
            if (obj instanceof FileInfo)
                return file.equals(((FileInfo)obj).file);
            return false;
        }

        public FileInfo(File file) {
            super();
            this.file = file;
            readFileInfo();
        }
    }

    public static class Cleaner implements Runnable {
        private static final long FREQUENCY=31;
        private static boolean INSTALLED = false;
        
        public void run() {
            try {
                log.fine("Validating caches...");
                for (MultiFileConvFormat mcf : MultiFileConvFormat.values()) {
                    final MultiFileConverter converter = mcf.getConverter();
                    if (converter.cache != null)
                        converter.cache.validateCache();
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Error cleaning caches", ex);
            }
        }
        
        public static synchronized void checkInstall() {
            if (!INSTALLED) {
                log.fine("Installing global cache cleaner...");
                Utils.executorService.scheduleWithFixedDelay(new Cleaner(), FREQUENCY, FREQUENCY, TimeUnit.SECONDS);
                INSTALLED = true;
            }
        }
    }
}
