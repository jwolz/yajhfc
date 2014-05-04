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
package yajhfc.options;

import static yajhfc.Utils._;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.TextViewPanel;
import yajhfc.TextViewPanel.Text;
import yajhfc.Utils;
import yajhfc.file.FileConverters;
import yajhfc.file.FormattedFile;
import yajhfc.file.textextract.FaxnumberExtractor;
import yajhfc.file.textextract.HylaToTextConverter;
import yajhfc.file.textextract.PDFToTextConverter;
import yajhfc.file.textextract.PSToTextConverter;
import yajhfc.file.textextract.RecipientExtractionMode;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.JTableTABAction;
import yajhfc.util.ProgressDialog;
import yajhfc.util.ProgressWorker;
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
public class TextExtractorPanel extends AbstractOptionsPanel<FaxOptions> {

	JComboBox comboExtractRecipients;
    JComboBox comboExtractionMethod;
    JLabel labelPath;
    FileTextField ftfPath;
    Action actView, actRemoveRow, actUseDefault;
    TagNameTableModel tagTableModel;
    JTable tagTable;
    JCheckBox checkMandatoryColon;
    JLabel labelTagNameWarning;
    
    Map<String,String> pathMap = new HashMap<String,String>();
    private HylaToTextConverter lastSelection = null;
    
    PathAndViewPanel pathAndViewPanel;
    
    public TextExtractorPanel(PathAndViewPanel pathAndViewPanel) {
        super(true);
        this.pathAndViewPanel = pathAndViewPanel;
    }

    public void loadSettings(FaxOptions foEdit) {
        pathMap.clear();
        pathMap.put(PDFToTextConverter.class.getName(), foEdit.pdftotextPath);
        pathMap.put(PSToTextConverter.class.getName(), foEdit.pstotextPath);
        
        comboExtractRecipients.setSelectedItem(foEdit.extractRecipients);
        
        lastSelection = null;
        comboExtractionMethod.setSelectedItem(HylaToTextConverter.findByString(foEdit.hylaToTextConverter));
        if (comboExtractionMethod.getSelectedIndex() < 0)
            comboExtractionMethod.setSelectedIndex(0);
        
        lastSelection = null;
        selectNewPath((HylaToTextConverter)comboExtractionMethod.getSelectedItem());
        
        checkMandatoryColon.setSelected(foEdit.recipientExtractionTagMandatoryColon);
        tagTableModel.setListContents(foEdit.recipientExtractionTags);
    }

    public void saveSettings(FaxOptions foEdit) {
        savePath();
        
        foEdit.extractRecipients = (RecipientExtractionMode)comboExtractRecipients.getSelectedItem();
        
        foEdit.hylaToTextConverter = ((HylaToTextConverter)comboExtractionMethod.getSelectedItem()).name();
        
        foEdit.pdftotextPath = pathMap.get(PDFToTextConverter.class.getName());
        foEdit.pstotextPath = pathMap.get(PSToTextConverter.class.getName());
        
        foEdit.recipientExtractionTagMandatoryColon = checkMandatoryColon.isSelected();
        foEdit.recipientExtractionTags.clear();
        foEdit.recipientExtractionTags.addAll(tagTableModel.getTags());
    }
    
    @Override
    public boolean validateSettings(OptionsWin optionsWin) {
        savePath();
        
        if (tagTable.isEditing()) {
            tagTable.getCellEditor().stopCellEditing();
        }
        int faxCount = 0;
        int mailCount = 0;
        for (String tag : tagTableModel.getTags() ) {
            if (tag.length() <= 1) {
                JOptionPane.showMessageDialog(optionsWin, _("Empty tag names are not allowed."));
                return false;
            }
            switch (tag.charAt(0)) {
            case FaxnumberExtractor.PATTERN_PREFIX_FAX:
                faxCount++;
                break;
            case FaxnumberExtractor.PATTERN_PREFIX_MAIL:
                mailCount++;
                break;
            }
        }
        if (faxCount==0) {
            JOptionPane.showMessageDialog(optionsWin, _("You have to define at least one tag name for fax numbers."));
            return false;
        }
        if (mailCount==0) {
            JOptionPane.showMessageDialog(optionsWin, _("You have to define at least one tag name for email addresses."));
            return false;
        }
        
        return true;
    }

    void savePath() {
        if (lastSelection != null) {
            String key = lastSelection.getClass().getName();
            if (pathMap.containsKey(key)) {
                pathMap.put(key, ftfPath.getText());
            }
        }
    }
    
    void selectNewPath(HylaToTextConverter newSelection) {
        savePath();
        String key = newSelection.getClass().getName();
        if (pathMap.containsKey(key)) {
            ftfPath.setEnabled(true);
            labelPath.setEnabled(true);
            labelPath.setText(MessageFormat.format(_("Path to {0}:"), newSelection.getDescription()));
            ftfPath.setText(pathMap.get(key));
        } else {
            ftfPath.setEnabled(false);
            labelPath.setEnabled(false);
            labelPath.setText(_("No path settings necessary"));
            ftfPath.setText("");
        }
            
       lastSelection = newSelection;
    }
    
    @Override
    protected void createOptionsUI() {
        //checkExtractRecipients = new JCheckBox(_("Extract recipients from documents"));
        JLabel lblExplanation = new JLabel("<html>" + _("By activating this option YajHFC will try to extract the recipients from documents given on the command line or fax printer.") + "<br>" + 
                _("To find recipients it searches for <tt>@@recipient:<i>faxnumber</i>@@</tt> tags in the files.") + "<br>" + 
                _("This behaviour can be overridden by the <tt>--extract-recipients</tt> command line option.") + "<br><br>" + 
                _("If you want to use this feature a PDF/PostScript to text conversion method must be selected below.") + "</html>");
        
        comboExtractionMethod = new JComboBox(new Vector<Object>(HylaToTextConverter.availableConverters));
        comboExtractionMethod.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                HylaToTextConverter extractor = (HylaToTextConverter)comboExtractionMethod.getSelectedItem();
                if (extractor == null)
                    return;
                
                selectNewPath(extractor);
            }
        });
        comboExtractRecipients = new JComboBox(RecipientExtractionMode.values());
        
        ftfPath = new FileTextField();
        ClipboardPopup.DEFAULT_POPUP.addToComponent(ftfPath.getJTextField());
        
        actView = new ExcDialogAbstractAction(_("Test...")) {
            private JFileChooser chooser;
            
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                final OptionsWin ow = (OptionsWin)SwingUtilities.getWindowAncestor(TextExtractorPanel.this);
                if (!validateSettings(ow) || (pathAndViewPanel.isUICreated() && !pathAndViewPanel.validateSettings(ow))) { 
                    return;
                }
                FaxOptions copy = Utils.getFaxOptions().clone();
                if (pathAndViewPanel.isUICreated())
                    pathAndViewPanel.saveSettings(copy);
                saveSettings(copy);
                
                if (chooser == null) {
                    chooser = new SafeJFileChooser();
                    
                    FileFilter[] fileFilters = FileConverters.getConvertableFileFilters();
                    for (FileFilter ff : fileFilters)
                        chooser.addChoosableFileFilter(ff);

                    FileFilter allf = chooser.getAcceptAllFileFilter();
                    chooser.removeChoosableFileFilter(allf);
                    chooser.addChoosableFileFilter(allf);
                    chooser.setFileFilter(fileFilters[0]);
                    
                    chooser.setDialogTitle(_("Select a file to view as text"));
                }
                
                if (chooser.showOpenDialog(TextExtractorPanel.this) == JFileChooser.APPROVE_OPTION) {
                    HylaToTextConverter extractor = ((HylaToTextConverter)comboExtractionMethod.getSelectedItem()).getInstanceForOptions(copy);
                    new ViewAsTextWorker(ow, extractor, chooser.getSelectedFile()).startWork();
                }
            }
        };
        
        double[][] dLay2 = {
                {OptionsWin.border, TableLayout.FILL, OptionsWin.border, },
                {OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border}
        };
        JPanel panelConverterSettings = new JPanel(new TableLayout(dLay2));
        panelConverterSettings.setBorder(BorderFactory.createTitledBorder(_("Settings for the selected conversion method")));
        labelPath = Utils.addWithLabel(panelConverterSettings, ftfPath, "path", "1,2");
        
        double[][] dLay3 = {
                {OptionsWin.border, TableLayout.FILL, OptionsWin.border, 0.4, OptionsWin.border, },
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, OptionsWin.border}
        };
        JPanel panelTags = new JPanel(new TableLayout(dLay3));
        panelTags.setBorder(BorderFactory.createTitledBorder(_("Recognized tag names")));
        actRemoveRow = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                int row = tagTable.getSelectedRow();
                if (row >= 0 && tagTableModel.rowIsRemovable(row)) {
                    if (tagTable.isEditing())
                        tagTable.getCellEditor().cancelCellEditing();
                    tagTableModel.removeRow(row);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        };
        actRemoveRow.putValue(Action.NAME, Utils._("Remove row"));
        
        actUseDefault = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                tagTableModel.setListContents(FaxOptions.DEFAULT_RECIPIENT_EXTRACTION_TAGS);
            }
        };
        actUseDefault.putValue(Action.NAME, Utils._("Reset"));
        
        tagTableModel = new TagNameTableModel();
        tagTable = new JTable(tagTableModel);
        tagTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                actRemoveRow.setEnabled(e.getFirstIndex() >= 0 && tagTableModel.rowIsRemovable(e.getFirstIndex()));
            }
        });
        JPopupMenu tablePopup = new JPopupMenu();
        tablePopup.add(actRemoveRow);
        tagTable.setComponentPopupMenu(tablePopup);
        JComboBox comboTagType = new JComboBox(TagNameTableModel.getAvailableTagTypes());
        tagTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboTagType));
        tagTable.getActionMap().put(actRemoveRow.getClass().getName(), actRemoveRow);
        tagTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), actRemoveRow.getClass().getName());
        JTableTABAction.wrapDefTabAction(tagTable);
        
        checkMandatoryColon = new JCheckBox(Utils._("Colon (:) after name is mandatory"));
        
        labelTagNameWarning = new JLabel("<html>"+_("Some tag names contain characters other than A-Z or 0-9. This is not recommended.") + "</html>");
        labelTagNameWarning.setForeground(Color.RED);
        labelTagNameWarning.setVisible(false);
        tagTableModel.addTableModelListener(new TableModelListener() {
            private Pattern validatePattern = Pattern.compile(".[a-zA-Z0-9]*");
            public void tableChanged(TableModelEvent e) {
                boolean visible = false;
                for (String s : tagTableModel.getTags()) {
                    visible = visible || !validatePattern.matcher(s).matches();
                }
                labelTagNameWarning.setVisible(visible);
            }
        });
        
        panelTags.add(new JScrollPane(tagTable), "1,1,1,7,f,f");
        panelTags.add(checkMandatoryColon, "3,1,f,c");
        panelTags.add(new JButton(actRemoveRow), "3,3,f,c");
        panelTags.add(new JButton(actUseDefault), "3,5,f,c");
        panelTags.add(labelTagNameWarning, "3,7,f,b");
        
        double[][] dLay = {
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border*2, TableLayout.PREFERRED, OptionsWin.border*2,  TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.FILL, OptionsWin.border}
        };
        setLayout(new TableLayout(dLay));
        add(lblExplanation, "1,1,5,1,f,t");
        add(new JSeparator(), "0,3,6,3,f,c");
        Utils.addWithLabel(this, comboExtractRecipients, _("Extract recipients from documents:"), "1,6");
        Utils.addWithLabel(this, comboExtractionMethod, _("PS/PDF to text conversion method:"), "1,9");
        add(new JButton(actView), "3,9");
        add(panelConverterSettings, "1,11,5,11,f,f");
        add(panelTags, "1,13,5,13,f,f");
    }

    
    protected final static class ViewAsTextWorker extends ProgressWorker implements ActionListener {
        private final Dialog ow;
        private final HylaToTextConverter extractor;
        private final File file;
        //private volatile boolean cancelled = false;
        private String title;
        
        private String result = null;

        protected ViewAsTextWorker(Dialog ow, HylaToTextConverter extractor, File file) {
            this.ow = ow;
            this.extractor = extractor;
            this.file = file;
            
            title = MessageFormat.format(_("View as text using {0}"), extractor);
            setProgressMonitor(new ProgressDialog(ow, title, this).progressPanel);
        }
        
        public void startWork() {
            startWork(ow, _("Converting file to text..."));
        }

        @Override
        public void doWork() {
            updateNote(_("Running converter..."));
            try {
                CharSequence[] results = extractor.convertFilesToText(Collections.singletonList(new FormattedFile(file)));
                StringBuilder sb = new StringBuilder();
                for (CharSequence r : results) {
                    sb.append(r).append('\n');
                }
                result = sb.toString();
            } catch (Exception e) {
                showExceptionDialog(_("Error converting file to text"), e);
            }
        }
        
        @Override
        protected void done() {
            if (result != null) {
                TextViewPanel.displayDialog(ow, title, Collections.singletonList(new Text(file.toString(), result)), true);
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            //cancelled = true;
            interrupt();
            getProgressMonitor().close();
        }
    }
    
    protected static class TagNameTableModel extends AbstractTableModel {
        private final List<String> tags = new ArrayList<String>();
        
        protected static final String TAG_TYPE_MAIL = Utils._("e-mail address");
        protected static final String TAG_TYPE_FAX = Utils._("Fax number");
        protected static final String TAG_TYPE_SUBJECT = Utils._("Subject");
        
        public static String[] getAvailableTagTypes() {
            return new String[] { TAG_TYPE_FAX, TAG_TYPE_MAIL, TAG_TYPE_SUBJECT };
        }
        
        @Override
        public String getColumnName(int column) {
            switch (column) {
            case 0:
                return Utils._("Tag name");
            case 1:
                return Utils._("Type");
            default:
                return "X";
            }
        }
        
        public int getRowCount() {
            return tags.size() + 1;
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= tags.size())
                return "";
            
            String tag = tags.get(rowIndex);
            if (tag.length() < 1)
                return "";
            
            switch (columnIndex) {
            case 0:
                return tag.substring(1);
            case 1:
                switch (tag.charAt(0)) {
                case FaxnumberExtractor.PATTERN_PREFIX_FAX:
                default:
                    return TAG_TYPE_FAX;
                case FaxnumberExtractor.PATTERN_PREFIX_MAIL:
                    return TAG_TYPE_MAIL;
                case FaxnumberExtractor.PATTERN_PREFIX_SUBJECT:
                    return TAG_TYPE_SUBJECT;
                }
            default:
                return null;
            }
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (aValue.equals(getValueAt(rowIndex, columnIndex)))
                return;
            
            String oldTag;
            if (rowIndex >= tags.size())
                oldTag = " ";
            else
                oldTag = tags.get(rowIndex);
            
            String newTag;
            switch (columnIndex) {
            case 0:
                if ("".equals(aValue) && rowIsRemovable(rowIndex)) { // Delete the row if the user enters an empty string
                    removeRow(rowIndex);
                    return;
                } else {
                    newTag = oldTag.charAt(0) + (String)aValue;
                }
                break;
            case 1:
                char prefix;
                if (TAG_TYPE_FAX.equals(aValue)) {
                    prefix = FaxnumberExtractor.PATTERN_PREFIX_FAX;
                } else if (TAG_TYPE_MAIL.equals(aValue)) {
                    prefix = FaxnumberExtractor.PATTERN_PREFIX_MAIL;
                } else if (TAG_TYPE_SUBJECT.equals(aValue)) {
                    prefix = FaxnumberExtractor.PATTERN_PREFIX_SUBJECT;
                } else {
                    prefix = FaxnumberExtractor.PATTERN_PREFIX_FAX;
                }
                char[] oldTagArray = oldTag.toCharArray();
                oldTagArray[0] = prefix;
                newTag = new String(oldTagArray);
                break;
            default:
                return;
            }

            if (rowIndex >= tags.size()) {
                tags.add(newTag);
                fireTableRowsInserted(rowIndex+1, rowIndex+1);
            } else {
                tags.set(rowIndex, newTag);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public void setListContents(String[] newContent) {
            tags.clear();
            Collections.addAll(tags, newContent);
            Collections.sort(tags);
            fireTableDataChanged();
        }
        
        public void setListContents(Collection<String> newContent) {
            tags.clear();
            tags.addAll(newContent);
            Collections.sort(tags);
            fireTableDataChanged();
        }
        
        public void removeRow(int rowIndex) {
            tags.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
        
        public boolean rowIsRemovable(int rowIndex) {
            return (rowIndex < tags.size());
        }
        
        /**
         * Returns the backing list. DO NOT MODIFY directly!
         * @return
         */
        public List<String> getTags() {
            return tags;
        }
        
        public TagNameTableModel() {
            super();
        }
    }
}
