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
package yajhfc.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.launch.Launcher2;

/**
 * @author jonas
 *
 */
public class ExternalProcessExecutor extends Thread {
    private static final Logger log = Logger.getLogger(ExternalProcessExecutor.class.getName());
    
    private Process process;
    private String commandName;
    
    /**
     * Starts the process specified by the command line
     * @param commandLine
     * @throws IOException
     */
    public ExternalProcessExecutor(List<String> commandLine) throws IOException {
        quoteCommandLine(commandLine);
        commandName = commandLine.get(0);
        setDaemon(true);
        setName(commandName + " executor");
        setPriority(MIN_PRIORITY);
        
        process = new ProcessBuilder(commandLine).redirectErrorStream(true).start();
        start();
    }

    @Override
    public void run() {
        try {
            process.getOutputStream().close();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(commandName + " output: " + line);
            }
            reader.close();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warning(commandName + " did not execute successfully (exitCode=" + exitCode + ").");
            } else if (Utils.debugMode) {
                log.info(commandName +  " executed successfully.");
            }
        } catch (Exception e) {
            Launcher2.application.getDialogUI().showExceptionDialog(MessageFormat.format(Utils._("Error executing {0}:"), commandName), e);
        }
    }
    
    public static void executeProcess(String commandLine) throws IOException {
        executeProcess(splitCommandLine(commandLine));
    }
    public static void executeProcess(String... commandLine) throws IOException {
        executeProcess(Arrays.asList(commandLine));
    }
    
    public static void executeProcess(List<String> commandLine) throws IOException {
        new ExternalProcessExecutor(commandLine);
    }
    
    /**
     * Splits a command line string and returns a list of arguments suitable
     * for a process builder
     * @param str
     * @return
     */
    public static List<String> splitCommandLine(String str) {
    	return splitCommandLine(str, !Utils.IS_WINDOWS);
    }
    
    private static final int STATE_NORMAL = 0;
    private static final int STATE_DQUOTE = 1;
    private static final int STATE_SQUOTE = 2;
    private static final int STATE_WHITESPACE = 3;
    /**
     * Splits a command line string and returns a list of arguments suitable
     * for a process builder
     * @param str the command line to split
     * @param stripQuotes whether to strip quotes from the arguments
     * @return
     */
    public static List<String> splitCommandLine(String str, boolean stripQuotes) {
        List<String> result = new ArrayList<String>();
        int state = STATE_NORMAL;
        int argStart = 0;
        str = str.trim();
        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            switch (state) {
            case STATE_NORMAL:
                switch (c) {
                case '\'':
                    state = STATE_SQUOTE;
                    break;
                case '\"':
                    state = STATE_DQUOTE;
                    break;
                case ' ':
                    String res = str.substring(argStart, i);
                    if (stripQuotes) {
                        res = Utils.stripQuotes(res);
                    }
                    result.add(res);
                    state = STATE_WHITESPACE;
                    break;
                default: // Do nothing
                    break;
                }
                break;
            case STATE_DQUOTE:
                if (c == '\"') {
                    state = STATE_NORMAL;
                }
                break;
            case STATE_SQUOTE:
                if (c == '\'') {
                    state = STATE_NORMAL;
                }
                break;
            case STATE_WHITESPACE:
                switch (c) {
                case ' ':
                    break;
                case '\'':
                    argStart = i;
                    state = STATE_SQUOTE;
                    break;
                case '\"':
                    argStart = i;
                    state = STATE_DQUOTE;
                    break;
                default:
                    argStart = i;
                    state = STATE_NORMAL;
                    break;
                }
                break;
            }
        }
        if (argStart < str.length() - 1) {
            String res = str.substring(argStart);
            if (stripQuotes) {
                res = Utils.stripQuotes(res);
            }
            result.add(res);
        }
        
        if (Utils.debugMode) {
            log.fine("Result from parsing command line «" + str + "»:");
            for (int i = 0; i < result.size(); i++) {
                log.fine("" + i + ": «" + result.get(i) + '»');
            }
        }
        
        return result;
    }
    
    /**
     * Adds quotes to the command line parameters in the list if necessary
     * @param commandLine
     */
    public static void quoteCommandLine(List<String> commandLine) {
        if (Utils.IS_WINDOWS) { // Another special case for Windows...
            ListIterator<String> it = commandLine.listIterator();
            while (it.hasNext()) {
                String curArg = it.next();
                if (curArg.indexOf(' ') >= 0) {
                    final char f = curArg.charAt(0);
                    final char l = curArg.charAt(curArg.length()-1);
                    if (!(f == l && (f == '\"' || f == '\''))) {
                        // If the argument contains a space *and* is not already
                        // quoted, add quotes around it
                        it.set("\"" + curArg + "\"");
                    }    
                }
            }
            
        }
    }
}
