package yajhfc.send;
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
import static yajhfc.Utils.addWithLabel;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import yajhfc.model.IconMap;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.convrules.PBEntryFieldContainer;
import yajhfc.phonebook.ui.NewPhoneBookWin;
import yajhfc.server.Server;
import yajhfc.server.ServerOptions;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;


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
    
    ClipboardPopup clpNumbers, clpFiles;
    
    boolean pollMode = false;
    boolean modalResult = false;
    
    static final Dimension buttonSize = new Dimension(120, 27);
    static final int border = 10;
    
    Server server;
    SendController sendController;
    
    
    public SendWin(Server server, Frame owner) {
        this(server, owner, false);
    }
    
    /**
     * This is the default constructor
     */
    public SendWin(Server server, Frame owner, boolean pollMode) {
        super(owner, true);
        this.server = server;
        this.pollMode = pollMode;
        if (Utils.debugMode) {
            log.fine("Creating new SendWin: server=" + server + ", owner = " + owner);
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
        sendController = new SendController(server, this, pollMode);
        
        this.setContentPane(getJContentPane());
        
        FaxOptions fo = Utils.getFaxOptions();
        ServerOptions so = server.getOptions();
        comboResolution.setSelectedItem(so.resolution);
        comboPaperSize.setSelectedItem(so.paperSize);
        comboNotification.setSelectedItem(so.notifyWhen);
        
        setModem(so.defaultModem);
        
        spinMaxTries.setValue(Integer.valueOf(so.maxTry));
        spinKillTime.setValue(so.killTime);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Utils.getFaxOptions().sendWinBounds = getBounds();
            }     
            
        });
        
        if (fo.sendWinBounds != null)
            this.setBounds(fo.sendWinBounds);
        else 
            Utils.setDefWinPos(this);
        
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
    
    public List<Long> getSubmittedJobIDs() {
        return sendController.getSubmittedJobIDs();
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
                ftfCustomCover.setText(server.getDefaultIdentity().defaultCover);
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
            
            List<NumberTFLItem> numbers = new ArrayList<NumberTFLItem>();
            List<PBEntryFieldContainer> numberView = Collections.<PBEntryFieldContainer>unmodifiableList(numbers);
            sendController.setNumbers(numberView);
            tflNumbers = new TextFieldList<NumberTFLItem>(textNumber, false, numbers) {
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
            
            comboNotification = new JComboBox(FaxNotification.values());
            comboNotification.setRenderer(new IconMap.ListCellRenderer());
            
            comboPaperSize = new JComboBox(PaperSize.values());
            
            comboResolution = new JComboBox(FaxResolution.values());
            
            spinKillTime = new JSpinner(new SpinnerNumberModel(180, 0, ServerOptions.MAX_KILLTIME, 15));
            
            spinMaxTries = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));
            
            comboModem = new JComboBox(server.getClientManager().getModems().toArray());
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
            
            tabMain.addTab(_("General"), getPaneCommon());
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
                    Utils.getFaxOptions().lastSendWinPath = getCurrentDirectory().getPath();
                }
            };
            ftfFilename.setFileFilters(FileConverters.getConvertableFileFilters()); 
            if (Utils.getFaxOptions().lastSendWinPath.length() > 0) {
                ftfFilename.setCurrentDirectory(new File(Utils.getFaxOptions().lastSendWinPath));
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
                    List<PhoneBookEntry> pbs = pbw.selectNumbers();
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
        return ClipboardPopup.DEFAULT_POPUP;
    }
    
//    public void addLocalFile(String fileName) {
//        tflFiles.addListItem(fileName);
//    }
//    
//    public void addServerFile(FaxDocument serverFile) {
//        tflFiles.model.add(new ServerFileTFLItem(serverFile));
//    }
//    
//    public void addInputStream(StreamTFLItem inStream) {
//        try {
//            tflFiles.model.add(inStream);
//        } catch (Exception e) {
//            //JOptionPane.showMessageDialog(ButtonSend, _("An error occured reading the input: ") + "\n" + e.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
//            ExceptionDialog.showExceptionDialog(this, _("An error occured reading the input: "), e);
//        }
//    }
            
    private List<PBEntryFieldContainer> recipientList;
    public Collection<PBEntryFieldContainer> getRecipients() {
        if (recipientList == null) {
            recipientList = new AbstractList<PBEntryFieldContainer>() {
                @Override
                public PBEntryFieldContainer get(int index) {
                    return sendController.getNumbers().get(index);
                }

                @Override
                public int size() {
                    return sendController.getNumbers().size();
                }
                
                @Override
                public boolean add(PBEntryFieldContainer o) {
                    NumberTFLItem tfl = new NumberTFLItem(o);
                    tflNumbers.addListItem(tfl);
                    return true;
                }
            };
        }
        return recipientList;
    }
    
    public void setSubject(String subject) {
        textSubject.setText(subject);
    }
    
    public void setModem(String modemName) {
        Object selModem = modemName;
        for (HylaModem modem : server.getClientManager().getModems()) {
            if (modem.getInternalName().equals(modemName)) {
                selModem = modem;
                break;
            }
        }
        comboModem.setSelectedItem(selModem);
    }
            
    protected void saveSettingsToSendController() {
        tflFiles.commit();
        tflNumbers.commit();
        
        if (textToComments != null)
            sendController.setComment(textToComments.getText());
        sendController.setKillTime((Integer)spinKillTime.getValue());
        sendController.setMaxTries((Integer)spinMaxTries.getValue());
        sendController.setNotificationType((FaxNotification)comboNotification.getSelectedItem());
        sendController.setPaperSize((PaperSize)comboPaperSize.getSelectedItem());
        sendController.setResolution((FaxResolution)comboResolution.getSelectedItem());
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


    public void setComment(String comment) {
        textToComments.setText(comment);
    }


    public void setUseCover(boolean useCover) {
        checkUseCover.setSelected(useCover);
    }

//    public void setIdentity(String identityToUse) {
//        SenderIdentity identity = IDAndNameOptions.getItemFromCommandLineCoding(Utils.getFaxOptions().identities, identityToUse);
//        if (identity != null) {
//            sendController.setIdentity(identity);
//        } else {
//            log.warning("Identity not found, using default instead: " + identityToUse);
//        }
//    }
    
//    public void setServer(String serverToUse) {
//        ServerOptions server = IDAndNameOptions.getItemFromCommandLineCoding(Utils.getFaxOptions().servers, serverToUse);
//        if (server != null) {
//            this.server = ServerManager.getDefault().getServerByID(server.id);
//            sendController.setServer(this.server);
//        } else {
//            log.warning("Server not found, using default instead: " + serverToUse);
//        }
//    }

    public boolean isPollMode() {
        return pollMode;
    }

    public Collection<HylaTFLItem> getDocuments() {
        return tflFiles.model;
    }

    public void setServer(Server serverToUse) {
        this.server = serverToUse;
        sendController.setServer(serverToUse);
    }

    public void setIdentity(SenderIdentity identityToUse) {
        sendController.setIdentity(identityToUse);
    }
}  








