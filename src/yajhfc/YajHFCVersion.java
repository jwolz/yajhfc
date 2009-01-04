/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
    public String toString() {
        //return "" + major + '.' + minor + '.' + revision + additionString + additionNumber;
        return stringVersion;
    }
    
    public YajHFCVersion() {
        this(Utils.AppVersion);
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
