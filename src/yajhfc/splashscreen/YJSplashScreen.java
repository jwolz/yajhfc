/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
