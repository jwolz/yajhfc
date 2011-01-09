/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2011 Jonas Wolz
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

import javax.swing.JComponent;

import yajhfc.FaxOptions;

class OptionsPageWrapper<T> implements OptionsPage<FaxOptions> {
    protected OptionsPage<T> realPage;
    protected T options;
    protected Callback<T> callback;
    
    public OptionsPage<T> getRealPage() {
        return realPage;
    }
    
    public T getOptions() {
        return options;
    }
    
    public JComponent getPanel() {
        return realPage.getPanel();
    }
    
    public void loadSettings(FaxOptions foEdit) {
        // NOP
    }
    public void saveSettings(FaxOptions foEdit) {
        callback.saveSettingsCalled(this, foEdit);
    }
    public boolean validateSettings(OptionsWin optionsWin) {
        return callback.validateSettingsCalled(this, optionsWin);
    }
    
    public boolean pageIsHidden(OptionsWin optionsWin) {
        if (!realPage.validateSettings(optionsWin))
            return false;
        realPage.saveSettings(options);
        callback.elementSaved(this);
        return true;
    }
    
    public void pageIsShown(OptionsWin optionsWin) {
        realPage.loadSettings(options);
    }

    public OptionsPageWrapper(OptionsPage<T> realPage, T options, Callback<T> callback) {
        super();
        this.realPage = realPage;
        this.options = options;
        this.callback = callback;
    }

    public void initializeTreeNode(PanelTreeNode node, FaxOptions foEdit) {
        // NOP
    }
    
    public interface Callback<T> {
        public void elementSaved(OptionsPageWrapper<T> source);
        public void saveSettingsCalled(OptionsPageWrapper<T> source, FaxOptions foEdit);
        public boolean validateSettingsCalled(OptionsPageWrapper<T> source, OptionsWin optionsWin);
    }
}