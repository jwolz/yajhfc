/**
 * 
 */
package yajhfc.phonebook.csv;

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
    /**
     * The count of columns used for new items
     */
    protected int columnCount;
    /**
     * The maximum index used in the mapping
     */
    protected int maxMapIndex;
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
        entries.add(entry);
        fireEntriesAdded(entries.size()-1, entry);
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
        maxMapIndex = -1;
        for (PBEntryField field : PBEntryField.values()) {
            String mapping = settings.getMappingFor(field);
            if (!ConnectionSettings.isNoField(mapping)) {
                final int mapIndex = Integer.parseInt(mapping);
                columnMapping.put(field, Integer.valueOf(mapIndex));
                
                if (mapIndex > maxMapIndex) {
                    maxMapIndex = mapIndex;
                }
            }
        }
        
        reloadEntries();
        open = true;
    }
    
    /**
     * Ensures that the respective line is large enough to hold the mapping
     * @param line
     * @return
     */
    private String[] ensureSize(String[] line) {
        if (line.length > maxMapIndex) {
            return line;
        } else {
            String[] rv = new String[maxMapIndex+1];
            Arrays.fill(rv, line.length, rv.length, "");
            System.arraycopy(line, 0, rv, 0, line.length);
            return rv;
        }
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
                    entries.add(new CSVPhonebookEntry(this, ensureSize(line)));
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
                if (columnCount <= maxMapIndex) {
                    // Ensure we create an array large enough to hold the complete mapping for new items
                    columnCount = maxMapIndex+1;
                }
            } catch (Exception ex) {
                throw new PhoneBookException(ex, false);
            }
        } else {
            initEmptyFile();
        }
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
    
    void deleteEntry(PhoneBookEntry entry) {
        int index = Utils.identityIndexOf(entries, entry);
        if (index >= 0) {
            entries.remove(index);
            fireEntriesRemoved(index, entry);
            wasChanged = true;
        }
    }

    void writeEntry(PhoneBookEntry entry) {
        int pos = Utils.identityIndexOf(entries, entry);
        fireEntriesChanged(pos, entry);
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
