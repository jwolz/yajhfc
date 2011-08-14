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
import static yajhfc.options.OptionsWin.border;
import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import yajhfc.FaxNotification;
import yajhfc.FaxResolution;
import yajhfc.HylaModem;
import yajhfc.IDAndNameOptions;
import yajhfc.PaperSize;
import yajhfc.SenderIdentity;
import yajhfc.Utils;
import yajhfc.model.IconMap;
import yajhfc.send.JobPropsEditorDialog;
import yajhfc.server.Server;
import yajhfc.server.ServerManager;
import yajhfc.server.ServerOptions;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ListComboModel;
import yajhfc.util.ListListModel;
import yajhfc.util.WrapperComboBoxModel;

/**
 * @author jonas
 *
 */
public class SendPanel extends AbstractOptionsPanel<ServerOptions> {
    static final Logger log = Logger.getLogger(SendPanel.class.getName());
    
    JTextField textFilterFromFaxNr;
    JTextField textNotifyAddress;
    JTextField textNumberPrefix;
    JCheckBox checkArchiveSentFaxes;
    JComboBox comboModem;
    JComboBox comboNotify;
    JComboBox comboPaperSize;
    JComboBox comboResolution;
    JSpinner spinKillTime;
    JSpinner spinMaxDial;
    JSpinner spinMaxTry;
    Action jobOptionAction;
    Map<String,String> customProperties;
    JComboBox comboIdentity;
    ListListModel<SenderIdentity> identitiesModel;
    ListComboModel<HylaModem> modemsModel;

    public SendPanel() {
        super(false);
    }
    
    public ListListModel<SenderIdentity> getIdentitiesModel() {
        return identitiesModel;
    }
    
    public void setIdentitiesModel(
            ListListModel<SenderIdentity> identitiesModel) {
        this.identitiesModel = identitiesModel;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.options.AbstractOptionsPanel#createOptionsUI()
     */
    @Override
    protected void createOptionsUI() {
        final int rowCount = 22;
        double[][] tablelay = {
                {border,  0.5, border, TableLayout.FILL, border},
                new double[rowCount]
        };
        for (int i=0; i<rowCount-1; i++) {
            if (i%3 == 0) {
                tablelay[1][i] = border;
            } else {
                tablelay[1][i] = TableLayout.PREFERRED;
            }
        }
        tablelay[1][rowCount - 1] = TableLayout.FILL;
        
        this.setLayout(new TableLayout(tablelay));
        this.setBorder(BorderFactory.createTitledBorder(_("Delivery settings")));
       
        textNotifyAddress = new JTextField();
        textNotifyAddress.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        
        textFilterFromFaxNr = new JTextField();
        textFilterFromFaxNr.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        textFilterFromFaxNr.setToolTipText( _("Characters filtered from the fax number sent to HylaFAX:"));
        
        textNumberPrefix = new JTextField();
        textNumberPrefix.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        textFilterFromFaxNr.setToolTipText( _("Characters put in front of the fax number before sending it to HylaFAX."));
        
        comboNotify = new JComboBox(FaxNotification.values());
        comboNotify.setRenderer(new IconMap.ListCellRenderer());
        comboPaperSize = new JComboBox(PaperSize.values());
        comboResolution = new JComboBox(FaxResolution.values());
        
        modemsModel = new ListComboModel<HylaModem>(Collections.<HylaModem>emptyList());
        comboModem = new JComboBox(modemsModel);
        comboModem.setEditable(true);
        
        spinMaxDial = new JSpinner(new SpinnerNumberModel(12, 1, 100, 1));
        spinMaxTry = new JSpinner(new SpinnerNumberModel(6, 1, 100, 1));
        spinKillTime= new JSpinner(new SpinnerNumberModel(180, 0, 2000, 15));
        
        checkArchiveSentFaxes = new JCheckBox(_("Archive sent fax jobs"));
        
        jobOptionAction = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                JobPropsEditorDialog editDlg = new JobPropsEditorDialog((Dialog)SwingUtilities.getWindowAncestor(SendPanel.this), customProperties);
                editDlg.setVisible(true);     
            }
        };
        jobOptionAction.putValue(Action.NAME, Utils._("Job properties") + "...");
        JButton buttonJobOption = new JButton(jobOptionAction);
        
        comboIdentity = new JComboBox(new WrapperComboBoxModel(getIdentitiesModel()));        
        
        addWithLabel(this, textNotifyAddress, _("E-mail address for notifications:"), "1, 2, 3, 2, f, c");
        addWithLabel(this, comboNotify, _("Notify when:"), "1, 5, 1, 5, f, c");
        addWithLabel(this, comboModem, _("Modem:"), "3, 5, 3, 5, f, c");
        addWithLabel(this, comboPaperSize, _("Paper size:"), "1, 8, f, c" );
        addWithLabel(this, comboResolution, _("Resolution:"), "3, 8, f, c");
        addWithLabel(this, spinMaxDial, _("Maximum dials:"), "1, 11, f, c");
        addWithLabel(this, spinKillTime, _("Cancel job after (minutes):"), "3,11,f, c");
        addWithLabel(this, textFilterFromFaxNr, _("Filter from fax number:"), "1,14,f,c");
        addWithLabel(this, spinMaxTry, _("Maximum tries:"), "3,14,f,c");
        addWithLabel(this, textNumberPrefix, _("Dial prefix:"), "1,17,1,17,f,c");
        addWithLabel(this, comboIdentity, _("Default identity:"), "3,17,3,17,f,c");
        this.add(buttonJobOption, "1,20,f,c");
        this.add(checkArchiveSentFaxes, "3,20,f,c");
    }

    private String getModem() {
        Object sel = comboModem.getSelectedItem();
        if (Utils.debugMode) {
            log.info("Selected modem (" + sel.getClass().getCanonicalName() + "): " + sel);
        }
        if (sel instanceof HylaModem) {
            return ((HylaModem)sel).getInternalName();
        } else {
            String str = sel.toString();
            int pos = str.indexOf(' '); // Use part up to the first space
            if (pos == -1)
                return str;
            else
                return str.substring(0, pos);
        }
    }
    
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(ServerOptions foEdit) {
        textNotifyAddress.setText(foEdit.notifyAddress);
        textFilterFromFaxNr.setText(foEdit.filterFromFaxNr);
        textNumberPrefix.setText(foEdit.numberPrefix);

        comboNotify.setSelectedItem(foEdit.notifyWhen);
        comboPaperSize.setSelectedItem(foEdit.paperSize);
        comboResolution.setSelectedItem(foEdit.resolution);

        List<HylaModem> availableModems;
        // TODO: Use correct modems for this server
        Server currentServer = ServerManager.getDefault().getCurrent();
        if (currentServer != null)
            availableModems = currentServer.getClientManager().getModems();
        else 
            availableModems = HylaModem.defaultModems;
        
        modemsModel.setList(availableModems);
        Object selModem = foEdit.defaultModem;
        for (HylaModem modem : availableModems) {
            if (modem.getInternalName().equals(selModem)) {
                selModem = modem;
                break;
            }
        }
        comboModem.setSelectedItem(selModem);


        checkArchiveSentFaxes.setSelected(foEdit.archiveSentFaxes);

        spinMaxDial.setValue(Integer.valueOf(foEdit.maxDial));
        spinMaxTry.setValue(Integer.valueOf(foEdit.maxTry));
        spinKillTime.setValue(foEdit.killTime);
        
        customProperties = new TreeMap<String,String>(foEdit.customJobOptions);
        
        List<SenderIdentity> identities = getIdentitiesModel().getList();
        SenderIdentity identity = IDAndNameOptions.getItemByID(identities, foEdit.defaultIdentity);
        if (identity == null)
            identity = foEdit.getParent().getDefaultIdentity();
        comboIdentity.setSelectedItem(identity);
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(ServerOptions foEdit) {
        
        foEdit.maxDial = ((Integer)spinMaxDial.getValue()).intValue();
        foEdit.maxTry = ((Integer)spinMaxTry.getValue()).intValue();
        foEdit.killTime = (Integer)spinKillTime.getValue();
        
        foEdit.notifyAddress = textNotifyAddress.getText();
        foEdit.filterFromFaxNr = textFilterFromFaxNr.getText();
        foEdit.numberPrefix = textNumberPrefix.getText();
        
        foEdit.notifyWhen = (FaxNotification)comboNotify.getSelectedItem();
        foEdit.paperSize = (PaperSize)comboPaperSize.getSelectedItem();
        foEdit.resolution = (FaxResolution)comboResolution.getSelectedItem();
        
        foEdit.archiveSentFaxes = checkArchiveSentFaxes.isSelected();
        
        foEdit.defaultModem = getModem();
        
        foEdit.customJobOptions.clear();
        foEdit.customJobOptions.putAll(customProperties);
        
        SenderIdentity identity = (SenderIdentity)comboIdentity.getSelectedItem();
        foEdit.defaultIdentity = (identity == null) ? -1 : identity.id;
    }


}
