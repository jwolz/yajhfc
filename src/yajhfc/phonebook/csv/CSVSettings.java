package yajhfc.phonebook.csv;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import yajhfc.phonebook.GeneralConnectionSettings;

public class CSVSettings extends GeneralConnectionSettings {
    private static final Logger log = Logger.getLogger(CSVSettings.class.getName());
    
    public String fileName;
    public String charset = Charset.defaultCharset().name();
    public String separator = ";";
    public boolean firstLineAreHeaders = true;
    public String quoteChar = "\"";
    public boolean overwrite = false;
    public String displayCaption = "";
    
    /**
     * Creates a CSV reader configured with this settings
     * @return
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    public CSVReader createReader() throws UnsupportedEncodingException, FileNotFoundException {
        return new CSVReader(new InputStreamReader(new FileInputStream(fileName), charset),
                separator.charAt(0), quoteChar.charAt(0));
    }
    
    /**
     * Creates a CSV writer configured with this settings
     * @return
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     */
    public CSVWriter createWriter() throws UnsupportedEncodingException, FileNotFoundException {
        return new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileName), charset),
                separator.charAt(0), quoteChar.charAt(0));
    }
    
    /**
     * Try to auto-detect correct settings (if fileName is set)
     * @param settings
     */
    public void tryAutodetect() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset));
            String firstLine = reader.readLine();
            reader.close();
            
            if (firstLine != null) {
                // Try to find out correct separator and quote char by analyzing the first line:
                //  Assume that the most frequent character is the correct separator/quote char
                int dQCount = 0, sQCount = 0;
                int tabCount = 0, commaCount = 0, semicolCount = 0;
                for (int i = 0; i < firstLine.length(); i++) {
                    switch (firstLine.charAt(i)) {
                    case '\'':
                        sQCount++;
                        break;
                    case '"':
                        dQCount++;
                        break;
                    case '\t':
                        tabCount++;
                        break;
                    case ';':
                        semicolCount++;
                        break;
                    case ',':
                        commaCount++;
                        break;
                    }
                }

                if (tabCount > commaCount) {
                    if (tabCount > semicolCount) { // TAB > ; > ,
                        separator = "\t";
                    } else { // ; >= TAB > ,
                        separator = ";";
                    }
                } else {
                    if (semicolCount > commaCount) { // ; > , >= TAB
                        separator = ";";
                    } else { // , >= ; >= TAB
                        separator = ",";
                    }
                }
                
                quoteChar = (sQCount > dQCount) ? "'" : "\"";
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Auto-detect failed:", ex);
        }
    }
}
