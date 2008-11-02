package yajhfc.phonebook;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2007 Jonas Wolz
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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import yajhfc.ClipboardPopup;
import yajhfc.utils;
import yajhfc.filters.StringFilter;
import yajhfc.filters.StringFilterOperator;

public final class NewSearchWin extends JDialog implements ActionListener {

    private JPanel myContentPane;
    private JButton buttonSearch, buttonClose;
    private JComboBox comboFields, comboOp;
    private JTextField textCondition;
    private JRadioButton radioForward, radioBackward;
    private JCheckBox checkCaseSensitive, checkWrapAround;
    private NewPhoneBookWin owner;
    private ButtonGroup groupDirection;
    
    private static final int border = 10;
    
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("search")) {
            int idx, startIdx, pbIdx, pbStartIdx;
            boolean hitEnd = false;
            JTree tree = owner.getPhoneBookTree();
            List<PhoneBook> availPBs = owner.getAvailablePhoneBooks();
            TreePath selPath = tree.getSelectionPath();
            PhoneBook pb;
            boolean searchBackwards = radioBackward.isSelected();
            
            if (selPath == null || selPath.getPathCount() <= 1) {
                pbStartIdx = 0;
                startIdx = 0;
            } else {
                pbStartIdx = availPBs.indexOf(selPath.getPathComponent(1));
                if (selPath.getPathCount() == 3) {
                    startIdx = ((PhoneBook)selPath.getPathComponent(1)).getEntries().indexOf((PhoneBookEntry)selPath.getPathComponent(2));
                } else {
                    startIdx = 0;
                }
            }
            
            StringFilter<PhoneBookEntry,PBEntryField> filter =
                new StringFilter<PhoneBookEntry, PBEntryField>(
                        (PBEntryField)comboFields.getSelectedItem(),
                        (StringFilterOperator)comboOp.getSelectedItem(),
                        textCondition.getText(),
                        checkCaseSensitive.isSelected());
            
            pbIdx = pbStartIdx;
            do {
                pb = availPBs.get(pbIdx);
                if ((searchBackwards && startIdx == 0) ||
                        (!searchBackwards && startIdx >= pb.getEntries().size()-1)) {
                    idx = -1;
                } else {
                    idx = pb.findEntry(startIdx + (searchBackwards ? -1 : 1),
                            searchBackwards,
                            filter);
                }
                if (idx >= 0)
                    break;
                
                pbIdx += (searchBackwards ? -1 : 1);
                if (hitEnd && ((!searchBackwards && pbIdx > pbStartIdx) || (searchBackwards && pbIdx < pbStartIdx)))
                    break;
                
                if (pbIdx >= availPBs.size() || pbIdx < 0) {
                    hitEnd = true;
                    if (checkWrapAround.isSelected())
                        pbIdx = searchBackwards ? availPBs.size()-1 : 0;
                    else 
                        break;   
                }
                startIdx = (searchBackwards ? availPBs.get(pbIdx).getEntries().size() : -1);
            } while (true);
            
            if (idx < 0) {
                JOptionPane.showMessageDialog(this, utils._("No matching phone book entry found."));
            } else {
                tree.setSelectionPath(new TreePath(new Object[] { tree.getModel().getRoot(), pb, pb.getEntries().get(idx)}));
            }
        }
    }
    
    private JPanel getMyContentPane() {
        if (myContentPane == null) {
            double[][] dLay = {
                    {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border},
                    {border, TableLayout.FILL, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border, TableLayout.PREFERRED, border}
            };
            myContentPane = new JPanel(new TableLayout(dLay));
            
            comboFields = new JComboBox(PBEntryField.values());
            comboOp = new JComboBox(StringFilterOperator.values());
            
            textCondition = new JTextField(30);
            textCondition.addMouseListener(new ClipboardPopup());
            
            buttonSearch = new JButton(utils._("Search"), utils.loadIcon("general/Search"));
            buttonSearch.setActionCommand("search");
            buttonSearch.addActionListener(this);
            
            Action actCancel = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                };
            };
            actCancel.putValue(Action.NAME, utils._("Close"));
            buttonClose = new JButton(actCancel);
            buttonClose.getActionMap().put("EscapePressed", actCancel);
            buttonClose.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EscapePressed");
            
            radioForward = new JRadioButton(utils._("Forward"));
            radioForward.setSelected(true);
            radioBackward = new JRadioButton(utils._("Backward"));
            groupDirection = new ButtonGroup();
            groupDirection.add(radioForward);
            groupDirection.add(radioBackward);
            
            checkCaseSensitive = new JCheckBox(utils._("Case sensitive"));
            checkWrapAround = new JCheckBox(utils._("Wrap Search"));
       
            JPanel panelDirection = new JPanel(new GridLayout(2, 1));
            panelDirection.setBorder(BorderFactory.createTitledBorder(utils._("Direction:")));
            panelDirection.add(radioForward);
            panelDirection.add(radioBackward);
            
            JPanel panelMisc = new JPanel(new GridLayout(2, 1));
            panelMisc.setBorder(BorderFactory.createTitledBorder(utils._("Search options:")));
            panelMisc.add(checkCaseSensitive);
            panelMisc.add(checkWrapAround);

            Box boxButtons = new Box(BoxLayout.X_AXIS);
            boxButtons.add(buttonSearch);
            boxButtons.add(Box.createHorizontalStrut(border));
            boxButtons.add(buttonClose);
            boxButtons.add(Box.createHorizontalGlue());
            
            myContentPane.add(comboFields, "1, 2");
            myContentPane.add(comboOp, "3, 2");
            myContentPane.add(textCondition, "5, 2");
            myContentPane.add(panelDirection, "1, 4, 3, 4");
            myContentPane.add(panelMisc, "5, 4");
            myContentPane.add(new JSeparator(JSeparator.HORIZONTAL), "0, 6, 6, 6, f, c");
            myContentPane.add(boxButtons, "1, 8, 5, 8");
        }
        return myContentPane;
    }
    
    private void initialize() {
        setContentPane(getMyContentPane());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
        setLocationRelativeTo(owner);
        pack();
    }
    
    public NewSearchWin(NewPhoneBookWin owner) {
        super(owner, utils._("Find phone book entry"), false);
        
        this.owner = owner;
        initialize();
    }
}
