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
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A Component drawing a color with alpha transparency over its backgriund
 * @author jonas
 *
 */
public class AlphaPanel extends JComponent {

    protected Color alphaColor;
    protected float transparency;
    
    public AlphaPanel(Color alphaColor, float transparency) {
        this.alphaColor = alphaColor;
        this.transparency = transparency;
        
        setOpaque(false);
    }
    
    public AlphaPanel() {
        this(UIManager.getColor("controlShadow"), 0.25f);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g2.setColor(alphaColor);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
    }

    public Color getAlphaColor() {
        return alphaColor;
    }

    public void setAlphaColor(Color alphaColor) {
        this.alphaColor = alphaColor;
        repaint();
    }

    public float getTransparency() {
        return transparency;
    }

    public void setTransparency(float transparency) {
        this.transparency = transparency;
        repaint();
    }
}
