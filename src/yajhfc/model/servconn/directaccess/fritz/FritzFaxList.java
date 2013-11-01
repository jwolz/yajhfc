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
package yajhfc.model.servconn.directaccess.fritz;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import yajhfc.model.FmtItemList;
import yajhfc.model.RecvFormat;
import yajhfc.model.TableType;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.servconn.FaxListConnection;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJob;
import yajhfc.model.servconn.directaccess.DirectAccessFaxJobList;
import yajhfc.server.ServerOptions;

/**
 * @author jonas
 *
 */
public class FritzFaxList extends DirectAccessFaxJobList<RecvFormat> {

    public static final String DEFAULT_FAX_DATE_FORMAT = "dd.MM.yy_HH.mm";
    public static final String DEFAULT_FAX_PATTERN = "(\\d{2}\\.\\d{2}\\.\\d{2}_(?:\\d{2}\\.){1,2}\\d{2})\\_Telefax\\.(.+)\\.pdf";
    protected static final int GROUP_DATE_TIME = 1;
    protected static final int GROUP_SENDER = 2;
    
    /** 
      * Files are called like "18.10.13_11.51_Telefax.unbekannt.pdf"
      * Group 1 is the date/time
      * Group 2 is the sender
     */
    protected Pattern faxPattern;
    /**
     * Date format of the fax in the PDF file name
     */
    protected DateFormat faxDateFormat;
    

    public FritzFaxList(FaxListConnection parent,
            FmtItemList<RecvFormat> columns, ServerOptions fo, String directory,
            String faxPattern, String faxDateFormat) {
        super(parent, columns, fo, directory);
        
        this.faxPattern = Pattern.compile(faxPattern, Pattern.CASE_INSENSITIVE);
        this.faxDateFormat = new SimpleDateFormat(faxDateFormat);
    }

    public void reloadSettings(ServerOptions fo) {
        // NOP
    }

    public TableType getJobType() {
        return TableType.RECEIVED;
    }

    @Override
    protected DirectAccessFaxJob<RecvFormat> createJob(String jobID)
            throws IOException {
        return new FritzFaxJob(this, jobID, jobID);
    }

    @Override
    protected String[] translateDirectoryEntries(String[] listing) {
        if (listing == null || listing.length == 0) {
            return null;
        }
        ArrayList<String> result = new ArrayList<String>(listing.length);
        for (String file : listing) {
            String fileLower = file.toLowerCase();
            if (fileLower.contains("fax") && fileLower.endsWith(".pdf")) {
                result.add(file);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public HylaDirAccessor getDirAccessor() {
        return ((FritzFaxListConnection)parent).getDirAccessor();
    }
    
    /**
     * Returns the Pattern to parse the PDF file names
     * 
     * @return the faxPattern
     */
    public Pattern getFaxPattern() {
        return faxPattern;
    }

    /**
     * @param faxPattern the faxPattern to set
     */
    public void setFaxPattern(Pattern faxPattern) {
        this.faxPattern = faxPattern;
    }

    /**
     * Returns the Date format of the fax in the PDF file name
     * @return the faxDateFormat
     */
    public DateFormat getFaxDateFormat() {
        return faxDateFormat;
    }

    /**
     * @param faxDateFormat the faxDateFormat to set
     */
    public void setFaxDateFormat(DateFormat faxDateFormat) {
        this.faxDateFormat = faxDateFormat;
    }
}
