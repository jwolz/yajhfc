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

import java.awt.Frame;
import java.io.PrintWriter;
import java.io.StringWriter;

import yajhfc.launch.Launcher2;

/**
 * Java 1.1 compatible launcher to show a nice error message when Java is too old
 * @author jonas
 *
 */
public class Launcher {

    /**
     * @param args
     */
    public static void main(String[] args) {
        boolean docheck = true;
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("--no-check")) {
                docheck = false;
                break;
            }
        }

        if (docheck) {
            JavaVersionParser jVersion = new JavaVersionParser();

            if (jVersion.isLessThan(1, 5)) {
                showMessage("You need at least Java 1.5 (Java 5) to run YajHFC.\nThe installed version is " + jVersion + ".", "Error");
                System.exit(1);
            }

            String vmName = System.getProperty("java.vm.name");
            if (vmName != null && vmName.indexOf("gcj") >= 0) {
                showMessage("You are apparently using GNU gcj/gij to run YajHFC.\nRunning YajHFC with gcj is not recommended and may cause problems.\n\nNote: You may use the --no-check command line parameter to suppress this warning.", "Warning");
            }
        }

        try {
            startRealLauncher(args);
        } catch (Throwable t) {
            System.err.print("Error starting YajHFC:\n");
            t.printStackTrace();
            
            StringWriter writer = new StringWriter();
            writer.write("Error starting YajHFC:\n");
            t.printStackTrace(new PrintWriter(writer));
            showMessage(writer.toString(), "Error");
            
            System.exit(1);
        }
    }
    
    public static void startRealLauncher(String[] args) {
        Launcher2.main(args);
    }
    
    public static void showMessage(String message, String title) {
        try {
            Frame msgFrame = new Frame();
            AWTMessageBox msgBox = new AWTMessageBox(msgFrame, title);
            msgBox.showMsgBox(message);
            msgFrame.dispose();
        } catch (Exception ex) {
            System.err.println(message);
        }
    }
}
