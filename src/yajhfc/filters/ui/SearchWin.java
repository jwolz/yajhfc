package yajhfc.filters.ui;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2007 Jonas Wolz
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
import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import yajhfc.Utils;
import yajhfc.filters.Filter;
import yajhfc.filters.FilterCreator;
import yajhfc.filters.FilterKey;
import yajhfc.filters.FilterableObject;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExceptionDialog;

/**
 * A  general search window for the filter classes
 * @author jonas
 *
 */
public abstract class SearchWin<T extends FilterableObject,S extends FilterKey> extends JDialog implements ActionListener {

    protected JPanel myContentPane;
    protected JButton buttonSearch, buttonClose;
    protected JComboBox comboFields, comboOp;
    protected JTextField textCondition;
    protected JRadioButton radioForward, radioBackward;
    protected JCheckBox checkCaseSensitive, checkWrapAround;
    protected ButtonGroup groupDirection;
    protected Class<?> oldDataClass;
    
    private static final int border = 10;
    
    /**
     * Performs the search operation with the selected filter
     * @param selectedFilter
     */
    protected abstract void performSearch(Filter<T,S> selectedFilter, boolean searchBackwards, boolean wrapAroundSearch);
    
    /**
     * Returns an array of the available fields
     * @return
     */
    protected abstract S[] getAvailableFields();
    
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("search")) {            
            try {
                Filter<T,S> filter =
                        FilterCreator.<T,S>getFilter(
                                (S)comboFields.getSelectedItem(),
                                comboOp.getSelectedItem(),
                                textCondition.getText(),
                                checkCaseSensitive.isSelected());
//                new StringFilter<T, S>(
//                        (S)comboFields.getSelectedItem(),
//                        (StringFilterOperator)comboOp.getSelectedItem(),
//                        textCondition.getText(),
//                        checkCaseSensitive.isSelected());
                performSearch(filter, radioBackward.isSelected(), checkWrapAround.isSelected());
            } catch (ParseException e1) {
                ExceptionDialog.showExceptionDialog(this, Utils._("Please enter a valid compare value."), e1);
            }
        } else if (cmd.equals("fieldsel")) {
            Class<?> colClass = ((FilterKey)comboFields.getSelectedItem()).getDataType();
            if (oldDataClass != colClass) {
                oldDataClass = colClass;
                Object[] ops = FilterCreator.getOperators(colClass);
                if (ops == null) {
                    comboOp.setEnabled(false);
                    textCondition.setEnabled(false);
                } else {
                    comboOp.setEnabled(true);
                    comboOp.setModel(new DefaultComboBoxModel(ops));
                    textCondition.setEnabled(FilterCreator.isInputEnabled(colClass));
                    checkCaseSensitive.setEnabled(FilterCreator.isCaseSensitiveEnabled(colClass));
                }
            }
        }
    }
    
    /**
     * Refreshes the contents of the fields combo box
     */
    protected void refreshFieldList() {
        Object lastSelection = comboFields.getSelectedItem();
        S[] availFields = getAvailableFields();
        comboFields.setModel(new DefaultComboBoxModel(availFields));
        if (Utils.indexOfArray(availFields, lastSelection) >= 0) {
            comboFields.setSelectedItem(lastSelection);
        } else {
            if (availFields.length > 0)
                comboFields.setSelectedIndex(0);
        }
    }
    
    private JPanel getMyContentPane() {
        if (myContentPane == null) {
            double[][] dLay = {
                    {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border},
                    {border, TableLayout.FILL, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border, TableLayout.PREFERRED, border}
            };
            myContentPane = new JPanel(new TableLayout(dLay));
            
            comboFields = new JComboBox(getAvailableFields());
            comboFields.setActionCommand("fieldsel");
            comboFields.addActionListener(this);
            
            comboOp = new JComboBox();

            
            textCondition = new JTextField(30);
            textCondition.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
            
            buttonSearch = new JButton(Utils._("Search"), Utils.loadIcon("general/Find"));
            buttonSearch.setActionCommand("search");
            buttonSearch.addActionListener(this);
            
            Action actCancel = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                };
            };
            actCancel.putValue(Action.NAME, Utils._("Close"));
            buttonClose = new JButton(actCancel);
            buttonClose.getActionMap().put("EscapePressed", actCancel);
            buttonClose.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EscapePressed");
            
            radioForward = new JRadioButton(Utils._("Forward"));
            radioForward.setSelected(true);
            radioBackward = new JRadioButton(Utils._("Backward"));
            groupDirection = new ButtonGroup();
            groupDirection.add(radioForward);
            groupDirection.add(radioBackward);
            
            checkCaseSensitive = new JCheckBox(Utils._("Case sensitive"));
            checkWrapAround = new JCheckBox(Utils._("Wrap Search"));
       
            JPanel panelDirection = new JPanel(new GridLayout(2, 1));
            panelDirection.setBorder(BorderFactory.createTitledBorder(Utils._("Direction:")));
            panelDirection.add(radioForward);
            panelDirection.add(radioBackward);
            
            JPanel panelMisc = new JPanel(new GridLayout(2, 1));
            panelMisc.setBorder(BorderFactory.createTitledBorder(Utils._("Search options:")));
            panelMisc.add(checkCaseSensitive);
            panelMisc.add(checkWrapAround);

            JPanel panelPanel = new JPanel(new GridLayout(1, 2));
            panelPanel.add(panelDirection);
            panelPanel.add(panelMisc);
            
            Box boxButtons = new Box(BoxLayout.X_AXIS);
            boxButtons.add(buttonSearch);
            boxButtons.add(Box.createHorizontalStrut(border));
            boxButtons.add(buttonClose);
            boxButtons.add(Box.createHorizontalGlue());
            
            myContentPane.add(comboFields, "1, 2");
            myContentPane.add(comboOp, "3, 2");
            myContentPane.add(textCondition, "5, 2");
            myContentPane.add(panelPanel, "1, 4, 5, 4");
            //myContentPane.add(panelMisc, "5, 4");
            myContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 6, 6, 6, f, c");
            myContentPane.add(boxButtons, "1, 8, 5, 8");
        }
        return myContentPane;
    }
    
    private void initialize() {
        setContentPane(getMyContentPane());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
        setLocationRelativeTo(getOwner());
        pack();
        
        if (comboFields.getModel().getSize() > 0)
            comboFields.setSelectedIndex(0);
    }
    
    public SearchWin(Dialog owner, String title) {
        super(owner, title, false);
        
        initialize();
    }
    
    public SearchWin(Frame owner, String title) {
        super(owner, title, false);
        
        initialize();
    }
}
