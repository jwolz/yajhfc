package yajhfc.phonebook.ui;
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

import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.export.PhoneBookHTMLExporter;
import yajhfc.file.FileFormat;
import yajhfc.filters.AndFilter;
import yajhfc.filters.ConcatStringFilter;
import yajhfc.filters.Filter;
import yajhfc.filters.OrFilter;
import yajhfc.filters.StringFilter;
import yajhfc.filters.StringFilterOperator;
import yajhfc.filters.ui.CustomFilterDialog;
import yajhfc.phonebook.DistributionList;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookEntryList;
import yajhfc.phonebook.PhoneBookException;
import yajhfc.phonebook.PhoneBookFactory;
import yajhfc.phonebook.PhoneBookType;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.PhoneBookTreeModel.PBTreeModelListener;
import yajhfc.phonebook.ui.PhoneBookTreeModel.RootNode;
import yajhfc.print.PhonebooksPrinter;
import yajhfc.util.AbstractQuickSearchHelper;
import yajhfc.util.AcceleratorKeyDialog;
import yajhfc.util.AcceleratorKeys;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.MultiButtonGroup;
import yajhfc.util.ProgressPanel;
import yajhfc.util.ProgressWorker;
import yajhfc.util.SafeJFileChooser;

public final class NewPhoneBookWin extends JDialog implements ActionListener {

    protected static final String FILTER_ACTION_COMMAND = "FilterEntries";

    protected static final String SHOWALL_ACTION_COMMAND = "ShowAllEntries";

    static final Logger log = Logger.getLogger(NewPhoneBookWin.class.getName());
    
    JSplitPane splitPane;
    JTree phoneBookTree;
    PhoneBookTreeModel treeModel;    
    JPanel rightPane, leftPane;

    SingleEntryPhonebookPanel singleEntryPanel;
    DistributionListPhonebookPanel distListPanel;
    
    JTextField textDescriptor;
    JButton buttonBrowse;
    
    JMenu pbMenu, importMenu, openMenu, entryMenu, exportMenu;
    JPopupMenu treePopup;
    
    Action listRemoveAction, addEntryAction, removeEntryAction, searchEntryAction, selectAction;
    Action addDistListAction, viewPopupMenuAction, printAction, exportHTMLAction, closeAction, editAcceleratorsAction;
    
    MultiButtonGroup nameStyleGroup, viewGroup;
    
    ProgressPanel progressPanel;
    
    SearchHelper searchHelper = new SearchHelper();
    
    NewSearchWin searchWin;
    
    boolean usedSelectButton = false;
    
    final List<PhoneBookEntry> selectedItems = new ArrayList<PhoneBookEntry>();
    PhoneBook currentPhonebook = null;
    
    static final int border = 5;
    
    TreeCellRenderer phoneBookRenderer = new PhoneBookRenderer();
    
    boolean allowSavePhonebooks = false;
    
    Map<String,Action> availableActions = new HashMap<String,Action>();
    
    private PhonebookPanel lastPanel;
    void writeToTextFields(PhoneBook phoneBook, List<PhoneBookEntry> pbs) {
        PhonebookPanel panel;
        if (pbs == null || pbs.size() == 0) {
            panel = getPanelFor(null);
            panel.writeToTextFields(null, null);
        } else if (pbs.size() == 1) {
            PhoneBookEntry pbe = pbs.get(0);
            panel = getPanelFor(pbe);
            panel.writeToTextFields(phoneBook, pbe);
        } else {
            panel = distListPanel;
            distListPanel.showMultiSelection(phoneBook, pbs);
        }
        if (panel != lastPanel) {
            if (lastPanel != null)
                lastPanel.setVisible(false);
            panel.setVisible(true);
            lastPanel = panel;
        }
    }
    
    void readFromTextFields(PhoneBookEntry pb, boolean updateOnly) {
        getPanelFor(pb).readFromTextFields(pb, updateOnly);
    }
    
    private PhonebookPanel getPanelFor(PhoneBookEntry pb) {
        if (pb instanceof DistributionList) {
            return distListPanel;
        } else {
            return singleEntryPanel;
        } 
    }
    
    private JPanel getRightPane() {
        if (rightPane == null) {
            double[][] dLay = {
                    {border, TableLayout.FILL, border},
                    {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.FILL, border}
            };
            rightPane = new JPanel(new TableLayout(dLay));
            
            textDescriptor = new JTextField();
            textDescriptor.setEditable(false);
            textDescriptor.setBackground(UIManager.getColor("Label.backgroundColor"));
            textDescriptor.addMouseListener(getDefClPop());
            
            buttonBrowse = new JButton(Utils.loadIcon("general/Open"));
            buttonBrowse.setActionCommand("browse");
            buttonBrowse.addActionListener(this);
                    
            JPanel panelPanel = new JPanel(null);
            panelPanel.setLayout(new OverlayLayout(panelPanel));
            
            singleEntryPanel = new SingleEntryPhonebookPanel(this);
            lastPanel = singleEntryPanel;
            distListPanel    = new DistributionListPhonebookPanel(this);
            distListPanel.setVisible(false);
            panelPanel.add(singleEntryPanel);
            panelPanel.add(distListPanel);
            
            Box box = Box.createHorizontalBox();
            box.add(textDescriptor);
            box.add(buttonBrowse);
            
            rightPane.add(new JLabel(Utils._("Current phone book:")), "1,1");
            rightPane.add(box, "1,2");
            
            rightPane.add(new JSeparator(), "0,4,2,4");
            
            rightPane.add(panelPanel, "1,6");
        }
        return rightPane;
    }
       
            
    private ClipboardPopup getDefClPop() {
        return ClipboardPopup.DEFAULT_POPUP;
    }
    
    public PhoneBook getCurrentPhoneBook() {
        return currentPhonebook;
    }
    
    public List<PhoneBookEntry> getRawSelectedEntries() {
        return selectedItems;
    }
 
    JTree getPhoneBookTree() {
        return phoneBookTree;
    }
    
    List<PhoneBook> getAvailablePhoneBooks() {
        return treeModel.getPhoneBooks();
    }
    
    /**
     * Adds and opens the given phone book asynchronously
     * @param descriptor
     */
    void addPhoneBook(String descriptor) {
        // Try to check if the phone book has already been added:
        for (PhoneBook pb : treeModel.getPhoneBooks()) {
            if (descriptor.equals(pb.getDescriptor())) {
                JOptionPane.showMessageDialog(this, Utils._("This phone book has already been added."), Utils._("Add to list"), JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        PBOpenWorker worker = new PBOpenWorker(descriptor);
        worker.startWork(this, Utils._("Opening phone books..."));
//        PhoneBook phoneBook = null;
//        try {
//            phoneBook = PhoneBookFactory.instanceForDescriptor(descriptor, this);
//            if (phoneBook == null) {
//                JOptionPane.showMessageDialog(this, Utils._("Unknown phone book type selected."), Utils._("Error"), JOptionPane.ERROR_MESSAGE);
//                return;
//            } else {
//                phoneBook.open(descriptor);
//            }
//
//        } catch (PhoneBookException e) {
//            if (!e.messageAlreadyDisplayed())
//                ExceptionDialog.showExceptionDialog(this, Utils._("Error loading the phone book: "), e);
//            //return; // do nothing...
//        }
//        if (phoneBook != null) {
//            treeModel.addPhoneBook(phoneBook);
//            phoneBookTree.expandPath(new TreePath(new Object[] { treeModel.rootNode, phoneBook }));
//        }
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
            String newPB = currentPhonebook.browseForPhoneBook(false);
            if (newPB != null) {
                //                closeCurrentPhoneBook();
                //                addPhoneBook(newPB);
                try {
                    currentPhonebook.close();
                    selectedItems.clear();
                    currentPhonebook.open(newPB);
                    treeModel.refreshPhoneBook(currentPhonebook);
                } catch (PhoneBookException e) {
                    if (!e.messageAlreadyDisplayed())
                        ExceptionDialog.showExceptionDialog(this, Utils._("Error loading the phone book: "), e);
                    closeCurrentPhoneBook();
                }
            }
        }
    }
    
    ProgressWorker closeWorker;
    void closeAndSaveAllPhonebooks(final boolean disposeAfterClose) {
        if (closeWorker != null && closeWorker.isWorking())
            return;
        
        if (!allowSavePhonebooks) {
            if (disposeAfterClose)
                dispose();
            return;
        }
        commitCurrentEdits();        
        
        allowSavePhonebooks = false;
        log.fine("Closing all phone books...");
        // Close phone books in a thread:
        closeWorker = new ProgressWorker() {
            @Override
            public void doWork() {
                List<String> pbList = Utils.getFaxOptions().phoneBooks;
                pbList.clear();

                for (PhoneBook pb : treeModel.getPhoneBooks()) {
                    try {
                        if (Utils.debugMode)
                            log.finest("Closing phone book " + pb.getDescriptor());
                        updateNote(pb.toString());
                        pbList.add(pb.getDescriptor());
                        pb.close();
                    } catch (Exception e) {
                        ExceptionDialog.showExceptionDialog(NewPhoneBookWin.this, Utils._("Error saving a phone book:"), e);
                    }
                }
            };
            
            @Override
            protected void done() {
                if (Utils.debugMode)
                    log.finest("Closed all phone books. pbList=" + Utils.getFaxOptions().phoneBooks);
                
                if (disposeAfterClose) {
                    dispose();
                } else {
                    checkMenuEnable();
                }
                allowSavePhonebooks = true;
                closeWorker = null;
            }
            
        };
        closeWorker.setProgressMonitor(progressPanel);
        
        selectedItems.clear();
        currentPhonebook = null;
        closeWorker.startWork(this, Utils._("Closing phone books..."));
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
        addDistListAction.setEnabled(writeOK && currentPhonebook.supportsDistributionLists());
    
        entryMenu.setEnabled(browseOK);
        importMenu.setEnabled(writeOK);
        exportMenu.setEnabled(selOK || havePB);
        
        listRemoveAction.setEnabled(havePB);
        buttonBrowse.setEnabled(havePB);
        if (selectAction != null)
            selectAction.setEnabled(selOK);
        
        exportHTMLAction.setEnabled(havePB);
    }
    
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("descopen")) {
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
            
            if (descriptor != null) {
                pb.open(descriptor); 
                currentPhonebook.addEntries(pb.getEntries());
                pb.close();
                //currentPhonebook.resort();
            }
        } catch (PhoneBookException e) {
            if (!e.messageAlreadyDisplayed())
                ExceptionDialog.showExceptionDialog(NewPhoneBookWin.this, Utils._("Error loading the phone book: "), e);
        }
    }
       
    protected void exportToPhonebook(PhoneBook pb, String descriptor) {
        try {
            if (pb == null) {
                pb = PhoneBookFactory.instanceForDescriptor(descriptor, NewPhoneBookWin.this);
            }
            if (pb == null) {
                JOptionPane.showMessageDialog(NewPhoneBookWin.this, Utils._("Unsupported phone book format."), Utils._("Error"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (descriptor != null) {
                pb.open(descriptor); 

                if (selectedItems.size() > 0) { 
                    pb.addEntries(selectedItems);
                } else if (currentPhonebook != null) {
                    pb.addEntries(currentPhonebook.getEntries());
                }

                pb.close();
            }
        } catch (PhoneBookException e) {
            if (!e.messageAlreadyDisplayed())
                ExceptionDialog.showExceptionDialog(NewPhoneBookWin.this, Utils._("Error loading the phone book: "), e);
        }
    }
    
    private JComponent createJContentPane() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(createToolBar(), BorderLayout.NORTH);
        contentPane.add(getSplitPane(), BorderLayout.CENTER);
        
        progressPanel = new ProgressPanel();
        progressPanel.setContentComponent(contentPane);
        return progressPanel;
    }
    
    private JToolBar createToolBar() {        
        return searchHelper.getQuickSearchBar(
                null,
                Utils._("Reset quick search and show all phone book entries."),
                searchEntryAction, viewPopupMenuAction);
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
            phoneBookTree.setCellRenderer(phoneBookRenderer);
            phoneBookTree.addTreeSelectionListener(new TreeSelectionListener() {

                private TreePath[] oldSelection = null;

                public void valueChanged(TreeSelectionEvent e) {
                    try {
                        TreePath[] paths = phoneBookTree.getSelectionPaths();
                        if (treePathsEqual(oldSelection, paths)) {
                            return;
                        }

                        commitCurrentEdits();

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
                        writeToTextFields(currentPhonebook, selectedItems);

                        if (currentPhonebook != null) {
                            textDescriptor.setText(currentPhonebook.getDescriptor());
                            textDescriptor.setCaretPosition(0);
                        } else {
                            textDescriptor.setText("<" + Utils._("Multiple or no phone books selected") + ">");
                        }

                        oldSelection = paths;
                    } catch (Exception ex) {
                        ExceptionDialog.showExceptionDialog(phoneBookTree, "", ex);
                    }
                } 
            });
            phoneBookTree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "RemoveEntry");
            phoneBookTree.getActionMap().put("RemoveEntry", removeEntryAction);
            phoneBookTree.setTransferHandler(new TransferHandler() {
                @Override
                public boolean canImport(JComponent comp,
                        DataFlavor[] transferFlavors) {
                    return (Utils.indexOfArray(transferFlavors, PBEntryTransferable.PHONEBOOKENTRY_FLAVOR) >= 0); /* &&
                        ((dropItem instanceof PhoneBook && addEntryAction.isEnabled()) || (dropItem instanceof DistributionList));*/
                }
                
                @Override
                protected Transferable createTransferable(JComponent c) {
                    return new PBEntryTransferable(new ArrayList<PhoneBookEntry>(selectedItems));
                }
                
                private Object getDropItem() {
                    Point mousePos = phoneBookTree.getMousePosition();
                    if (mousePos == null)
                        return null;
                    
                    int row = phoneBookTree.getRowForLocation(mousePos.x, mousePos.y);
                    if (row >= 0)
                        return phoneBookTree.getPathForRow(row).getLastPathComponent();
                    else
                        return null;
                }
                
                @Override
                public int getSourceActions(JComponent c) {
                    return (selectedItems.size() > 0) ? COPY : NONE;
                }
                
                @SuppressWarnings("unchecked")
                @Override
                public boolean importData(JComponent comp, Transferable t) {
                    if (t.isDataFlavorSupported(PBEntryTransferable.PHONEBOOKENTRY_FLAVOR)) {
                        try {
                            List<PhoneBookEntry> entries = (List<PhoneBookEntry>)t.getTransferData(PBEntryTransferable.PHONEBOOKENTRY_FLAVOR);
                            Object dropItem = getDropItem();
                            if (dropItem instanceof PhoneBookEntryList) {
                                PhoneBookEntryList target = (PhoneBookEntryList)dropItem;
                                if (!target.isReadOnly()) {
                                    target.addEntries(entries);
                                    if (target instanceof DistributionList) {
                                        ((DistributionList)target).commit();
                                    }
                                    return true;
                                }
                            }
                            return false;
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "Error importing drag data", e);
                            return false;
                        }
                        
                    } else {
                        return super.importData(comp, t);
                    }
                }
            });
            phoneBookTree.setDragEnabled(true);
            
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
            exportMenu = new JMenu(Utils._("Export"));
            exportMenu.setIcon(Utils.loadIcon("general/Export"));

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

                if (pbt.canExport()) {
                    mi = new JMenuItem(menuText);
                    mi.setActionCommand(PhonebookMenuActionListener.EXPORT_COMMAND);
                    mi.addActionListener(pbmal);
                    exportMenu.add(mi);
                }
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

            pbMenu = new JMenu(Utils._("Phone book"));
            pbMenu.add(openMenu);
            pbMenu.add(importMenu);
            pbMenu.add(exportMenu);
            pbMenu.addSeparator();
            pbMenu.add(printAction);
            pbMenu.add(exportHTMLAction);
            pbMenu.addSeparator();
            pbMenu.add(new JMenuItem(listRemoveAction));
            pbMenu.addSeparator();
            pbMenu.add(new JMenuItem(editAcceleratorsAction));
            pbMenu.addSeparator();
            pbMenu.add(new JMenuItem(closeAction));
        }
        return pbMenu;
    }
    
    private JMenu getEntryMenu() {
        if (entryMenu == null) {
            entryMenu = new JMenu(Utils._("Entry"));

            entryMenu.add(new JMenuItem(addEntryAction));
            entryMenu.add(new JMenuItem(addDistListAction));
            entryMenu.add(new JMenuItem(removeEntryAction));
            entryMenu.addSeparator();
            JMenu viewMenu = new JMenu(nameStyleGroup.label);
            for (JMenuItem item : nameStyleGroup.createMenuItems()) {
                viewMenu.add(item);
            }
            entryMenu.add(viewMenu);
            entryMenu.addSeparator();
            entryMenu.add(new JMenuItem(searchEntryAction));
            entryMenu.addSeparator();
            for (JRadioButtonMenuItem item : viewGroup.createMenuItems()) {
                entryMenu.add(item);
            }
        }
        return entryMenu;
    }
    
    private JMenuBar getMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getPhonebookMenu());
        menuBar.add(getEntryMenu());
        return menuBar;
    }
    
    private void putAvailableAction(String key, Action act) {
        if (availableActions.put(key, act) != null) {
            log.severe("Action " + key + " already existed!");
        }
        act.putValue(Action.ACTION_COMMAND_KEY, key);
    }
    
    private void putAvailableAction(Action act) {
        String key = (String)act.getValue(Action.ACTION_COMMAND_KEY);
        if (availableActions.put(key, act) != null) {
            log.severe("Action " + key + " already existed!");
        }
    }
    
    private void createActions() {
        addEntryAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (currentPhonebook == null || currentPhonebook.isReadOnly()) 
                    return;
                
                PhoneBookEntry pbe = currentPhonebook.addNewEntry();
                phoneBookTree.setSelectionPath(new TreePath(new Object[] { treeModel.getRootNode(), currentPhonebook, pbe }));
            }
        };
        addEntryAction.putValue(Action.NAME, Utils._("Add"));
        addEntryAction.putValue(Action.SMALL_ICON, Utils.loadCustomIcon("pbaddentry.png"));
        addEntryAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Add new entry"));
        addEntryAction.setEnabled(false);
        putAvailableAction("AddEntry", addEntryAction);
        
        addDistListAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (currentPhonebook == null || currentPhonebook.isReadOnly()) 
                    return;
                
                DistributionList dl = currentPhonebook.addDistributionList();
                if (selectedItems.size() > 1) {
                    dl.addEntries(resolveDistributionLists(selectedItems));
                }
                phoneBookTree.setSelectionPath(new TreePath(new Object[] { treeModel.getRootNode(), currentPhonebook, dl }));
            }
        };
        addDistListAction.putValue(Action.NAME, Utils._("Add distribution list"));
        addDistListAction.putValue(Action.SMALL_ICON, Utils.loadCustomIcon("pbadddistlist.png"));
        addDistListAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Add a new distribution list"));
        addDistListAction.setEnabled(false);
        putAvailableAction("AddDistList", addDistListAction);
        
        removeEntryAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (selectedItems.size() == 0)
                    return;
                
                if (JOptionPane.showConfirmDialog(NewPhoneBookWin.this, Utils._("Do you want to delete the selected entries?"), Utils._("Delete entries"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    PhoneBookEntry[] entries = selectedItems.toArray(new PhoneBookEntry[selectedItems.size()]);
                    
                    for (PhoneBookEntry pbe : entries) {
                        // Clear selection to prevent saving of the current edits
                        selectedItems.clear();
                        pbe.delete();
                    }
                    selectedItems.clear();
                    writeToTextFields(null, null);
                }
            }
        };
        removeEntryAction.putValue(Action.NAME, Utils._("Delete"));
        removeEntryAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Delete"));
        removeEntryAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Delete selected entry"));
        removeEntryAction.setEnabled(false);
        putAvailableAction("RemoveEntry", removeEntryAction);
        
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
        putAvailableAction("SearchEntry", searchEntryAction);
        
        listRemoveAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(NewPhoneBookWin.this, Utils._("Do you want to remove the current phone book from the list?"), Utils._("Remove from list"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    closeCurrentPhoneBook();
                }
            }
        };
        listRemoveAction.putValue(Action.NAME, Utils._("Remove from list"));
        listRemoveAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Remove"));
        putAvailableAction("ListRemove", listRemoveAction);
        
        nameStyleGroup = new MultiButtonGroup() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                treeModel.setNameToStringRule(NameRule.valueOf(e.getActionCommand()));
            }
        };
        nameStyleGroup.label = Utils._("Display style");
        for (NameRule rule : NameRule.values()) {
            putAvailableAction(nameStyleGroup.addItem(rule.getDisplayName(), rule.name()));
        }
        nameStyleGroup.setSelectedActionCommand(Utils.getFaxOptions().phonebookDisplayStyle.name());
        
        viewGroup = new MultiButtonGroup() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                if (SHOWALL_ACTION_COMMAND.equals(cmd)) {
                    searchHelper.setUserFilter(null);
                } else if (FILTER_ACTION_COMMAND.equals(cmd)) {
                    CustomFilterDialog<PhoneBookEntry, PBEntryField> cfd = 
                        new CustomFilterDialog<PhoneBookEntry, PBEntryField>(NewPhoneBookWin.this,
                                Utils._("Filter phone book entries"),
                                Utils._("Only display phone book items fulfilling:"),
                                Utils._("You have entered no filtering conditions. Do you want to show all phone book entries instead?"),
                                Utils._("Please enter a valid date/time!"),
                                PBEntryField.filterKeyList,
                                searchHelper.getUserFilter());
                    cfd.setVisible(true);
                    if (cfd.okClicked) {
                        searchHelper.setUserFilter(cfd.returnValue);
                    } else {
                        if (searchHelper.getUserFilter() == null) {
                            setSelectedActionCommand(SHOWALL_ACTION_COMMAND);
                        }
                    }
                }
            }
        };
        
        putAvailableAction(viewGroup.addItem(Utils._("Show all entries"), SHOWALL_ACTION_COMMAND));
        putAvailableAction(viewGroup.addItem(Utils._("Filter entries") + "...", FILTER_ACTION_COMMAND));
        viewGroup.setSelectedActionCommand(SHOWALL_ACTION_COMMAND);
        
        viewPopupMenuAction = new ExcDialogAbstractAction() {
            private JPopupMenu popup;
            
            private JPopupMenu getPopup() {
                if (popup == null) {
                    popup = new JPopupMenu();
                    for (JMenuItem item : viewGroup.createMenuItems()) {
                        popup.add(item);
                    }
                }
                return popup;
            }
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                if (e.getSource() instanceof Component) {
                    Component sourceComp = (Component)e.getSource();
                    
                    getPopup().show(sourceComp, 0, sourceComp.getHeight());
                } 
            }
        };
        viewPopupMenuAction.putValue(Action.SMALL_ICON, Utils.loadCustomIcon("filter.png"));
        viewPopupMenuAction.putValue(Action.SHORT_DESCRIPTION, Utils._("Filter phone book items"));
        
        printAction = new ExcDialogAbstractAction() {
			@Override
			protected void actualActionPerformed(ActionEvent e) {
				PhonebooksPrinter.printPhonebooks(NewPhoneBookWin.this, treeModel.getPhoneBooks(), currentPhonebook, selectedItems, treeModel.isShowingFilteredResults());
			}
		};
		printAction.putValue(Action.NAME, _("Print phone books") + "...");
		printAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Print"));
        putAvailableAction("Print", printAction);
		
        exportHTMLAction = new ExcDialogAbstractAction() {
            private JFileChooser fileChooser;
            
            private JFileChooser getFileChooser() {
                if (fileChooser == null) {
                    fileChooser = new SafeJFileChooser();
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.addChoosableFileFilter(new ExampleFileFilter(FileFormat.HTML.getPossibleExtensions(), FileFormat.HTML.getDescription()));
                    if (Utils.getFaxOptions().lastExportSavePath != null)
                        fileChooser.setCurrentDirectory(new File(Utils.getFaxOptions().lastExportSavePath));
                }
                return fileChooser;
            }

            /* (non-Javadoc)
             * @see yajhfc.util.ExcDialogAbstractAction#actualActionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                if (currentPhonebook == null)
                    return;
                
                JFileChooser chooser = getFileChooser();
                if (chooser.showSaveDialog(NewPhoneBookWin.this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                Utils.setWaitCursor(null);
                try {
                    Utils.getFaxOptions().lastExportSavePath = chooser.getCurrentDirectory().getAbsolutePath();
                    File selectedFile = Utils.getSelectedFileFromSaveChooser(chooser);
                    
                    exportPhonebookToHTML(NewPhoneBookWin.this, currentPhonebook, selectedFile);
                } catch (Exception ex) {
                    ExceptionDialog.showExceptionDialog(NewPhoneBookWin.this, Utils._("Error saving the table:"), ex);
                } finally {
                    Utils.unsetWaitCursor(null);
                }
            }
            
            private void exportPhonebookToHTML(final Dialog parent, final PhoneBook pb, final File selectedFile) {
                Utils.setWaitCursor(null);
                ProgressWorker pw = new ProgressWorker() {
                    protected void initialize() {
                        this.progressMonitor = NewPhoneBookWin.this.progressPanel;
                    }
                    
                    @Override
                    public void doWork() {
                        try {
                            updateNote(_("Exporting..."));
                            String title = pb.toString();
                            String footer = "";

                            PhoneBookHTMLExporter hexp = new PhoneBookHTMLExporter();
                            hexp.saveToFile(selectedFile, new PBEntryFieldTableModel(Collections.<PBEntryFieldContainer>unmodifiableList(pb.getEntries())), 
                                    title, footer);
                        } catch (Exception ex) {
                            ExceptionDialog.showExceptionDialog(parent, Utils._("Error saving the table:"), ex);
                        } 
                    }
                    
                    protected void done() {
                        Utils.unsetWaitCursor(null);
                    }
                };
                pw.startWork(parent, _("Export to HTML"));
            }
        };
        exportHTMLAction.putValue(Action.NAME, _("Save as HTML") + "...");
        exportHTMLAction.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Save"));
        putAvailableAction("ExportHTML", exportHTMLAction);
        
        
        closeAction = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                usedSelectButton = false;
                closeAndSaveAllPhonebooks(true);
            }
        };
        closeAction.putValue(Action.NAME, Utils._("Close"));
        closeAction.putValue(Action.SMALL_ICON, Utils.loadCustomIcon("close.gif"));
        putAvailableAction("Close", closeAction);
        
        editAcceleratorsAction = new ExcDialogAbstractAction() {
            public void actualActionPerformed(java.awt.event.ActionEvent e) {
                AcceleratorKeyDialog akd = new AcceleratorKeyDialog(NewPhoneBookWin.this, availableActions.values(), AcceleratorKeys.DEFAULT_PBWIN_MAPPING);
                akd.setVisible(true);
                if (akd.modalResult) {
                    AcceleratorKeys.saveToMap(Utils.getFaxOptions().pbwinKeyboardAccelerators, availableActions);
                }
            }
        };
        editAcceleratorsAction.putValue(Action.NAME, _("Edit keyboard shortcuts") + "...");
        editAcceleratorsAction.putValue(Action.SHORT_DESCRIPTION, _("Customize the keyboard shortcuts"));
        putAvailableAction("EditAccelerators", editAcceleratorsAction);
    }
    
    private void initialize() {
        createActions();
        setContentPane(createJContentPane());
        setJMenuBar(getMenu());
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setTitle(Utils._("Phone book"));
        
        addWindowListener(new WindowAdapter() {
            private boolean saved = false;

            @Override
            public void windowClosed(WindowEvent e) {
                if (!saved) {
                    savePhonebooks();
                }
                    
                Utils.getFaxOptions().phoneWinBounds = getBounds();
                Utils.getFaxOptions().phonebookDisplayStyle = NameRule.valueOf(nameStyleGroup.getSelectedActionCommand());

                if (searchWin != null) {
                    searchWin.dispose();
                    searchWin = null;
                }
            }
            
            private void savePhonebooks() {
                try {
                    closeAndSaveAllPhonebooks(true);
                    saved = true;
                } catch (Exception e1) {
                    ExceptionDialog.showExceptionDialog(NewPhoneBookWin.this, Utils._("Error closing the phone books:"), e1);
                }    
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                savePhonebooks();
            }
        });
        
        FaxOptions fopts = Utils.getFaxOptions();
        
        AcceleratorKeys.loadFromMap(fopts.pbwinKeyboardAccelerators, availableActions);
        
        if (fopts.phoneWinBounds != null)
            this.setBounds(fopts.phoneWinBounds);
        else {
            this.setSize(640, 480);
            //this.setLocationByPlatform(true);
            Utils.setDefWinPos(this);
        }
        
        if (fopts.phoneBooks.size() > 0) {
            if (Utils.debugMode)
                log.finest("Phonebooks found: " + fopts.phoneBooks);
            
            Collections.sort(fopts.phoneBooks); // Bring the phone books in a defined order
            for (String pbdesc : fopts.phoneBooks) {
                if (Utils.debugMode)
                    log.finest("Adding phone book: " + pbdesc);
                allowSavePhonebooks = false;
                addPhoneBook(pbdesc);
            }
        } else {
            log.finest("No phonebooks found.");
            addPhoneBook(PhoneBookFactory.getDefaultPhonebookDescriptor());
        }
        
//        if (fopts.lastSelectedPhonebook >= 0 && fopts.lastSelectedPhonebook < tabPhonebooks.getTabCount())
//            tabPhonebooks.setSelectedIndex(fopts.lastSelectedPhonebook);
        checkMenuEnable();
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
    
    public List<PhoneBookEntry> selectNumbers() {
        usedSelectButton = false;
        setModal(true);
        showSelectButton();
        
        setVisible(true);
        
        if (usedSelectButton && selectedItems.size() > 0) {
            List<PhoneBookEntry> res = resolveDistributionLists(selectedItems);
            dispose();
            return res;
        } else {
            return null;
        }
    }

    void commitCurrentEdits() {
        if (selectedItems.size() == 1) {
            readFromTextFields(selectedItems.get(0), false);
        }
    }

    static List<PhoneBookEntry> resolveDistributionLists(List<PhoneBookEntry> entries) {
        List<PhoneBookEntry> result = new ArrayList<PhoneBookEntry>(entries.size() + 20);
        for (PhoneBookEntry entry : entries) {
            if (entry instanceof DistributionList) {
                result.addAll(((DistributionList)entry).getEntries());
            } else {
                result.add(entry);
            }
        }
        return result;
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

    
    public static class PhoneBookRenderer extends DefaultTreeCellRenderer {
        private ImageIcon allPhoneBooksIcon = Utils.loadIcon("general/Bookmarks"); 
        private ImageIcon phoneBookIcon = Utils.loadCustomIcon("phonebook.png");
        private ImageIcon entryIcon = Utils.loadCustomIcon("pbentry.png");
        private ImageIcon distlistIcon = Utils.loadCustomIcon("pbdistlist.png");

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                    row, hasFocus);
            if (value instanceof DistributionList) {
                setIcon(distlistIcon);
            } else if (value instanceof RootNode) {
                setIcon(allPhoneBooksIcon);
            }
            return this;
        }
        
        public PhoneBookRenderer() {
            setLeafIcon(entryIcon);
            setOpenIcon(phoneBookIcon);
            setClosedIcon(phoneBookIcon);
        }
    }

    class SearchHelper extends AbstractQuickSearchHelper implements PBTreeModelListener {
        
        protected Filter<PhoneBookEntry,PBEntryField> quickSearchFilter;
        protected Filter<PhoneBookEntry,PBEntryField> userFilter;
        
        public void setUserFilter(
                Filter<PhoneBookEntry, PBEntryField> userFilter) {
            this.userFilter = userFilter;
            refreshFilter();
        }
        
        public Filter<PhoneBookEntry, PBEntryField> getUserFilter() {
            return userFilter;
        }
        
        public Filter<PhoneBookEntry,PBEntryField> createQuickSearchFilter(String quickSearchVal) {
            if (quickSearchVal == null || quickSearchVal.length() == 0) {
                return null;
            } else {
                OrFilter<PhoneBookEntry,PBEntryField> filter = new OrFilter<PhoneBookEntry, PBEntryField>();
                filter.addChild(new ConcatStringFilter<PhoneBookEntry, PBEntryField>(
                        PBEntryField.class, 
                        new Object[] { PBEntryField.GivenName, " ", PBEntryField.Name, ", ", PBEntryField.Department, ", ", PBEntryField.Company },
                        StringFilterOperator.CONTAINS, quickSearchVal, false));
                filter.addChild(new StringFilter<PhoneBookEntry, PBEntryField>(PBEntryField.FaxNumber, StringFilterOperator.CONTAINS, quickSearchVal, false));
                return filter;
            }
        }
        
        @SuppressWarnings("unchecked")
        private void refreshFilter() {
            if (quickSearchFilter == null) {
                treeModel.applyFilter(userFilter);
            } else {
                if (userFilter == null) {
                    treeModel.applyFilter(quickSearchFilter);
                } else {
                    treeModel.applyFilter(new AndFilter<PhoneBookEntry, PBEntryField>(userFilter, quickSearchFilter));
                }
            }
        }
        
        protected void performActualQuickSearch() {
            quickSearchFilter = createQuickSearchFilter(textQuickSearch.getText());
            refreshFilter();
        }
        
        public void filterWasReset() {
            eventLock = true;
            textQuickSearch.setText("");
            clearQuickSearchButton.setEnabled(false);
            viewGroup.setSelectedActionCommand(SHOWALL_ACTION_COMMAND);
            userFilter = null;
            quickSearchFilter = null;
            eventLock = false;
        }
        
        @Override
        protected Component getFocusComponent() {
            return phoneBookTree;
        }
    }
    
    private class PhonebookMenuActionListener implements ActionListener{
        
        public static final String IMPORT_COMMAND = "pb_import";
        public static final String EXPORT_COMMAND = "pb_export";
        public static final String OPEN_COMMAND = "pb_open";
        
        public PhoneBookType pbType;
        
        public void actionPerformed(ActionEvent e) {
            try {
                String cmd = e.getActionCommand();

                if (cmd.equals(OPEN_COMMAND)) 
                    doOpen();
                else if (cmd.equals(EXPORT_COMMAND))
                    doExport();
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
            
            descriptor = pb.browseForPhoneBook(false);
            if (descriptor != null) {
                importFromPhonebook(pb, descriptor);
            }
        }
        
        private void doExport() {
            if (selectedItems.size() == 0 && currentPhonebook == null) {
                return;
            }
            
            PhoneBook pb = pbType.createInstance(NewPhoneBookWin.this);
            
            String descriptor = pb.browseForPhoneBook(true);
            if (descriptor != null) {
                exportToPhonebook(pb, descriptor);
            }
        }
        
        private void doOpen() {
            PhoneBook pb;
            String descriptor;
            
//            phoneBook = getCurrentPhoneBook(); 
//            
//            if (pbType.targetClass.isInstance(phoneBook))
//                pb = phoneBook;
//            else
            if (Utils.debugMode)
                log.fine("Opening new phone book of type " + pbType);
            pb = pbType.createInstance(NewPhoneBookWin.this);
            
            descriptor = pb.browseForPhoneBook(false);
            if (descriptor != null) {
                if (Utils.debugMode)
                    log.fine("Adding phone book " + descriptor);
                addPhoneBook(descriptor);                
            }
        }
        
        public PhonebookMenuActionListener(PhoneBookType pbType) {
            this.pbType = pbType;
        }
    }
    
    static class PBEntryTransferable implements Transferable {
        public static final DataFlavor PHONEBOOKENTRY_FLAVOR = new DataFlavor(List.class, "Phone book entries");
        protected final List<PhoneBookEntry> items;

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (flavor == PHONEBOOKENTRY_FLAVOR) {
                return items;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                return Utils.listToString(items, "\n");
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { PHONEBOOKENTRY_FLAVOR, DataFlavor.stringFlavor };
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return (flavor == PHONEBOOKENTRY_FLAVOR) || DataFlavor.stringFlavor.equals(flavor);
        }

        public PBEntryTransferable(List<PhoneBookEntry> items) {
            super();
            this.items = items;
        }
    }
    
    int openCounter = 0;
    class PBOpenWorker extends ProgressWorker {
        protected PhoneBook phoneBook = null;
        protected String descriptor;
        
        @Override
        public void doWork() {
            synchronized (NewPhoneBookWin.this) {
                if (openCounter++ == 0) {
                    updateNote(Utils._("Loading..."));
                }
            }
            try {
                if (Utils.debugMode)
                    log.fine("Opening phone book " + descriptor);
                phoneBook = PhoneBookFactory.instanceForDescriptor(descriptor, NewPhoneBookWin.this);
                
                if (phoneBook == null) {
                    showMessageDialog(Utils._("Unknown phone book type selected."), Utils._("Error"), JOptionPane.ERROR_MESSAGE);
                    log.info("Unknown phone book type selected.");
                    return;
                } else {
                    phoneBook.open(descriptor);
                }
                if (Utils.debugMode)
                    log.fine("Successfully opened phone book " + descriptor);
            } catch (PhoneBookException e) {
                if (!e.messageAlreadyDisplayed())
                    showExceptionDialog(Utils._("Error loading the phone book: "), e);
                //return; // do nothing...
            }
        }
     
        @Override
        protected void done() {
            if (phoneBook != null) {
                treeModel.addPhoneBook(phoneBook);
                if (Utils.debugMode)
                    log.fine("Added phone book to tree: " + descriptor);
            }
            synchronized (NewPhoneBookWin.this) {
                if (--openCounter == 0) {
                    treeModel.sortPhonebooks();
                    for (PhoneBook pb : treeModel.getPhoneBooks()) {
                        if (Utils.debugMode)
                            log.finest("Expanding tree node for phone book " + pb.getDescriptor());
                        phoneBookTree.expandPath(new TreePath(new Object[] { treeModel.getRootNode(), pb }));
                    }
                    if (phoneBookTree.getSelectionPath() == null) {
                        phoneBookTree.setSelectionRow(1);
                    }
                    progressMonitor.close();
                    
                    allowSavePhonebooks = true;
                }
            }
        }
        
        public PBOpenWorker(String descriptor) {
            this.descriptor = descriptor;
            this.closeOnExit = false;
            this.progressMonitor = progressPanel;
        }
    }
}



