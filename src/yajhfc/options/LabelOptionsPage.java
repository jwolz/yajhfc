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
 */
package yajhfc.options;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;

import yajhfc.FaxOptions;

/**
 * A pseudo options page showing a centered text.
 * 
 * @author jonas
 *
 */
public class LabelOptionsPage extends JLabel implements OptionsPage<FaxOptions> {

    public LabelOptionsPage(String text) {
        super(text);
        initialize();
    }

    private void initialize() {
        this.setHorizontalAlignment(JLabel.CENTER);
        this.setVerticalAlignment(JLabel.CENTER);
        this.setHorizontalTextPosition(JLabel.CENTER);
        this.setVerticalTextPosition(JLabel.CENTER);
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.setAlignmentY(Component.CENTER_ALIGNMENT);
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#getPanel()
     */
    public JComponent getPanel() {
        return this;
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        // NOP
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        // NOP
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        return true;
    }

    public boolean pageIsHidden(OptionsWin optionsWin) {
        return true;
    }
    
    public void pageIsShown(OptionsWin optionsWin) {
        // NOP
    }
    
    public void initializeTreeNode(PanelTreeNode node, FaxOptions foEdit) {
        // NOP
    };
}
