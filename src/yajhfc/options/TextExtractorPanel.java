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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.TextViewPanel;
import yajhfc.TextViewPanel.Text;
import yajhfc.Utils;
import yajhfc.file.FileConverters;
import yajhfc.file.FormattedFile;
import yajhfc.file.textextract.HylaToTextConverter;
import yajhfc.file.textextract.PDFToTextConverter;
import yajhfc.file.textextract.PSToTextConverter;
import yajhfc.file.textextract.RecipientExtractionMode;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ExcDialogAbstractAction;
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
    Action actView;
    
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
    }

    public void saveSettings(FaxOptions foEdit) {
        savePath();
        
        foEdit.extractRecipients = (RecipientExtractionMode)comboExtractRecipients.getSelectedItem();
        
        foEdit.hylaToTextConverter = ((HylaToTextConverter)comboExtractionMethod.getSelectedItem()).name();
        
        foEdit.pdftotextPath = pathMap.get(PDFToTextConverter.class.getName());
        foEdit.pstotextPath = pathMap.get(PSToTextConverter.class.getName());
    }
    
    @Override
    public boolean validateSettings(OptionsWin optionsWin) {
        savePath();
        
        return super.validateSettings(optionsWin);
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
        
        double[][] dLay = {
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border*2, TableLayout.PREFERRED, OptionsWin.border*2,  TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.FILL, OptionsWin.border}
        };
        setLayout(new TableLayout(dLay));
        add(lblExplanation, "1,1,5,1,f,t");
        add(new JSeparator(), "0,3,6,3,f,c");
        Utils.addWithLabel(this, comboExtractRecipients, _("Extract recipients from documents:"), "1,6");
        Utils.addWithLabel(this, comboExtractionMethod, _("PS/PDF to text conversion method:"), "1,9");
        add(new JButton(actView), "3,9");
        add(panelConverterSettings, "1,11,5,11,f,f");
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
}
