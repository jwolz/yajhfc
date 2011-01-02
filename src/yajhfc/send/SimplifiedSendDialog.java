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
import java.util.List;
import java.util.concurrent.Callable;

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
import yajhfc.HylaClientManager;
import yajhfc.HylaModem;
import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.file.FormattedFile;
import yajhfc.model.IconMap;
import yajhfc.model.servconn.FaxDocument;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.NewPhoneBookWin;
import yajhfc.phonebook.ui.PBEntryFieldTableModel;
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
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
final class SimplifiedSendDialog extends JDialog implements SendWinControl {
    protected static final int FAXNUMBER_LRU_COUNT = 10;
    
    HylaClientManager clientManager;
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
    
    private static final int border = 10;
    
    private static final String ADVANCED_TEXT = Utils._(">> Advanced");
    private static final String SIMPLIFIED_TEXT = Utils._("<< Simplified");
    
    /**
     * @param owner
     * @throws HeadlessException
     */
    public SimplifiedSendDialog(HylaClientManager clientManager, Frame owner, boolean initiallyHideFiles) throws HeadlessException {
        super(owner, Utils._("Send fax"), true);
        this.clientManager = clientManager;
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
        sendController = new SendController(clientManager, this, false);
        
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
        
        JPanel buttonPanel = new JPanel(new TableLayout(
                new double[][] {
                        { TableLayout.FILL },
                        { TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border}
                }
                ), false);
        buttonPanel.add(buttonSend, "0,0");
        buttonPanel.add(buttonPreview, "0,2");
        buttonPanel.add(buttonCancel, "0,4");
        buttonPanel.add(buttonCustomProps, "0,6");
        buttonPanel.add(buttonAdvanced, "0,8");

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
        ftfFileName.setFileFilters(FormattedFile.getConvertableFileFilters()); 
        if (Utils.getFaxOptions().lastSendWinPath.length() > 0) {
            ftfFileName.setCurrentDirectory(new File(Utils.getFaxOptions().lastSendWinPath));
        }
        
        tflFiles = new TextFieldList<HylaTFLItem>(ftfFileName.getJTextField(), true, sendController.getFiles()) {
            @Override
            protected HylaTFLItem createListItem(String text) {
                return new LocalFileTFLItem(text);
            }
        };
        tflFiles.addLocalComponent(ftfFileName.getJButton());

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
            ftfCustomCover.setText(fo.defaultCover);
        }
        ftfCustomCover.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);

        comboNotification = new JComboBox(FaxNotification.values());
        comboNotification.setRenderer(new IconMap.ListCellRenderer());

        comboPaperSize = new JComboBox(PaperSize.values());

        comboResolution = new JComboBox(FaxResolution.values());

        spinKillTime = new JSpinner(new SpinnerNumberModel(180, 0, 2000, 15));

        spinMaxTries = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));

        modemToSet = fo.defaultModem;
        modemModel =
            new AsyncComboBoxOrListModel<HylaModem>(
                    HylaModem.defaultModems,
                    new Callable<List<HylaModem>>() {
                        public List<HylaModem> call() throws Exception {
                            return clientManager.getModems();
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
        
        comboResolution.setSelectedItem(fo.resolution);
        comboPaperSize.setSelectedItem(fo.paperSize);
        comboNotification.setSelectedItem(fo.notifyWhen);
        
        spinMaxTries.setValue(Integer.valueOf(fo.maxTry));
        spinKillTime.setValue(fo.killTime);

        checkArchiveJob.setSelected(fo.archiveSentFaxes);   
        
        return advancedPane;
    }
    
        
    protected void saveSettingsToSendController() {
        tflFiles.commit();
        final Object selNumber = comboNumber.getSelectedItem();
        if (selNumber != null && !"".equals(selNumber)) {
            actAddNumber.actionPerformed(null);
        }
        if (tableNumbers.isEditing()) {
           tableNumbers.getCellEditor().stopCellEditing();
        }
        
        sendController.setComments(textComments.getText());
        sendController.setKillTime((Integer)spinKillTime.getValue());
        sendController.setMaxTries((Integer)spinMaxTries.getValue());
        sendController.setNotificationType((FaxNotification)comboNotification.getSelectedItem());
        sendController.setPaperSize((PaperSize)comboPaperSize.getSelectedItem());
        sendController.setResolution((FaxResolution)comboResolution.getSelectedItem());
        sendController.setSelectedModem(comboModem.getSelectedItem());
        sendController.setSendTime(ttsEntry.getSelection());
        sendController.setArchiveJob(checkArchiveJob.isSelected());
        
        sendController.setSubject(textSubject.getText());
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
    
    public void addInputStream(StreamTFLItem inStream) {
        try {
            tflFiles.model.add(inStream);
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(this, Utils._("An error occured reading the input: "), e);
        }
    }

    public void addLocalFile(String fileName) {
        tflFiles.addListItem(fileName);        
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

    public void addServerFile(FaxDocument serverFile) {
        tflFiles.model.add(new ServerFileTFLItem(serverFile));        
    }

    public void setSubject(String subject) {
        textSubject.setText(subject);
    }
    
    public List<Long> getSubmittedJobIDs() {
        return sendController.getSubmittedJobs();
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
        for (HylaModem modem : clientManager.getModems()) {
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
    
}

