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

import static yajhfc.Utils._;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

import yajhfc.FaxOptions;
import yajhfc.model.FmtItem;
import yajhfc.model.FmtItemDescComparator;
import yajhfc.model.ui.FmtItemRenderer;
import yajhfc.util.fmtEditor;

/**
 * @author jonas
 *
 */
public class FmtEditorPanel<T extends FmtItem> implements OptionsPage {

    protected List<T> selection;
    protected fmtEditor<T> fmtEditor;
    protected final T[] values;
    protected final Field option;
    
    public FmtEditorPanel(T[] values, String optionName) {
        this.values = values;
        try {
            this.option = FaxOptions.class.getField(optionName);
        } catch (Exception e) {
            throw new RuntimeException("Field not found", e);
        } 
    }
    
    public JComponent getPanel() {
        if (fmtEditor == null) {
            selection = new ArrayList<T>();
            fmtEditor = new fmtEditor<T>(values, selection, Collections.<T>emptyList(), new FmtItemRenderer(), FmtItemDescComparator.<T>getInstance(), null, _("Selected columns:"), _("Available columns:")); //Arrays.asList(Utils.requiredRecvFmts));
        }
        return fmtEditor;
    }

    @SuppressWarnings("unchecked")
    private List<T> getOptionField(FaxOptions foEdit) {
        try {
            return (List<T>)option.get(foEdit);
        } catch (Exception e) {
            throw new RuntimeException("Error getting FaxOption property", e);
        } 
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        fmtEditor.setNewSelection(getOptionField(foEdit));
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        List<T> optionField = getOptionField(foEdit);
        optionField.clear();
        optionField.addAll(selection);
    }

    public boolean validateSettings(OptionsWin optionsWin) {
        return true;
    }

}
