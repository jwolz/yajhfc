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
package yajhfc.options;

import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author jonas
 *
 */
public abstract class AbstractOptionsPanel extends JPanel implements OptionsPage {

    public AbstractOptionsPanel(LayoutManager layout) {
        super(layout);
    }

    public AbstractOptionsPanel(boolean isDoubleBuffered) {
        super(null, isDoubleBuffered);
    }

    public AbstractOptionsPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    protected boolean uiCreated = false;
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#getPanel()
     */
    public JComponent getPanel() {
        if (!uiCreated) {
            createOptionsUI();
            uiCreated = true;
        }
        return this;
    }
    
    /**
     * Creates the actual UI. This class makes sure it only gets called once.
     */
    protected abstract void createOptionsUI();
    

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        return true;
    }

}
