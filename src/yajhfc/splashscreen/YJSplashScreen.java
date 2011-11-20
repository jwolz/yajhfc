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
package yajhfc.splashscreen;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jonas
 *
 */
public abstract class YJSplashScreen {

    /**
     * Close the splash screen.
     * @throws IllegalStateException
     */
    public abstract void close() throws IllegalStateException;

    public abstract void setImageURL(URL imageURL) throws NullPointerException, IOException, IllegalStateException;

    public abstract URL getImageURL() throws IllegalStateException;

    public abstract Rectangle getBounds() throws IllegalStateException;

    public abstract Dimension getSize() throws IllegalStateException;

    public abstract Graphics2D createGraphics() throws IllegalStateException;

    public abstract void update() throws IllegalStateException;

    public abstract boolean isVisible();
    
    /**
     * Returns true if a valid splash screen implementation has been found.
     * @return
     */
    protected abstract boolean haveSplashscreen();
    
    
    ////////////////////////////////////////////////////////////
    // Static methods:
    ////////////////////////////////////////////////////////////
    
    private static final String IMPL_CLASS_NAME = "yajhfc.splashscreen.Java6YJSplashScreen";
    
    private static boolean triedLoad = false;
    private static YJSplashScreen splashScreenImpl;
    /**
     * Returns the splash screen or null if it is not available.
     * @return
     */
    public static YJSplashScreen getSplashScreen() {
        if (!triedLoad) {
            final Logger log = Logger.getLogger(YJSplashScreen.class.getName());
            try {
                Class<?> splashScreenImplClass = Class.forName(IMPL_CLASS_NAME);
                splashScreenImpl = (YJSplashScreen)splashScreenImplClass.newInstance();
                if (!splashScreenImpl.haveSplashscreen()) {
                    log.fine("No valid splash screen found (splashScreenImpl.haveSplashscreen() == false).");
                    splashScreenImpl = null;
                }
            } catch (Exception ex) {
                log.log(Level.FINE, "Could not create a splash screen implementation.", ex);
                splashScreenImpl = null;
            }
            triedLoad = true;
        }
        return splashScreenImpl;
    }
}
