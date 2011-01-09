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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.file.ExternalProcessConverter;
import yajhfc.file.FileConverter;
import yajhfc.file.FileFormat;
import yajhfc.file.FormattedFile;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.SafeJFileChooser;
import yajhfc.util.URIClickListener;

/**
 * @author jonas
 *
 */
public class ConvertersPage extends AbstractOptionsPanel<FaxOptions> {

    ConverterTableModel tableModel;
    JTable formatsTable;
    
    private static final String WIKI_URL = "http://openfacts2.berlios.de/wikien/index.php/BerliosProject:Yet_another_Java_HylaFAX_client_-_Custom_File_Filters";
    
    public ConvertersPage() {
        super(new BorderLayout());        
    }
    
    
    @Override
    protected void createOptionsUI() {
        tableModel = new ConverterTableModel(FormattedFile.fileConverters);
        formatsTable = new JTable(tableModel);
        formatsTable.setDefaultRenderer(Row.class, new RowRenderer());
        formatsTable.setDefaultEditor(Row.class, new RowEditor());
        formatsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        formatsTable.getColumnModel().getColumn(0).setMaxWidth(100);
        formatsTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        
        JLabel infoLabel = new JLabel(_("For information on how to use this page, please see the Wiki at:"));
        JLabel linkLabel = new JLabel("<html><a href=\"" + WIKI_URL + "\">" + WIKI_URL + "</a></html>");
        linkLabel.addMouseListener(new URIClickListener(WIKI_URL));
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Box box = Box.createVerticalBox();
        box.add(infoLabel);
        box.add(linkLabel);
        box.add(Box.createRigidArea(new Dimension(OptionsWin.border,OptionsWin.border)));
        
        add(box, BorderLayout.NORTH);
        add(new JScrollPane(formatsTable), BorderLayout.CENTER);
    }
   
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        for (Map.Entry<String,String> entry : foEdit.customFileConverters.entrySet()) {
            FileFormat format = Enum.valueOf(FileFormat.class, entry.getKey());
            tableModel.setCommandLine(format, entry.getValue());
            //System.out.println(entry);
        }
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        foEdit.customFileConverters.clear();
        for (Row row : tableModel.getRows()) {
            if (row.hasCommandLine()) {
                foEdit.customFileConverters.put(row.format.name(), row.commandLine);
            } 
        }
        
        FormattedFile.loadCustomConverters(foEdit.customFileConverters);
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        if (formatsTable.isEditing()) {
            formatsTable.getCellEditor().stopCellEditing();
        }
        return true;
    }

    
    protected static class ConverterTableModel extends AbstractTableModel {

        private static final String[] columns = {
          _("File format"),
          _("Converter")
        };
        
        protected final Row[] rows;
        
        public int getColumnCount() {
            return columns.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        public int getRowCount() {
            return rows.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return rows[rowIndex].format;
            case 1:
                return rows[rowIndex];
            default:
                return null;
            }
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Row row = rows[rowIndex];
            switch (columnIndex) {
            case 1:
                if (row.changeable) {
                    row.commandLine = aValue.toString();
                    fireTableCellUpdated(rowIndex, columnIndex);
                }
                break;
            default:
                // do nothing;
            }
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            switch (columnIndex) {
            case 0:
                return false;
            case 1:
                return rows[rowIndex].changeable;
            default:
                return false;
            }
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return FileFormat.class;
            case 1:
                return Row.class;
            default:
                return Object.class;
            }
        }
        
        public Row[] getRows() {
            return rows;
        }
        
        public int indexOf(FileFormat format) {
            for (int i=0; i<rows.length; i++) {
                if (rows[i].format == format)
                    return i;
            }
            return -1;
        }
        
        public void setCommandLine(FileFormat format, String commandLine) {
            int index = indexOf(format);
            if (index >= 0) {
                rows[index].commandLine = commandLine;
                fireTableCellUpdated(index, 1);
            }
        }
        
        public ConverterTableModel(Map<FileFormat,FileConverter> convMap) {
            FileFormat[] formats = FileFormat.values();
            rows = new Row[formats.length-1]; // All except unknown
            
            MessageFormat defaultsFmt = new MessageFormat(_("(internal {0})"));
            int j=0;
            for (FileFormat format : formats) {
                if (format != FileFormat.Unknown)
                    rows[j++] = new Row(format, convMap.get(format), defaultsFmt);
            }
        }
    }
    
    public static class Row {
        public final FileFormat format;
        public final String defaultValue;
        public       String commandLine = null;
        public final boolean changeable;
        public final FileConverter defaultConverter;
        
        @Override
        public String toString() {
            return commandLine;
        }

        public boolean hasCommandLine() {
            return commandLine != null && commandLine.length() > 0;
        }
        
        Row(FileFormat format, FileConverter converter, MessageFormat defaultsFmt) {
            super();
            this.format = format;
            
            if (converter instanceof ExternalProcessConverter) {
                ExternalProcessConverter epc = (ExternalProcessConverter)converter;
                if (epc.isUserDefined()) {
                    commandLine = epc.getCommandLine();
                    converter = epc.getInternalConverter();
                }
            }
            
            if (converter == null) {
                defaultValue = _("(none)");
                changeable = true;
            } else {
                changeable = converter.isOverridable();
                defaultValue = defaultsFmt.format(new Object[] { (converter == FileConverter.IDENTITY_CONVERTER) ? "IDENTITY_CONVERTER" : converter.getClass().getSimpleName()});
            }
            this.defaultConverter = converter;
        }
    }
    
    static class RowRenderer extends DefaultTableCellRenderer {
        Font italicFont, normalFont;
        boolean fontLock = false;
        
        @Override
        public void setFont(Font font) {
            if (fontLock)
                return;
            
            super.setFont(font);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int rowIndex,
                int columnIndex) {
            Row row = (Row)value;
            String text = null;
            if (value != null) {
                if (row.hasCommandLine()) {
                    text = row.commandLine;
                    setForeground(UIManager.getColor("textText"));
                    setFont(normalFont);
                } else {
                    text = row.defaultValue;
                    setForeground(UIManager.getColor("textInactiveText"));
                    setFont(italicFont);   
                }
            }
            
            fontLock = true;
            final Component tableCellRendererComponent = super.getTableCellRendererComponent(table, text, isSelected, hasFocus,
                    rowIndex, columnIndex);
            fontLock = false;
            return tableCellRendererComponent;
        }
        
        public RowRenderer() {
            normalFont = UIManager.getFont("Table.font");
            italicFont = normalFont.deriveFont(Font.ITALIC);
        }
        
    }
    
    static class RowEditor extends DefaultCellEditor {
        Action browseAction;
        private JFileChooser fileChooser;
        JTextField textField;
        
        JFileChooser getFileChooser() {
            if (fileChooser == null) {
                fileChooser = new SafeJFileChooser();
            }
            return fileChooser;
        }
        
        public RowEditor() {
            super(new JTextField());
            
            browseAction = new ExcDialogAbstractAction("...") {
                @Override
                protected void actualActionPerformed(ActionEvent e) {
                    String text = textField.getText();
                    String exe = Utils.extractExecutableFromCmdLine(text);
                    JFileChooser fileChooser = getFileChooser();
                    fileChooser.setSelectedFile(new File(exe));
                    
                    if (fileChooser.showOpenDialog(getComponent()) == JFileChooser.APPROVE_OPTION) {
                        exe = fileChooser.getSelectedFile().getAbsolutePath();
                        if (exe.contains(" ")) 
                            textField.setText("\"" + exe + "\" \"%s\"");
                        else
                            textField.setText(exe + " \"%s\"");
                    }
                }
            };
            
            JPanel box = new JPanel(new BorderLayout(), false);
            textField = (JTextField)editorComponent;
            textField.setBorder(null);       
            textField.addMouseListener(ClipboardPopup.DEFAULT_POPUP);

            box.add(textField, BorderLayout.CENTER);
            box.add(new JButton(browseAction), BorderLayout.EAST);
            editorComponent = box;
        }
    }
}
