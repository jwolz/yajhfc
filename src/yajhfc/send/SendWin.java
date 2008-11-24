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

import static yajhfc.Utils._;
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
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import yajhfc.FaxIntProperty;
import yajhfc.FaxOptions;
import yajhfc.FaxStringProperty;
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
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;


final class SendWin extends JDialog implements SendWinControl  {
    private static final Logger log = Logger.getLogger(SendWin.class.getName());
    
    JPanel jContentPane = null;
    JButton buttonCancel = null;
    
    JTabbedPane tabMain = null;
    
    // Common:
    JPanel paneCommon = null;
    
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
        if (Utils.debugMode) {
            log.fine("Creating new SendWin: manager=" + manager + ", owner = " + owner);
        }
        initialize();
        if (Utils.debugMode) {
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
        
        FaxOptions fo = Utils.getFaxOptions();
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
                Utils.getFaxOptions().sendWinPos = getLocation();
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
            
            FaxOptions fo = Utils.getFaxOptions();
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
            JButton buttonPhonebook = getButtonPhoneBook();
            box.add(buttonPhonebook);
            
            Dimension d = buttonPhonebook.getPreferredSize();
            Dimension d2 = textNumber.getPreferredSize();
            if (d2.height > d.height)
                d.height = d2.height;
            else
                d2.height = d.height;
            d2.width = Integer.MAX_VALUE;
            buttonPhonebook.setMaximumSize(d);
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
                            if (company.equals(current.fields.get(PBEntryField.Company)) && location.equals(current.fields.get(PBEntryField.Location)) &&
                                    name.equals(current.fields.get(PBEntryField.Name)) && voiceNumber.equals(current.fields.get(PBEntryField.VoiceNumber))) {

                                company = ""; //textToCompany.getText();
                                location  = ""; //textToLocation.getText();
                                name = ""; //textToName.getText();
                                voiceNumber = ""; //textToVoiceNumber.getText();
                            }
                        } 
                        rv.fields.put(PBEntryField.Company, company);
                        rv.fields.put(PBEntryField.Location, location);
                        rv.fields.put(PBEntryField.Name, name);
                        rv.fields.put(PBEntryField.VoiceNumber, voiceNumber);
                    }
                    return rv;
                }
                
                @Override
                protected void commitChanges(TFLItem sel) {
                    if (!pollMode) {
                        NumberTFLItem numSel = (NumberTFLItem)sel;
                        numSel.fields.put(PBEntryField.Company, textToCompany.getText());
                        numSel.fields.put(PBEntryField.Location, textToLocation.getText());
                        numSel.fields.put(PBEntryField.Name, textToName.getText());
                        numSel.fields.put(PBEntryField.VoiceNumber, textToVoiceNumber.getText());
                    }
                    super.commitChanges(sel);
                }
                
                @Override
                protected void displayItem(TFLItem sel) {
                    if (!pollMode) {
                        NumberTFLItem numSel = (NumberTFLItem)sel;
                        textToCompany.setText(numSel.fields.get(PBEntryField.Company));
                        textToLocation.setText(numSel.fields.get(PBEntryField.Location));
                        textToName.setText(numSel.fields.get(PBEntryField.Name));
                        textToVoiceNumber.setText(numSel.fields.get(PBEntryField.VoiceNumber));
                    }
                    super.displayItem(sel);
                }
            };
            tflNumbers.addLocalComponent(buttonPhonebook);
            clpNumbers = new ClipboardPopup();
            clpNumbers.getPopupMenu().addSeparator();
            clpNumbers.getPopupMenu().add(tflNumbers.getModifyAction());
            clpNumbers.getPopupMenu().add(tflNumbers.getAddAction());
            textNumber.addMouseListener(clpNumbers);
            
            comboNotification = new JComboBox(Utils.notifications);
            comboNotification.setRenderer(new IconMap.ListCellRenderer());
            
            comboPaperSize = new JComboBox(Utils.papersizes);
            
            comboResolution = new JComboBox(Utils.resolutions);
            
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
        //if (buttonSend == null) {
            Action actSend = new ExcDialogAbstractAction() {
                @Override
                protected void actualActionPerformed(ActionEvent e) {
                    saveSettingsToSendController();

                    if (sendController.validateEntries()) {
                        sendController.sendFax();
                        modalResult = true;
                    }
                }
            };
            if (pollMode) {
                actSend.putValue(Action.NAME, _("Poll"));
                actSend.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Import"));
            } else {
                actSend.putValue(Action.NAME, _("Send"));
                actSend.putValue(Action.SMALL_ICON, Utils.loadIcon("general/SendMail"));
            }
            JButton buttonSend = new JButton(actSend);

            /*ButtonSend.setMinimumSize(buttonSize);
            ButtonSend.setPreferredSize(buttonSize);
            ButtonSend.setMaximumSize(buttonSize);*/
        //}
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
        //if (buttonPreview == null) {
            Action actPreview = new ExcDialogAbstractAction(_("Preview"), Utils.loadIcon("general/PrintPreview")) {
                public void actualActionPerformed(ActionEvent e) {
                    saveSettingsToSendController();
                    
                    if (!checkUseCover.isSelected() && tflFiles.model.getSize() == 0) {
                        JOptionPane.showMessageDialog(SendWin.this, _("Nothing to preview! (Neither a cover page nor a file to send has been selected.)"), _("Preview"), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    sendController.previewFax((NumberTFLItem)tflNumbers.list.getSelectedValue());
                }
            };
            JButton buttonPreview = new JButton(actPreview);
        //}
        return buttonPreview;
    }
    
    private FileTextField getFtfFilename() {
        if (ftfFilename == null) {
            ftfFilename = new FileTextField() {
                @Override
                protected void writeTextFieldFileName(String fName) {
                    super.writeTextFieldFileName(fName);
                    tflFiles.addListItem(fName);
                    Utils.getFaxOptions().lastSendWinPath = getJFileChooser().getCurrentDirectory().getPath();
                }
            };
            ftfFilename.setFileFilters(FormattedFile.getConvertableFileFilters()); 
            if (Utils.getFaxOptions().lastSendWinPath.length() > 0) {
                ftfFilename.getJFileChooser().setCurrentDirectory(new File(Utils.getFaxOptions().lastSendWinPath));
            }
        }
        return ftfFilename;
    }
    
    private JButton getButtonPhoneBook() {
        //if (buttonPhoneBook == null) {
            Action actPhonebook = new ExcDialogAbstractAction() {
                public void actualActionPerformed(ActionEvent e) {
                    Utils.setWaitCursor(SendWin.this);
                    NewPhoneBookWin pbw = new NewPhoneBookWin(SendWin.this);
                    Utils.unsetWaitCursorOnOpen(SendWin.this, pbw);
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
            };
            actPhonebook.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Bookmarks"));
            actPhonebook.putValue(Action.SHORT_DESCRIPTION, _("Choose number from phone book"));
            JButton buttonPhoneBook = new JButton(actPhonebook);
            
        //}
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
        tfl.fields.put(PBEntryField.Name, name);
        tfl.fields.put(PBEntryField.Company, company);
        tfl.fields.put(PBEntryField.Location, location);
        tfl.fields.put(PBEntryField.VoiceNumber, voiceNumber);
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

    public Window getWindow() {
        return this;
    }
}  








