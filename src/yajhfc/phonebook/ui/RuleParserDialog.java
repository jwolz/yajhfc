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
package yajhfc.phonebook.ui;

import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.ConcatRule;
import yajhfc.phonebook.convrules.EntryToStringRule;
import yajhfc.phonebook.convrules.RuleParser;
import yajhfc.phonebook.convrules.RuleParser.RuleParseException;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;

/**
 * @author jonas
 *
 */
public class RuleParserDialog extends JDialog {
    private static final Logger log = Logger.getLogger(RuleParserDialog.class.getName());
    private static final int border = 10;
    
    JList listFields, listExamples;
    Action actOK, actValidate, actAddField, actAddExample;
    JTextArea textEdit;

    ConcatRule result;

    /**
     * @param owner
     * @param title
     * @throws HeadlessException
     */
    public RuleParserDialog(Frame owner, String title, EntryToStringRule[] examples) throws HeadlessException {
        super(owner, title, true);
        initialize(examples);
    }

    /**
     * @param owner
     * @param title
     * @throws HeadlessException
     */
    public RuleParserDialog(Dialog owner, String title, EntryToStringRule[] examples)
            throws HeadlessException {
        super(owner, title, true);
        initialize(examples);
    }

    private void initialize(EntryToStringRule[] examples) {
        listFields = new JList(buildFieldList(PBEntryField.values()));
        actAddField = new AddFromListAction(listFields);
        
        listExamples = new JList(buildRuleList(examples));
        actAddExample = new AddFromListAction(listExamples);
        
        textEdit = new JTextArea();
        textEdit.setFont(new Font("DialogInput", java.awt.Font.PLAIN, 12));
        ClipboardPopup.DEFAULT_POPUP.addToComponent(textEdit);

        actOK = new ExcDialogAbstractAction(_("OK")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                result = validateRule();
                if (result != null) {
                    dispose();
                }
            }
        };
        
        actValidate = new ExcDialogAbstractAction(_("Validate")) {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                ConcatRule rule = validateRule();
                if (rule != null)
                    JOptionPane.showMessageDialog(RuleParserDialog.this, _("Display style is valid."));
            }
        };
        
        CancelAction actCancel = new CancelAction(this);
        
        double[][] dLay = {
            {border, 0.5, border, TableLayout.FILL, border},
            {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.FILL, border, TableLayout.PREFERRED, border}
        };

        JPanel contentPane = new JPanel(new TableLayout(dLay));
        contentPane.add(new JLabel("<html>" +_("Please enter the desired display style in the text box below. Double click on the lists to add the selected field/predefined style.") + "</html>" ), "1,1,3,1,c,c");
        Utils.addWithLabel(contentPane, new JScrollPane(listFields), _("Available fields"), "1,4,1,4,f,f");
        Utils.addWithLabel(contentPane, new JScrollPane(listExamples), _("Predefined styles"), "3,4,3,4,f,f");
        
        contentPane.add(new JScrollPane(textEdit), "1,6,3,6,f,f");
        
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, border, border));
        panelButtons.add(new JButton(actOK));
        panelButtons.add(new JButton(actValidate));
        panelButtons.add(new JButton(actCancel));
        
        contentPane.add(panelButtons, "1,8,3,8,f,c");
        
        setContentPane(contentPane);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        Utils.setDefWinPos(this);
    }

    ConcatRule validateRule() {
        String text = textEdit.getText();
        if (text.length() == 0) {
            JOptionPane.showMessageDialog(this, _("Please enter a display style.") , _("Validate"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        try {
            ConcatRule res = RuleParser.parseRule(text, true);
            
            boolean haveField = false;
            for (Object child : res.getChildren()) {
                if (child instanceof PBEntryField) {
                    haveField = true;
                    break;
                }
            }
            if (!haveField) {
                JOptionPane.showMessageDialog(this, _("The display style must contain at least one field.") , _("Validate"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
            
            return res;
        } catch (RuleParseException e) {
            log.log(Level.INFO, "Validation failed", e);
            JOptionPane.showMessageDialog(this, _("Display style is not valid") + ":\n" + e.getLocalizedMessage(), _("Validate"), JOptionPane.WARNING_MESSAGE);
            int errorEnd = e.getErrorEnd()+1;
            if (errorEnd == 0)
                errorEnd = text.length();
            
            textEdit.select(e.getErrorOffset(), errorEnd);
            textEdit.requestFocusInWindow();
            return null;
        }
    }
    
    private Vector<String> buildFieldList(PBEntryField[] fields) {
        Vector<String> result = new Vector<String>(fields.length);
        for (PBEntryField  field : fields) {
            result.add("[" + field.getDescription() + "]");
        }
        return result;
    }

    private Vector<String> buildRuleList(EntryToStringRule[] rules) {
        Vector<String> result = new Vector<String>(rules.length);
        for (EntryToStringRule rule : rules) {
            result.add(RuleParser.ruleToString(rule, true));
        }
        return result;
    }
    
    public void setEditedRule(EntryToStringRule rule) {
        textEdit.setText(RuleParser.ruleToString(rule, true));
    }

    /**
     * Shows the dialog with the given configuration
     * Returns the rule or null if the user clicked cancel.
     * @param owner
     * @param title
     * @param examples
     * @param rule
     * @return
     */
    public static ConcatRule showForRule(Window owner, String title, EntryToStringRule[] examples, EntryToStringRule rule) {
        RuleParserDialog rpd;
        if (owner instanceof Dialog) {
            rpd = new RuleParserDialog((Dialog)owner, title, examples);
        } else if (owner instanceof Frame) {
            rpd = new RuleParserDialog((Frame)owner, title, examples);
        } else {
            throw new RuntimeException("owner must be a Dialog or Frame");
        }
        rpd.setEditedRule(rule);
        rpd.setVisible(true);
        return rpd.result;
    }
    
    class AddFromListAction extends ExcDialogAbstractAction implements MouseListener, ListSelectionListener {
        protected final JList list;
        protected JPopupMenu popupMenu;

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                setEnabled(list.getSelectedIndex() >= 0);
            }
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0) {
                    actionPerformed(new ActionEvent(e.getSource(), e.getID(), ""));
                }
            }
        }

        public JPopupMenu getPopupMenu() {
            if (popupMenu == null) {
                popupMenu = new JPopupMenu();
                popupMenu.add(this);
            }
            return popupMenu;
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseEntered(MouseEvent e) {  }

        public void mouseExited(MouseEvent e) {  }

        @Override
        protected void actualActionPerformed(ActionEvent e) {
            if (list.getSelectedValue() == null)
                return;
            
            final String text = list.getSelectedValue().toString();
            textEdit.replaceSelection(text);
            textEdit.requestFocusInWindow();
        }

        public AddFromListAction(JList list) {
            super(Utils._("Add"));
            this.list = list;
            
            list.addListSelectionListener(this);
            list.addMouseListener(this);
            list.getActionMap().put(getClass().getName(), this);
            list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), getClass().getName());
        }        
    }
}
