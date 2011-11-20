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
package yajhfc.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

/**
 * A Component drawing a color with alpha transparency over its backgriund
 * @author jonas
 *
 */
public class AlphaPanel extends JComponent {

    protected static final String DEFAULT_COLOR = "controlShadow";
    
    protected Color alphaColor;
    protected float transparency;
    protected AlphaComposite alphaComposite = null;
    
    public AlphaPanel(Color alphaColor, float transparency) {
        this.alphaColor = alphaColor;
        this.transparency = transparency;
        
        setOpaque(false);
    }
    
    public AlphaPanel() {
        this(UIManager.getColor(DEFAULT_COLOR), 0.25f);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D)g;
        
        if (alphaComposite == null) {
            alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
        }
        Composite oldCs = g2.getComposite();
        
        g2.setComposite(alphaComposite);
        g2.setColor(alphaColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setComposite(oldCs);
    }

    @Override
    public void updateUI() {
        if (alphaColor instanceof UIResource) {
            setAlphaColor(UIManager.getColor(DEFAULT_COLOR));
        }
        super.updateUI();
    }
    
    public Color getAlphaColor() {
        return alphaColor;
    }

    public void setAlphaColor(Color alphaColor) {
        this.alphaColor = alphaColor;
        alphaComposite = null;
        repaint();
    }

    public float getTransparency() {
        return transparency;
    }

    public void setTransparency(float transparency) {
        this.transparency = transparency;
        alphaComposite = null;
        repaint();
    }
}
