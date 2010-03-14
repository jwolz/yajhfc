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
import java.awt.SplashScreen;
import java.io.IOException;
import java.net.URL;


public class Java6YJSplashScreen extends YJSplashScreen {
    protected final SplashScreen javaSplashScreen;

    public void close() throws IllegalStateException {
        javaSplashScreen.close();
    }

    public Graphics2D createGraphics() throws IllegalStateException {
        return javaSplashScreen.createGraphics();
    }

    public Rectangle getBounds() throws IllegalStateException {
        return javaSplashScreen.getBounds();
    }

    public URL getImageURL() throws IllegalStateException {
        return javaSplashScreen.getImageURL();
    }

    public Dimension getSize() throws IllegalStateException {
        return javaSplashScreen.getSize();
    }

    public int hashCode() {
        return javaSplashScreen.hashCode();
    }

    public boolean isVisible() {
        return javaSplashScreen.isVisible();
    }

    public void setImageURL(URL imageURL) throws NullPointerException,
            IOException, IllegalStateException {
        javaSplashScreen.setImageURL(imageURL);
    }

    public void update() throws IllegalStateException {
        javaSplashScreen.update();
    }
    
    @Override
    protected boolean haveSplashscreen() {
        return (javaSplashScreen != null);
    }

    public Java6YJSplashScreen() {
        this(SplashScreen.getSplashScreen());
    }
    
    public Java6YJSplashScreen(SplashScreen javaSplashScreen) {
        super();
        this.javaSplashScreen = javaSplashScreen;
    }
}
