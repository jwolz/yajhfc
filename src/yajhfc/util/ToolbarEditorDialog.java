/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
package yajhfc.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;

import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class ToolbarEditorDialog extends JDialog {

    private static final Logger log = Logger.getLogger(ToolbarEditorDialog.class.getName());
    private static final int border = 5; 
    
    private static final String SEPARATOR_KEY = "---";
    private static final Action separatorAction;
    static {
        separatorAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // Dummy
            }
        };
        separatorAction.putValue(Action.ACTION_COMMAND_KEY, SEPARATOR_KEY);
        separatorAction.putValue(Action.NAME, "---");
        separatorAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Separator"));
    }
    
    public static final Comparator<Action> actionComparator = new Comparator<Action>() {
        public int compare(Action o1, Action o2) {
            String name1 = o1 != null ? (String)o1.getValue(Action.NAME) : null;
            String name2 = o2 != null ? (String)o2.getValue(Action.NAME) : null;
            
            if (name1 == null) {
                return (name2 == null) ? 0 : -1;
            } else if (name2 == null) {
                return 1;
            } else {
                return name1.compareToIgnoreCase(name2);
            }
          }  
      };
    
    fmtEditor<Action> fmtEditor;
    Action actOK, actReset;
    
    JToolBar toolBarToEdit;
    Map<String,Action> availableActions;
    List<Action> selected;
    String defConfig;
    
    public ToolbarEditorDialog(Frame owner, Map<String,Action> availableActions,
            JToolBar toolBarToEdit, String defConfig) throws HeadlessException {
        super(owner);
        this.availableActions = availableActions;
        this.toolBarToEdit = toolBarToEdit;
        this.defConfig = defConfig;
        initialize();
    }

    public ToolbarEditorDialog(Dialog owner, Map<String,Action> availableActions,
            JToolBar toolBarToEdit, String defConfig) throws HeadlessException {
        super(owner);
        this.availableActions = availableActions;
        this.toolBarToEdit = toolBarToEdit;
        this.defConfig = defConfig;
        initialize();
    }

    private void initialize() {
        setTitle(Utils._("Customize toolbar"));
        setModal(true);
        
        setContentPane(createContentPane());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(640,480);
        Utils.setDefWinPos(this);
    }

  
    private JPanel createContentPane() {
        JPanel contentPane = new JPanel(new BorderLayout());
        
        createFmtEditor();
        
        actOK = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                saveToToolBar(toolBarToEdit, selected);
                dispose();
            }
        };
        actOK.putValue(Action.NAME, Utils._("OK"));
        
        actReset = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                fmtEditor.setNewSelection(parseStringToActionList(defConfig, availableActions));
            }
        };
        actReset.putValue(Action.NAME, Utils._("Reset"));
        
        Box buttonBox = Box.createHorizontalBox();
        CancelAction cancelAction = new CancelAction(this);
        
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(new JButton(actOK));
        buttonBox.add(Box.createHorizontalStrut(border));
        buttonBox.add(new JButton(actReset));
        buttonBox.add(Box.createHorizontalStrut(border));
        buttonBox.add(cancelAction.createCancelButton());
        buttonBox.add(Box.createHorizontalGlue());
        
        contentPane.add(fmtEditor, BorderLayout.CENTER);
        contentPane.add(buttonBox, BorderLayout.SOUTH);
        return contentPane;
    }
    
    private fmtEditor<Action> createFmtEditor() {
        ListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                
                Action data = (Action)value;
                String text;
                Icon icon;
                String tooltip;
                if (data == null) {
                    text = "";
                    icon = null;
                    tooltip = null;
                } else {
                    text = (String)data.getValue(Action.NAME);
                    icon = (Icon)data.getValue(Action.SMALL_ICON);
                    tooltip = (String)data.getValue(Action.SHORT_DESCRIPTION);
                }
                
                JLabel renderer = (JLabel)super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                renderer.setIcon(icon);
                renderer.setToolTipText(tooltip);
                return renderer;
            }
        };
        
        selected = getActionsFromToolbar(toolBarToEdit);
        return fmtEditor = new fmtEditor<Action>(availableActions.values(), selected, Collections.<Action>emptyList(), renderer, actionComparator, separatorAction, Utils._("Selected toolbar buttons:"), Utils._("Available toolbar buttons:"));
    }
    
    
    static void saveToToolBar(JToolBar toolBarToEdit, List<Action> selectedActs) {
        toolBarToEdit.removeAll();
        for (Action a : selectedActs) {
            if (a != separatorAction) {
                if (a.getValue(SelectedActionPropertyChangeListener.SELECTED_PROPERTY) != null) {
                    ActionToggleButton atb = new ActionToggleButton(a);
                    if (a.getValue(Action.SMALL_ICON) != null) {
                        atb.putClientProperty("hideActionText", Boolean.TRUE);
                        atb.setText("");
                    }
                    toolBarToEdit.add(atb);
                } else {
                    toolBarToEdit.add(a);
                }
            } else {
                toolBarToEdit.addSeparator();
            }
        }
        toolBarToEdit.repaint();
    }
    
    static List<Action> getActionsFromToolbar(JToolBar toolBarToEdit) {
        List<Action> selected = new ArrayList<Action>(toolBarToEdit.getComponentCount());
        for (int i = 0; i < toolBarToEdit.getComponentCount(); i++) {
            Component comp = toolBarToEdit.getComponent(i);
            if (comp instanceof AbstractButton) {
                Action act = ((AbstractButton)comp).getAction();
                if (act != null) {
                    selected.add(act);
                }
            } else if (comp instanceof JSeparator){
                selected.add(separatorAction);
            } else {
                log.info("Unknown toolbar component: " + comp + " (" + comp.getClass() + ")");
            }
        }
        return selected;
    }
    
    static List<Action> parseStringToActionList(String config, Map<String,Action> availableActions) {
        String[] actKeys = Utils.fastSplit(config, SEP_CHAR);
        List<Action> actions = new ArrayList<Action>(actKeys.length);
        
        for (String actKey:actKeys) {
            if (actKey.equals(SEPARATOR_KEY)) {
                actions.add(separatorAction);
            } else {
                Action a = availableActions.get(actKey);
                if (a != null) {
                    actions.add(a);
                } else {
                    log.warning("No action found for key " + actKey);
                }
            }
        }
        return actions;
    }
    
    private static final char SEP_CHAR = '|';
    /**
     * Saves the toolbar configuration to a String. For every action the ACTION_COMMAND_KEY
     * is used as key. Thus, this property must be set and may not contain a "|".
     * @param toolbar
     * @return
     */
    public static String saveConfigToString(JToolBar toolbar) {
        StringBuilder rv = new StringBuilder();
        for (Action a : getActionsFromToolbar(toolbar)) {
            rv.append(a.getValue(Action.ACTION_COMMAND_KEY)).append(SEP_CHAR);
        }
        return rv.toString();
    }
    
    /**
     * Loads the toolbar configuration from a String. For every action the ACTION_COMMAND_KEY
     * is used as key. Thus, this property must be set and may not contain a "|".
     * @param toolbar
     * @return
     */
    public static void loadConfigFromString(JToolBar toolbar, String config, Map<String,Action> availableActions) {
        saveToToolBar(toolbar, parseStringToActionList(config, availableActions));
    }
}
