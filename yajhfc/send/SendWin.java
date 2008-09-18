package yajhfc.send;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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

import static yajhfc.utils._;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Logger;

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
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import yajhfc.CancelAction;
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
import yajhfc.phonebook.NewPhoneBookWin;
import yajhfc.phonebook.PhoneBookEntry;


final class SendWin extends JDialog implements SendWinControl  {
    private static final Logger log = Logger.getLogger(SendWin.class.getName());
    
    JPanel jContentPane = null;
    JButton buttonSend = null;
    JButton buttonCancel = null;
    
    JTabbedPane tabMain = null;
    
    // Common:
    JPanel paneCommon = null;
    
    JButton buttonPhoneBook = null;
    JTextField textNumber = null;
    
    JComboBox comboResolution = null;
    JComboBox comboPaperSize = null;
    JComboBox comboNotification = null;
    JComboBox comboModem = null;
    
    JSpinner spinKillTime = null;
    JSpinner spinMaxTries = null;
    
    //JLabel lblFilename = null;
    FileTextField ftfFilename = null;
    
    TextFieldList<NumberTFLItem> tflNumbers;
    TextFieldList<HylaTFLItem> tflFiles;    
    
    // Cover:
    JPanel paneCover = null; 
    
    JCheckBox checkUseCover = null;
    JCheckBox checkCustomCover = null;
    FileTextField ftfCustomCover = null;
    
//    ArrayList<JComponent> coverComps = null;
    JTextField textToName = null;
    JTextField textToCompany = null;
    JTextField textToLocation = null;
    JTextField textToVoiceNumber = null;
    JTextField textSubject = null;
    JScrollPane scrollToComments = null;
    JTextArea textToComments = null;
    JButton buttonPreview;
    
    ClipboardPopup defClPop, clpNumbers, clpFiles;
    
    boolean pollMode = false;
    boolean modalResult = false;
    
    static final Dimension buttonSize = new Dimension(120, 27);
    static final int border = 10;
    
    HylaClientManager clientManager;
    SendController sendController;
    
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
    
    
    public SendWin(HylaClientManager manager, Frame owner) {
        this(manager, owner, false);
    }
    
    /**
     * This is the default constructor
     */
    public SendWin(HylaClientManager manager, Frame owner, boolean pollMode) {
        super(owner, true);
        this.clientManager = manager;
        this.pollMode = pollMode;
        if (utils.debugMode) {
            log.fine("Creating new SendWin: manager=" + manager + ", owner = " + owner);
        }
        initialize();
        if (utils.debugMode) {
            log.fine("New SendWin created.");
        }
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(530, 380);
        //this.setResizable(false);
        this.setName("SendWin");
        this.setTitle(_("Send Fax"));
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        sendController = new SendController(clientManager, this, pollMode);
        
        this.setContentPane(getJContentPane());
        
        FaxOptions fo = utils.getFaxOptions();
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
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                utils.getFaxOptions().sendWinPos = getLocation();
            }     
            
        });
        
        if (fo.sendWinPos != null)
            this.setLocation(fo.sendWinPos);
        /*else
            this.setLocationByPlatform(true);*/
        
        if (pollMode) {
            ftfFilename.setText(_("<none>"));
            ftfFilename.setEnabled(false);
            tflFiles.setEnabled(false);
            setTitle(_("Poll fax"));
            pack();
        }

    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            modalResult = false; //Reset the modal result
        }
        super.setVisible(b);
    }
    
    public boolean getModalResult() {
        return modalResult;
    }
    
    private JLabel addCoverComp(JComponent comp, String lblText, String layout) {
        JLabel lbl = addWithLabel(paneCover, comp, lblText, layout);
//        coverComps.add(comp);
//        coverComps.add(lbl);
        return lbl;
    }
    
    void enableCoverComps(boolean state) {
//        for (JComponent comp: coverComps)
//            comp.setEnabled(state);
        checkCustomCover.setEnabled(state);
        ftfCustomCover.setEnabled(checkCustomCover.isSelected() && state);
    }
    
    private JPanel getPaneCover() {
        if (paneCover == null) {
            double[][] tablelay = {
                    {border, TableLayout.FILL, border},
                    new double[16]
            };
            double rowh = 1 / (double)8;
            tablelay[1][0] = border / 2;
            //Arrays.fill(tablelay[1], 1, tablelay[1].length - 1, rowh);
            tablelay[1][1] = TableLayout.PREFERRED;
            for (int i = 2; i <= 14; i++) {
                if (i%2 == 0)
                    tablelay[1][i] = TableLayout.PREFERRED;
                else
                    tablelay[1][i] = rowh;
            }
            tablelay[1][tablelay[1].length - 1] = TableLayout.FILL;
            
            FaxOptions fo = utils.getFaxOptions();
//            coverComps = new ArrayList<JComponent>();
            
            paneCover = new JPanel(new TableLayout(tablelay));
            
            checkUseCover = new JCheckBox(_("Use cover page"));
            checkUseCover.setSelected(fo.useCover);
            checkUseCover.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    enableCoverComps(checkUseCover.isSelected());
                };
            });
            
            checkCustomCover = new JCheckBox(_("Use custom cover page:"));
            checkCustomCover.setSelected(fo.useCustomCover);
            checkCustomCover.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                    ftfCustomCover.setEnabled(checkCustomCover.isSelected() && checkCustomCover.isEnabled());
                } 
            });
            
            ftfCustomCover = new FileTextField();
            ftfCustomCover.setFileFilters(Faxcover.getAcceptedFileFilters());
            if (fo.CustomCover != null && fo.CustomCover.length() > 0) {
                ftfCustomCover.setText(fo.CustomCover);
            } else {
                ftfCustomCover.setText(fo.defaultCover);
            }
            
            textToName = new JTextField();
            textToCompany = new JTextField();
            textToLocation = new JTextField();
            textSubject = new JTextField();
            textToVoiceNumber = new JTextField();
            
            textToName.addMouseListener(getDefClPop());
            textToCompany.addMouseListener(getDefClPop());
            textToLocation.addMouseListener(getDefClPop());
            textSubject.addMouseListener(getDefClPop());
            textToVoiceNumber.addMouseListener(getDefClPop());
            ftfCustomCover.getJTextField().addMouseListener(getDefClPop());
            
            textToComments = new JTextArea();
            textToComments.setWrapStyleWord(true);
            textToComments.setLineWrap(true);
            textToComments.addMouseListener(getDefClPop());
            scrollToComments = new JScrollPane(textToComments, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            paneCover.add(checkUseCover, "1, 1, F, C");
            paneCover.add(checkCustomCover, "1, 2, F, C");
//            coverComps.add(checkCustomCover);
            paneCover.add(ftfCustomCover, "1, 3, F, T");
            
            addCoverComp(textToName, _("Recipient Name:"), "1, 5, F, T");
            addCoverComp(textToCompany, _("Company:"), "1, 7, F, T");
            addCoverComp(textToLocation, _("Location:"), "1, 9, F, T");
            addCoverComp(textToVoiceNumber, _("Voice number:"), "1, 11, F, T");
            addCoverComp(textSubject, _("Subject:"), "1, 13, F, T");
            addCoverComp(scrollToComments, _("Comments:"), "1, 15");
//            coverComps.add(textToComments);
            
            enableCoverComps(fo.useCover);
        }
        return paneCover;
    }
    
    private JPanel getPaneCommon() {
        if (paneCommon == null) {
            double[][] tablelay = {
                    {border, 0.5, border, 0.5, border},
                    new double[14]
            };
            double rowh = 1 / (double)(tablelay[1].length - 1);
            tablelay[1][0] = border;
            tablelay[1][tablelay[1].length - 1] = border;
            
            tablelay[1][1] = TableLayout.PREFERRED; 
            Arrays.fill(tablelay[1], 2, tablelay[1].length - 2, rowh);
            tablelay[1][3] = tablelay[1][6] = 2*rowh;
            tablelay[1][tablelay[1].length - 2] = TableLayout.FILL;
            
            paneCommon = new JPanel(new TableLayout(tablelay));
            
            tflFiles = new TextFieldList<HylaTFLItem>(getFtfFilename().getJTextField(), true, sendController.getFiles()) {
                @Override
                protected HylaTFLItem createListItem(String text) {
                    return new LocalFileTFLItem(text);
                }
            };
            tflFiles.addLocalComponent(getFtfFilename().getJButton());
         
            clpFiles = new ClipboardPopup();
            clpFiles.getPopupMenu().addSeparator();
            clpFiles.getPopupMenu().add(tflFiles.getModifyAction());
            clpFiles.getPopupMenu().add(tflFiles.getAddAction());
            getFtfFilename().getJTextField().addMouseListener(clpFiles);
            
            /*lblFilename = */addWithLabel(paneCommon, getFtfFilename(), _("File(s):"), "1, 2, 3, 2, F, C");
            paneCommon.add(tflFiles, "1, 3, 3, 3, F, F");
            
            Box box = Box.createHorizontalBox();
            textNumber = new JTextField();
            box.add(textNumber);
            box.add(getButtonPhoneBook());
            
            Dimension d = buttonPhoneBook.getPreferredSize();
            Dimension d2 = textNumber.getPreferredSize();
            if (d2.height > d.height)
                d.height = d2.height;
            else
                d2.height = d.height;
            d2.width = Integer.MAX_VALUE;
            buttonPhoneBook.setMaximumSize(d);
            textNumber.setMaximumSize(d2);
            
            tflNumbers = new TextFieldList<NumberTFLItem>(textNumber, false, sendController.getNumbers()) {
                @Override
                protected NumberTFLItem createListItem(String text) {
                    NumberTFLItem rv = new NumberTFLItem(text);
                    if (!pollMode) {
                        NumberTFLItem current = (NumberTFLItem)this.getList().getSelectedValue();
                        String company = textToCompany.getText();
                        String location  = textToLocation.getText();
                        String name = textToName.getText();
                        String voiceNumber = textToVoiceNumber.getText();
                        if (current != null) {
                            if (company.equals(current.company) && location.equals(current.location) &&
                                    name.equals(current.name) && voiceNumber.equals(current.voiceNumber)) {

                                company = ""; //textToCompany.getText();
                                location  = ""; //textToLocation.getText();
                                name = ""; //textToName.getText();
                                voiceNumber = ""; //textToVoiceNumber.getText();
                            }
                        } 
                        rv.company = company;
                        rv.location  = location;
                        rv.name = name;
                        rv.voiceNumber = voiceNumber;
                    }
                    return rv;
                }
                
                @Override
                protected void commitChanges(TFLItem sel) {
                    if (!pollMode) {
                        NumberTFLItem numSel = (NumberTFLItem)sel;
                        numSel.company = textToCompany.getText();
                        numSel.location = textToLocation.getText();
                        numSel.name = textToName.getText();
                        numSel.voiceNumber = textToVoiceNumber.getText();
                    }
                    super.commitChanges(sel);
                }
                
                @Override
                protected void displayItem(TFLItem sel) {
                    if (!pollMode) {
                        NumberTFLItem numSel = (NumberTFLItem)sel;
                        textToCompany.setText(numSel.company);
                        textToLocation.setText(numSel.location);
                        textToName.setText(numSel.name);
                        textToVoiceNumber.setText(numSel.voiceNumber);
                    }
                    super.displayItem(sel);
                }
            };
            tflNumbers.addLocalComponent(buttonPhoneBook);
            clpNumbers = new ClipboardPopup();
            clpNumbers.getPopupMenu().addSeparator();
            clpNumbers.getPopupMenu().add(tflNumbers.getModifyAction());
            clpNumbers.getPopupMenu().add(tflNumbers.getAddAction());
            textNumber.addMouseListener(clpNumbers);
            
            comboNotification = new JComboBox(utils.notifications);
            comboNotification.setRenderer(new IconMap.ListCellRenderer());
            
            comboPaperSize = new JComboBox(utils.papersizes);
            
            comboResolution = new JComboBox(utils.resolutions);
            
            spinKillTime = new JSpinner(new SpinnerNumberModel(180, 0, 2000, 15));
            
            spinMaxTries = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));
            
            comboModem = new JComboBox(clientManager.getModems().toArray());
            comboModem.setEditable(true);
            
            addWithLabel(paneCommon, box, _("Fax number(s):"), "1, 5, 3, 5, F, C");
            paneCommon.add(tflNumbers, "1, 6, 3, 6, F, F");
            
            addWithLabel(paneCommon, comboNotification, _("Notify when:"), "1, 8, 1, 8, F, C");
            addWithLabel(paneCommon, comboModem, _("Modem:"), "3, 8, F, C");
            addWithLabel(paneCommon, comboResolution, _("Resolution:"), "1, 10, F, C");
            addWithLabel(paneCommon, comboPaperSize, _("Paper size:"), "3, 10, F, C");
            addWithLabel(paneCommon, spinKillTime, _("Cancel job after (minutes):"), "1, 12, F, C");
            addWithLabel(paneCommon, spinMaxTries, _("Maximum tries:"), "3, 12, F, C");
        }
        return paneCommon;
    }
    
    private JTabbedPane getTabMain() {
        if (tabMain == null) {
            tabMain = new JTabbedPane(JTabbedPane.BOTTOM);
            
            tabMain.addTab(_("Common"), getPaneCommon());
            tabMain.addTab(_("Cover page"), getPaneCover());

        }
        return tabMain;
    }
    
    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {         
            
            double[][] tablelay = {
                    { TableLayout.FILL, border, buttonSize.width, border },
                    { border, buttonSize.height, border, buttonSize.height, border, buttonSize.height, TableLayout.FILL, border}
            };
            
            jContentPane = new JPanel(new TableLayout(tablelay));
            
            if (pollMode) {
                 jContentPane.add(getButtonSend(), "2, 1");
                 jContentPane.add(getButtonCancel(), "2, 3");
                 
                 jContentPane.add(getPaneCommon(), "0, 0, 0, 7");
                 jContentPane.add(new JSeparator(JSeparator.VERTICAL), "1, 0, 1, 7, L, F");
            } else {
                 jContentPane.add(getButtonSend(), "2, 1");
                 jContentPane.add(getButtonPreview(), "2, 3");
                 jContentPane.add(getButtonCancel(), "2, 5");
                 
                 jContentPane.add(getTabMain(), "0, 0, 0, 7");
            }
        }
        return jContentPane;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getButtonSend() {
        if (buttonSend == null) {
            buttonSend = new JButton();
            if (pollMode) {
                buttonSend.setText(_("Poll"));
                buttonSend.setIcon(utils.loadIcon("general/Import"));
            } else {
                buttonSend.setText(_("Send"));
                buttonSend.setIcon(utils.loadIcon("general/SendMail"));
            }

            /*ButtonSend.setMinimumSize(buttonSize);
            ButtonSend.setPreferredSize(buttonSize);
            ButtonSend.setMaximumSize(buttonSize);*/
            buttonSend.addActionListener(new SendButtonListener());
        }
        return buttonSend;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getButtonCancel() {
        if (buttonCancel == null) {
            CancelAction actCancel = new CancelAction(this);
            buttonCancel = actCancel.createCancelButton();
        }
        return buttonCancel;
    }

    private JButton getButtonPreview() {
        if (buttonPreview == null) {
            buttonPreview = new JButton(_("Preview"), utils.loadIcon("general/PrintPreview"));
            buttonPreview.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveSettingsToSendController();
                    
                    if (!checkUseCover.isSelected() && tflFiles.model.getSize() == 0) {
                        JOptionPane.showMessageDialog(SendWin.this, _("Nothing to preview! (Neither a cover page nor a file to send has been selected.)"), _("Preview"), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    sendController.previewFax((NumberTFLItem)tflNumbers.list.getSelectedValue());
                }
            });
        }
        return buttonPreview;
    }
    
    private FileTextField getFtfFilename() {
        if (ftfFilename == null) {
            ftfFilename = new FileTextField() {
                @Override
                protected void writeTextFieldFileName(String fName) {
                    super.writeTextFieldFileName(fName);
                    tflFiles.addListItem(fName);
                    utils.getFaxOptions().lastSendWinPath = getJFileChooser().getCurrentDirectory().getPath();
                }
            };
            ftfFilename.setFileFilters(FormattedFile.getConvertableFileFilters()); 
            if (utils.getFaxOptions().lastSendWinPath.length() > 0) {
                ftfFilename.getJFileChooser().setCurrentDirectory(new File(utils.getFaxOptions().lastSendWinPath));
            }
        }
        return ftfFilename;
    }
    
    private JButton getButtonPhoneBook() {
        if (buttonPhoneBook == null) {
            buttonPhoneBook = new JButton(utils.loadIcon("general/Bookmarks"));
            buttonPhoneBook.setToolTipText(_("Choose number from phone book"));
            
            buttonPhoneBook.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    utils.setWaitCursor(SendWin.this);
                    NewPhoneBookWin pbw = new NewPhoneBookWin(SendWin.this);
                    utils.unsetWaitCursorOnOpen(SendWin.this, pbw);
                    PhoneBookEntry[] pbs = pbw.selectNumbers();
                    if (pbs != null) {
                        for (PhoneBookEntry pb : pbs)
                        {
                            NumberTFLItem nti = new NumberTFLItem(pb);
                            tflNumbers.addListItem(nti);
                        }
                        
                        /*TextNumber.setText(pb.getFaxNumber());
                        tflNumbers.addListItem(pb.getFaxNumber());
                        
                        textToCompany.setText(pb.getCompany());
                        textToLocation.setText(pb.getLocation());
                        textToVoiceNumber.setText(pb.getVoiceNumber());
                        String name = "";
                        if (pb.getTitle().length() > 0)
                            name += pb.getTitle() + " ";
                        if (pb.getGivenName().length() > 0)
                            name += pb.getGivenName() + " ";
                        name += pb.getName();
                        textToName.setText(name);*/
                    }
                }
            });
        }
        return buttonPhoneBook;
    }
    
    private ClipboardPopup getDefClPop() {
        if (defClPop == null) {
            defClPop = new ClipboardPopup();
        }
        return defClPop;
    }
    
    public void addLocalFile(String fileName) {
        tflFiles.addListItem(fileName);
    }
    
    public void addServerFile(HylaServerFile serverFile) {
        tflFiles.model.add(new ServerFileTFLItem(serverFile));
    }
    
    public void addInputStream(InputStream inStream) {
        try {
            tflFiles.model.add(new StreamTFLItem(inStream));
        } catch (Exception e) {
            //JOptionPane.showMessageDialog(ButtonSend, _("An error occured reading the input: ") + "\n" + e.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            ExceptionDialog.showExceptionDialog(this, _("An error occured reading the input: "), e);
        }
    }
    
    public void addRecipient(String faxNumber, String name, String company, String location, String voiceNumber) {
        NumberTFLItem tfl = new NumberTFLItem(faxNumber);
        tfl.name = name;
        tfl.company = company;
        tfl.location = location;
        tfl.voiceNumber = voiceNumber;
        tflNumbers.addListItem(tfl);
    }
    
    public void setSubject(String subject) {
        textSubject.setText(subject);
    }
            
    protected void saveSettingsToSendController() {
        tflFiles.commit();
        tflNumbers.commit();
        
        if (textToComments != null)
            sendController.setComments(textToComments.getText());
        sendController.setKillTime((Integer)spinKillTime.getValue());
        sendController.setMaxTries((Integer)spinMaxTries.getValue());
        sendController.setNotificationType(((FaxStringProperty)comboNotification.getSelectedItem()).type);
        sendController.setPaperSize((PaperSize)comboPaperSize.getSelectedItem());
        sendController.setResolution(((FaxIntProperty)comboResolution.getSelectedItem()).type);
        sendController.setSelectedModem(comboModem.getSelectedItem());
        
        if (textSubject != null)
            sendController.setSubject(textSubject.getText());
        if (checkUseCover != null && checkUseCover.isSelected()) {
            sendController.setUseCover(true);
            sendController.setCustomCover(checkCustomCover.isSelected() ? new File(ftfCustomCover.getText()) : null);
        } else {
            sendController.setUseCover(false);
            sendController.setCustomCover(null);
        }
    }

    
    class SendButtonListener implements ActionListener {
               
        public void actionPerformed(ActionEvent e) {
            
            saveSettingsToSendController();
            
            if (!pollMode && tflFiles.model.getSize() == 0) {
                if (checkUseCover.isSelected()) {
                    if (JOptionPane.showConfirmDialog(SendWin.this, _("You haven't selected a file to transmit, so your fax will ONLY contain the cover page.\nContinue anyway?"), _("Continue?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
                        return;
                } else {
                    JOptionPane.showMessageDialog(SendWin.this, _("To send a fax you must select at least one file!"), _("Warning"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            
            if (tflNumbers.model.getSize() == 0) {
                JOptionPane.showMessageDialog(SendWin.this, _("To send a fax you have to enter at least one phone number!"), _("Warning"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            sendController.sendFax();
            modalResult = true;
        }
    }


    public Window getWindow() {
        return this;
    }
}  








