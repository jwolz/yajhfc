package yajhfc.filters.ui;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import yajhfc.Utils;
import yajhfc.filters.Filter;
import yajhfc.filters.FilterCreator;
import yajhfc.filters.FilterKey;
import yajhfc.filters.FilterKeyList;
import yajhfc.filters.FilterableObject;
import yajhfc.model.FmtItem;
import yajhfc.model.ui.FmtItemRenderer;
import yajhfc.util.ClipboardPopup;

public class FilterPanel<V extends FilterableObject,K extends FilterKey> extends JPanel implements ActionListener {

    private JComboBox comboColumns, comboOperator;
    private JTextField textValue;
    private JButton buttonDelete;
    private JCheckBox checkCaseSensitive;
    private DefaultComboBoxModel colModel;
    private Class<?> oldClass;
    
    private static final FmtItem voidFmtItem = new FmtItem() {
        private final String desc = Utils._("(none)");
        
        public String getDescription() {
            return desc;
        }

        public DateFormat getDisplayDateFormat() {
            return null;
        }

        public DateFormat getHylaDateFormat() {
            return null;
        }

        public String getHylaFmt() {
            return "";
        }

        public String getLongDescription() {
            return desc;
        }

        public Class<?> getDataType() {
            return Void.class;
        }
        
        public String name() {
            return "void";
        }
        
        public Format getFormat() {
            return null;
        }
        
        @Override
        public String toString() {
            return desc;
        }
    }; 
    private static final String[] comboOperatorDummy = { "                         " };
    
    public void addDeleteActionListener(ActionListener al) {
        buttonDelete.addActionListener(al);
    }
    
    public void removeDeleteActionListener(ActionListener al) {
        buttonDelete.removeActionListener(al);
    }
    
    public boolean isDeleteEnabled() {
        return buttonDelete.isEnabled();
    }
    
    public void setDeleteEnabled(boolean value) {
        buttonDelete.setEnabled(value);
    }
    
   public void setDeleteActionCommand(String cmd) {
        buttonDelete.setActionCommand(cmd);
    }
    
    public void initFromFilter(Filter<V,K> filter) {
        if (filter == null)
            return;
        
        K col = FilterCreator.columnFromFilter(filter);
        if (col != null) {
            comboColumns.setSelectedItem(col);
            Object op = FilterCreator.operatorFromFilter(filter);
            if (op != null) {
                comboOperator.setSelectedItem(op);
                if (FilterCreator.isInputEnabled(col.getDataType()))
                    textValue.setText(FilterCreator.inputFromFilter(filter));
                else
                    textValue.setText("");
                checkCaseSensitive.setEnabled(FilterCreator.isCaseSensitiveEnabled(col.getDataType()));
                checkCaseSensitive.setSelected(FilterCreator.getIsCaseSensitive(filter));
            }
        } else
            comboColumns.setSelectedIndex(0);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("columnsel")) {
            Class<?> colClass = ((FilterKey)comboColumns.getSelectedItem()).getDataType();
            if (oldClass != colClass) {
                oldClass = colClass;
                Object [] ops = FilterCreator.getOperators(colClass);
                if (ops == null) {
                    comboOperator.setEnabled(false);
                    textValue.setEnabled(false);
                } else {
                    comboOperator.setEnabled(true);
                    comboOperator.setModel(new DefaultComboBoxModel(ops));
                    textValue.setEnabled(FilterCreator.isInputEnabled(colClass));
                    checkCaseSensitive.setEnabled(FilterCreator.isCaseSensitiveEnabled(colClass));
                }
            }
        }
    }
    
    public void focusInput() {
        textValue.selectAll();
        textValue.requestFocus();
    }
    
    @SuppressWarnings("unchecked")
    public Filter<V,K> getFilter() throws ParseException {
        return FilterCreator.getFilter((K)comboColumns.getSelectedItem(), comboOperator.getSelectedItem(), textValue.getText(), checkCaseSensitive.isSelected());
    }
    
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
    
    public FilterPanel(FilterKeyList<? extends K> columns) {
        super(null, false);
        
        final int border = 12;
        double[][] dLay = {
                { TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border, TableLayout.PREFERRED },
                { TableLayout.PREFERRED, border/2, TableLayout.PREFERRED}
        };
        
        setLayout(new TableLayout(dLay));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(border, border, border, border)));
        
        final K[] availableKeys=columns.getAvailableKeys();
        colModel = new DefaultComboBoxModel(availableKeys);
        colModel.insertElementAt(voidFmtItem, 0);
        comboColumns = new JComboBox(colModel);
        // Assign a special renderer for FmtItems
        if (FmtItem.class.isAssignableFrom(availableKeys.getClass().getComponentType())) {
            comboColumns.setRenderer(new FmtItemRenderer());
        }
        comboColumns.setActionCommand("columnsel");
        comboColumns.addActionListener(this);
        
        comboOperator = new JComboBox(comboOperatorDummy);
        comboOperator.setEnabled(false);
        Dimension dim = new Dimension(comboOperator.getPreferredSize());
        comboOperator.setMaximumSize(dim);
        comboOperator.setMinimumSize(dim);
        comboOperator.setPreferredSize(dim);
        
        textValue = new JTextField(20);
        textValue.setEnabled(false);
        textValue.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        checkCaseSensitive = new JCheckBox(Utils._("Case sensitive"));
        checkCaseSensitive.setEnabled(false);
        
        buttonDelete = new JButton(Utils.loadIcon("general/Delete"));
        buttonDelete.setToolTipText(Utils._("Remove this condition"));
        
        add(comboColumns,  "0,0,f,f");
        add(comboOperator, "2,0,f,f");
        add(textValue,     "4,0,6,0,f,f");
        add(checkCaseSensitive, "0,2,4,2,f,f");
        add(buttonDelete,  "6,2,f,f");
        
        comboColumns.setSelectedIndex(0);
    }

    public FilterPanel(FilterKeyList<K> columns, Filter<V,K> initValue) {
        this(columns);
        initFromFilter(initValue);
    }
}
