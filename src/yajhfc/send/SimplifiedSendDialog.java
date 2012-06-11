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
package yajhfc.send;

import static yajhfc.Utils.addWithLabel;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import yajhfc.FaxNotification;
import yajhfc.FaxOptions;
import yajhfc.FaxResolution;
import yajhfc.FileTextField;
import yajhfc.HylaModem;
import yajhfc.PaperSize;
import yajhfc.SenderIdentity;
import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.file.FileConverters;
import yajhfc.file.FormattedFile;
import yajhfc.file.textextract.FaxnumberExtractor;
import yajhfc.model.IconMap;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.NewPhoneBookWin;
import yajhfc.phonebook.ui.PBEntryFieldTableModel;
import yajhfc.server.Server;
import yajhfc.server.ServerManager;
import yajhfc.server.ServerOptions;
import yajhfc.util.AsyncComboBoxOrListModel;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.JTableTABAction;
import yajhfc.util.LimitedPlainDocument;
import yajhfc.util.ListComboModel;
import yajhfc.util.ProgressPanel;
import yajhfc.util.ProgressWorker;
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
final class SimplifiedSendDialog extends JDialog implements SendWinControl {
    protected static final int FAXNUMBER_LRU_COUNT = 10;
    static final Logger log = Logger.getLogger(SimplifiedSendDialog.class.getName());
    
    Server server;
    SendController sendController;
    
    protected JPanel contentPane, advancedPane;
    protected ProgressPanel progressPanel;
    protected JButton buttonCancel;
    protected FileTextField ftfFileName;
    protected TextFieldList<HylaTFLItem> tflFiles;
    protected JButton buttonAdvanced;
    protected JTable tableNumbers;
    protected PBEntryFieldTableModel numberTableModel;
    protected JComboBox comboNumber;
    protected ListComboModel<String> comboNumberModel;
    protected Action actSend, actPreview, actPhonebook, actFromFile;
    
    protected JButton buttonCustomProps;
    
    protected JComboBox comboResolution;
    protected JComboBox comboPaperSize;
    protected JComboBox comboNotification;
    protected JComboBox comboModem;
    protected JComboBox comboIdentity;
    protected JLabel labelServer;
    protected JComboBox comboServer;
    
    protected JSpinner spinKillTime;
    protected JSpinner spinMaxTries;
    
    protected JTextField textSubject;
    protected JTextArea textComments;
    protected JCheckBox checkUseCover;
    protected JCheckBox checkCustomCover;
    protected JCheckBox checkArchiveJob;
    protected FileTextField ftfCustomCover;
    protected TimeToSendEntry ttsEntry;
    
    protected Action actAddNumber, actRemoveNumber, actCustomProps;
    
    protected AsyncComboBoxOrListModel<HylaModem> modemModel;
    protected String modemToSet;
    
    protected boolean isAdvancedView = false;
    protected boolean initiallyHideFiles = false;
    protected boolean modalResult = false;
    protected boolean identitySelectedByUser = false;
    
    private static final int border = 10;
    
    private static final String ADVANCED_TEXT = Utils._(">> Advanced");
    private static final String SIMPLIFIED_TEXT = Utils._("<< Simplified");
    
    /**
     * @param owner
     * @throws HeadlessException
     */
    public SimplifiedSendDialog(Server server, Frame owner, boolean initiallyHideFiles) throws HeadlessException {
        super(owner, Utils._("Send fax"), true);
        this.server = server;
        this.initiallyHideFiles = initiallyHideFiles;
        initialize();
    }

    public boolean getModalResult() {
        return modalResult;
    }

    public Window getWindow() {
        return this;
    }

    private void initialize() {
        sendController = new SendController(server, this, false);
        
        setContentPane(createContentPane());
        this.setSize(640, initiallyHideFiles ? 400 : 480);
        createAdvancedPane();
        setAdvancedView(Utils.getFaxOptions().sendWinIsAdvanced);
        
        sendController.setProgressMonitor(progressPanel);
        sendController.addSendControllerListener(new SendControllerListener() {
           public void sendOperationComplete(boolean success) {
               actSend.setEnabled(true);
               SimplifiedSendDialog.this.setEnabled(true);
            } 
        });
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                final FaxOptions faxOptions = Utils.getFaxOptions();
                faxOptions.sendWinBounds = getBounds();
                faxOptions.sendWinIsAdvanced = isAdvancedView;
                List<String> numberLRU = faxOptions.faxNumbersLRU;
                numberLRU.clear();
                numberLRU.addAll(comboNumberModel.getList());
            }            
        });
        
        if (Utils.getFaxOptions().sendWinBounds != null)
            this.setBounds(Utils.getFaxOptions().sendWinBounds);
        else
            Utils.setDefWinPos(this);

        enableCoverComps(Utils.getFaxOptions().useCover);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    protected JComponent createContentPane() {
        final double verticalAreaPart = initiallyHideFiles ? (0.5) : (1.0/3.0);
        double[][] tablelay = {
                { border, TableLayout.FILL, border, TableLayout.PREFERRED, border },
                { border, initiallyHideFiles ? 0 : verticalAreaPart , initiallyHideFiles ? 0 : border,  TableLayout.FILL, border, TableLayout.PREFERRED, border,
                        TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, verticalAreaPart*0.5, border, TableLayout.PREFERRED, border}
        };
        
        progressPanel = new ProgressPanel();
        contentPane = new JPanel(new TableLayout(tablelay));

        actSend = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                saveSettingsToSendController();

                if (sendController.validateEntries()) {
                    sendController.sendFax();
                    
                    actSend.setEnabled(false);
                    SimplifiedSendDialog.this.setEnabled(false);
                    modalResult = true;
                }
            }
        };
        actSend.putValue(Action.NAME, Utils._("Send"));
        actSend.putValue(Action.SMALL_ICON, Utils.loadIcon("general/SendMail"));

        JButton buttonSend = new JButton(actSend);

        actPreview = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                saveSettingsToSendController();

                boolean wantCover = checkUseCover.isSelected();
                if ((tflFiles.model.getSize() == 0) && 
                        (!wantCover || (wantCover && sendController.getNumbers().size() == 0))) {
                    JOptionPane.showMessageDialog(SimplifiedSendDialog.this, Utils._("Nothing to preview! (Neither a cover page nor a file to send has been selected.)"), Utils._("Preview"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // If nothing is selected, use the first element for preview
                PBEntryFieldContainer coverItem;
                int selIdx = tableNumbers.getSelectedRow();
                if (selIdx < 0) {
                    if (sendController.getNumbers().size() > 0) {
                        coverItem = sendController.getNumbers().get(0);
                    } else {
                        coverItem = null;
                    }
                } else {
                    coverItem = sendController.getNumbers().get(selIdx);
                }
                sendController.previewFax(coverItem);
            }
        };
        actPreview.putValue(Action.NAME, Utils._("Preview"));
        actPreview.putValue(Action.SMALL_ICON, Utils.loadIcon("general/PrintPreview"));
        JButton buttonPreview = new JButton(actPreview);

        actCustomProps = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                JobPropsEditorDialog editDlg = new JobPropsEditorDialog(SimplifiedSendDialog.this, sendController.getCustomProperties());
                editDlg.setVisible(true);                
            }
        };
        actCustomProps.putValue(Action.NAME, Utils._("Job properties") + "...");
        
        buttonCustomProps = new JButton(actCustomProps);
        buttonCustomProps.setVisible(false);
        
        CancelAction actCancel = new CancelAction(this);
        buttonCancel = actCancel.createCancelButton();

        buttonAdvanced = new JButton(SIMPLIFIED_TEXT);
        // Set the preferred size large enough to hold both possible texts
        Dimension prefSize1 = buttonAdvanced.getPreferredSize();
        buttonAdvanced.setText(ADVANCED_TEXT);
        Dimension prefSize2 = buttonAdvanced.getPreferredSize();
        buttonAdvanced.setPreferredSize(new Dimension(Math.max(prefSize1.width, prefSize2.width), Math.max(prefSize1.height, prefSize2.height)));
        
        buttonAdvanced.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               setAdvancedView(!isAdvancedView);
            } 
        });
        
        comboIdentity = new JComboBox(new ListComboModel<SenderIdentity>(Utils.getFaxOptions().identities));
        comboIdentity.setSelectedItem(sendController.fromIdentity);
        comboIdentity.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               identitySelectedByUser = true;
            } 
        });
        comboServer = new JComboBox(new ListComboModel<Server>(ServerManager.getDefault().getServers()));
        comboServer.setSelectedItem(server);
        comboServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setServer((Server)comboServer.getSelectedItem());
            }
        });
        
        JPanel buttonPanel = new JPanel(new TableLayout(
                new double[][] {
                        { TableLayout.FILL },
                        { TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border}
                }
                ), false);
        buttonPanel.add(buttonSend, "0,0");
        buttonPanel.add(buttonPreview, "0,2");
        buttonPanel.add(buttonCancel, "0,4");
        buttonPanel.add(buttonCustomProps, "0,6");
        labelServer = addWithLabel(buttonPanel, comboServer, Utils._("Server") + ':', "0,9");
        addWithLabel(buttonPanel, comboIdentity, Utils._("Identity") + ':', "0,12");
        buttonPanel.add(buttonAdvanced, "0,14");
        
        comboServer.setVisible(false);
        labelServer.setVisible(false);

        checkUseCover = new JCheckBox(Utils._("Use cover page"));
        checkUseCover.setSelected(Utils.getFaxOptions().useCover);
        checkUseCover.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableCoverComps(checkUseCover.isSelected());
            };
        });
        
        textSubject = new JTextField();
        textSubject.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        textSubject.setDocument(new LimitedPlainDocument(60));
        
        textComments = new JTextArea();
        textComments.setWrapStyleWord(true);
        textComments.setLineWrap(true);
        textComments.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        JScrollPane scrollComments = new JScrollPane(textComments, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        if (!initiallyHideFiles) {
            contentPane.add(createFileEntryList(), "1,1, f, f");
        }
        contentPane.add(createNumberEntryList(), "1,3, f, f");
        contentPane.add(checkUseCover, "1,5");
        addWithLabel(contentPane, textSubject, Utils._("Subject:"), "1,8");
        addWithLabel(contentPane, scrollComments, Utils._("Comments:"), "1,10");
        
        contentPane.add(buttonPanel, "3,1,3,12,f,f");
        
        progressPanel.setContentComponent(contentPane);
        return progressPanel;
    }
    
    void extractRecipientsFor(final HylaTFLItem item) {
        ProgressWorker pw = new ProgressWorker() {
            private Set<String> numbers;
            
            @Override
            public void doWork() {
                try {
                    updateNote(Utils._("Extracting recipients..."));
                    FaxnumberExtractor extractor = new FaxnumberExtractor();
                    numbers = new TreeSet<String>();
                    extractor.extractFromMultipleDocuments(Collections.singleton(item), numbers);
                } catch (Exception e) {
                    showExceptionDialog(Utils._("Error extracting recipients"), e);
                    numbers = null;
                } 
            }
            
            @Override
            protected void done() {
                if (numbers != null) {
                    for (String number : numbers) {
                        PBEntryFieldContainer pbe = new DefaultPBEntryFieldContainer("");
                        pbe.setField(PBEntryField.FaxNumber, number);
                        getRecipients().add(pbe);
                    }
                }
            }
        };
        pw.setProgressMonitor(progressPanel);
        pw.startWork(this, Utils._("Extracting recipients..."));
    }
    
    protected Component createFileEntryList() {
        Box box = Box.createVerticalBox();
        
        ftfFileName = new FileTextField() {
            @Override
            protected void configureFileChooser(JFileChooser fileChooser) {
                super.configureFileChooser(fileChooser);
                fileChooser.setMultiSelectionEnabled(true);
            }
            
            @Override
            protected void handleUserSelection(JFileChooser fileChooser) {
                File[] selection = fileChooser.getSelectedFiles();
                for (File f : selection) {
                    tflFiles.addListItem(f.getPath()); 
                }
                Utils.getFaxOptions().lastSendWinPath = fileChooser.getCurrentDirectory().getPath();
                
                if (selection.length == 1) {
                    writeTextFieldFileName(selection[0].getPath());
                } else {
                    writeTextFieldFileName("");
                }
            }
        };
        ftfFileName.setFileFilters(FileConverters.getConvertableFileFilters()); 
        if (Utils.getFaxOptions().lastSendWinPath.length() > 0) {
            ftfFileName.setCurrentDirectory(new File(Utils.getFaxOptions().lastSendWinPath));
        }
        
        tflFiles = new TextFieldList<HylaTFLItem>(ftfFileName.getJTextField(), true, sendController.getFiles()) {
            @Override
            protected HylaTFLItem createListItem(String text) {
                LocalFileTFLItem item = new LocalFileTFLItem(text);
                
                // Check if we should extract recipients
                switch (Utils.getFaxOptions().extractRecipients) {
                case AUTO:
                case YES:
                    extractRecipientsFor(item);
                    break;
                default:
                    // Do not extract
                    break;
                }
                
                return item;
            }
        };
        tflFiles.addLocalComponent(ftfFileName.getJButton());
        SaveAsOriginalAction.installOn(tflFiles);
        
        ClipboardPopup clpFiles = new ClipboardPopup();
        clpFiles.getPopupMenu().addSeparator();
        clpFiles.getPopupMenu().add(tflFiles.getModifyAction());
        clpFiles.getPopupMenu().add(tflFiles.getAddAction());
        ftfFileName.getJTextField().addMouseListener(clpFiles);
        
        JLabel fileLabel = new JLabel(Utils._("File(s) to send:"));
        //fileLabel.setMaximumSize(new Dimension(Short.MAX_VALUE, fileLabel.getPreferredSize().height));
        //fileLabel.setHorizontalAlignment(JLabel.LEFT);
        
        fileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ftfFileName.setAlignmentX(Component.LEFT_ALIGNMENT);
        tflFiles.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        box.add(fileLabel);
        box.add(ftfFileName);
        box.add(tflFiles);
        return box;
    }
    
    protected Component createNumberEntryList() {
        double[][] tablelay = {
                { TableLayout.FILL, TableLayout.PREFERRED},
                { TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL }
        };
        JPanel tablePanel = new JPanel(new TableLayout(tablelay), false);
        
        actPhonebook = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                Utils.setWaitCursor(SimplifiedSendDialog.this);
                NewPhoneBookWin pbw = new NewPhoneBookWin(SimplifiedSendDialog.this);
                Utils.unsetWaitCursorOnOpen(SimplifiedSendDialog.this, pbw);
                List<PhoneBookEntry> pbs = pbw.selectNumbers();
                if (pbs != null) {
                    for (PhoneBookEntry pb : pbs)
                    {
                        numberTableModel.addRow(new DefaultPBEntryFieldContainer(pb));
                    }
                }
            }
        };
        actPhonebook.putValue(Action.NAME, Utils._("Add from phone book..."));
        actPhonebook.putValue(Action.SHORT_DESCRIPTION, Utils._("Choose number from phone book"));
        actPhonebook.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Bookmarks"));
        JButton buttonPhonebook = new JButton(actPhonebook);
        buttonPhonebook.setText("");

        
        actAddNumber = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                Object selNum = comboNumber.getSelectedItem();
                if (selNum == null)
                    selNum = "";
                
                PBEntryFieldContainer pbe = new DefaultPBEntryFieldContainer("");                
                final String faxNumber = selNum.toString();
                pbe.setField(PBEntryField.FaxNumber, faxNumber);
                numberTableModel.addRow(pbe);
                
                if (faxNumber.length() > 0) {
                    int index = comboNumberModel.getList().indexOf(faxNumber);
                    if (index != 0) { // If not already at the right position...
                        if (index >= 0) {
                            comboNumberModel.remove(index);
                        }
                        comboNumberModel.add(0, faxNumber);
                        if (comboNumberModel.getSize() > FAXNUMBER_LRU_COUNT) {
                            comboNumberModel.remove(FAXNUMBER_LRU_COUNT);
                        }
                    }
                }
                comboNumber.setSelectedItem("");
            }
        };
        actAddNumber.putValue(Action.NAME, Utils._("Add"));
        actAddNumber.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Add"));
        actAddNumber.putValue(Action.SHORT_DESCRIPTION, Utils._("Add a new recipient"));
        JButton buttonAddNumber = new JButton(actAddNumber);
        buttonAddNumber.setText("");

        actRemoveNumber = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                int[] selIdx = tableNumbers.getSelectedRows();
                if (selIdx.length >= 0) {
                    for (int i = selIdx.length-1; i>=0; i--) {
                        numberTableModel.removeRow(selIdx[i]);
                    }
                    actRemoveNumber.setEnabled(false);
                }
            }    
        };
        actRemoveNumber.putValue(Action.NAME, Utils._("Remove"));
        actRemoveNumber.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Delete"));
        actRemoveNumber.putValue(Action.SHORT_DESCRIPTION, Utils._("Remove selected recipient"));
        actRemoveNumber.setEnabled(false);
        JButton buttonRemoveNumber = new JButton(actRemoveNumber);
        buttonRemoveNumber.setText("");

        actFromFile = new ExcDialogAbstractAction() {
            private JFileChooser chooser;
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                if (chooser == null) {
                    chooser = new SafeJFileChooser();
                }
                FileFilter ff = new ExampleFileFilter("txt", Utils._("Text files"));
                chooser.addChoosableFileFilter(ff);
                chooser.setFileFilter(ff);
                
                if (chooser.showOpenDialog(SimplifiedSendDialog.this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        DefaultPBEntryFieldContainer.readListFile(getRecipients(), chooser.getSelectedFile().getPath());
                    } catch (Exception e1) {
                        ExceptionDialog.showExceptionDialog(SimplifiedSendDialog.this, Utils._("Error reading the specified file:"), e1);
                    }
                }
            }
        };
        actFromFile.putValue(Action.NAME, Utils._("Add from text file..."));
        actFromFile.putValue(Action.SMALL_ICON, Utils.loadCustomIcon("importtxt.png"));
        actFromFile.putValue(Action.SHORT_DESCRIPTION, Utils._("Adds recipients from a text file containing one recipient per line"));
        JButton buttonFromFile = new JButton(actFromFile);
        buttonFromFile.setText("");
        
        
        JPopupMenu tablePopup = new JPopupMenu();
        tablePopup.add(new JMenuItem(actAddNumber));
        tablePopup.add(new JMenuItem(actPhonebook));
        tablePopup.add(new JMenuItem(actFromFile));
        tablePopup.addSeparator();
        tablePopup.add( new JMenuItem(actRemoveNumber));
        
        numberTableModel = new PBEntryFieldTableModel(sendController.numbers);
        tableNumbers = new JTable(numberTableModel);
        tableNumbers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableNumbers.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JTableTABAction.wrapDefTabAction(tableNumbers);
        
        //tableNumbers.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableNumbers.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    actRemoveNumber.setEnabled(e.getFirstIndex() >= 0);
                }
            }          
        });
        tableNumbers.getActionMap().put("yajhfc-delete", actRemoveNumber);
        final InputMap im = tableNumbers.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "yajhfc-delete");
        
        Font boldFont = tableNumbers.getFont().deriveFont(Font.BOLD);
        TableColumn firstCol = tableNumbers.getColumnModel().getColumn(0);
        firstCol.setCellRenderer(new BoldCellRenderer(tableNumbers.getDefaultRenderer(String.class), boldFont));
        firstCol.setHeaderRenderer(new BoldCellRenderer(tableNumbers.getTableHeader().getDefaultRenderer(), boldFont));
        
        JScrollPane tablePane = new JScrollPane(tableNumbers, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableNumbers.setComponentPopupMenu(tablePopup);
        tablePane.setComponentPopupMenu(tablePopup);
        
        ClipboardPopup clpNumber = new ClipboardPopup();
        clpNumber.getPopupMenu().addSeparator();
        clpNumber.getPopupMenu().add(actAddNumber);
        
        comboNumberModel = new ListComboModel<String>(new ArrayList<String>(11));
        comboNumberModel.addAll(Utils.getFaxOptions().faxNumbersLRU);
        comboNumber = new JComboBox(comboNumberModel);
        comboNumber.setEditable(true);
        comboNumber.setMaximumRowCount(FAXNUMBER_LRU_COUNT);
        JComponent numberEditor = (JComponent)comboNumber.getEditor().getEditorComponent();
        numberEditor.getActionMap().put("yajhfc-addnum", actAddNumber);
        numberEditor.getInputMap().put(KeyStroke.getKeyStroke('\n'), "yajhfc-addnum");
        clpNumber.addToComponent(comboNumber);
        
        tablePanel.add(new JLabel(Utils._("Fax number:")), "0,0,1,0, f,f");
        tablePanel.add(comboNumber, "0,1");
        tablePanel.add(new JLabel(Utils._("Recipients:")), "0,2,1,2, f,f");
        tablePanel.add(buttonAddNumber, "1,1");
        tablePanel.add(tablePane, "0,3,0,6,f,f");
        tablePanel.add(buttonRemoveNumber, "1,3");
        tablePanel.add(buttonPhonebook, "1,4");
        tablePanel.add(buttonFromFile, "1,5");
        
        return tablePanel;
    }

    protected JPanel createAdvancedPane() {
        final int rowCount = 16;
        double[] rows = new double[rowCount];
        rows[0] = initiallyHideFiles ? TableLayout.PREFERRED : 0;
        for (int i = 1; i < rowCount; i++) {
            if ((i%3) == 1) {
                rows[i] = border/2;
            } else {
                rows[i] = TableLayout.PREFERRED;
            }
        }
        double[][] dLay = {
                {0.5, border, TableLayout.FILL},
                rows
        };
        advancedPane = new JPanel(new TableLayout(dLay), false);

        final FaxOptions fo = Utils.getFaxOptions();
        checkCustomCover = new JCheckBox(Utils._("Use custom cover page:"));
        checkCustomCover.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                 ftfCustomCover.setEnabled(checkCustomCover.isSelected() && checkCustomCover.isEnabled());
             } 
         });
        checkCustomCover.setSelected(fo.useCustomCover);

        ftfCustomCover = new FileTextField();
        ftfCustomCover.setFileFilters(Faxcover.getAcceptedFileFilters());
        if (fo.CustomCover != null && fo.CustomCover.length() > 0) {
            ftfCustomCover.setText(fo.CustomCover);
        } else {
            ftfCustomCover.setText(server.getDefaultIdentity().defaultCover);
        }
        ftfCustomCover.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);

        comboNotification = new JComboBox(FaxNotification.values());
        comboNotification.setRenderer(new IconMap.ListCellRenderer());

        comboPaperSize = new JComboBox(PaperSize.values());

        comboResolution = new JComboBox(FaxResolution.values());

        spinKillTime = new JSpinner(new SpinnerNumberModel(180, 0, ServerOptions.MAX_KILLTIME, 15));

        spinMaxTries = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));

        modemToSet = server.getOptions().defaultModem;
        modemModel =
            new AsyncComboBoxOrListModel<HylaModem>(
                    HylaModem.defaultModems,
                    new Callable<List<HylaModem>>() {
                        public List<HylaModem> call() throws Exception {
                            return server.getClientManager().getModems();
                        }
                    }, 
                    true,
                    new Runnable() {
                        public void run() {
                            setModemInternal(modemToSet);
                        }
                    });
        comboModem = new JComboBox(modemModel);
        comboModem.setEditable(true);
        comboModem.setSelectedItem(modemToSet);
        ClipboardPopup.DEFAULT_POPUP.addToComponent(comboModem);

        checkArchiveJob = new JCheckBox(Utils._("Archive fax job"));
        
        ttsEntry = new TimeToSendEntry();

        if (initiallyHideFiles) {
            advancedPane.add(createFileEntryList(), "0,0,2,0,f,f");
        }
        advancedPane.add(checkCustomCover, "0,2,2,2,f,c");
        advancedPane.add(ftfCustomCover, "0,3,2,3,f,c");
        addWithLabel(advancedPane, comboNotification, Utils._("Notify when:"), "0,6,F,C");
        addWithLabel(advancedPane, comboModem, Utils._("Modem:"), "2,6,,F,C");
        addWithLabel(advancedPane, comboResolution, Utils._("Resolution:"), "0,9,F,C");
        addWithLabel(advancedPane, comboPaperSize, Utils._("Paper size:"), "2,9,F,C");
        addWithLabel(advancedPane, spinKillTime, Utils._("Cancel job after (minutes):"), "0,12,F,C");
        addWithLabel(advancedPane, spinMaxTries, Utils._("Maximum tries:"), "2,12,F, C");
        Box box = Box.createHorizontalBox();
        ttsEntry.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        checkArchiveJob.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        box.add(ttsEntry);
        box.add(checkArchiveJob);
        addWithLabel(advancedPane, box, Utils._("Time to send:"), "0,15,2,15");
        
        intializeAdvancedPaneFromServerOptions(server.getOptions(), null);
        
        return advancedPane;
    }
    
    
    protected void intializeAdvancedPaneFromServerOptions(ServerOptions so, ServerOptions soOld) {
        if (soOld == null || comboResolution.getSelectedItem() == soOld.resolution)
            comboResolution.setSelectedItem(so.resolution);
        if (soOld == null || comboPaperSize.getSelectedItem() == soOld.paperSize)
            comboPaperSize.setSelectedItem(so.paperSize);
        if (soOld == null || comboNotification.getSelectedItem() == soOld.notifyWhen)
            comboNotification.setSelectedItem(so.notifyWhen);

        if (soOld == null || ((Integer)spinMaxTries.getValue()).intValue() == soOld.maxTry)
            spinMaxTries.setValue(Integer.valueOf(so.maxTry));
        if (soOld == null || ((Integer)spinKillTime.getValue()).intValue() == soOld.killTime)
            spinKillTime.setValue(Integer.valueOf(so.killTime));

        if (soOld == null || checkArchiveJob.isSelected() == soOld.archiveSentFaxes)
            checkArchiveJob.setSelected(so.archiveSentFaxes);   
    }
        
    protected void saveSettingsToSendController() {
        sendController.setServer(server);
        
        tflFiles.commit();
        final Object selNumber = comboNumber.getSelectedItem();
        if (selNumber != null && !"".equals(selNumber)) {
            actAddNumber.actionPerformed(null);
        }
        if (tableNumbers.isEditing()) {
           tableNumbers.getCellEditor().stopCellEditing();
        }
        
        sendController.setComment(textComments.getText());
        sendController.setKillTime((Integer)spinKillTime.getValue());
        sendController.setMaxTries((Integer)spinMaxTries.getValue());
        sendController.setNotificationType((FaxNotification)comboNotification.getSelectedItem());
        sendController.setPaperSize((PaperSize)comboPaperSize.getSelectedItem());
        sendController.setResolution((FaxResolution)comboResolution.getSelectedItem());
        sendController.setSelectedModem(comboModem.getSelectedItem());
        sendController.setSendTime(ttsEntry.getSelection());
        sendController.setArchiveJob(checkArchiveJob.isSelected());
        
        sendController.setSubject(textSubject.getText());
        sendController.setIdentity((SenderIdentity)comboIdentity.getSelectedItem());
        if (checkUseCover != null && checkUseCover.isSelected()) {
            sendController.setUseCover(true);
            sendController.setCustomCover(checkCustomCover.isSelected() ? new File(ftfCustomCover.getText()) : null);
        } else {
            sendController.setUseCover(false);
            sendController.setCustomCover(null);
        }
    }

    
    protected void setAdvancedView(boolean isAdvanced) {
        if (isAdvanced == isAdvancedView) {
            return;
        }
        
        isAdvancedView = isAdvanced;
        buttonCustomProps.setVisible(isAdvanced);
        labelServer.setVisible(isAdvanced);
        comboServer.setVisible(isAdvanced);
        if (isAdvanced) {
            buttonAdvanced.setText(SIMPLIFIED_TEXT);
            contentPane.add(advancedPane, "1,12,f,f");
            Dimension size = getSize();
            size.height += advancedPane.getPreferredSize().height;
            setSize(size);
        } else {
            buttonAdvanced.setText(ADVANCED_TEXT);
            Dimension size = getSize();
            size.height -= advancedPane.getPreferredSize().height;
            contentPane.remove(advancedPane);
            setSize(size);
        }
        validate();
    }
    
    
    private List<PBEntryFieldContainer> recipientList;
    public Collection<PBEntryFieldContainer> getRecipients() {
        if (recipientList == null) {
            recipientList = new AbstractList<PBEntryFieldContainer>() {
                @Override
                public PBEntryFieldContainer get(int index) {
                    return numberTableModel.getRow(index);
                }

                @Override
                public int size() {
                    return numberTableModel.getRowCount();
                }
                
                @Override
                public boolean add(PBEntryFieldContainer o) {
                    numberTableModel.addRow(new DefaultPBEntryFieldContainer(o));
                    return true;
                }
                
                @Override
                public PBEntryFieldContainer remove(int index) {
                    PBEntryFieldContainer res = numberTableModel.getRow(index);
                    numberTableModel.removeRow(index);
                    return res;
                }
            };
        }
        return recipientList;
    }

    public void setSubject(String subject) {
        textSubject.setText(subject);
    }
    
    public List<Long> getSubmittedJobIDs() {
        return sendController.getSubmittedJobIDs();
    }
    
    void enableCoverComps(boolean state) {
//      for (JComponent comp: coverComps)
//          comp.setEnabled(state);
      checkCustomCover.setEnabled(state);
      ftfCustomCover.setEnabled(checkCustomCover.isSelected() && state);
  }
 
    protected static class BoldCellRenderer implements TableCellRenderer {
        protected TableCellRenderer wrapped;
        public Font boldFont;
        
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Component comp = wrapped.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            comp.setFont(boldFont);
            return comp;
        }

        public BoldCellRenderer(TableCellRenderer wrapped, Font boldFont) {
            super();
            this.wrapped = wrapped;
            this.boldFont = boldFont;
        }
        
    }
    
    
    public void setComment(String comment) {
        textComments.setText(comment);
    }

    public void setUseCover(boolean useCover) {
        checkUseCover.setSelected(useCover);
    }
    
    protected void setModemInternal(String modemName) {
        Object selModem = modemName;
        for (HylaModem modem : server.getClientManager().getModems()) {
            if (modem.getInternalName().equals(modemName)) {
                selModem = modem;
                break;
            }
        }
        comboModem.setSelectedItem(selModem);
    }
    
    public void setModem(String modem) {
        if (modemModel.hasFinished()) {
            setModemInternal(modem);
        } else {
            modemToSet = modem;
            comboModem.setSelectedItem(modem);
        }
    }

    public boolean isPollMode() {
        return false;
    }
    
    public void setIdentity(SenderIdentity identity, boolean byUser) {
        comboIdentity.setSelectedItem(identity);
        identitySelectedByUser = byUser;
    }
    
    
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        if (this.server != server) {
            intializeAdvancedPaneFromServerOptions(server.getOptions(), this.server.getOptions());
            this.server = server;
            comboServer.setSelectedItem(server);
            modemModel.refreshListAsync();
            if (!identitySelectedByUser) {
                setIdentity(server.getDefaultIdentity(), false);
            }
        }
    }
    
    public Collection<HylaTFLItem> getDocuments() {
        return tflFiles.model;
    }

    public void setIdentity(SenderIdentity identityToUse) {
        setIdentity(identityToUse, true);
    }

    static class SaveAsOriginalAction extends ExcDialogAbstractAction implements ListSelectionListener {
        private final TextFieldList<? extends HylaTFLItem> target;
        
        @Override
        protected void actualActionPerformed(ActionEvent e) {
            HylaTFLItem selection = (HylaTFLItem)target.getList().getSelectedValue();
            if (selection == null)
                return;
            
            try {
                FormattedFile selFile = selection.getPreviewFilename();
                JFileChooser chooser = new SafeJFileChooser();
                chooser.setSelectedFile(selFile.file);
                FileFilter ff = selFile.format.createFileFilter();
                chooser.addChoosableFileFilter(ff);
                chooser.setFileFilter(ff);
                if (chooser.showSaveDialog(target) == JFileChooser.APPROVE_OPTION) {
                    File targetFile = chooser.getSelectedFile();
                    Utils.copyFile(selFile.file, targetFile);
                }
            } catch (Exception e1) {
                ExceptionDialog.showExceptionDialog(target, Utils._("Error saving the file"), e1);
            }
        }
        
        public void valueChanged(ListSelectionEvent e) {
            setEnabled(target.getList().getSelectedIndex() >= 0);
        }
        
        private SaveAsOriginalAction(TextFieldList<? extends HylaTFLItem> target) {
            super(Utils._("Save a copy as..."), Utils.loadIcon("general/SaveAs"));
            this.target=target;
            
            setEnabled(false);
            target.getList().addListSelectionListener(this);
            target.getPopup().addSeparator();
            target.getPopup().add(this);
        }
        
        public static SaveAsOriginalAction installOn(TextFieldList<? extends HylaTFLItem> target) {
            return new SaveAsOriginalAction(target);
        }
    }
}

