/*
 * YajHFC - Yet another Java Hylafax client
 * Copyright (C) 2009 Jonas Wolz
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
package yajhfc.printerport;

import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.options.AbstractOptionsPanel;
import yajhfc.options.OptionsWin;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.IntVerifier;

/**
 * @author jonas
 *
 */
public class PortOptionsPanel extends AbstractOptionsPanel<FaxOptions> {
    private static final int border = 8;    
    
    JTextField textBindAddr;
    JTextField textPort;
    JTextField textMkfifo;
    JTextField textFIFO;
    JCheckBox checkEnabled;
    JCheckBox checkEnableFIFO;
    JLabel labelBindAddr, labelPort, labelMkfifo, labelFIFO;

    
    public PortOptionsPanel() {
        super(false);
    }
    
    @Override
    protected void createOptionsUI() {
        double[][] dLay = {
                {border, 0.66, border, TableLayout.FILL, border},
                {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border*2, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}
        };
        setLayout(new TableLayout(dLay));
        
        JLabel labelDesc = new JLabel("<html>" + 
        _("YajHFC supports listening for new PostScript documents to send on a TCP port or a named pipe.") + "<br>" +
        _("The TCP port can be used as <i>AppSocket</i> port (<i>socket:</i> protocol) in CUPS or <i>standard TCP/IP port</i> in Windows to create a virtual fax printer.")  + "<br>" +
        _("The named pipe is especially useful on Unix, where it can be used together with a special CUPS backend to get fax printer support for multiple simultaneously logged in users.") +
        "</html>");
        
        checkEnabled = new JCheckBox(_("Enable TCP/IP printer port for YajHFC"));
        checkEnabled.setSelected(true);
        checkEnabled.addItemListener(new ItemListener() {
           public void itemStateChanged(ItemEvent e) {
               boolean enable = checkEnabled.isSelected();
               textBindAddr.setEnabled(enable);
               textPort.setEnabled(enable);
               labelBindAddr.setEnabled(enable);
               labelPort.setEnabled(enable);
            } 
        });
        
        checkEnableFIFO = new JCheckBox(_("Enable FIFO/named pipe printer port for YajHFC"));
        checkEnableFIFO.setSelected(true);
        checkEnableFIFO.addItemListener(new ItemListener() {
           public void itemStateChanged(ItemEvent e) {
               boolean enable = checkEnableFIFO.isSelected();
               textFIFO.setEnabled(enable);
               textMkfifo.setEnabled(enable);
               labelFIFO.setEnabled(enable);
               labelMkfifo.setEnabled(enable);
            } 
        });
        
        textBindAddr = new JTextField();
        textBindAddr.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        textPort = new JTextField();
        textPort.setInputVerifier(new IntVerifier(1, 65536));
        textPort.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        textFIFO = new JTextField();
        textFIFO.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        textMkfifo = new JTextField();
        textMkfifo.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        add(labelDesc, "1,1,3,1,f,f");
        add(checkEnabled, "1,3,3,3,l,c");        
        labelBindAddr = Utils.addWithLabel(this, textBindAddr, _("Bind address:"), "1,6");
        labelPort = Utils.addWithLabel(this, textPort, _("Port:"), "3,6");
        
        add(checkEnableFIFO, "1,8,3,8,l,c");
        labelFIFO = Utils.addWithLabel(this, textFIFO, _("Name of named pipe (%u is replaced with user name):"), "1,11,3,11,f,c");
        labelMkfifo = Utils.addWithLabel(this, textMkfifo, _("mkfifo command:"), "1,14,3,14,f,c");
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        PortOptions po = EntryPoint.getOptions();
        
        checkEnabled.setSelected(po.enabled);
        textBindAddr.setText(po.bindAddress);
        textPort.setText(String.valueOf(po.port));
        
        checkEnableFIFO.setSelected(po.enableFIFO);
        textFIFO.setText(po.fifoName);
        textMkfifo.setText(po.mkfifo);
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        PortOptions po = EntryPoint.getOptions();
        boolean needReopenTCP = false;
        boolean needReopenFIFO = false;
        
        final boolean newEnable = checkEnabled.isSelected();
        final String newBindAddress = textBindAddr.getText();
        final int newPort = Integer.parseInt(textPort.getText());
        
        if (newEnable != po.enabled) {
            po.enabled = newEnable;
            needReopenTCP = true;
        }
        if (!newBindAddress.equals(po.bindAddress)) {
            po.bindAddress = newBindAddress;
            needReopenTCP = true;
        }
        if (newPort != po.port) {
            po.port = newPort;
            needReopenTCP = true;
        }
        
        final boolean newEnableFIFO = checkEnableFIFO.isSelected();
        final String newFifoName = textFIFO.getText();
        final String newMkFifo = textMkfifo.getText();
        
        if (newEnableFIFO != po.enableFIFO) {
            po.enableFIFO = newEnableFIFO;
            needReopenFIFO = true;
        }
        if (!newFifoName.equals(po.fifoName)) {
            po.fifoName = newFifoName;
            needReopenFIFO = true;
        }
        if (!newMkFifo.equals(po.mkfifo)) {
            po.mkfifo = newMkFifo;
            needReopenFIFO = true;
        }

        if (needReopenTCP) {
            EntryPoint.reopenTCPThread();
        }
        if (needReopenFIFO) {
            EntryPoint.reopenFIFOThread();
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        try {
            int val = Integer.parseInt(textPort.getText());
            if (val < 1 || val > 65536) {
                JOptionPane.showMessageDialog(this, _("Please enter a number between 1 and 65536 as port number!"));
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, _("Please enter a number between 1 and 65536 as port number!"));
            return false;
        }

        if (checkEnabled.isSelected()) {
            String addr = textBindAddr.getText();
            if (addr.length() > 0) {
                try {
                    InetAddress.getByName(addr);
                } catch (UnknownHostException e) {
                    JOptionPane.showMessageDialog(this, _("Invalid bind address: ") + e);
                    return false;
                }
            }
        }

        if (checkEnableFIFO.isSelected()) {
            String mkfifoCommand = textMkfifo.getText();
            if (mkfifoCommand.length() == 0 || 
                    Utils.searchExecutableInPath(Utils.extractExecutableFromCmdLine(mkfifoCommand)) == null) {
                JOptionPane.showMessageDialog(this, _("The mkfifo command entered cannot be found."));
                return false;
            }
        }

        return true;
    }

}
