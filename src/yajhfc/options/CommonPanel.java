/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2010 Jonas Wolz
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
package yajhfc.options;

import static yajhfc.Utils._;
import static yajhfc.Utils.addWithLabel;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import yajhfc.DateStyle;
import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.YajLanguage;
import yajhfc.send.SendWinStyle;
import yajhfc.util.ClipboardPopup;

/**
 * @author jonas
 *
 */
public class CommonPanel extends AbstractOptionsPanel<FaxOptions> {
    static final Logger log = Logger.getLogger(CommonPanel.class.getName());
    
    static class LF_Entry {
        public String name;
        public String className;
        
        public LF_Entry(String name, String className) {
            this.name = name;
            this.className = className;
        }
        
        public LF_Entry(UIManager.LookAndFeelInfo lfi) {
            this(lfi.getName(), lfi.getClassName());
        }
        
        public String toString() {
            return name;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj instanceof LF_Entry) {
                return ((LF_Entry)obj).className.equals(className);
            } else if (obj instanceof String) {
                return obj.equals(className);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return className.hashCode();
        }
        
        public static Vector<LF_Entry> getLookAndFeelList() {
            UIManager.LookAndFeelInfo[] lfiList = UIManager.getInstalledLookAndFeels();
            Vector<LF_Entry> entries = new Vector<LF_Entry>(lfiList.length + 2);
            entries.add(new LF_Entry(Utils._("(System native)"), FaxOptions.LOOKANDFEEL_SYSTEM));
            entries.add(new LF_Entry(Utils._("(Crossplatform)"), FaxOptions.LOOKANDFEEL_CROSSPLATFORM));
            for (UIManager.LookAndFeelInfo lfi : lfiList) {
                entries.add(new LF_Entry(lfi));
            }
            return entries;
        }
    }

    JPanel panelUpdates, panelUI, panelDateFormat, panelNewFaxAction;
    JCheckBox checkAutoCheckForUpdate;
    JComboBox comboLang;
    Vector<LF_Entry> lookAndFeels;
    JComboBox comboLookAndFeel;
    JComboBox comboSendWinStyle;
    JCheckBox checkShowTrayIcon;
    boolean changedLF;
    JCheckBox checkMinimizeToTray;
    JCheckBox checkMinimizeToTrayOnMainWinClose;
    JCheckBox checkNewFax_Beep;
    JCheckBox checkNewFax_BlinkTrayIcon;
    JCheckBox checkNewFax_MarkAsRead;
    JCheckBox checkNewFax_Open;
    JCheckBox checkNewFax_ToFront;
    JCheckBox checkNewFax_TrayNotification;
    JComboBox comboDateFormat;
    JComboBox comboTimeFormat;
    final OptionsWin parent;
    String oldLookAndFeel;
    
    public CommonPanel(OptionsWin parent) {
        super(false);
        this.parent = parent;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.AbstractOptionsPanel#createOptionsUI()
     */
    @Override
    protected void createOptionsUI() {
            double[][] tablelay = {
                    {OptionsWin.border, 0.4, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                    {OptionsWin.border, 0.75, OptionsWin.border, TableLayout.PREFERRED, TableLayout.FILL, OptionsWin.border }
            };
            this.setLayout(new TableLayout(tablelay));
            
            this.add(getPanelUI(), "1,1");
            this.add(getPanelNewFaxAction(), "3,1");
            this.add(getPanelDateFormat(), "1,3");
            this.add(getPanelUpdates(), "3,3");
    }

    private JPanel getPanelUpdates() {
        if (panelUpdates == null) {
            panelUpdates = new JPanel(new BorderLayout(), false);
            panelUpdates.setBorder(BorderFactory.createTitledBorder(_("Update check")));
            
            checkAutoCheckForUpdate = new JCheckBox(_("Automatically check for updates"));
            
            panelUpdates.add(checkAutoCheckForUpdate, BorderLayout.CENTER);
        }
        return panelUpdates;
    }
    
    private JPanel getPanelUI() {
        if (panelUI == null) {
            final int rowCount = 11;
            final double[][] tablelay = {
                    {OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                    new double[rowCount]
            };
            final double rowh = 2 / (double)(rowCount + 1);
            //tablelay[1][0] = border;
            tablelay[1][rowCount - 1] = OptionsWin.border;
            for (int i = 0; i < rowCount-4; i++) {
                if (i%2 == 0) {
                    tablelay[1][i] = TableLayout.PREFERRED;
                } else {
                    tablelay[1][i] = rowh;
                }
            }
            tablelay[1][rowCount - 3] = tablelay[1][rowCount - 4] = rowh;
            tablelay[1][rowCount - 2] =  TableLayout.FILL;
            
            panelUI = new JPanel(new TableLayout(tablelay), false);
            panelUI.setBorder(BorderFactory.createTitledBorder(_("User interface")));
            
            //comboNewFaxAction = new JComboBox(Utils.newFaxActions);
            
            comboLang = new JComboBox(new Vector<YajLanguage>(YajLanguage.supportedLanguages));
            
            lookAndFeels = LF_Entry.getLookAndFeelList();
            comboLookAndFeel = new JComboBox(lookAndFeels);
            comboLookAndFeel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LF_Entry sel = (LF_Entry)comboLookAndFeel.getSelectedItem();
                    log.fine("Look&Feel " + sel + " selected");
                    if (changedLF || !sel.className.equals(oldLookAndFeel)) {
                        log.info("Changing L&F of OptionsDialog to " + sel.className);
                        Utils.setLookAndFeel(sel.className);
                        
                        SwingUtilities.updateComponentTreeUI(parent);
                        changedLF = true;
                    }
                }
            });
            
            comboSendWinStyle = new JComboBox(SendWinStyle.values());
            
            checkShowTrayIcon = new JCheckBox(_("Show tray icon"));
            checkShowTrayIcon.setToolTipText(_("Show a system tray icon (works only with Java 6 or higher)"));
            checkShowTrayIcon.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    final boolean selected = checkShowTrayIcon.isSelected();
                    checkMinimizeToTray.setEnabled(selected);
                    checkMinimizeToTrayOnMainWinClose.setEnabled(selected);
                } 
            });
            
            checkMinimizeToTray = new JCheckBox(_("Minimize to tray"));
            checkMinimizeToTray.setEnabled(false);
            //checkMinimizeToTray.setToolTipText(_("Minimize to system tray (works only with Java 6 or higher)"));

            checkMinimizeToTrayOnMainWinClose = new JCheckBox("<html>" + _("Minimize to tray when main window is closed") + "</html>");
            checkMinimizeToTrayOnMainWinClose.addPropertyChangeListener("enabled", new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    checkMinimizeToTrayOnMainWinClose.setForeground((Boolean)evt.getNewValue() ? 
                            UIManager.getColor("CheckBox.foreground") :
                            UIManager.getColor("CheckBox.disabledText"));
                }
            });
            checkMinimizeToTrayOnMainWinClose.setEnabled(false);
            
            addWithLabel(panelUI, comboLang, _("Language:"), "1, 1, 1, 1, f, c");
            addWithLabel(panelUI, comboLookAndFeel, _("Look and Feel:"), "1, 3, 1, 3, f, c");
            addWithLabel(panelUI, comboSendWinStyle, _("Style of send dialog:"), "1, 5, 1, 5, f, c");
            //addWithLabel(panelUI, comboNewFaxAction, "<html>" + _("When a new fax is received:") + "</html>", "1, 3, 1, 3, f, c");
            
            panelUI.add(checkShowTrayIcon, "1,7,1,7,f,c");
            panelUI.add(checkMinimizeToTray, "1,8,1,8,f,c");
            panelUI.add(checkMinimizeToTrayOnMainWinClose, "1,9,1,9,f,c");
        }
        return panelUI;
    }
    
    private JPanel getPanelNewFaxAction() {
        if (panelNewFaxAction == null) {
            final int rowCount = 8;
            double[][] tablelay = {
                    {OptionsWin.border, 4*OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                    new double[rowCount]
            };
            double rowh = 1 / (double)(rowCount - 2);
            tablelay[1][0] = OptionsWin.border;
            tablelay[1][rowCount - 1] = OptionsWin.border;
            Arrays.fill(tablelay[1], 1, rowCount - 1, rowh);
            tablelay[1][rowCount - 2] = TableLayout.FILL;
            
            panelNewFaxAction = new JPanel(new TableLayout(tablelay), false);
            panelNewFaxAction.setBorder(BorderFactory.createTitledBorder(_("Actions after receiving a new fax:")));
            
            checkNewFax_Beep = new JCheckBox(_("Beep"));
            checkNewFax_ToFront = new JCheckBox(_("Bring to front"));
            checkNewFax_Open = new JCheckBox(_("Open in viewer"));
            checkNewFax_Open.addChangeListener(new ChangeListener() {
               public void stateChanged(ChangeEvent e) {
                   checkNewFax_MarkAsRead.setEnabled(checkNewFax_Open.isSelected());
               }
            });
            checkNewFax_MarkAsRead = new JCheckBox(_("And mark as read"));
            checkNewFax_MarkAsRead.setEnabled(false);
            checkNewFax_BlinkTrayIcon = new JCheckBox(_("Show flashing tray icon"));
            checkNewFax_TrayNotification = new JCheckBox(_("Display tray icon message"));
            
            panelNewFaxAction.add(checkNewFax_Beep, "1,1,2,1");
            panelNewFaxAction.add(checkNewFax_ToFront, "1,2,2,2");
            panelNewFaxAction.add(checkNewFax_Open, "1,3,2,3");
            panelNewFaxAction.add(checkNewFax_MarkAsRead, "2,4");
            panelNewFaxAction.add(checkNewFax_BlinkTrayIcon, "1,5,2,5");
            panelNewFaxAction.add(checkNewFax_TrayNotification, "1,6,2,6");
        }
        return panelNewFaxAction;
    }
    
    private JPanel getPanelDateFormat() {
        if (panelDateFormat == null) {
            double[][] tablelay = {
                    {OptionsWin.border, 0.5, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                    {OptionsWin.border, TableLayout.FILL, TableLayout.PREFERRED, OptionsWin.border}
            };
            panelDateFormat = new JPanel(new TableLayout(tablelay),false);
            panelDateFormat.setBorder(BorderFactory.createTitledBorder(_("Date and Time Format:")));
            
            comboDateFormat = new JComboBox(DateStyle.getAvailableDateStyles());
            comboDateFormat.setEditable(true);
            ClipboardPopup.DEFAULT_POPUP.addToComponent(comboDateFormat);
            comboTimeFormat = new JComboBox(DateStyle.getAvailableTimeStyles());
            comboTimeFormat.setEditable(true);
            ClipboardPopup.DEFAULT_POPUP.addToComponent(comboTimeFormat);
            
            addWithLabel(panelDateFormat, comboDateFormat, _("Date format:"), "1,2");
            addWithLabel(panelDateFormat, comboTimeFormat, _("Time format:"), "3,2");
        }
        return panelDateFormat;
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        oldLookAndFeel = foEdit.lookAndFeel;
        comboLang.setSelectedItem(foEdit.locale);
        comboSendWinStyle.setSelectedItem(foEdit.sendWinStyle);

        int pos = 0; 
        for (int i=0; i<lookAndFeels.size(); i++) {
            if (lookAndFeels.get(i).className.equals(foEdit.lookAndFeel)) {
                pos = i;
                break;
            }
        }
        comboLookAndFeel.setSelectedIndex(pos);
        
        
        setDateStyle(comboDateFormat, foEdit.dateStyle, DateStyle.getAvailableDateStyles());
        setDateStyle(comboTimeFormat, foEdit.timeStyle, DateStyle.getAvailableTimeStyles());
        
        checkShowTrayIcon.setSelected(foEdit.showTrayIcon);
        checkMinimizeToTray.setSelected(foEdit.minimizeToTray);
        checkMinimizeToTrayOnMainWinClose.setSelected(foEdit.minimizeToTrayOnMainWinClose);
        checkAutoCheckForUpdate.setSelected(foEdit.automaticallyCheckForUpdate);
        
        checkNewFax_Beep.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_BEEP) != 0);
        checkNewFax_ToFront.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_TOFRONT) != 0);
        checkNewFax_Open.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_VIEWER) != 0);
        checkNewFax_MarkAsRead.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_MARKASREAD) != 0);
        checkNewFax_BlinkTrayIcon.setSelected((foEdit.newFaxAction & FaxOptions.NEWFAX_BLINKTRAYICON) != 0);
        checkNewFax_TrayNotification.setSelected(foEdit.newFaxTrayNotification);
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        foEdit.sendWinStyle = (SendWinStyle)comboSendWinStyle.getSelectedItem();
        
        String newLF = ((LF_Entry)comboLookAndFeel.getSelectedItem()).className;
        if (!newLF.equals(foEdit.lookAndFeel)) {
            foEdit.lookAndFeel = newLF;
            //JOptionPane.showMessageDialog(OptionsWin.this, _("You must restart the program for a change of the look&feel to take effect."), _("Options"), JOptionPane.INFORMATION_MESSAGE);
            
            Utils.setLookAndFeel(newLF);
            refreshLF();
        }
        
        YajLanguage newLang = (YajLanguage)comboLang.getSelectedItem();
        if (!newLang.equals(foEdit.locale)) {
            foEdit.locale = newLang;
            JOptionPane.showMessageDialog(this, _("You must restart the program for the change of the language to take effect."), _("Options"), JOptionPane.INFORMATION_MESSAGE);
        }
        
        foEdit.showTrayIcon = checkShowTrayIcon.isSelected();
        foEdit.minimizeToTray = checkMinimizeToTray.isSelected();
        foEdit.minimizeToTrayOnMainWinClose = checkMinimizeToTrayOnMainWinClose.isSelected();
        foEdit.automaticallyCheckForUpdate = checkAutoCheckForUpdate.isSelected();
        
        int val = 0;
        if (checkNewFax_Beep.isSelected())
            val |= FaxOptions.NEWFAX_BEEP;
        if (checkNewFax_ToFront.isSelected())
            val |= FaxOptions.NEWFAX_TOFRONT;
        if (checkNewFax_Open.isSelected())
            val |= FaxOptions.NEWFAX_VIEWER;
        if (checkNewFax_MarkAsRead.isSelected())
            val |= FaxOptions.NEWFAX_MARKASREAD;
        if (checkNewFax_BlinkTrayIcon.isSelected())
            val |= FaxOptions.NEWFAX_BLINKTRAYICON;
        foEdit.newFaxAction = val;
        foEdit.newFaxTrayNotification = checkNewFax_TrayNotification.isSelected();
        
        foEdit.dateStyle = getDateStyle(comboDateFormat);
        foEdit.timeStyle = getDateStyle(comboTimeFormat);
    }

    @Override
    public boolean validateSettings(OptionsWin optionsWin) {
        try {
            DateFormat fmt = DateStyle.getDateFormatFromString(getDateStyle(comboDateFormat));
            fmt.format(new Date());
        } catch (Exception e) {
            optionsWin.focusComponent(comboDateFormat);
            JOptionPane.showMessageDialog(this, _("Please enter a valid date format:") + '\n' + e.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            DateFormat fmt = DateStyle.getTimeFormatFromString(getDateStyle(comboTimeFormat));
            fmt.format(new Date());
        } catch (Exception e) {
            optionsWin.focusComponent(comboTimeFormat);
            JOptionPane.showMessageDialog(this, _("Please enter a valid time format.") + '\n' + e.getMessage(), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void setDateStyle(JComboBox combo, String optsField, DateStyle[] availableVals) {
        Object selStyle = optsField;
        for (DateStyle style : availableVals) {
            if (style.getSaveString().equals(selStyle)) {
                selStyle = style;
                break;
            }
        }
//        if (selStyle instanceof String) {
//            selStyle = new SimpleDateFormat((String)selStyle, Utils.getLocale()).toLocalizedPattern();
//        }
        combo.setSelectedItem(selStyle);
    }
    
    private String getDateStyle(JComboBox combo) {
        Object sel = combo.getSelectedItem();

        if (sel instanceof DateStyle) {
            return ((DateStyle)sel).getSaveString();
        } else {
            //SimpleDateFormat fmt = new SimpleDateFormat("", Utils.getLocale());
            //fmt.applyLocalizedPattern(sel.toString());
            //return fmt.toPattern();
            return sel.toString();
        }
    }
    
    // Does not work correctly :-(
    /*
     * Refreshes the Look&Feel for the complete application
     */
    static void refreshLF() {
        for (Frame f: Frame.getFrames()) {
            //refreshLF(f);
            SwingUtilities.updateComponentTreeUI(f);
        }
    }

}
