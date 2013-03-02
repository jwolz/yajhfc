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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yajhfc.util.ArrayCharSequence;

/**
 * @author jonas
 *
 */
public class FileUtils {
    private static Logger log = Logger.getLogger(FileUtils.class.getName());
            
    /**
     * Extracts the document title from the specified PostScript File
     * @return
     */
    public static String extractTitleFromPSFile(File psFile) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(psFile), "ISO8859-1");
        char[] buf = new char[4000];
        
        int size = reader.read(buf);
        reader.close();
        
        Pattern pattern = Pattern.compile("^%%Title:\\s*(.+)$", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
        ArrayCharSequence acs = new ArrayCharSequence(buf, 0, size);
        String result = null;
        Matcher m = pattern.matcher(acs);
        if (m.find()) {
            result = m.group(1);
            result = decodePSString(result);
        }
        return result;
    }
    
    /**
     * Decodes the specified title
     * @param title
     * @return
     */
    public static String decodePSString(String title) {
        title = title.trim();
        if (title.length() == 0)
            return title;
        
        if (title.charAt(0) == '(' && title.charAt(title.length()-1) == ')') {
            title = title.substring(1, title.length()-1);
            Pattern escape = Pattern.compile("\\\\(?:(\\d{3})|(.))");
            
            Matcher m = escape.matcher(title);
            StringBuilder res = new StringBuilder();
            int start = 0;
            while (m.find(start)) {
                res.append(title.substring(start, m.start()));
                if (m.group(2) != null) {
                    char c = m.group(2).charAt(0);
                    switch (c) {
                    case 'n':
                    case 'r':
                    case 'f':
                    case 'b':
                        res.append(' ');
                        break;
                    case 't':
                        res.append("    ");
                        break;
                    default:
                        res.append(c);
                        break;
                    }
                } else if (m.group(1) != null) {
                    try {
                        int code = Integer.valueOf(m.group(1), 8);
                        if (code < 0 || code > 255) {
                            log.fine("Invalid codepoint: " + code);
                            res.append('?');
                        } else {
                            res.append(decode_ISO_8859_1(code));
                        }
                    } catch (NumberFormatException e) {
                        log.fine("Invalid octal code: " + m.group(1));
                        res.append('#');
                    }
                }
                start = m.end();
            }
            if (start < title.length()) {
                res.append(title.substring(start));
            }
            return res.toString();
        } else {
            return title;
        }
    }
    
    /**
     * Decodes the specified ISO8859-1 character
     * @param code
     * @return
     */
    public static char decode_ISO_8859_1(int code) {
        return ISO8859_1_Table.DECODE_TABLE[code];
    }
    
    private static class ISO8859_1_Table {
        public static final char[] DECODE_TABLE;
        
        static {
            // Build decoding table
            byte[] bytes = new byte[256];
            for (int i=0; i<256; i++) {
                bytes[i] = (byte)i;
            }
            CharBuffer decoded = Charset.forName("ISO8859-1").decode(ByteBuffer.wrap(bytes));
            decoded.get(DECODE_TABLE = new char[256]);
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        File[] psFiles = new File("/home/jonas/java/yajhfc/extracttitle").listFiles();
        
        for (File f : psFiles) {
            System.out.println("" + f + ": " + extractTitleFromPSFile(f));
        }
    }
}
