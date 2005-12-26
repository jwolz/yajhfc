package yajhfc;
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

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import yajhfc.faxcover.Faxcover;


public class SendWin extends JDialog {
    private final int border = 10;
    
    private JPanel jContentPane = null;
    private JButton ButtonSend = null;
    private JButton ButtonCancel = null;
    
    private JTabbedPane tabMain = null;
    
    // Common:
    private JPanel paneCommon = null;
    
    private JButton ButtonPhoneBook = null;
    private JTextField TextNumber = null;
    
    private JComboBox ComboResolution = null;
    private JComboBox ComboPaperSize = null;
    private JComboBox ComboNotification = null;
    
    private JSpinner SpinKillTime = null;
    private JSpinner SpinMaxTries = null;
    
    private JLabel lblFilename = null;
    private FileTextField ftfFilename = null;
    
    private TextFieldList tflNumbers, tflFiles;    
    
    // Cover:
    private JPanel paneCover = null; 
    
    private JCheckBox checkUseCover = null;
    private JCheckBox checkCustomCover = null;
    private FileTextField ftfCustomCover = null;
    
    private ArrayList<JComponent> coverComps = null;
    private JTextField textToName = null;
    private JTextField textToCompany = null;
    private JTextField textToLocation = null;
    private JTextField textToVoiceNumber = null;
    private JTextField textSubject = null;
    private JScrollPane scrollToComments = null;
    private JTextArea textToComments = null;
    
    private ClipboardPopup defClPop, clpNumbers, clpFiles;
    
    private InputStream myInStream = null;
    private boolean pollMode = false;
    private static final Dimension buttonSize = new Dimension(120, 27);
    
    HylaFAXClient hyfc;
    
    private JLabel addWithLabel(JPanel pane, JComponent comp, String text, String layout) {
        TableLayoutConstraints c = new TableLayoutConstraints(layout);
        
        pane.add(comp, c);
        
        JLabel lbl = new JLabel(text);
        lbl.setLabelFor(comp);
        c.row1 = c.row2 = c.row1 - 1;
        c.vAlign = TableLayoutConstraints.BOTTOM;
        c.hAlign = TableLayoutConstraints.LEFT;
        pane.add(lbl, c); 
        
        return lbl;
    }
    
    private String _(String key) {
        return utils._(key);
    }
    
    
    public SendWin(HylaFAXClient hyfc, Frame owner) {
        this(hyfc, owner, false);
    }
    
    /**
     * This is the default constructor
     */
    public SendWin(HylaFAXClient hyfc, Frame owner, boolean pollMode) {
        super(owner);
        this.hyfc = hyfc;
        this.pollMode = pollMode;
        initialize();

    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(530, 380);
        this.setResizable(false);
        this.setName("SendWin");
        this.setTitle(_("Send Fax"));
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(getJContentPane());
        
        FaxOptions fo = utils.getFaxOptions();
        getComboResolution().setSelectedItem(fo.resolution);
        getComboPaperSize().setSelectedItem(fo.paperSize);
        getComboNotification().setSelectedItem(fo.notifyWhen);
        
        getSpinMaxTries().setValue(Integer.valueOf(fo.maxTry));
        
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                utils.getFaxOptions().sendWinPos = getLocation();
            }     
            
        });
        
        if (fo.sendWinPos != null)
            this.setLocation(fo.sendWinPos);
        else
            this.setLocationByPlatform(true);
        
        if (pollMode) {
            setFilename(false, null);
            setTitle(_("Poll fax"));
            pack();
        }

    }

    private JLabel addCoverComp(JComponent comp, String lblText, String layout) {
        JLabel lbl = addWithLabel(paneCover, comp, lblText, layout);
        coverComps.add(comp);
        coverComps.add(lbl);
        return lbl;
    }
    
    private void enableCoverComps(boolean state) {
        for (JComponent comp: coverComps)
            comp.setEnabled(state);
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
            coverComps = new ArrayList<JComponent>();
            
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
            ftfCustomCover.setFileFilters(new ExampleFileFilter("ps", _("Postscript files")));
            ftfCustomCover.setText(fo.CustomCover);
            
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
            
            textToComments = new JTextArea();
            textToComments.setWrapStyleWord(true);
            textToComments.setLineWrap(true);
            textToComments.addMouseListener(getDefClPop());
            scrollToComments = new JScrollPane(textToComments, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
            paneCover.add(checkUseCover, "1, 1, F, C");
            paneCover.add(checkCustomCover, "1, 2, F, C");
            coverComps.add(checkCustomCover);
            paneCover.add(ftfCustomCover, "1, 3, F, T");
            
            addCoverComp(textToName, _("Recipient Name:"), "1, 5, F, T");
            addCoverComp(textToCompany, _("Company:"), "1, 7, F, T");
            addCoverComp(textToLocation, _("Location:"), "1, 9, F, T");
            addCoverComp(textToVoiceNumber, _("Voice number:"), "1, 11, F, T");
            addCoverComp(textSubject, _("Subject:"), "1, 13, F, T");
            addCoverComp(scrollToComments, _("Comments:"), "1, 15");
            coverComps.add(textToComments);
            
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
            
            tflFiles = new TextFieldList(getFtfFilename().getJTextField(), true);
            clpFiles = new ClipboardPopup();
            clpFiles.getPopupMenu().addSeparator();
            clpFiles.getPopupMenu().add(tflFiles.getModifyAction());
            clpFiles.getPopupMenu().add(tflFiles.getAddAction());
            getFtfFilename().getJTextField().addMouseListener(clpFiles);
            
            lblFilename = addWithLabel(paneCommon, getFtfFilename(), _("File(s):"), "1, 2, 3, 2, F, C");
            paneCommon.add(tflFiles, "1, 3, 3, 3, F, F");
            
            Box box = Box.createHorizontalBox();
            TextNumber = new JTextField();
            box.add(TextNumber);
            box.add(getButtonPhoneBook());
            
            Dimension d = ButtonPhoneBook.getPreferredSize();
            Dimension d2 = TextNumber.getPreferredSize();
            if (d2.height > d.height)
                d.height = d2.height;
            else
                d2.height = d.height;
            d2.width = Integer.MAX_VALUE;
            ButtonPhoneBook.setMaximumSize(d);
            TextNumber.setMaximumSize(d2);
            
            tflNumbers = new TextFieldList(TextNumber, false);
            clpNumbers = new ClipboardPopup();
            clpNumbers.getPopupMenu().addSeparator();
            clpNumbers.getPopupMenu().add(tflNumbers.getModifyAction());
            clpNumbers.getPopupMenu().add(tflNumbers.getAddAction());
            TextNumber.addMouseListener(clpNumbers);
            
            addWithLabel(paneCommon, box, _("Fax number(s):"), "1, 5, 3, 5, F, C");
            paneCommon.add(tflNumbers, "1, 6, 3, 6, F, F");
            
            addWithLabel(paneCommon, getComboNotification(), _("Notify when:"), "1, 8, 3, 8, F, C");
            addWithLabel(paneCommon, getComboResolution(), _("Resolution:"), "1, 10, F, C");
            addWithLabel(paneCommon, getComboPaperSize(), _("Paper size:"), "3, 10, F, C");
            addWithLabel(paneCommon, getSpinKillTime(), _("Cancel job after (minutes):"), "1, 12, F, C");
            addWithLabel(paneCommon, getSpinMaxTries(), _("Maximum tries:"), "3, 12, F, C");
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
                    { border, buttonSize.height, border, buttonSize.height, TableLayout.FILL }
            };
            
            jContentPane = new JPanel(new TableLayout(tablelay));
            
            jContentPane.add(getButtonSend(), "2, 1");
            jContentPane.add(getButtonCancel(), "2, 3");
            
            if (pollMode) {
                jContentPane.add(getPaneCommon(), "0, 0, 0, 4");
                jContentPane.add(new JSeparator(JSeparator.VERTICAL), "1, 0, 1, 4, L, F");
            } else
                jContentPane.add(getTabMain(), "0, 0, 0, 4");
        }
        return jContentPane;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getButtonSend() {
        if (ButtonSend == null) {
            ButtonSend = new JButton();
            if (pollMode) {
                ButtonSend.setText(_("Poll"));
                ButtonSend.setIcon(utils.loadIcon("general/Import"));
            } else {
                ButtonSend.setText(_("Send"));
                ButtonSend.setIcon(utils.loadIcon("general/SendMail"));
            }

            /*ButtonSend.setMinimumSize(buttonSize);
            ButtonSend.setPreferredSize(buttonSize);
            ButtonSend.setMaximumSize(buttonSize);*/
            ButtonSend.addActionListener(new SendButtonListener());
        }
        return ButtonSend;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getButtonCancel() {
        if (ButtonCancel == null) {
            ButtonCancel = new JButton();
            ButtonCancel.setText(_("Cancel"));
            
            ButtonCancel.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   dispose();
               } 
            });
            
            /*ButtonCancel.setMinimumSize(buttonSize);
            ButtonCancel.setPreferredSize(buttonSize);
            ButtonCancel.setMaximumSize(buttonSize);*/
        }
        return ButtonCancel;
    }

    
    private JComboBox getComboResolution() {
        if (ComboResolution == null) {
            ComboResolution = new JComboBox(utils.resolutions);
            //ComboResolution.setBounds(10, 130, 150, 25);
            //AddLabel(ComboResolution, _("Resolution:"));
        }
        return ComboResolution;
    }
    
    private JComboBox getComboPaperSize() {
        if (ComboPaperSize== null) {
            ComboPaperSize= new JComboBox(utils.papersizes);
            //ComboPaperSize.setBounds(180, 130, 150, 25);
            //AddLabel(ComboPaperSize, _("Paper size:"));
        }
        return ComboPaperSize;
    }
    
    private JComboBox getComboNotification() {
        if (ComboNotification== null) {
            ComboNotification= new JComboBox(utils.notifications);
            //ComboNotification.setBounds(10, 180, 320, 25);
            //AddLabel(ComboNotification, _("Notify when:"));
        }
        return ComboNotification;
    }
    
    private JSpinner getSpinKillTime() {
        if (SpinKillTime== null) {
            SpinKillTime= new JSpinner(new SpinnerNumberModel(180, 0, 2000, 15));
            //SpinKillTime.setBounds(10, 230, 150, 25);
            //AddLabel(SpinKillTime, _("Cancel job after (minutes):"));
        }
        return SpinKillTime;
    }
    
    private JSpinner getSpinMaxTries() {
        if (SpinMaxTries== null) {
            SpinMaxTries= new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));
            //SpinMaxTries.setBounds(180, 230, 150, 25);
            //AddLabel(SpinMaxTries, _("Maximum tries:"));
        }
        return SpinMaxTries;
    }
    
    private FileTextField getFtfFilename() {
        if (ftfFilename == null) {
            ftfFilename = new FileTextField() {
                @Override
                protected void writeTextFieldFileName(String fName) {
                    super.writeTextFieldFileName(fName);
                    tflFiles.model.addElement(fName);
                    tflFiles.list.setSelectedIndex(tflFiles.model.getSize() - 1);
                }
            };
            ftfFilename.setFileFilters(
                    new ExampleFileFilter("ps", _("Postscript files")),
                    new ExampleFileFilter("pdf", _("PDF documents")),
                    new ExampleFileFilter("txt", _("Plain text"))
            );           
        }
        return ftfFilename;
    }
    
    private JButton getButtonPhoneBook() {
        if (ButtonPhoneBook == null) {
            ButtonPhoneBook = new JButton(utils.loadIcon("general/Bookmarks"));
            ButtonPhoneBook.setToolTipText(_("Choose number from phone book"));
            
            ButtonPhoneBook.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PhoneBookWin pbw = new PhoneBookWin(SendWin.this);
                    PhoneBookEntry pb = pbw.selectNumber();
                    if (pb != null) {
                        TextNumber.setText(pb.faxnumber);
                        tflNumbers.model.addElement(pb.faxnumber);
                        tflNumbers.list.setSelectedIndex(tflNumbers.model.getSize() - 1);
                        
                        textToCompany.setText(pb.company);
                        textToLocation.setText(pb.location);
                        textToVoiceNumber.setText(pb.voicenumber);
                        String name = "";
                        if (pb.title.length() > 0)
                            name += pb.title + " ";
                        if (pb.givenname.length() > 0)
                            name += pb.givenname + " ";
                        name += pb.surname;
                        textToName.setText(name);
                    }
                }
            });
        }
        return ButtonPhoneBook;
    }
    
    private ClipboardPopup getDefClPop() {
        if (defClPop == null) {
            defClPop = new ClipboardPopup();
        }
        return defClPop;
    }
    
    public void setFilename(boolean allowUserInput, String filename) {
        if (filename != null)
            tflFiles.model.addElement(filename);
        else
            tflFiles.model.addElement(_("<none>"));
        tflFiles.list.setSelectedIndex(tflFiles.model.getSize() - 1);
        
        getFtfFilename().setEnabled(allowUserInput);
        lblFilename.setEnabled(allowUserInput);
        tflFiles.setEnabled(allowUserInput);
    }
    
    public void setInputStream(InputStream inStream) {
        setFilename(false, null);
        myInStream = inStream;
    }
    
    class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {        
                String coverName = null;
                String remoteName = null;
                FaxOptions fo = utils.getFaxOptions();
                
                if (!pollMode) {
                    File submitFile = null;
                    boolean delSubmitFile = false;                     
                    
                    if (checkUseCover.isSelected() && checkCustomCover.isSelected() && (!(new File(ftfCustomCover.getText()).canRead()))) {
                        JOptionPane.showMessageDialog(SendWin.this, MessageFormat.format(_("Can not read file \"{0}\"!"), ftfCustomCover.getText()), _("Error"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    hyfc.type(HylaFAXClient.TYPE_IMAGE);
                    
                    if (myInStream != null) { // read from custom input stream
                        if (checkUseCover.isSelected()) { // We need to read the data 2 times
                            submitFile = File.createTempFile("submit", ".tmp");
                            submitFile.deleteOnExit();
                            delSubmitFile = true;
                            
                            byte[] buf = new byte[8000];
                            int len = 0;
                            FileOutputStream fOut = new FileOutputStream(submitFile);
                            BufferedInputStream fIn = new BufferedInputStream(myInStream);
                            while ((len = fIn.read(buf)) >= 0) {
                                fOut.write(buf, 0, len);
                            }
                            fOut.close();
                        } else                        
                            remoteName = hyfc.putTemporary(myInStream);
                    } else if (ftfFilename.getText().startsWith("@Server:")) // Reference file on server
                        remoteName = ftfFilename.getText().substring(8);
                    else
                        submitFile = new File(ftfFilename.getText());
                    
                    if (submitFile != null) {
                        if (!submitFile.canRead()) {
                            JOptionPane.showMessageDialog(SendWin.this, MessageFormat.format(_("Can not read file \"{0}\"!"), submitFile.getPath()), _("Error"), JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                            
                        FileInputStream fi = new FileInputStream(submitFile);
                        remoteName = hyfc.putTemporary(fi);
                        fi.close();
                    }                    
                    
                    // Create cover
                    if (checkUseCover.isSelected()) {
                        Faxcover cov = new Faxcover();
                        
                        cov.fromCompany = fo.FromCompany;
                        cov.fromFaxNumber = fo.FromFaxNumber;
                        cov.fromLocation = fo.FromLocation;
                        cov.fromVoiceNumber = fo.FromVoiceNumber;
                        cov.sender = fo.FromName;
                        
                        cov.comments = textToComments.getText();
                        cov.regarding = textSubject.getText();
                        cov.toCompany = textToCompany.getText();
                        cov.toFaxNumber = TextNumber.getText();
                        cov.toLocation = textToLocation.getText();
                        cov.toName = textToName.getText();
                        cov.toVoiceNumber = textToVoiceNumber.getText();
                        
                        cov.setPageSize(((PaperSize)ComboPaperSize.getSelectedItem()).size);
                        
                        cov.pageCount = 0;
                        
                        // Try to get page count
                        FileInputStream fi = new FileInputStream(submitFile);
                        cov.estimatePostscriptPages(fi);
                        fi.close();
                        
                        if (checkCustomCover.isSelected())
                            cov.coverTemplate = new File(ftfCustomCover.getText());
                        
                        // Create cover:
                        File coverFile = File.createTempFile("cover", ".tmp");
                        coverFile.deleteOnExit();
                        FileOutputStream fout = new FileOutputStream(coverFile);
                        cov.makeCoverSheet(fout);
                        fout.close();
                        fi = new FileInputStream(coverFile);
                        coverName = hyfc.putTemporary(fi);
                        fi.close();
                        coverFile.delete();                        
                    }
                    
                    if (delSubmitFile && (submitFile != null))
                        submitFile.delete();
                }            
                
                Job j = hyfc.createJob();
                
                j.setFromUser(fo.user);
                j.setNotifyAddress(fo.notifyAddress);
                j.setMaximumDials(fo.maxDial);
                
                j.setDialstring(TextNumber.getText());
                j.setMaximumTries(((Integer)SpinMaxTries.getValue()).intValue());
                j.setNotifyType(((FaxStringProperty)ComboNotification.getSelectedItem()).type);
                j.setPageDimension(((PaperSize)ComboPaperSize.getSelectedItem()).size);
                j.setVerticalResolution(((FaxIntProperty)ComboResolution.getSelectedItem()).type);
                j.setKilltime(utils.minutesToHylaTime(((Integer)SpinKillTime.getValue()).intValue()));  
                
                if (pollMode) 
                    j.setProperty("POLL", "\"\" \"\"");
                else {               
                    if (coverName != null)
                        j.setProperty("COVER", coverName);
                    if (remoteName != null)
                        j.addDocument(remoteName);
                    fo.useCover = checkUseCover.isSelected();
                    fo.useCustomCover = checkCustomCover.isSelected();
                    fo.CustomCover = ftfCustomCover.getText();
                }
                                
                hyfc.submit(j);       
                
                dispose();
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(ButtonSend, _("An error occured while submitting the job: ") + "\n" + e1.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
        
    }
}  //  @jve:decl-index=0:visual-constraint="10,10"
