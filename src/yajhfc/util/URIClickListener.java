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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.DesktopManager;

/**
 * @author jonas
 *
 */
public class URIClickListener implements MouseListener {

    protected URI uri;

    public URIClickListener(String uri) {
        super();
        try {
            this.uri = new URI(uri);
        } catch (URISyntaxException e) {
            Logger.getLogger(URIClickListener.class.getName()).log(Level.SEVERE, "Invalid URI:", e);
        }
    }
    
    public URIClickListener(URI uri) {
        super();
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }
    
    public void setUri(URI uri) {
        this.uri = uri;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        DesktopManager.getDefault().safeBrowse(uri, e.getComponent());
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
        // NOP
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
        // NOP
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        // NOP
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        // NOP
    }

}
