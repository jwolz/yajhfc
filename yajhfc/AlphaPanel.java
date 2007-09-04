/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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
