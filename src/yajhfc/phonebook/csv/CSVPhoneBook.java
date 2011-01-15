/**
 * 
 */
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

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import yajhfc.Utils;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.PhoneBook;
import yajhfc.phonebook.PhoneBookEntry;
import yajhfc.phonebook.PhoneBookException;
import yajhfc.phonebook.jdbc.ConnectionSettings;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.SafeJFileChooser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author jonas
 *
 */
public class CSVPhoneBook extends PhoneBook {
    private static final Logger log = Logger.getLogger(CSVPhoneBook.class.getName());
    
    public static final String PB_Prefix = "CSV";      // The prefix of this Phonebook type's descriptor
    public static final String PB_DisplayName = Utils._("CSV file"); // A user-readable name for this Phonebook type
    public static final String PB_Description = Utils._("A CSV/text file containing phone book entries."); // A user-readable description of this Phonebook type
    public static final boolean PB_CanExport = true;   // Can the phone book used to export entries?
    
    protected CSVSettings settings;
    protected List<CSVPhonebookEntry> entries = new ArrayList<CSVPhonebookEntry>();
    protected List<PhoneBookEntry> entryView = Collections.<PhoneBookEntry>unmodifiableList(entries);
    protected String[] columnHeaders;
    protected Map<PBEntryField,Integer> columnMapping = new EnumMap<PBEntryField, Integer>(PBEntryField.class);
    protected int columnCount;
    protected boolean wasChanged = false;
    protected boolean open = false;
    
    /**
     * @param parent
     */
    public CSVPhoneBook(Dialog parent) {
        super(parent);
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBook#addNewEntry()
     */
    @Override
    public PhoneBookEntry addNewEntry() {
        String[] values = new String[columnCount];
        Arrays.fill(values, "");
        CSVPhonebookEntry entry = new CSVPhonebookEntry(this, values);
        int pos = getInsertionPos(entry);
        entries.add(pos, entry);
        fireEntriesAdded(pos, entry);
        wasChanged = true;
        return entry;
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBook#browseForPhoneBook()
     */
    @Override
    public String browseForPhoneBook(boolean exportMode) {
        CSVSettings newSettings = new CSVSettings();
        if (settings != null) {
            newSettings.copyFrom(settings);
        }
        
        JFileChooser fileChooser = new SafeJFileChooser();
        FileFilter csvFilter = new ExampleFileFilter(new String[] { "txt", "csv" }, Utils._("CSV files"));
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.setFileFilter(csvFilter);
        
        if (newSettings.fileName != null && newSettings.fileName.length() > 0) {
            fileChooser.setSelectedFile(new File(newSettings.fileName));
        }
        fileChooser.setDialogType(exportMode ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG);
        if (fileChooser.showDialog(parentDialog, null) == JFileChooser.APPROVE_OPTION) {
            newSettings.fileName = fileChooser.getSelectedFile().getAbsolutePath();
            newSettings.overwrite = exportMode;
            
            if (settings == null && fileChooser.getSelectedFile().exists()) {
                newSettings.tryAutodetect();
            }
            
            CSVDialog csvDialog = new CSVDialog(parentDialog, newSettings);
            csvDialog.setVisible(true);
            if (csvDialog.clickedOK) {
                return PB_Prefix + ":" + newSettings.saveToString();
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBook#close()
     */
    @Override
    public void close() {
        if (wasChanged) {
            try {
                saveEntries();
            } catch (IOException e) {
                log.log(Level.WARNING, "Error saving the phone book:", e);
            }
            wasChanged = false;
            open = false;
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBook#getEntries()
     */
    @Override
    public List<PhoneBookEntry> getEntries() {
        return entryView;
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBook#isOpen()
     */
    @Override
    public boolean isOpen() {
        return open;
    }

    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBook#openInternal(java.lang.String)
     */
    @Override
    protected void openInternal(String descriptorWithoutPrefix)
            throws PhoneBookException {
        settings = new CSVSettings();
        settings.loadFromString(descriptorWithoutPrefix);
        
        columnMapping.clear();
        for (PBEntryField field : PBEntryField.values()) {
            String mapping = settings.getMappingFor(field);
            if (!ConnectionSettings.isNoField(mapping)) {
                columnMapping.put(field, Integer.valueOf(Integer.parseInt(mapping)));
            }
        }
        
        reloadEntries();
        open = true;
    }

    private void reloadEntries() throws PhoneBookException {
        entries.clear();
        
        File input = new File(settings.fileName);
        if (input.exists()) {
            try {
                CSVReader reader = settings.createReader();
                if (settings.firstLineAreHeaders) {
                    columnHeaders = reader.readNext();
                    if (columnHeaders == null) {
                        initEmptyFile();
                        wasChanged = false;
                        return;
                    }
                } else {
                    columnHeaders = null;
                }
                String[] line;
                while ((line = reader.readNext()) != null) {
                    entries.add(new CSVPhonebookEntry(this, line));
                }
                reader.close();

                if (settings.firstLineAreHeaders) {
                    columnCount = columnHeaders.length;
                } else {
                    if (entries.size() > 0) {
                        columnCount = entries.get(0).columnData.length;
                    } else {
                        initEmptyFile();
                    }
                }
            } catch (Exception ex) {
                throw new PhoneBookException(ex, false);
            }
        } else {
            initEmptyFile();
        }
        resort();
        wasChanged = false;
    }
    
    private void initEmptyFile() {
        if (settings.firstLineAreHeaders) {
            PBEntryField[] fields = PBEntryField.values();
            columnHeaders = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                columnHeaders[i] = fields[i].getDescription();
            }
        } else {
            columnHeaders = null;
        }
        columnCount = PBEntryField.FIELD_COUNT;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.phonebook.PhoneBook#resort()
     */
    @Override
    public void resort() {
        Collections.sort(entries);
    }
    
    private int getInsertionPos(PhoneBookEntry pbe) {
        int res = Collections.binarySearch(entries, pbe);
        if (res >= 0) // Element found?
            return res + 1;
        else
            return -(res + 1);
    }
    
    void deleteEntry(PhoneBookEntry entry) {
        int index = Utils.identityIndexOf(entries, entry);
        if (index >= 0) {
            entries.remove(index);
            fireEntriesRemoved(index, entry);
            wasChanged = true;
        }
    }

    void writeEntry(PhoneBookEntry entry) {
        int oldpos = Utils.identityIndexOf(entries, entry);
        entries.remove(oldpos);
        int pos = getInsertionPos(entry);
        entries.add(pos, (CSVPhonebookEntry)entry);
        fireEntriesChanged(eventObjectForInterval(oldpos, pos));
        wasChanged = true;
    }

    private void saveEntries() throws IOException {
        CSVWriter writer = settings.createWriter();
        if (settings.firstLineAreHeaders) {
            writer.writeNext(columnHeaders);
        }
        for (CSVPhonebookEntry entry : entries) {
            writer.writeNext(entry.columnData);
        }
        writer.close();
    }
    
    @Override
    public String getDisplayCaption() {
        String caption = settings.displayCaption;
        if (caption != null && caption.length() > 0) {
            return caption;
        } else {
            return Utils.shortenFileNameForDisplay(settings.fileName, CAPTION_LENGTH);
        }
    }
}
