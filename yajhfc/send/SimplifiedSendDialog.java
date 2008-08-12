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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import yajhfc.ClipboardPopup;
import yajhfc.ExceptionDialog;
import yajhfc.FaxIntProperty;
import yajhfc.FaxOptions;
import yajhfc.FaxStringProperty;
import yajhfc.FileTextField;
import yajhfc.FormattedFile;
import yajhfc.HylaClientManager;
import yajhfc.HylaModem;
import yajhfc.HylaServerFile;
import yajhfc.IconMap;
import yajhfc.PaperSize;
import yajhfc.utils;
import yajhfc.faxcover.Faxcover;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookWin;

/**
 * @author jonas
 *
 */
final class SimplifiedSendDialog extends JDialog implements SendWinControl {
    HylaClientManager clientManager;
    SendController sendController;
    
    protected JPanel contentPane, advancedPane;
    protected JButton buttonSend, buttonCancel, buttonPreview;
    protected FileTextField ftfFileName;
    protected TextFieldList<HylaTFLItem> tflFiles;
    protected JButton buttonPhonebook, buttonAdvanced;
    protected JTable tableNumbers;
    protected NumberTFLItemTableModel numberTableModel;
    protected JTextField textNumber;
    
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
    
    protected ClipboardPopup clpDefault;
    
    protected Action actAddNumber, actRemoveNumber;
    
    protected boolean isAdvancedView = false;
    protected boolean initiallyHideFiles = false;
    protected boolean modalResult = false;
    
    private static final int border = 10;
    
    private static final String ADVANCED_TEXT = utils._(">> Advanced");
    private static final String SIMPLIFIED_TEXT = utils._("<< Simplified");
    
    /**
     * @param owner
     * @throws HeadlessException
     */
    public SimplifiedSendDialog(HylaClientManager clientManager, Frame owner, boolean initiallyHideFiles) throws HeadlessException {
        super(owner, utils._("Send fax"), true);
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
        
        clpDefault = new ClipboardPopup();
        createContentPane();
        setContentPane(contentPane);
        this.setSize(640, initiallyHideFiles ? 400 : 480);
        createAdvancedPane();
        setAdvancedView(utils.getFaxOptions().sendWinIsAdvanced);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                utils.getFaxOptions().sendWinPos = getLocation();
                utils.getFaxOptions().sendWinIsAdvanced = isAdvancedView;
            }     
            
        });
        
        if (utils.getFaxOptions().sendWinPos != null)
            this.setLocation(utils.getFaxOptions().sendWinPos);
        else
            utils.setDefWinPos(this);

    }

    protected void createContentPane() {
        final double verticalAreaPart = initiallyHideFiles ? (0.5) : (1.0/3.0);
        double[][] tablelay = {
                { border, TableLayout.FILL, border, TableLayout.PREFERRED, border },
                { border, initiallyHideFiles ? 0 : verticalAreaPart , initiallyHideFiles ? 0 : border,  TableLayout.FILL, border, TableLayout.PREFERRED, border,
                        TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, verticalAreaPart*0.5, border, TableLayout.PREFERRED, border}
        };

        contentPane = new JPanel(new TableLayout(tablelay));

        buttonSend = new JButton();
        buttonSend.setText(utils._("Send"));
        buttonSend.setIcon(utils.loadIcon("general/SendMail"));
        buttonSend.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               saveSettingsToSendController();
               
               if (tflFiles.model.getSize() == 0) {
                   if (checkUseCover.isSelected()) {
                       if (JOptionPane.showConfirmDialog(SimplifiedSendDialog.this, utils._("You haven't selected a file to transmit, so your fax will ONLY contain the cover page.\nContinue anyway?"), utils._("Continue?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
                           return;
                   } else {
                       JOptionPane.showMessageDialog(SimplifiedSendDialog.this, utils._("To send a fax you must select at least one file!"), utils._("Warning"), JOptionPane.INFORMATION_MESSAGE);
                       return;
                   }
               }
               
               if (sendController.getNumbers().size() == 0) {
                   JOptionPane.showMessageDialog(SimplifiedSendDialog.this, utils._("To send a fax you have to enter at least one phone number!"), utils._("Warning"), JOptionPane.INFORMATION_MESSAGE);
                   return;
               }

               sendController.sendFax();
               modalResult = true;
            } 
        });

        buttonPreview = new JButton(utils._("Preview"), utils.loadIcon("general/PrintPreview"));
        buttonPreview.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveSettingsToSendController();

                boolean wantCover = checkUseCover.isSelected();
                if ((!wantCover && tflFiles.model.getSize() == 0) &&
                        (wantCover && sendController.getNumbers().size() == 0)) {
                    JOptionPane.showMessageDialog(SimplifiedSendDialog.this, utils._("Nothing to preview! (Neither a cover page nor a file to send has been selected.)"), utils._("Preview"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // If nothing is selected, use the first element for preview
                NumberTFLItem coverItem;
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
        });

        Action actCancel = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            };
        };
        actCancel.putValue(Action.NAME, utils._("Cancel"));
        buttonCancel = new JButton(actCancel);
        buttonCancel.getActionMap().put("EscapePressed", actCancel);
        buttonCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EscapePressed");

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

        checkUseCover = new JCheckBox(utils._("Use cover page"));
        checkUseCover.setSelected(utils.getFaxOptions().useCover);
        checkUseCover.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableCoverComps(checkUseCover.isSelected());
            };
        });
        
        textSubject = new JTextField();
        textSubject.addMouseListener(clpDefault);
        
        textComments = new JTextArea();
        textComments.setWrapStyleWord(true);
        textComments.setLineWrap(true);
        textComments.addMouseListener(clpDefault);
        JScrollPane scrollComments = new JScrollPane(textComments, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        if (!initiallyHideFiles) {
            contentPane.add(createFileEntryList(), "1,1, f, f");
        }
        contentPane.add(createNumberEntryList(), "1,3, f, f");
        contentPane.add(checkUseCover, "1,5");
        addWithLabel(contentPane, textSubject, utils._("Subject:"), "1,8");
        addWithLabel(contentPane, scrollComments, utils._("Comments:"), "1,10");
        
        contentPane.add(buttonPanel, "3,1,3,12,f,f");
    }
    
    protected Component createFileEntryList() {
        Box box = Box.createVerticalBox();
        
        ftfFileName = new FileTextField() {
            @Override
            protected void writeTextFieldFileName(String fName) {
                super.writeTextFieldFileName(fName);
                tflFiles.addListItem(fName);
                utils.getFaxOptions().lastSendWinPath = getJFileChooser().getCurrentDirectory().getPath();
            }
        };
        ftfFileName.setFileFilters(FormattedFile.getConvertableFileFilters()); 
        if (utils.getFaxOptions().lastSendWinPath.length() > 0) {
            ftfFileName.getJFileChooser().setCurrentDirectory(new File(utils.getFaxOptions().lastSendWinPath));
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
        
        JLabel fileLabel = new JLabel(utils._("File(s) to send:"));
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
        
        buttonPhonebook = new JButton(utils.loadIcon("general/Bookmarks"));
        buttonPhonebook.setToolTipText(utils._("Choose number from phone book"));
        
        buttonPhonebook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                utils.setWaitCursor(SimplifiedSendDialog.this);
                PhoneBookWin pbw = new PhoneBookWin(SimplifiedSendDialog.this);
                utils.unsetWaitCursorOnOpen(SimplifiedSendDialog.this, pbw);
                PhoneBookEntry[] pbs = pbw.selectNumbers();
                if (pbs != null) {
                    for (PhoneBookEntry pb : pbs)
                    {
                        numberTableModel.addRow(new NumberTFLItem(pb));
                    }
                }
            }
        });

        
        actAddNumber = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                numberTableModel.addRow(new NumberTFLItem(textNumber.getText()));
                textNumber.setText("");
            }
        };
        actAddNumber.putValue(Action.SMALL_ICON, utils.loadIcon("general/Add"));
        actAddNumber.putValue(Action.SHORT_DESCRIPTION, utils._("Add a new recipient"));
        
        JButton buttonAddNumber = new JButton(actAddNumber);

        actRemoveNumber = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int selIdx = tableNumbers.getSelectedRow();
                if (selIdx >= 0) {
                    numberTableModel.removeRow(selIdx);
                    actRemoveNumber.setEnabled(false);
                }
            }    
        };
        actRemoveNumber.putValue(Action.SMALL_ICON, utils.loadIcon("general/Delete"));
        actRemoveNumber.putValue(Action.SHORT_DESCRIPTION, utils._("Remove selected recipient"));
        actRemoveNumber.setEnabled(false);
        
        JButton buttonRemoveNumber = new JButton(actRemoveNumber);

        numberTableModel = new NumberTFLItemTableModel(sendController.numbers);
        tableNumbers = new JTable(numberTableModel);
        tableNumbers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        
        textNumber = new JTextField();
        textNumber.addActionListener(actAddNumber);
        textNumber.addMouseListener(clpDefault);
        
        tablePanel.add(new JLabel(utils._("Fax number:")), "0,0,1,0, f,f");
        tablePanel.add(textNumber, "0,1");
        tablePanel.add(new JLabel(utils._("Recipients:")), "0,2,1,2, f,f");
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

        FaxOptions fo = utils.getFaxOptions();
        checkCustomCover = new JCheckBox(utils._("Use custom cover page:"));
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
        ftfCustomCover.getJTextField().addMouseListener(clpDefault);

        comboNotification = new JComboBox(utils.notifications);
        comboNotification.setRenderer(new IconMap.ListCellRenderer());

        comboPaperSize = new JComboBox(utils.papersizes);

        comboResolution = new JComboBox(utils.resolutions);

        spinKillTime = new JSpinner(new SpinnerNumberModel(180, 0, 2000, 15));

        spinMaxTries = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));

        comboModem = new JComboBox(clientManager.getModems().toArray());
        comboModem.setEditable(true);

        if (initiallyHideFiles) {
            advancedPane.add(createFileEntryList(), "0,0,2,0,f,f");
        }
        advancedPane.add(checkCustomCover, "0,2,2,2,f,c");
        advancedPane.add(ftfCustomCover, "0,3,2,3,f,c");
        addWithLabel(advancedPane, comboNotification, utils._("Notify when:"), "0, 6, F, C");
        addWithLabel(advancedPane, comboModem, utils._("Modem:"), "2,6, F, C");
        addWithLabel(advancedPane, comboResolution, utils._("Resolution:"), "0,9, F, C");
        addWithLabel(advancedPane, comboPaperSize, utils._("Paper size:"), "2,9, F, C");
        addWithLabel(advancedPane, spinKillTime, utils._("Cancel job after (minutes):"), "0,12, F, C");
        addWithLabel(advancedPane, spinMaxTries, utils._("Maximum tries:"), "2,12, F, C");

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
        sendController.setNotificationType(((FaxStringProperty)comboNotification.getSelectedItem()).type);
        sendController.setPaperSize((PaperSize)comboPaperSize.getSelectedItem());
        sendController.setResolution(((FaxIntProperty)comboResolution.getSelectedItem()).type);
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
            ExceptionDialog.showExceptionDialog(this, utils._("An error occured reading the input: "), e);
        }
    }

    public void addLocalFile(String fileName) {
        tflFiles.addListItem(fileName);        
    }

    public void addRecipient(String faxNumber, String name, String company,
            String location, String voiceNumber) {
        NumberTFLItem tfl = new NumberTFLItem(faxNumber);
        tfl.name = name;
        tfl.company = company;
        tfl.location = location;
        tfl.voiceNumber = voiceNumber;
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

