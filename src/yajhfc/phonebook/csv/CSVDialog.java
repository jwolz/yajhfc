package yajhfc.phonebook.csv;

/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.jdbc.ConnectionSettings;
import yajhfc.util.CancelAction;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExceptionDialog;
import au.com.bytecode.opencsv.CSVReader;

public class CSVDialog extends JDialog {
    private static final int border = 8;
    
    protected CSVSettings settings;
    protected JComboBox comboCharset, comboSeparator, comboQuote;
    protected JCheckBox checkFirstLineHeader;
    
    protected JTable tablePreview;
    protected Map<PBEntryField,JComboBox> mappingEntryFields;
    protected DefaultTableModel previewModel;

    protected JTextField textDisplayCaption;
    
    protected boolean doPreview;
    
    public boolean clickedOK = false;
    
    protected boolean eventLock = false;
    
    public CSVDialog(Frame owner, CSVSettings settings, String title) {
        super(owner, title, true);
        this.settings = settings;
        initialize();
    }
    
    public CSVDialog(Dialog owner, CSVSettings settings) {
        super(owner, MessageFormat.format(Utils._("Open CSV file {0}"), Utils.shortenFileNameForDisplay(settings.fileName, 30)), true);
        this.settings = settings;
        initialize();
    }
    
    private void initialize() {
        double[][] dLay = {
                {border, 0.5, border, TableLayout.FILL, border},
                {border, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, border,
                    TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.FILL, TableLayout.PREFERRED, border}
        };
        JPanel contentPane = new JPanel(new TableLayout(dLay));
        
        Vector<Charset> availCharsets = new Vector<Charset>(Charset.availableCharsets().values());
        Collections.sort(availCharsets);
        comboCharset = new JComboBox(availCharsets);
        
        String[] separators = {
                ";", ",", "TAB"
        };
        comboSeparator = new JComboBox(separators);
        comboSeparator.setEditable(true);
        
        String[] quoteChars = {
                "\"", "'"
        };
        comboQuote = new JComboBox(quoteChars);
        
        checkFirstLineHeader = new JCheckBox(Utils._("First line contains headers"));
               
        Utils.addWithLabel(contentPane, comboCharset, Utils._("Charset:"), "1,2");
        Utils.addWithLabel(contentPane, comboSeparator, Utils._("Separator:"), "3,2");
        Utils.addWithLabel(contentPane, comboQuote, Utils._("Quote character:"), "1,5");
        contentPane.add(checkFirstLineHeader, "3,5");
        
        if (!settings.overwrite) {
            textDisplayCaption = new JTextField();
            textDisplayCaption.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
            Utils.addWithLabel(contentPane, textDisplayCaption, Utils._("Phone book name to display:"), "1,8,3,8");
        }
        
        doPreview = !settings.overwrite && new File(settings.fileName).exists();
        if (doPreview) {
            contentPane.add(getMappingEntries(), "1,10,3,10,f,f");
        }
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, border, border));
        JButton buttonOK = new JButton(Utils._("OK"));
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveToSettings(settings);
                clickedOK = true;
                dispose();
            }
        });
        CancelAction cancelAct = new CancelAction(this);
        
        buttonsPanel.add(buttonOK);
        buttonsPanel.add(cancelAct.createCancelButton());
        
        contentPane.add(buttonsPanel, "0,11,4,11");

        setContentPane(contentPane);
        
        loadFromSettings(settings);
        
        pack();
    }

    
    void loadFromSettings(CSVSettings settings) {
        eventLock = true;
        comboCharset.setSelectedItem(Charset.forName(settings.charset));
        comboSeparator.setSelectedItem(separatorToUserDisplay(settings.separator));
        comboQuote.setSelectedItem(settings.quoteChar);
        
        checkFirstLineHeader.setSelected(settings.firstLineAreHeaders);
        
        if (textDisplayCaption != null) {
            textDisplayCaption.setText(settings.displayCaption);
        }
        
        if (doPreview) {
            refreshPreview();
            for (PBEntryField field : PBEntryField.values()) {
                int selIdx;
                String mapping = settings.getMappingFor(field);
                if (ConnectionSettings.isNoField(mapping)) {
                    selIdx = 0;
                } else {
                    selIdx = Integer.parseInt(mapping) + 1;
                }
                mappingEntryFields.get(field).setSelectedIndex(selIdx);
            }
        }
        eventLock = false;
    }
    
    void saveToSettings(CSVSettings settings) {
        settings.charset = ((Charset)comboCharset.getSelectedItem()).name();
        settings.separator = userDisplayToSeparator((String)comboSeparator.getSelectedItem());
        settings.quoteChar = (String)comboQuote.getSelectedItem();
        
        settings.firstLineAreHeaders = checkFirstLineHeader.isSelected();
        
        if (textDisplayCaption != null) {
            settings.displayCaption = textDisplayCaption.getText();
        }
        
        if (doPreview) {
            for (PBEntryField field : PBEntryField.values()) {
                int selIdx = mappingEntryFields.get(field).getSelectedIndex();
                
                settings.setMappingFor(field, (selIdx == 0) ? ConnectionSettings.noField : String.valueOf(selIdx - 1));
            }
        } else {
            PBEntryField[] fields = PBEntryField.values();
            for (int i=0; i < fields.length; i++) {
                settings.setMappingFor(fields[i], String.valueOf(i));
            }
        }
    }
    
    private String separatorToUserDisplay(String sep) {
        if (sep.equals("\t")) {
            return "TAB";
        } else {
            return sep;
        }
    }
    
    private String userDisplayToSeparator(String sep) {
        if (sep.equals("TAB")) {
            return "\t";
        } else if (sep.length() > 1) {
            return sep.substring(0,1);
        } else if (sep.length() == 0) {
            return ";";
        } else {
            return sep;
        }
    }
    
    void refreshPreview() {
        try {
            CSVReader reader = settings.createReader();
            

            String[] cols = null;
            if (settings.firstLineAreHeaders) {
                cols = reader.readNext();
            }
            
            Vector<Vector<String>> dataVector = new Vector<Vector<String>>(20);
            String[] line;
            int row = 0;
            while ((line = reader.readNext()) != null && row++ < 20) {
                Vector<String> lineVec = new Vector<String>(line.length);
                for (String s : line) {
                    lineVec.add(s);
                }
                dataVector.add(lineVec);
            }
            
            Vector<String> columns = new Vector<String>();
            if (cols == null) {
                int colCount;
                if (dataVector.size() > 0) {
                    colCount = dataVector.get(0).size();
                } else {
                    colCount = 0;
                }
                MessageFormat columnTemplate = new MessageFormat(Utils._("Column {0}"));
                for (int i = 0; i < colCount; i++) {
                    columns.add(columnTemplate.format(new Object[] { Integer.valueOf(i+1) }));
                }
            } else {
                for (String s : cols) {
                    columns.add(s);
                }
            }
            
            previewModel.setDataVector(dataVector, columns);
            Vector<String> comboCols = new Vector<String>(columns.size() + 1);
            comboCols.add(ConnectionSettings.noField_translated);
            comboCols.addAll(columns);
            
            for (PBEntryField field : PBEntryField.values()) {
                JComboBox combo = mappingEntryFields.get(field);
                int selIdx = combo.getSelectedIndex();
                combo.setModel(new DefaultComboBoxModel(comboCols));
                if (selIdx >= 0 && selIdx < comboCols.size()) {
                    combo.setSelectedIndex(selIdx);
                } else {
                    if (comboCols.size() > 0)
                        combo.setSelectedIndex(0);
                }
            }
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(this, Utils._("Error previewing your settings:"), ex);
        }
    }
    
    private JPanel getMappingEntries() {
        mappingEntryFields = new EnumMap<PBEntryField, JComboBox>(PBEntryField.class);
                
        final int dataRowCount = ((PBEntryField.FIELD_COUNT + 2) / 3);
        double rows[] = new double[dataRowCount*3 + 4];
        rows[0] = TableLayout.PREFERRED;
        rows[1] = TableLayout.FILL;
        rows[2] = border;
        rows[3] = TableLayout.PREFERRED;
        for (int i = 3; i < rows.length; i++) {
            if (i%3 == 1) {
                rows[i] = border/2;
            } else {
                rows[i] = TableLayout.PREFERRED;
            }
        }
        double dLay[][] = {
                {0.33333, border/2, 0.33333, border/2, TableLayout.FILL},      
                rows
        };
        JPanel res = new JPanel(new TableLayout(dLay));
        
        previewModel = new DefaultTableModel();
        tablePreview = new JTable(previewModel);
        tablePreview.setPreferredScrollableViewportSize(new Dimension(100, 100));
        tablePreview.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablePreview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Utils.addWithLabel(res, new JScrollPane(tablePreview), Utils._("Preview") + ":", "0,1,4,1");
        
        res.add(new JLabel("<html>" + Utils._("Please select which columns in the CSV file represent the phone book fields:") + "</html>"), "0,3,4,3");
        
        PBEntryField[] fields = PBEntryField.values();
        int col = 0;
        int row = 6;
        final int MAX_COL = 4;
        for (int i = 0; i<fields.length; i++) {
            PBEntryField field = fields[i];
            JComboBox box = new JComboBox();
            
            mappingEntryFields.put(field, box);
            
            Utils.addWithLabel(res, box, field.getDescription() + ":", new TableLayoutConstraints(col, row));
            col += 2;
            if (col > MAX_COL) {
                col = 0;
                row += 3;
            }
        }
        
        ActionListener refreshListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (eventLock)
                    return;
                
                Utils.setWaitCursor(CSVDialog.this);
                saveToSettings(settings);
                refreshPreview();
                Utils.unsetWaitCursor(CSVDialog.this);
            }  
        };
        comboCharset.addActionListener(refreshListener);
        comboQuote.addActionListener(refreshListener);
        comboSeparator.addActionListener(refreshListener);
        checkFirstLineHeader.addActionListener(refreshListener);
        
        return res;
    }
}
