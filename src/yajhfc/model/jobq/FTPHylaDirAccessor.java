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
package yajhfc.model.jobq;

import gnu.inet.ftp.FtpClient;
import gnu.inet.ftp.ServerResponseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.Utils;
import yajhfc.shutdown.ShutdownManager;

/**
 * @author jonas
 *
 */
public class FTPHylaDirAccessor implements HylaDirAccessor {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    static final Logger log = Logger.getLogger(FileHylaDirAccessor.class.getName());
    
    public final String server;
    public final int port;
    public final String user;
    public final String pass;
    public final String baseDir;
    public final boolean pasv;
    
    /**
     * maximum age in milliseconds before a new directory listing is retrieved
     */
    protected long maxCacheAge = 5000;
    /**
     * Last time a directory listing has been retrieved
     */
    protected long cacheTime = -1;
    /**
     * Cache of last directory listing
     */
    protected Map<String,FTPFile> fileCache = new TreeMap<String, FTPFile>();
    
    /**
     * Pattern to recognize the fields in a FTP listing
     * 
     * Group 1 : mode
     * Group 2 : owner
     * Group 3 : size
     * Group 4 : date/time
     * Group 5 : file name
     */
    //Output will look like this on a Fritz!Box (others might need a different pattern)
    // drwxrwxrwx 4 ftp ftp 144 Oct 27 08:51 Online-Speicher
    // -r--r--r-- 1 ftp ftp 6640 Jan 10 13:07 FRITZ-NAS.txt
    // -rwxrwxrwx 1 ftp ftp 13465 Dec 13 2013 13.12.13_17.20_Telefax.080033024246.pdf
    protected Pattern ftpFileListPattern = Pattern.compile("([a-z-]+)\\s+\\d+\\s+(\\w+)\\s+\\w+\\s+(\\d+)\\s+(?:([A-Za-z]+\\s+\\d+\\s+\\d+:\\d+)|([A-Za-z]+\\s+\\d+\\s+\\d+))\\s+(.+)$");
    
    private static final int GROUP_MODE      = 1;
    private static final int GROUP_OWNER     = 2;
    private static final int GROUP_SIZE      = 3;
    private static final int GROUP_DATE_TIME = 4;
    private static final int GROUP_DATE_ONLY = 5;
    private static final int GROUP_FILENAME  = 6;
    
    /**
     * DateFormat to parse the date/time in a FTP listing
     */
    protected DateFormat ftpFileListDateFormat = new SimpleDateFormat("MMM dd HH:mm", Locale.US);
    
    /**
     * DateFormat to parse the date/time in a FTP listing
     */
    protected DateFormat ftpFileListDateOnlyFormat = new SimpleDateFormat("MMM dd yyyy", Locale.US);
    
    /**
     * Logout when nothing has happened for this many milliseconds
     */
    protected long logoutTimeout = 30000;

    protected FtpClient ftpClient;
    protected long lastActionTime;
    
    public FTPHylaDirAccessor(String server, int port, String user, String pass, String baseDir, boolean pasv) {
        super();
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.baseDir = baseDir;
        this.pasv = pasv;
    }
    
    protected ScheduledFuture<?> logoutChecker;
    private void initializeLogoutChecker() {
        if (logoutChecker != null) {
            logoutChecker = Utils.executorService.scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    if (ftpClient != null) {
                        synchronized (FTPHylaDirAccessor.this) {
                            if (System.currentTimeMillis()-lastActionTime > logoutTimeout) {
                                log.fine("Closing FTP connection...");
                                try {
                                    ftpClient.quit();
                                } catch (Exception e) {
                                    log.log(Level.WARNING, "Error closing FTP connection", e);
                                } 
                                ftpClient = null;
                                logoutChecker.cancel(false);
                                logoutChecker = null;
                            }
                        }
                    }
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }
    
    protected synchronized FtpClient getFtpClient() throws UnknownHostException, IOException, ServerResponseException {
        if (ftpClient == null) {
            ftpClient = new FtpClient();
            ftpClient.open(server, port);
            if (ftpClient.user(user)) {
                ftpClient.pass(pass);
            }
            ftpClient.cwd(baseDir);
            ftpClient.setPassive(pasv);
            initializeLogoutChecker();
            lastActionTime = System.currentTimeMillis();
        }
        return ftpClient;
    }

    /**
     * Return a new file object implementing some caching
     * @param fileName
     * @return
     * @throws ParseException 
     * @throws ServerResponseException 
     * @throws IOException 
     * @throws UnknownHostException 
     * @throws FileNotFoundException 
     */
    protected synchronized FTPFile getFtpFile(String fileName) throws  IOException {
        FTPFile file = getFileList().get(fileName);
        if (file==null)
            throw new FileNotFoundException(fileName + " does not exist.");
        return file;
    }
    
    protected synchronized Map<String,FTPFile> getFileList() throws IOException {
        if (System.currentTimeMillis() - cacheTime < maxCacheAge) {
            log.fine("Using cached file list...");
            return fileCache;
        }
        log.fine("Refreshing file list");
        try {
            FtpClient cli = getFtpClient();
            Vector<?> list = cli.getList();
            fileCache.clear();
            final Date nullDate = new Date(0);
            
            for (Object o : list) {
                String line = (String)o;
                if (Utils.debugMode) {
                    log.fine("Parsing listing line " + line);
                }
                Matcher m = ftpFileListPattern.matcher(line);
                if (!m.matches()) {
                    log.warning("\"" + line + "\" does not match the expected pattern, ignoring it. Pattern=" + ftpFileListPattern);
                    continue;
                }
                
                String fileName = m.group(GROUP_FILENAME);
                
                Date   modTime = nullDate;
                if (m.group(GROUP_DATE_TIME) != null) {
                    try {
                        modTime = ftpFileListDateFormat.parse(m.group(GROUP_DATE_TIME));
                    } catch (Exception e1) {
                        log.log(Level.WARNING, "Unparseable file date for \"" + line + "\": " + m.group(GROUP_DATE_TIME), e1);
                    }
                } else if (m.group(GROUP_DATE_ONLY) != null) {
                    try {
                        modTime = ftpFileListDateOnlyFormat.parse(m.group(GROUP_DATE_ONLY));
                    } catch (Exception e1) {
                        log.log(Level.WARNING, "Unparseable file date for \"" + line + "\": " + m.group(GROUP_DATE_ONLY), e1);
                    }
                }
                
                long size = -1;
                try {
                    size = Long.parseLong(m.group(GROUP_SIZE));
                } catch (Exception e) {
                    log.warning("Unparseable file size for \"" + line + "\": " + m.group(GROUP_SIZE));
                }
                String stat    = m.group(GROUP_MODE);
                int mode;
                boolean isDirectory;
                // Result will look like: -r--r--r-- 
                if (stat.length() >= 10) {
                    mode =  modeToInt(stat, 1) * 0100 |
                            modeToInt(stat, 4) * 0010 |
                            modeToInt(stat, 7);
                    isDirectory = (stat.charAt(0) == 'd');
                } else {
                    mode = 0;
                    isDirectory = false;
                } 
                
                String owner = m.group(GROUP_OWNER);
                
                fileCache.put(fileName, new FTPFile(fileName, isDirectory, mode, size, modTime, owner));
            }
            cacheTime = System.currentTimeMillis();
            return fileCache;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#listDirectory()
     */
    public synchronized String[] listDirectory() throws IOException {
        return getFileList().keySet().toArray(EMPTY_STRING_ARRAY);
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#listDirectory(java.lang.String)
     */
    public synchronized String[] listDirectory(String dir) throws IOException {
        try {
            FtpClient cli = getFtpClient();
            Vector<?> list = cli.getNameList(dir);
            String[] result = list.toArray(EMPTY_STRING_ARRAY);
            for (int i=0; i<result.length; i++) {
                int slashPos = result[i].lastIndexOf('/');
                if (slashPos >= 0) {
                    result[i] = result[i].substring(slashPos+1);
                }
            }
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#getInputReader(java.lang.String)
     */
    public Reader getInputReader(String fileName) throws IOException {
        return new FileReader(getFtpFile(fileName).getTempFile());
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#copyFile(java.lang.String, java.io.OutputStream)
     */
    public synchronized void copyFile(String fileName, OutputStream target)
            throws IOException {
        try {
            FtpClient cli = getFtpClient();
            cli.get(fileName, target);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#getFile(java.lang.String)
     */
    public File getFile(String fileName) throws IOException {
        return getFtpFile(fileName).getTempFile();
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#deleteFile(java.lang.String)
     */
    public void deleteFile(String fileName) throws IOException {
        try {
            FtpClient cli = getFtpClient();
            cli.dele(fileName);
            invalidateCache();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Invalidates the file cache (i.e. forces a reload on the next action)
     */
    public void invalidateCache() {
        cacheTime = -1;
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#deleteTree(java.lang.String)
     */
    public void deleteTree(String dirName) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }


    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#getLastModified()
     */
    public long getLastModified() throws IOException {
        getFileList();
        return cacheTime;
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#getLastModified(java.lang.String)
     */
    public long getLastModified(String fileName) throws IOException {
        return getFtpFile(fileName).modificationTime;
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#getSize(java.lang.String)
     */
    public long getSize(String fileName) throws IOException {
        return getFtpFile(fileName).size;
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#getProtection(java.lang.String)
     */
    public int getProtection(String fileName) throws IOException {
        return getFtpFile(fileName).mode;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#getProtection(java.lang.String)
     */
    public String getOwner(String fileName) throws IOException {
        return getFtpFile(fileName).owner;
    }

    /* (non-Javadoc)
     * @see yajhfc.model.jobq.HylaDirAccessor#getBasePath()
     */
    public String getBasePath() {
        return baseDir;
    }

    protected static int modeToInt(String stat, int offset) {
        return 
             ((stat.charAt(offset  ) == 'r') ? 04 : 0) 
           | ((stat.charAt(offset+1) == 'w') ? 02 : 0)
           | ((stat.charAt(offset+2) == 'x') ? 01 : 0) ;
    }

    public long getMaxCacheAge() {
        return maxCacheAge;
    }

    public Pattern getFtpFileListPattern() {
        return ftpFileListPattern;
    }

    public DateFormat getFtpFileListDateFormat() {
        return ftpFileListDateFormat;
    }

    public void setMaxCacheAge(long maxCacheAge) {
        this.maxCacheAge = maxCacheAge;
    }

    public void setFtpFileListPattern(Pattern ftpFileListPattern) {
        this.ftpFileListPattern = ftpFileListPattern;
    }

    public void setFtpFileListDateFormat(DateFormat ftpFileListDateFormat) {
        this.ftpFileListDateFormat = ftpFileListDateFormat;
    }
    
    /**
     * Returns the logout timeout in milliseconds
     * @return
     */
    public long getLogoutTimeout() {
        return logoutTimeout;
    }

    public void setLogoutTimeout(long logoutTimeout) {
        this.logoutTimeout = logoutTimeout;
    }



    protected class FTPFile {
        public final String name;
        public final int mode;
        public final long size;
        public final long modificationTime;
        public final boolean isDirectory;
        public final String owner;
        protected File tempFile;
        

        public FTPFile(String name, boolean isDirectory, int mode, long size,
                Date modificationTime, String owner) {
            super();
            this.name = name;
            this.isDirectory = isDirectory;
            this.mode = mode;
            this.size = size;
            this.modificationTime = modificationTime.getTime();
            this.owner = owner;
            
            if (Utils.debugMode)
                log.finer("new FTPFile: " + this);
        }


        public synchronized File getTempFile() throws IOException {
            if (tempFile == null) {
                tempFile = File.createTempFile("ftp", (name.length()>4) ? name.substring(name.length()-4) : null);
                ShutdownManager.deleteOnExit(tempFile);
                FileOutputStream fOut = new FileOutputStream(tempFile);
                copyFile(name, fOut);
                fOut.close();
            }
            return tempFile;
        }
        
        @Override
        public String toString() {
            return name + 
                    " (mode=" + Integer.toOctalString(mode) + 
                    "; size=" +  size + 
                    "; owner=" + owner +
                    "; modificationTime=" + new Date(modificationTime) +
                    "; isDirectory=" + isDirectory +
                    "; tempFile=" + tempFile + ")";
        }
    }
}
