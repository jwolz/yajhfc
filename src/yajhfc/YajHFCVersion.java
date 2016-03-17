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
package yajhfc;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jonas
 *
 */
public class YajHFCVersion implements Comparable<YajHFCVersion> {
    private static final Logger log = Logger.getLogger(YajHFCVersion.class.getName());
    
    private int major = -1, minor = -1, revision = 0;
    private String additionString = "";
    private int additionInt = 0, additionNumber = 0;
    private String stringVersion;
        
    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    /**
     * Returns the addition such as "alpha" or "beta"
     * @return
     */
    public String getAdditionString() {
        return additionString;
    }

    /**
     * Returns an integer useful for comparisons between different additions.
     * @return
     */
    public int getAdditionInt() {
        return additionInt;
    }

    public int getAdditionNumber() {
        return additionNumber;
    }

    public int compareTo(YajHFCVersion o) {
        int rv;
        rv = major - o.major;
        if (rv == 0) {
            rv = minor - o.minor;
            if (rv == 0) {
                rv = revision - o.revision;
                if (rv == 0) {
                    rv = additionInt - o.additionInt;
                    if (rv == 0) {
                        rv = additionNumber - o.additionNumber;  
                    }                    
                }
            }
        }
        return rv;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof YajHFCVersion)
            return compareTo((YajHFCVersion)obj)==0;
        else
            return false;
    }
    
    @Override
    public int hashCode() {
        return (((((major * 10) + minor) * 10) + revision) * 10 + additionInt) * 10 + additionNumber; 
    }
    
    @Override
    public String toString() {
        //return "" + major + '.' + minor + '.' + revision + additionString + additionNumber;
        return stringVersion;
    }
    
    public YajHFCVersion() {
        this(VersionInfo.AppVersion);
    }
    
    public YajHFCVersion(String stringVersion) {
        this.stringVersion = stringVersion;
        parseVersion(stringVersion);
    }
    
    /*
     * Version pattern for strings like 1.2.4alpha10
     */
    private static final Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?(\\D*)(\\d*)");

    private void parseVersion(String stringVersion) {        
        Matcher m = versionPattern.matcher(stringVersion);
        if (m.matches()) {
            try {
                major = Integer.parseInt(m.group(1));
                minor = Integer.parseInt(m.group(2));
                String group3 = m.group(3);

                if (group3 != null && group3.length() > 0 && Character.isDigit(group3.charAt(0))) {
                    revision = Integer.parseInt(group3);
                } 

                String group4 = m.group(4);
                additionString = group4;
                if (group4 == null || group4.length() == 0) { // Release
                    additionInt = 10;
                } else if (group4.equalsIgnoreCase("alpha")) {
                    additionInt = -30;
                } else if (group4.equalsIgnoreCase("beta")) {
                    additionInt = -20;
                } else if (group4.equalsIgnoreCase("rc")) {
                    additionInt = -10;
                } else if (group4.length() == 1) { // a, b, c, ...
                    additionInt = Character.toLowerCase(group4.charAt(0));
                } else {
                    additionInt = -40;
                }

                String group5 = m.group(5);
                if (group5 != null && group5.length() > 0) {
                    additionNumber = Integer.parseInt(group5);
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error parsing the version " + stringVersion, e);
            }
        }
    }
   
}
