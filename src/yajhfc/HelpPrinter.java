/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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

import gnu.getopt.LongOpt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Class to generate the text for the "--help" command line option
 * 
 * @author jonas
 *
 */
public class HelpPrinter {
    /**
     * The minimum value of a "real" short option
     */
    private static final int MIN_OPT_VAL = 'A';
    
    private static final ResourceBundle resources = ResourceBundle.getBundle("yajhfc.i18n.CommandLineOpts");
    
    public static String getResource(String key) {
        try {
            return resources.getString(key);
        } catch (Exception ex) {
            return key;
        }
    }
    
    public static String getDescription(LongOpt option) {
        return getResource(option.getName() + "-desc");
    }
    
    public static String getArgDesc(LongOpt option) {
        return getResource(option.getName() + "-arg");
    }
    
    /**
     * Prints usage information
     * @param out
     * @throws IOException 
     */
    public static void printHelp(PrintWriter out, LongOpt[] options, String cols) {
        int screenWidth = 80;
        if (cols != null) {
            try {
                screenWidth = Integer.parseInt(cols);
                if (screenWidth < 40) {
                    screenWidth = 40;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid number of columns: " + cols);
            }
        }
        
        String[] argDescs = new String[options.length]; // Cache for argument descriptions (we need them twice)
        
        int optionwidth = 0;
        for (int i = 0; i < options.length; i++) {
            final LongOpt option = options[i];
            
            int argwidth = 0;
            
            switch (option.getHasArg()) { // Fall throughs are intended here
            case LongOpt.OPTIONAL_ARGUMENT:
                argwidth += 2;
            case LongOpt.REQUIRED_ARGUMENT:
                argwidth += (argDescs[i] = getArgDesc(option)).length()+1;
            case LongOpt.NO_ARGUMENT:
                argwidth += option.getName().length();
            }

            if (argwidth > optionwidth) {
                optionwidth = argwidth;
            }
        }
        optionwidth += 1;
        out.append(getResource("usage")).println(':');
        out.append("java -jar yajhfc.jar [").append(getResource("options")).
            append("]... [").append(getResource("files-to-send")).println("]...");
        out.println();
        out.append(getResource("argument-description")).println(':');
        for (int i = 0; i < options.length; i++) {
            final LongOpt option = options[i];

            if (option.getVal() < MIN_OPT_VAL) {
                out.append("  ");
            } else {
                out.append('-');
                out.append((char)option.getVal());
            }
            if (option.getName() == null) {
                appendSpaces(out, optionwidth + 4);
            } else {
                if (option.getVal() < MIN_OPT_VAL) {
                    out.append("  --");
                } else {
                    out.append(", --");
                }
                out.append(option.getName());
                
                final int hasArg = option.getHasArg();
                if (hasArg != LongOpt.NO_ARGUMENT) {
                    if (hasArg == LongOpt.OPTIONAL_ARGUMENT) {
                        out.append('[');
                    }
                    out.append('=').append(argDescs[i]);
                    if (hasArg == LongOpt.OPTIONAL_ARGUMENT) {
                        out.append(']');
                    }
                    appendSpaces(out, optionwidth-option.getName().length()-argDescs[i].length()-((hasArg == LongOpt.OPTIONAL_ARGUMENT) ? 3 : 1));
                } else {
                    appendSpaces(out, optionwidth-option.getName().length());
                }
            }
            printWrapped(out, optionwidth + 6, getDescription(option), screenWidth);
            //out.append('\n');
            out.println();
        }
        
        out.flush();
    }
    
    private static void appendSpaces(PrintWriter out, int numspaces) {
        for (int i=0; i<numspaces; i++) {
            out.append(' ');
        }
    }
    
    private static final Pattern wordSplitter = Pattern.compile("(\\s|\n)+");
    private static void printWrapped(PrintWriter out, int indent, String text, int screenWidth) {
        int pos = indent;

        for (String word: wordSplitter.split(text)) {
            pos += word.length();
            if (pos >= screenWidth) {
                //out.append('\n');
                out.println();
                appendSpaces(out, indent);
                pos = indent + word.length();
            }
            out.append(word);
            if (pos < screenWidth-1) {
                out.append(' ');
                pos+=1;
            }
        }
    }
    
}
