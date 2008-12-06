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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import yajhfc.FaxNotification;
import yajhfc.FaxOptions;
import yajhfc.FaxResolution;
import yajhfc.FileTextField;
import yajhfc.HylaClientManager;
import yajhfc.HylaModem;
import yajhfc.HylaServerFile;
import yajhfc.IconMap;
import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.file.FormattedFile;
import yajhfc.phonebook.NewPhoneBookWin;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.JTableTABAction;
import yajhfc.util.ProgressPanel;

/**
 * @author jonas
 *
 */
final class SimplifiedSendDialog extends JDialog implements SendWinControl {
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
    protected JTextField textNumber;
    protected Action actSend, actPreview, actPhonebook;
    
    
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
    protected FileTextField ftfCustomCover;
    
    protected Action actAddNumber, actRemoveNumber;
    
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
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Utils.getFaxOptions().sendWinBounds = getBounds();
                Utils.getFaxOptions().sendWinIsAdvanced = isAdvancedView;
            }     
            
        });
        
        if (Utils.getFaxOptions().sendWinBounds != null)
            this.setBounds(Utils.getFaxOptions().sendWinBounds);
        else
            Utils.setDefWinPos(this);

        enableCoverComps(Utils.getFaxOptions().useCover);
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
                        { TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, border}
                }
                ), false);
        buttonPanel.add(buttonSend, "0,0");
        buttonPanel.add(buttonPreview, "0,2");
        buttonPanel.add(buttonCancel, "0,4");
        buttonPanel.add(buttonAdvanced, "0,6");

        checkUseCover = new JCheckBox(Utils._("Use cover page"));
        checkUseCover.setSelected(Utils.getFaxOptions().useCover);
        checkUseCover.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableCoverComps(checkUseCover.isSelected());
            };
        });
        
        textSubject = new JTextField();
        textSubject.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
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
            protected void writeTextFieldFileName(String fName) {
                super.writeTextFieldFileName(fName);
                tflFiles.addListItem(fName);
                Utils.getFaxOptions().lastSendWinPath = getJFileChooser().getCurrentDirectory().getPath();
            }
        };
        ftfFileName.setFileFilters(FormattedFile.getConvertableFileFilters()); 
        if (Utils.getFaxOptions().lastSendWinPath.length() > 0) {
            ftfFileName.getJFileChooser().setCurrentDirectory(new File(Utils.getFaxOptions().lastSendWinPath));
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
                { TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL }
        };
        JPanel tablePanel = new JPanel(new TableLayout(tablelay), false);
        
        actPhonebook = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                Utils.setWaitCursor(SimplifiedSendDialog.this);
                NewPhoneBookWin pbw = new NewPhoneBookWin(SimplifiedSendDialog.this);
                Utils.unsetWaitCursorOnOpen(SimplifiedSendDialog.this, pbw);
                PhoneBookEntry[] pbs = pbw.selectNumbers();
                if (pbs != null) {
                    for (PhoneBookEntry pb : pbs)
                    {
                        numberTableModel.addRow(new DefaultPBEntryFieldContainer(pb));
                    }
                }
            }
        };
        actPhonebook.putValue(Action.NAME, Utils._("Add from phonebook..."));
        actPhonebook.putValue(Action.SHORT_DESCRIPTION, Utils._("Choose number from phone book"));
        actPhonebook.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Bookmarks"));
        JButton buttonPhonebook = new JButton(actPhonebook);
        buttonPhonebook.setText("");

        
        actAddNumber = new ExcDialogAbstractAction() {
            public void actualActionPerformed(ActionEvent e) {
                PBEntryFieldContainer pbe = new DefaultPBEntryFieldContainer("");
                pbe.setField(PBEntryField.FaxNumber, textNumber.getText());
                numberTableModel.addRow(pbe);
                textNumber.setText("");
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

        JPopupMenu tablePopup = new JPopupMenu();
        tablePopup.add(new JMenuItem(actAddNumber));
        tablePopup.add(new JMenuItem(actPhonebook));
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
        
        textNumber = new JTextField();
        textNumber.addActionListener(actAddNumber);
        textNumber.addMouseListener(clpNumber);
        
        tablePanel.add(new JLabel(Utils._("Fax number:")), "0,0,1,0, f,f");
        tablePanel.add(textNumber, "0,1");
        tablePanel.add(new JLabel(Utils._("Recipients:")), "0,2,1,2, f,f");
        tablePanel.add(buttonAddNumber, "1,1");
        tablePanel.add(tablePane, "0,3,0,5,f,f");
        tablePanel.add(buttonRemoveNumber, "1,3");
        tablePanel.add(buttonPhonebook, "1,4");
        
        return tablePanel;
    }

    protected JPanel createAdvancedPane() {
        final int rowCount = 13;
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

        FaxOptions fo = Utils.getFaxOptions();
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

        comboModem = new JComboBox(clientManager.getModems().toArray());
        comboModem.setEditable(true);

        if (initiallyHideFiles) {
            advancedPane.add(createFileEntryList(), "0,0,2,0,f,f");
        }
        advancedPane.add(checkCustomCover, "0,2,2,2,f,c");
        advancedPane.add(ftfCustomCover, "0,3,2,3,f,c");
        addWithLabel(advancedPane, comboNotification, Utils._("Notify when:"), "0, 6, F, C");
        addWithLabel(advancedPane, comboModem, Utils._("Modem:"), "2,6, F, C");
        addWithLabel(advancedPane, comboResolution, Utils._("Resolution:"), "0,9, F, C");
        addWithLabel(advancedPane, comboPaperSize, Utils._("Paper size:"), "2,9, F, C");
        addWithLabel(advancedPane, spinKillTime, Utils._("Cancel job after (minutes):"), "0,12, F, C");
        addWithLabel(advancedPane, spinMaxTries, Utils._("Maximum tries:"), "2,12, F, C");

        comboResolution.setSelectedItem(fo.resolution);
        comboPaperSize.setSelectedItem(fo.paperSize);
        comboNotification.setSelectedItem(fo.notifyWhen);

        Object selModem = fo.defaultModem;
        for (HylaModem modem : clientManager.getModems()) {
            if (modem.getInternalName().equals(fo.defaultModem)) {
                selModem = modem;
                break;
            }
        }
        comboModem.setSelectedItem(selModem);

        spinMaxTries.setValue(Integer.valueOf(fo.maxTry));
        spinKillTime.setValue(fo.killTime);

        return advancedPane;
    }

    private JLabel addWithLabel(JPanel pane, JComponent comp, String text, String layout) {
        TableLayoutConstraints c = new TableLayoutConstraints(layout);
        
        pane.add(comp, c);
        
        JLabel lbl = new JLabel(text);
        lbl.setLabelFor(comp);
        c.row1 = c.row2 = c.row1 - 1;
        c.vAlign = TableLayoutConstants.BOTTOM;
        c.hAlign = TableLayoutConstants.LEFT;
        pane.add(lbl, c); 
        
        return lbl;
    }
    
        
    protected void saveSettingsToSendController() {
        tflFiles.commit();
        if (textNumber.getText().length() > 0) {
            actAddNumber.actionPerformed(null);
        }
        if (tableNumbers.isEditing()) {
           tableNumbers.getCellEditor().stopCellEditing();
        }
        
        sendController.setComments(textComments.getText());
        sendController.setKillTime((Integer)spinKillTime.getValue());
        sendController.setMaxTries((Integer)spinMaxTries.getValue());
        sendController.setNotificationType(((FaxNotification)comboNotification.getSelectedItem()).getType());
        sendController.setPaperSize((PaperSize)comboPaperSize.getSelectedItem());
        sendController.setResolution(((FaxResolution)comboResolution.getSelectedItem()).getResolution());
        sendController.setSelectedModem(comboModem.getSelectedItem());
        
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
    
    public void addInputStream(InputStream inStream) {
        try {
            tflFiles.model.add(new StreamTFLItem(inStream));
        } catch (Exception e) {
            ExceptionDialog.showExceptionDialog(this, Utils._("An error occured reading the input: "), e);
        }
    }

    public void addLocalFile(String fileName) {
        tflFiles.addListItem(fileName);        
    }

    public void addRecipient(String faxNumber, String name, String company,
            String location, String voiceNumber) {
        PBEntryFieldContainer tfl = new DefaultPBEntryFieldContainer("");
        tfl.setField(PBEntryField.FaxNumber, faxNumber);
        tfl.setField(PBEntryField.Name, name);
        tfl.setField(PBEntryField.Company, company);
        tfl.setField(PBEntryField.Location, location);
        tfl.setField(PBEntryField.VoiceNumber, voiceNumber);
        numberTableModel.addRow(tfl);
    }

    public void addServerFile(HylaServerFile serverFile) {
        tflFiles.model.add(new ServerFileTFLItem(serverFile));        
    }

    public void setSubject(String subject) {
        textSubject.setText(subject);
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
    
}

