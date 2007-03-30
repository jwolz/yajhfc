package yajhfc;
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

import gnu.hylafax.HylaFAXClient;
import gnu.hylafax.Job;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

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
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;

import yajhfc.FormattedFile.FileFormat;
import yajhfc.faxcover.Faxcover;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookWin;


public class SendWin extends JDialog  {
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
    
    //private JLabel lblFilename = null;
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
    private JButton buttonPreview;
    
    private ClipboardPopup defClPop, clpNumbers, clpFiles;
    
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
            
            tflFiles = new TextFieldList(getFtfFilename().getJTextField(), true) {
                @Override
                protected TFLItem createListItem(String text) {
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
            
            tflNumbers = new TextFieldList(TextNumber, false) {
                @Override
                protected TFLItem createListItem(String text) {
                    NumberTFLItem rv = new NumberTFLItem(text);
                    if (!pollMode) {
                        rv.company = textToCompany.getText();
                        rv.location = textToLocation.getText();
                        rv.name = textToName.getText();
                        rv.voiceNumber = textToVoiceNumber.getText();
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
            tflNumbers.addLocalComponent(ButtonPhoneBook);
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
            Action actCancel = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                };
            };
            actCancel.putValue(Action.NAME, _("Cancel"));
            ButtonCancel = new JButton(actCancel);
            ButtonCancel.getActionMap().put("EscapePressed", actCancel);
            ButtonCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "EscapePressed");
            
            /*ButtonCancel.setMinimumSize(buttonSize);
            ButtonCancel.setPreferredSize(buttonSize);
            ButtonCancel.setMaximumSize(buttonSize);*/
        }
        return ButtonCancel;
    }

    private JButton getButtonPreview() {
        if (buttonPreview == null) {
            buttonPreview = new JButton(_("Preview"), utils.loadIcon("general/PrintPreview"));
            buttonPreview.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tflFiles.commit();
                    tflNumbers.commit();
                    
                    if (!checkUseCover.isSelected() && tflFiles.model.size() == 0) {
                        JOptionPane.showMessageDialog(SendWin.this, _("Nothing to preview! (Neither a cover page nor a file to send has been selected.)"), _("Preview"), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    PreviewWorker wrk = new PreviewWorker();
                    wrk.startWork(SendWin.this, _("Previewing fax"));
                }
            });
        }
        return buttonPreview;
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
                    tflFiles.addListItem(fName);
                }
            };
            ftfFilename.setFileFilters(
                    new ExampleFileFilter(new String[] { "ps", "pdf", "jpg", "jpeg", "gif", "png" }, _("All supported formats")),
                    new ExampleFileFilter("ps", _("Postscript files")),
                    new ExampleFileFilter("pdf", _("PDF documents")),
                    new ExampleFileFilter(new String[] {"jpg", "jpeg"}, _("JPEG pictures")),
                    new ExampleFileFilter("gif", _("GIF pictures")),
                    new ExampleFileFilter("png", _("PNG pictures"))
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
                        NumberTFLItem nti = new NumberTFLItem(pb);
                        tflNumbers.addListItem(nti);
                        
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
        return ButtonPhoneBook;
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
        tflFiles.model.addElement(new ServerFileTFLItem(serverFile));
    }
    
    public void addInputStream(InputStream inStream) {
        try {
            tflFiles.model.addElement(new StreamTFLItem(inStream));
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
            
    private void setPaperSizes() {
        PaperSize desiredSize = (PaperSize)ComboPaperSize.getSelectedItem();
        for (int i = 0; i < tflFiles.model.size(); i++) {
            HylaTFLItem item = (HylaTFLItem)tflFiles.model.get(i);

            item.setDesiredPaperSize(desiredSize);
        }
    }
    
    private Faxcover initFaxCover() throws IOException, FileNotFoundException {
        FaxOptions fo = utils.getFaxOptions();   
        Faxcover cov;

        cov = new Faxcover();
        cov.pageCount = 0;

        if (checkCustomCover.isSelected()) {
            if (!(new File(ftfCustomCover.getText()).canRead())) {
                JOptionPane.showMessageDialog(SendWin.this, MessageFormat.format(_("Can not read file \"{0}\"!"), ftfCustomCover.getText()), _("Error"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } else if (fo.useCustomDefaultCover) {
            if (!(new File(fo.defaultCover).canRead())) {
                JOptionPane.showMessageDialog(SendWin.this, MessageFormat.format(_("Can not read default cover page file \"{0}\"!"), fo.defaultCover), _("Error"), JOptionPane.WARNING_MESSAGE);
                return null;
            }
        }

        for (int i = 0; i < tflFiles.model.size(); i++) {
            HylaTFLItem item = (HylaTFLItem)tflFiles.model.get(i);

            InputStream strIn = item.getInputStream();
            if (strIn != null) {
                // Try to get page count 
                cov.estimatePostscriptPages(strIn);
                strIn.close();
            }
        }

        cov.fromCompany = fo.FromCompany;
        cov.fromFaxNumber = fo.FromFaxNumber;
        cov.fromLocation = fo.FromLocation;
        cov.fromVoiceNumber = fo.FromVoiceNumber;
        cov.sender = fo.FromName;

        cov.comments = textToComments.getText();
        cov.regarding = textSubject.getText();
        
        cov.setPageSize(((PaperSize)ComboPaperSize.getSelectedItem()).size);

        if (checkCustomCover.isSelected())
            cov.coverTemplate = new File(ftfCustomCover.getText());
        else if (fo.useCustomDefaultCover)
            cov.coverTemplate = new File(fo.defaultCover);
        
        return cov;
    }
    private File makeCoverFile(Faxcover cov, NumberTFLItem to) throws IOException, FileNotFoundException {
        File coverFile;
        
        if (to != null) {
            cov.toCompany = to.company;
            cov.toFaxNumber = to.faxNumber;
            cov.toLocation = to.location;
            cov.toName = to.name;
            cov.toVoiceNumber = to.voiceNumber;
        } else {
            cov.toCompany = textToCompany.getText();
            cov.toFaxNumber = TextNumber.getText();
            cov.toLocation = textToLocation.getText();
            cov.toName = textToName.getText();
            cov.toVoiceNumber = textToVoiceNumber.getText();
        }
        
        // Create cover:
        coverFile = File.createTempFile("cover", ".tmp");
        coverFile.deleteOnExit();
        FileOutputStream fout = new FileOutputStream(coverFile);
        cov.makeCoverSheet(fout);
        fout.close();                       

        return coverFile;
    }

    private class PreviewWorker extends ProgressWorker {
        
        protected int calculateMaxProgress() {
            return 10000;
        }
        
        @Override
        public void doWork() {
            try {
                int step;
                setPaperSizes();
                
                if (checkUseCover.isSelected()) {
                    step = 10000 / (tflFiles.model.size() + 1);
                    updateNote(_("Creating cover page"));
                    
                    File coverFile = makeCoverFile(initFaxCover(), (NumberTFLItem)tflNumbers.getList().getSelectedValue());
                    FormattedFile.viewFile(coverFile.getPath(), FileFormat.PostScript);
                    setProgress(step);
                } else {
                    if (tflFiles.model.size() > 0)
                        step = 10000 / tflFiles.model.size();
                    else
                        step = 0;
                }
                for (int i = 0; i < tflFiles.model.size(); i++) {
                    HylaTFLItem item = (HylaTFLItem)tflFiles.model.get(i);
                    updateNote(MessageFormat.format(_("Formatting {0}"), item.getText()));
                    item.preview(SendWin.this, hyfc);
                    stepProgressBar(step);
                }
            } catch (Exception e1) {
                showExceptionDialog(utils._("Error previewing the documents:"), e1);
            }
        } 
    }
    private class SendWorker extends ProgressWorker {
        private void setIfNotEmpty(Job j, String prop, String val) {
            try {
            if (val.length() >  0)
                j.setProperty(prop, val);
            } catch (Exception e) {
                System.err.println("Couldn't set additional job info " + prop + ": " + e.getMessage());
            }
        }
        
        @Override
        protected int calculateMaxProgress() {
            int maxProgress;
            maxProgress = 20 * tflFiles.model.size() + 20 * tflNumbers.model.size() + 10;
            if (checkUseCover != null && checkUseCover.isSelected()) {
                maxProgress += 20;
            }
            return maxProgress;
        }
        
        @Override
        public void doWork() {
            try {        
                //File coverFile = null;
                Faxcover cover = null;
                FaxOptions fo = utils.getFaxOptions();                    
                
                if (!pollMode) {
                    setPaperSizes();
                    
                    if (checkUseCover.isSelected()) {
                        cover = initFaxCover();
                        stepProgressBar(20);
                    }
                    
                    // Upload documents:
                    synchronized (hyfc) {
                        hyfc.type(HylaFAXClient.TYPE_IMAGE);

                        for (int i = 0; i < tflFiles.model.size(); i++) {
                            HylaTFLItem item = (HylaTFLItem)tflFiles.model.get(i);
                            updateNote(MessageFormat.format(_("Uploading {0}"), item.getText()));
                            item.upload(hyfc);

                            stepProgressBar(20);
                        }
                    }
                }            
                
                for (int i = 0; i < tflNumbers.model.size(); i++) {
                    NumberTFLItem numItem = (NumberTFLItem)tflNumbers.model.get(i);
                    updateNote(MessageFormat.format(_("Creating job to {0}"), numItem.getText()));
                    
                    try {
                        String coverName = null;
                        if (cover != null) {
                            File coverFile = makeCoverFile(cover, numItem);

                            FileInputStream fi = new FileInputStream(coverFile);
                            coverName = hyfc.putTemporary(fi);
                            fi.close();
                            
                            coverFile.delete();
                        }
                        stepProgressBar(5);
                        
                        synchronized (hyfc) {
                            Job j = hyfc.createJob();

                            stepProgressBar(5);

                            j.setFromUser(fo.user);
                            j.setNotifyAddress(fo.notifyAddress);
                            j.setMaximumDials(fo.maxDial);

                            if (!pollMode) {
                                // Set general job information...
                                setIfNotEmpty(j, "TOUSER", numItem.name);
                                setIfNotEmpty(j, "TOCOMPANY", numItem.company);
                                setIfNotEmpty(j, "TOLOCATION", numItem.location);
                                setIfNotEmpty(j, "TOVOICE", numItem.voiceNumber);
                                setIfNotEmpty(j, "REGARDING", textSubject.getText());
                                setIfNotEmpty(j, "FROMCOMPANY", fo.FromCompany);
                                setIfNotEmpty(j, "FROMLOCATION", fo.FromLocation);
                                setIfNotEmpty(j, "FROMVOICE", fo.FromVoiceNumber);
                            }

                            j.setDialstring(numItem.faxNumber);
                            j.setProperty("EXTERNAL", numItem.faxNumber); // needed to fix an error while sending multiple jobs
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

                                for (int k = 0; k < tflFiles.model.size(); k++) {
                                    HylaTFLItem item = (HylaTFLItem)tflFiles.model.get(k);
                                    j.addDocument(item.getServerName());                        
                                }

                                fo.useCover = checkUseCover.isSelected();
                                fo.useCustomCover = checkCustomCover.isSelected();
                                fo.CustomCover = ftfCustomCover.getText();
                            }

                            stepProgressBar(5);

                            hyfc.submit(j);
                        }
                        
                        stepProgressBar(5);
                    } catch (Exception e1) {
                        showExceptionDialog(MessageFormat.format(_("An error occured while submitting the fax job for phone number \"{0}\" (will try to submit the fax to the other numbers anyway): "), numItem.getText()) , e1);
                    }
                }
                
                updateNote(_("Cleaning up"));
                for (int i = 0; i < tflFiles.model.size(); i++) {
                    HylaTFLItem item = (HylaTFLItem)tflFiles.model.get(i);
                    item.cleanup();
                }
                
            } catch (Exception e1) {
                //JOptionPane.showMessageDialog(ButtonSend, _("An error occured while submitting the fax: ") + "\n" + e1.getLocalizedMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
                showExceptionDialog(_("An error occured while submitting the fax: "), e1);
            } 
        }
        
        @Override
        protected void done() {
            dispose();
        }
    }
    private class SendButtonListener implements ActionListener {
               
        public void actionPerformed(ActionEvent e) {
            
            tflFiles.commit();
            tflNumbers.commit();
            
            if (!pollMode && tflFiles.model.size() == 0) {
                if (checkUseCover.isSelected()) {
                    if (JOptionPane.showConfirmDialog(SendWin.this, _("You haven't selected a file to transmit, so your fax will ONLY contain the cover page.\nContinue anyway?"), _("Continue?"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
                        return;
                } else {
                    JOptionPane.showMessageDialog(SendWin.this, _("To send a fax you must select at least one file!"), _("Warning"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            
            if (tflNumbers.model.size() == 0) {
                JOptionPane.showMessageDialog(SendWin.this, _("To send a fax you have to enter at least one phone number!"), _("Warning"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            SendWorker wrk = new SendWorker();
            wrk.startWork(SendWin.this, _("Sending fax"));
        }
    }
    private static class NumberTFLItem extends TFLItem {
        public String faxNumber;
        public String name, company, location, voiceNumber;
        
        @Override
        public String getText() {
            return faxNumber;
        }
        
        @Override
        public void setText(String newText) {
            faxNumber = newText;
        }
        
        public void loadFromPBE(PhoneBookEntry pbe) {
            faxNumber = pbe.getFaxNumber();
            
            company = pbe.getCompany();
            location = pbe.getLocation();
            voiceNumber = pbe.getVoiceNumber();
            
            name = "";
            if (pbe.getTitle().length() > 0)
                name += pbe.getTitle() + " ";
            if (pbe.getGivenName().length() > 0)
                name += pbe.getGivenName() + " ";
            name += pbe.getName();
        }
        
        public NumberTFLItem(String number) {
            this.faxNumber = number;
        }
        
        public NumberTFLItem(PhoneBookEntry pbe) {
            loadFromPBE(pbe);
        }
    }
}  








