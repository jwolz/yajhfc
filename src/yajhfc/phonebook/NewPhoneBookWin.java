package yajhfc.phonebook;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.phonebook.PhoneBookTreeModel.PBTreeModelListener;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.MultiButtonGroup;

public final class NewPhoneBookWin extends JDialog implements ActionListener {

    private static final Logger log = Logger.getLogger(NewPhoneBookWin.class.getName());
    
    private static final String PBFIELD_PROP = "YajHFC-PBEntryfield";
    
    JSplitPane splitPane;
    JTree phoneBookTree;
    PhoneBookTreeModel treeModel;
    JPanel rightPane, leftPane;

    JTextField textDescriptor;
    JButton buttonBrowse;
    
    Map<PBEntryField,JTextComponent> entryFields = new EnumMap<PBEntryField, JTextComponent>(PBEntryField.class);
    JScrollPane scrollComment;
    JTextArea textComment;
    
    EntryTextFieldListener entryListener;
    
    JMenu pbMenu, importMenu, openMenu, entryMenu;
    JPopupMenu treePopup;
    
    Action listRemoveAction, addEntryAction, removeEntryAction, searchEntryAction, selectAction;
    
    JTextField searchField;
    JButton clearButton;
    MultiButtonGroup nameStyleGroup;
    
    SearchHelper searchHelper = new SearchHelper();
    
    NewSearchWin searchWin;
    
    boolean usedSelectButton = false;
    
    List<PhoneBookEntry> selectedItems = new ArrayList<PhoneBookEntry>();
    PhoneBook currentPhonebook = null;
    
    private static final double border = 5;
    
    public void writeToTextFields(PhoneBook phoneBook, PhoneBookEntry pb) {
        
        if (pb == null || phoneBook == null) {
            for (JTextComponent comp : entryFields.values()) {
                comp.setText("");
                comp.setEnabled(false);
            }
            scrollComment.setEnabled(false);
        } else {
            boolean editable = !phoneBook.isReadOnly();
            for (Map.Entry<PBEntryField, JTextComponent> entry : entryFields.entrySet()) {
                JTextComponent comp = entry.getValue();
                PBEntryField field = entry.getKey();
                
                comp.setText(pb.getField(field));
                comp.setEnabled(phoneBook.isFieldAvailable(field));
                comp.setEditable(editable);
            }
            scrollComment.setEnabled(textComment.isEnabled());
        }
    }
    
    public void readFromTextFields(PhoneBookEntry pb, boolean updateOnly) {
        if (pb == null)
           return; 
        
        for (Map.Entry<PBEntryField, JTextComponent> entry : entryFields.entrySet()) {
            JTextComponent comp = entry.getValue();
            PBEntryField field = entry.getKey();
            
            pb.setField(field, comp.getText());
        }

        if (updateOnly)
            pb.updateDisplay();
        else
            pb.commit();
    }
    
//    private void addWithLabel(JPanel pane, JComponent comp, String text, String layout) {
//        addWithLabel(pane, comp, text, new TableLayoutConstraints(layout));
//    }
    
    private void addWithLabel(JPanel pane, JComponent comp, String text, TableLayoutConstraints c) {
        pane.add(comp, c);
        
        JLabel lbl = new JLabel(text);
        lbl.setLabelFor(comp);
        c.row1 = c.row2 = c.row1 - 1;
        c.vAlign = TableLayoutConstraints.BOTTOM;
        c.hAlign = TableLayoutConstraints.LEFT;
        pane.add(lbl, c);
    }
    
    
    private JTextField createEntryTextField(PBEntryField field) {
        JTextField res = new JTextField();
        res.addFocusListener(entryListener);
        res.addActionListener(entryListener);
        res.addMouseListener(getDefClPop());
        
        entryFields.put(field, res);
        res.putClientProperty(PBFIELD_PROP, field);
        return res;
    }
    
    private JPanel getRightPane() {
        if (rightPane == null) {
            int longFields = 0;
            int shortFields = 0;
            for (PBEntryField field : PBEntryField.values()) {
                if (field.isShortLength()) {
                    shortFields++;
                } else {
                    longFields++;
                }
            }
            final int rowCount = 5 + 2 * (longFields + (shortFields+1)/2);
            double[][] dLay = {
                    {border, 0.5, border, TableLayout.FILL, border},
                    new double[rowCount]
            };
            final double rowH = 1.0 / (double)(rowCount+3);
            Arrays.fill(dLay[1], 1, rowCount - 2, rowH);
            dLay[1][0] = dLay[1][rowCount - 1] = border;
            dLay[1][rowCount - 2] = TableLayout.FILL;
            
            rightPane = new JPanel(new TableLayout(dLay));
            
            textDescriptor = new JTextField();
            textDescriptor.setEditable(false);
            textDescriptor.setBackground(UIManager.getColor("Label.backgroundColor"));
            textDescriptor.addMouseListener(getDefClPop());
            
            buttonBrowse = new JButton(Utils.loadIcon("general/Open"));
            buttonBrowse.setActionCommand("browse");
            buttonBrowse.addActionListener(this);
                    

            
            Box box = Box.createHorizontalBox();
            box.add(textDescriptor);
            box.add(buttonBrowse);
            
            rightPane.add(new JLabel(Utils._("Current phone book:")), "1, 1, L, B");
            rightPane.add(box, "1, 2, 3, 2");
            
            //rightPane.add(buttonSelect, "3, 1");
            
            rightPane.add(new JSeparator(), "0,3,4,3,F,C");
            
            entryListener = new EntryTextFieldListener();
            
            int row = 5;
            int col = 1;
            for (PBEntryField field : PBEntryField.values()) {
                if (field != PBEntryField.Comment) {
                    JTextField textField = createEntryTextField(field);
                    TableLayoutConstraints layout;
                    if (field.isShortLength()) {
                        layout = new TableLayoutConstraints(col, row, col, row, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER);
                        if (col == 1) {
                            col = 3;
                        } else {
                            row += 2;
                            col  = 1;
                        }
                    } else {
                        layout = new TableLayoutConstraints(1, row, 3, row, TableLayoutConstraints.FULL, TableLayoutConstraints.CENTER);
                        col  = 1;
                        row += 2;
                    }
                    addWithLabel(rightPane, textField, field.getDescription()+":", layout);
                }
            }
            
            textComment = new JTextArea();
            textComment.setWrapStyleWord(true);
            textComment.setLineWrap(true);
            textComment.addFocusListener(entryListener);
            textComment.addMouseListener(getDefClPop());
            entryFields.put(PBEntryField.Comment, textComment);
            textComment.putClientProperty(PBFIELD_PROP, PBEntryField.Comment);
            
            scrollComment = new JScrollPane(textComment, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            
            addWithLabel(rightPane, scrollComment, Utils._("Comments:"), new TableLayoutConstraints(1,row,3,row,TableLayoutConstraints.FULL,TableLayoutConstraints.FULL));
            
        }
        return rightPane;
    }
       
            
    private ClipboardPopup getDefClPop() {
        return ClipboardPopup.DEFAULT_POPUP;
    }
    
    public PhoneBook getCurrentPhoneBook() {
        return currentPhonebook;
    }
    
    public List<PhoneBookEntry> getSelectedEntries() {
        return selectedItems;
    }
 
    JTree getPhoneBookTree() {
        return phoneBookTree;
    }
    
    List<PhoneBook> getAvailablePhoneBooks() {
        return treeModel.getPhoneBooks();
    }
    
    void addPhoneBook(String descriptor) {
        // Try to check if the phone book has already been added:
        for (PhoneBook pb : treeModel.getPhoneBooks()) {
            if (descriptor.equals(pb.getDescriptor())) {
                JOptionPane.showMessageDialog(this, Utils._("This phone book has already been added."), Utils._("Add to list"), JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        PhoneBook phoneBook = null;
        try {
            phoneBook = PhoneBookFactory.instanceForDescriptor(descriptor, this);
            if (phoneBook == null) {
                JOptionPane.showMessageDialog(this, Utils._("Unknown Phonebook type selected."), Utils._("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                phoneBook.open(descriptor);
            }

        } catch (PhoneBookException e) {
            if (!e.messageAlreadyDisplayed())
                ExceptionDialog.showExceptionDialog(this, Utils._("Error loading the phone book: "), e);
            //return; // do nothing...
        }
        if (phoneBook != null) {
            treeModel.addPhoneBook(phoneBook);
            phoneBookTree.expandPath(new TreePath(new Object[] { treeModel.rootNode, phoneBook }));
        }
    }
    
    void closeCurrentPhoneBook() {
        if (currentPhonebook != null) {
            PhoneBook curPB = currentPhonebook;
            curPB.close();
            currentPhonebook = null;
            selectedItems.clear();
            
            treeModel.removePhoneBook(curPB);
        }
    }
    
    public void browseForPhonebook() {
        if (currentPhonebook != null) {
            String newPB = currentPhonebook.browseForPhoneBook();
            if (newPB != null) {
                closeCurrentPhoneBook();
                addPhoneBook(newPB);
            }
        }
    }
    
    void closeAndSaveAllPhonebooks() {
        List<String> pbList = Utils.getFaxOptions().phoneBooks;
        pbList.clear();
        
        selectedItems.clear();
        currentPhonebook = null;
        for (PhoneBook pb : treeModel.getPhoneBooks()) {
            pbList.add(pb.getDescriptor());
            pb.close();
        }

        checkMenuEnable();
    }
    
    void checkMenuEnable() {
        boolean delOK = false, writeOK = false, browseOK = false, selOK = false;
        boolean havePB = currentPhonebook != null;
        
        browseOK = (treeModel.getPhoneBooks().size() > 0);
        writeOK = havePB && !currentPhonebook.isReadOnly() && currentPhonebook.isOpen();
        selOK = (selectedItems.size() > 0);
        delOK = selOK && writeOK;
        
        removeEntryAction.setEnabled(delOK);
        addEntryAction.setEnabled(writeOK);
    
        entryMenu.setEnabled(browseOK);
        importMenu.setEnabled(writeOK);
        
        listRemoveAction.setEnabled(havePB);
        buttonBrowse.setEnabled(havePB);
        if (selectAction != null)
            selectAction.setEnabled(selOK);
    }
    
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("close")) {
            usedSelectButton = false;
            dispose();
        } else if (cmd.equals("descopen")) {
            doDescOpen();
        } else if (cmd.equals("descimport")) {
            doDescImport();
        } else if (cmd.equals("browse")) {
            browseForPhonebook();
        } else
            assert(false);
    }
    
    private String promptForDescriptor(String title) {
        return JOptionPane.showInputDialog(NewPhoneBookWin.this, Utils._("Please enter the phone book descriptor to open."), title, JOptionPane.QUESTION_MESSAGE);
    }
    
    void doDescOpen() {
        String desc = promptForDescriptor(Utils._("Open by descriptor"));
        if (desc != null) {
            addPhoneBook(desc);
        }
    }
    
    void doDescImport() {
        if (currentPhonebook == null || currentPhonebook.isReadOnly()) 
            return;
        
        String desc = promptForDescriptor(Utils._("Import by descriptor"));
        if (desc != null) {
            importFromPhonebook(null, desc);
        }
    }
    
    protected void importFromPhonebook(PhoneBook pb, String descriptor) {
        try {
            if (pb == null) {
                pb = PhoneBookFactory.instanceForDescriptor(descriptor, NewPhoneBookWin.this);
            }
            if (pb == null) {
                JOptionPane.showMessageDialog(NewPhoneBookWin.this, Utils._("Unsupported phone book format."), Utils._("Error"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (descriptor != null) 
                pb.open(descriptor); 
            
            List<PhoneBookEntry> importedEntries = pb.getEntries();
            for (int i=0; i < importedEntries.size(); i++) {
                PhoneBookEntry pbe = currentPhonebook.addNewEntry();
                pbe.copyFrom(importedEntries.get(i));
            }
            
            if (descriptor != null) // Phone book has been opened above...
                pb.close();
            
            currentPhonebook.resort();
        } catch (PhoneBookException e) {
            if (!e.messageAlreadyDisplayed())
                ExceptionDialog.showExceptionDialog(NewPhoneBookWin.this, Utils._("Error loading the phone book: "), e);
        }
    }
       
    
    private JPanel createJContentPane() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(createToolBar(), BorderLayout.NORTH);
        contentPane.add(getSplitPane(), BorderLayout.CENTER);
        return contentPane;
    }
    
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(searchHelper);
        searchField.setActionCommand("focus");
        searchField.addActionListener(searchHelper);
        Dimension prefSize = searchField.getPreferredSize();
        prefSize.width = Integer.MAX_VALUE;
        prefSize.height += 4;
        searchField.setMaximumSize(prefSize);
        searchField.addMouseListener(getDefClPop());
        
        clearButton = new JButton(Utils._("Reset"));
        clearButton.setActionCommand("clear");
        clearButton.addActionListener(searchHelper);
        clearButton.setEnabled(false);
        clearButton.setToolTipText(Utils._("Reset quick search and show all phone book entries."));
        
        toolBar.add(new JLabel(Utils._("Search") + ": "));
        toolBar.add(searchField);
        toolBar.add(clearButton);
        toolBar.addSeparator();
        toolBar.add(searchEntryAction);
        
        return toolBar;
    }
    
    private JSplitPane getSplitPane() {
        if (splitPane == null) {
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLeftPane(), getRightPane());
            splitPane.setDividerLocation(200);
            splitPane.setOpaque(true);
        }
        return splitPane;
    }
    
    private JPanel getLeftPane() {
        if (leftPane == null) {
            double[][] dLay = {
                    {0.5, border, TableLayout.FILL},
                    {TableLayout.FILL, border/2, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}
            };
            leftPane = new JPanel(new TableLayout(dLay), false);
            
            treeModel = new PhoneBookTreeModel();
            treeModel.addPBTreeModelListener(searchHelper);
            treeModel.setNameToStringRule(Utils.getFaxOptions().phonebookDisplayStyle);
            
            phoneBookTree = new JTree(treeModel);
            treeModel.setTree(phoneBookTree);
            phoneBookTree.setEditable(false);
            phoneBookTree.setRootVisible(true);
            phoneBookTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            phoneBookTree.setCellRenderer(new DefaultTreeCellRenderer() {
                private ImageIcon phoneBookIcon = Utils.loadIcon("general/Bookmarks");
                
                @Override
                public Component getTreeCellRendererComponent(JTree tree,
                        Object value, boolean sel, boolean expanded,
                        boolean leaf, int row, boolean hasFocus) {
                    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                            row, hasFocus);
                    if (value instanceof PhoneBook) {
                        setIcon(phoneBookIcon);
                    }
                    return this;
                } 
            });
            phoneBookTree.addTreeSelectionListener(new TreeSelectionListener() {

                private TreePath[] oldSelection = null;

                public void valueChanged(TreeSelectionEvent e) {
                    TreePath[] paths = phoneBookTree.getSelectionPaths();
                    if (treePathsEqual(oldSelection, paths)) {
                        return;
                    }

                    if (selectedItems.size() == 1) {
                        readFromTextFields(selectedItems.get(0), false);
                    }

                    // Read out selection:
                    selectedItems.clear();
                    currentPhonebook = null;

                    if (paths != null && paths.length > 0) {
                        PhoneBook currentPB = null;
                        boolean haveAtMostOnePB = true;
                        for (TreePath tp : paths) {
                            if (tp.getPathCount() > 1) {
                                if (currentPB == null) {
                                    currentPB = (PhoneBook)tp.getPathComponent(1);
                                } else {
                                    haveAtMostOnePB = haveAtMostOnePB && (currentPB == tp.getPathComponent(1));
                                }

                                if (tp.getPathCount() == 3) {
                                    selectedItems.add((PhoneBookEntry)tp.getPathComponent(2));
                                }
                            } else {
                                haveAtMostOnePB = false;
                            }
                        }

                        if (haveAtMostOnePB) {
                            currentPhonebook = currentPB;
                        }
                    }

                    checkMenuEnable();
                    if (selectedItems.size() == 1) {
                        writeToTextFields(currentPhonebook, selectedItems.get(0));
                    } else {
                        writeToTextFields(null, null);
                    }

                    if (currentPhonebook != null) {
                        textDescriptor.setText(currentPhonebook.getDescriptor());
                        textDescriptor.setCaretPosition(0);
                    } else {
                        textDescriptor.setText("");
                    }

                    oldSelection = paths;
                } 
            });
            phoneBookTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "RemoveEntry");
            phoneBookTree.getActionMap().put("RemoveEntry", removeEntryAction);
            
            treePopup = new JPopupMenu();
            treePopup.add(new JMenuItem(addEntryAction));
            final JMenuItem removeEntryItem = new JMenuItem(removeEntryAction);
            final JMenuItem removePBItem = new JMenuItem(listRemoveAction);
            treePopup.add(removeEntryItem);
            treePopup.add(removePBItem);
            treePopup.add(new JSeparator());
            JMenu viewMenu = new JMenu(nameStyleGroup.label);
            for (JMenuItem item : nameStyleGroup.createMenuItems()) {
                viewMenu.add(item);
            }
            treePopup.add(viewMenu);
            treePopup.addSeparator();
            treePopup.add(new JMenuItem(searchEntryAction));
            treePopup.addPopupMenuListener(new PopupMenuListener() {

                public void popupMenuCanceled(PopupMenuEvent e) {
                    // No action
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    // No action
                }

                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    boolean showRemoveEntry = (selectedItems.size() > 0);
                    removeEntryItem.setVisible(showRemoveEntry);
                    removePBItem.setVisible(!showRemoveEntry);
                }
                
            });
            phoneBookTree.setComponentPopupMenu(treePopup);
            
            
            JScrollPane treeScroller = new JScrollPane(phoneBookTree);
            
            JButton addButton = new JButton(addEntryAction);
            addButton.setText("");
            
            JButton removeButton = new JButton(removeEntryAction);
            removeButton.setText("");
            
            leftPane.add(treeScroller, "0,0,2,0,f,f");
            leftPane.add(addButton, "0,2");
            leftPane.add(removeButton, "2,2");
        }
        return leftPane;
    }
    
    private JMenu getPhonebookMenu() {
        if (pbMenu == null) {
            importMenu = new JMenu(Utils._("Import"));
            importMenu.setIcon(Utils.loadIcon("general/Import"));
            openMenu = new JMenu(Utils._("Add to list"));
            openMenu.setIcon(Utils.loadIcon("general/Open"));
            /*JMenu saveAsMenu = new JMenu(Utils._("Save as"));
            saveAsMenu.setIcon(Utils.loadIcon("general/SaveAs"));*/

            for (PhoneBookType pbt : PhoneBookFactory.PhonebookTypes) {
                String menuText = pbt.getDisplayName();
                PhonebookMenuActionListener pbmal = new PhonebookMenuActionListener(pbt);

                JMenuItem mi;
                mi = new JMenuItem(menuText);
                mi.setActionCommand(PhonebookMenuActionListener.IMPORT_COMMAND);
                mi.addActionListener(pbmal);
                importMenu.add(mi);

                mi = new JMenuItem(menuText);
                mi.setActionCommand(PhonebookMenuActionListener.OPEN_COMMAND);
                mi.addActionListener(pbmal);
                openMenu.add(mi);

                /*mi = new JMenuItem(menuText);
            mi.setActionCommand(PhonebookMenuActionListener.SAVEAS_COMMAND);
            mi.addActionListener(pbmal);
            saveAsMenu.add(mi);*/
            }
            
            JMenuItem mi = new JMenuItem(Utils._("By descriptor..."));
            mi.setActionCommand("descimport");
            mi.addActionListener(this);
            
            importMenu.addSeparator();
            importMenu.add(mi);
            
            mi = new JMenuItem(Utils._("By descriptor..."));
            mi.setActionCommand("descopen");
            mi.addActionListener(this);
            
            openMenu.addSeparator();
            openMenu.add(mi);
            
            JMenuItem closeWinMenu = new JMenuItem(Utils._("Close"), Utils.loadCustomIcon("close.gif"));
            closeWinMenu.setActionCommand("close");
            closeWinMenu.addActionListener(this);

            pbMenu = new JMenu(Utils._("Phonebook"));
            pbMenu.add(openMenu);
            pbMenu.add(importMenu);
            //pbMenu.add(saveAsMenu);
            pbMenu.add(new JSeparator());
            pbMenu.add(new JMenuItem(listRemoveAction));
            pbMenu.add(new JSeparator());
            pbMenu.add(closeWinMenu);
        }
        return pbMenu;
    }
    
    private JMenu getEntryMenu() {
        if (entryMenu == null) {
            entryMenu = new JMenu(Utils._("Entry"));

            entryMenu.add(new JMenuItem(addEntryAction));
            entryMenu.add(new JMenuItem(removeEntryAction));
            entryMenu.add(new JSeparator());
            JMenu viewMenu = new JMenu(nameStyleGroup.label);
            for (JMenuItem item : nameStyleGroup.createMenuItems()) {
                viewMenu.add(item);
            }
            entryMenu.add(viewMenu);
            entryMenu.add(new JSeparator());
            entryMenu.add(new JMenuItem(searchEntryAction));
        }
        return entryMenu;
    }
    
    private JMenuBar getMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getPhonebookMenu());
        menuBar.add(getEntryMenu());
        return menuBar;
    }
    
    private void createActions() {
        addEntryAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (currentPhonebook == null || currentPhonebook.isReadOnly()) 
                    return;
                
                PhoneBookEntry pbe = currentPhonebook.addNewEntry();
                phoneBookTree.setSelectionPath(new TreePath(new Object[] { treeModel.rootNode, currentPhonebook, pbe }));
            }
        };
        addEntryAction.putValue(Action.NAME, Utils._("Add"));
        addEntryAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Add"));
        addEntryAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Add new entry"));
        addEntryAction.setEnabled(false);
        
        removeEntryAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (selectedItems.size() == 0)
                    return;
                
                if (JOptionPane.showConfirmDialog(NewPhoneBookWin.this, Utils._("Do you want to delete the selected entries?"), Utils._("Delete entries"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    PhoneBookEntry[] entries = selectedItems.toArray(new PhoneBookEntry[selectedItems.size()]);
                    
                    for (PhoneBookEntry pbe : entries) {
                        selectedItems.clear();
                        pbe.delete();
                    }
                    writeToTextFields(null, null);
                }
            }
        };
        removeEntryAction.putValue(Action.NAME, Utils._("Delete"));
        removeEntryAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Delete"));
        removeEntryAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Delete selected entry"));
        removeEntryAction.setEnabled(false);
        
        searchEntryAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (searchWin == null) {
                    searchWin = new NewSearchWin(NewPhoneBookWin.this);
                }
                if (searchWin.isVisible()) 
                    searchWin.toFront();
                else
                    searchWin.setVisible(true);  
            }
        };
        searchEntryAction.putValue(Action.NAME, Utils._("Find..."));
        searchEntryAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Find"));
        searchEntryAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Search for an entry"));
        
        listRemoveAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(NewPhoneBookWin.this, Utils._("Do you want to remove the current phone book from the list?"), Utils._("Remove from list"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    closeCurrentPhoneBook();
                }
            }
        };
        listRemoveAction.putValue(Action.NAME, Utils._("Remove from list"));
        listRemoveAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Remove"));
        
        nameStyleGroup = new MultiButtonGroup() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                treeModel.setNameToStringRule(NameRule.valueOf(e.getActionCommand()));
            }
        };
        nameStyleGroup.label = Utils._("Display style");
        //nameStyleGroup.addItem(NameRule.GIVENNAME_NAME.getDisplayName(), NameRule.GIVENNAME_NAME.name());
        //nameStyleGroup.addItem(NameRule.NAME_GIVENNAME.getDisplayName(), NameRule.NAME_GIVENNAME.name());
        for (NameRule rule : NameRule.values()) {
            nameStyleGroup.addItem(rule.getDisplayName(), rule.name());
        }
        nameStyleGroup.setSelectedActionCommand(Utils.getFaxOptions().phonebookDisplayStyle.name());
        
    }
    
    private void initialize() {
        createActions();
        setContentPane(createJContentPane());
        setJMenuBar(getMenu());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle(Utils._("Phone book"));
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                
//                Utils.getFaxOptions().lastSelectedPhonebook = tabPhonebooks.getSelectedIndex();
                Utils.getFaxOptions().phoneWinBounds = getBounds();
                Utils.getFaxOptions().phonebookDisplayStyle = NameRule.valueOf(nameStyleGroup.getSelectedActionCommand());
                
                closeAndSaveAllPhonebooks();
                
                if (searchWin != null) {
                    searchWin.dispose();
                    searchWin = null;
                }
            }
        });
        
        FaxOptions fopts = Utils.getFaxOptions();
        
        if (fopts.phoneWinBounds != null)
            this.setBounds(fopts.phoneWinBounds);
        else {
            this.setSize(640, 480);
            //this.setLocationByPlatform(true);
            Utils.setDefWinPos(this);
        }
        
        if (fopts.phoneBooks.size() > 0) {
            Collections.sort(fopts.phoneBooks); // Bring the phone books in a defined order
            for (String pbdesc : fopts.phoneBooks) {
                addPhoneBook(pbdesc);
            }
        } else {
            addPhoneBook(PhoneBookFactory.getDefaultPhonebookDescriptor());
        }
        
//        if (fopts.lastSelectedPhonebook >= 0 && fopts.lastSelectedPhonebook < tabPhonebooks.getTabCount())
//            tabPhonebooks.setSelectedIndex(fopts.lastSelectedPhonebook);
        phoneBookTree.setSelectionRow(1);
    }
    
    public NewPhoneBookWin(Dialog owner) {
        super(owner);
        initialize();
    }
    
    public NewPhoneBookWin(Frame owner) {
        super(owner);
        initialize();
    }
    
    private void showSelectButton() {
        if (selectAction == null) {
            selectAction = new ExcDialogAbstractAction() {
                public void actualActionPerformed(ActionEvent e) {
                    if (selectedItems.size() == 0)
                        return;
                    
                    usedSelectButton = true;
                    setVisible(false);
                }
            };
            selectAction.putValue(Action.NAME, Utils._("Select"));
            selectAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Undo"));
            selectAction.setEnabled(false);
            
            JButton buttonSelect = new JButton(selectAction);
            leftPane.add(Box.createVerticalStrut((int)border), "0,3");
            leftPane.add(buttonSelect, "0,4,2,4");
            
            entryMenu.addSeparator();
            entryMenu.add(new JMenuItem(selectAction));
            
            treePopup.addSeparator();
            treePopup.add(new JMenuItem(selectAction));
            
            phoneBookTree.getActionMap().put("selectEntry", selectAction);
            phoneBookTree.getInputMap().put(KeyStroke.getKeyStroke('\n'), "selectEntry");
            
            phoneBookTree.addMouseListener(new MouseAdapter() {
               @Override
                public void mouseClicked(MouseEvent e) {
                   if (e.getClickCount() == 2) {
                       selectAction.actionPerformed(null);
                   }
                } 
            });
        }
    }
    
    public PhoneBookEntry[] selectNumbers() {
        usedSelectButton = false;
        setModal(true);
        showSelectButton();
        
        setVisible(true);
        
        if (usedSelectButton && selectedItems.size() > 0) {
            PhoneBookEntry[] res = selectedItems.toArray(new PhoneBookEntry[selectedItems.size()]);
            dispose();
            return res;
        } else {
            return null;
        }
    }

    protected static boolean treePathsEqual(TreePath[] sel1, TreePath[] sel2) {
        if (sel1 == sel2)
            return true;
        if (sel1 == null || sel2 == null)
            return false;
        
        int sel1Len = sel1.length;
        
        if (sel1Len != sel2.length) {
            return false;
        }
        if (Arrays.equals(sel1, sel2))
            return true;
        if (sel1Len == 1) // If both Arrays have length 1, there are no other possible permutations
            return false; 
        
        // Compare all possible permutations (i.e. compare if all elements of the
        //  first array can also be found in the second)
        boolean[] sel2Match = new boolean[sel1Len];
        for (int i = 0; i < sel1Len; i++) {
            boolean found = false;
            for (int j = 0; j < sel1Len; j++) {
                if (!sel2Match[j]) { // Small optimization
                    if (sel1[i].equals(sel2[j])) {
                        found = true;
                        sel2Match[j] = true;
                        break;
                    }
                }
            }
            if (!found) { // If one element of the first array is not in the other -> not equal
                return false;
            }
        }
        return true;
    }
    
    class EntryTextFieldListener implements ActionListener, FocusListener {

        public void actionPerformed(ActionEvent e) {
            updateListEntries();
        }

        public void focusGained(FocusEvent e) {
            // do nothing
        }

        public void focusLost(FocusEvent e) {
            updateListEntries();
        }
        
        private void updateListEntries() { 
            if (selectedItems.size() == 1) {
                readFromTextFields(selectedItems.get(0), true); 
            }
        }
    }
    
    class SearchHelper implements DocumentListener, PBTreeModelListener, ActionListener {
        private boolean eventLock = false;
        
        public void changedUpdate(DocumentEvent e) {
            // do nothing
        }

        public void insertUpdate(DocumentEvent e) {
            if (eventLock)
                return;
            
            performQuickSearch();
        }

        public void removeUpdate(DocumentEvent e) {
            if (eventLock)
                return;
            
            performQuickSearch();
        }

        private void performQuickSearch() {
            String text = searchField.getText();
            treeModel.applyFilter(text);
            clearButton.setEnabled(text.length() > 0);
        }
        
        public void filterWasReset() {
            eventLock = true;
            searchField.setText("");
            clearButton.setEnabled(false);
            eventLock = false;
        }

        public void actionPerformed(ActionEvent e) {
            String actCmd = e.getActionCommand();
            if (actCmd.equals("clear")) {
                searchField.setText("");
            } else if (actCmd.equals("focus")) {
                phoneBookTree.requestFocusInWindow();
            }
        }
        
    }
    
    private class PhonebookMenuActionListener implements ActionListener{
        
        public static final String IMPORT_COMMAND = "pb_import";
        //public static final String SAVEAS_COMMAND = "pb_saveas";
        public static final String OPEN_COMMAND = "pb_open";
        
        public PhoneBookType pbType;
        
        public void actionPerformed(ActionEvent e) {
            try {
                String cmd = e.getActionCommand();

                if (cmd.equals(OPEN_COMMAND)) 
                    doOpen();
                /*else if (cmd.equals(SAVEAS_COMMAND))
                    doSaveAs();*/
                else if (cmd.equals(IMPORT_COMMAND)) 
                    doImport();
                else
                    log.log(Level.WARNING, "Unknown Action command: " + cmd);
            } catch (Exception ex) {
                ExceptionDialog.showExceptionDialog(NewPhoneBookWin.this, Utils._("Error executing the desired action:"), ex);
            }
        }
        
        private void doImport() {
            PhoneBook pb;
            String descriptor;
            
            if (currentPhonebook == null || currentPhonebook.isReadOnly()) 
                return;
            
            pb = pbType.createInstance(NewPhoneBookWin.this);
            
            descriptor = pb.browseForPhoneBook();
            if (descriptor != null) {
                importFromPhonebook(pb, descriptor);
            }
        }
        
        /*private void doSaveAs() {
            
        }*/
        
        private void doOpen() {
            PhoneBook pb;
            String descriptor;
            
//            phoneBook = getCurrentPhoneBook(); 
//            
//            if (pbType.targetClass.isInstance(phoneBook))
//                pb = phoneBook;
//            else
            pb = pbType.createInstance(NewPhoneBookWin.this);
            
            descriptor = pb.browseForPhoneBook();
            if (descriptor != null) {
                addPhoneBook(descriptor);                
            }
        }
        
        public PhonebookMenuActionListener(PhoneBookType pbType) {
            this.pbType = pbType;
        }
    }
}



