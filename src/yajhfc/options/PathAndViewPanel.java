/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.Utils;
import yajhfc.file.MultiFileConverter;
import yajhfc.file.FormattedFile.FileFormat;
import yajhfc.util.ClipboardPopup;

/**
 * @author jonas
 *
 */
public class PathAndViewPanel extends JPanel implements OptionsPage {

    /**
     * Set this to false to disable the "tiff2pdf location" entry field
     */
    public static boolean allowTIFF2PDF = true;
    
    FileTextField ftfFaxViewer, ftfPSViewer;
    FileTextField ftfPDFViewer, ftfGSLocation, ftfTIFF2PDFLocation;
    JCheckBox checkPDFSameAsPS, checkCreateSingleFile, checkCreateAlwaysAsTargetFormat;
    JComboBox comboSendMode, comboTargetFormat;
    
    public PathAndViewPanel() {
        super(false);
        setLayout(new TableLayout(new double[][] {
                {OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                {OptionsWin.border, 0.7, OptionsWin.border, TableLayout.FILL, OptionsWin.border}
        }));
        
        JPanel panelPaths = createPanelPaths();
        JPanel panelView = createPanelView();

        add(panelPaths, "1,1,f,f");
        add(panelView, "1,3,f,f");
    }

    private JPanel createPanelPaths() {
        JPanel panelPaths = new JPanel(false);
        panelPaths.setLayout(new BoxLayout(panelPaths, BoxLayout.Y_AXIS));
        panelPaths.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(_("Path settings")),
                BorderFactory.createEmptyBorder(OptionsWin.border, OptionsWin.border, OptionsWin.border, OptionsWin.border))
                );

        ftfFaxViewer = new ExeFileTextField();
        ftfFaxViewer.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        ftfPSViewer = new ExeFileTextField();
        ftfPSViewer.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        ftfPDFViewer = new ExeFileTextField();
        ftfPDFViewer.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        ftfGSLocation = new FileTextField();
        ftfGSLocation.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        ftfGSLocation.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        ftfTIFF2PDFLocation = new FileTextField();
        ftfTIFF2PDFLocation.getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        ftfTIFF2PDFLocation.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        checkPDFSameAsPS = new JCheckBox(_("Same as PostScript viewer"));
        checkPDFSameAsPS.setSelected(true);
        checkPDFSameAsPS.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ftfPDFViewer.setEnabled(!checkPDFSameAsPS.isSelected());
            }
        });
        
        JLabel labelTIFF = new JLabel(_("Command line for fax viewer: (insert %s as a placeholder for the filename)"));
        labelTIFF.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        JLabel labelPS = new JLabel(_("Command line for Postscript viewer: (insert %s as a placeholder for the filename)"));
        labelPS.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        JLabel labelPDF = new JLabel(_("Command line for PDF viewer: (insert %s as a placeholder for the filename)"));
        labelPDF.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        JLabel labelGS = new JLabel(_("Location of GhostScript executable (optional):"));
        labelGS.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        JLabel labelTIFF2PDF = new JLabel(_("Location of tiff2pdf executable (optional):"));
        labelTIFF2PDF.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        ftfTIFF2PDFLocation.setEnabled(allowTIFF2PDF);
        labelTIFF2PDF.setEnabled(allowTIFF2PDF);
        
        panelPaths.add(labelTIFF);
        panelPaths.add(ftfFaxViewer);
        panelPaths.add(Box.createVerticalGlue());
        panelPaths.add(labelPS);
        panelPaths.add(ftfPSViewer);
        panelPaths.add(Box.createVerticalGlue());
        panelPaths.add(labelPDF);
        panelPaths.add(checkPDFSameAsPS);
        panelPaths.add(ftfPDFViewer);
        panelPaths.add(Box.createVerticalGlue());
        panelPaths.add(labelGS);
        panelPaths.add(ftfGSLocation);
        panelPaths.add(Box.createVerticalGlue());
        panelPaths.add(labelTIFF2PDF);
        panelPaths.add(ftfTIFF2PDFLocation);
        return panelPaths;
    }

    private JPanel createPanelView() {
        final int rowCount = 5;
        double[][] dLay =  {
                {OptionsWin.border, 0.5, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                new double[rowCount]
        };
        final double rowH = 2.0/(rowCount-1);
        dLay[1][rowCount-1] = OptionsWin.border;
        for (int i=0; i < rowCount-1; i++) {
            dLay[1][i] = ((i%2) == 0) ? rowH : TableLayout.PREFERRED; 
        }
        dLay[1][rowCount-3] = TableLayout.FILL;
        
        JPanel panelView = new JPanel(false);
        panelView.setLayout(new TableLayout(dLay));
        panelView.setBorder(BorderFactory.createTitledBorder(_("View and send settings")));
        
        checkCreateSingleFile = new JCheckBox(_("View faxes as single file (needs GhostScript+tiff2pdf)"));
        checkCreateAlwaysAsTargetFormat = new JCheckBox(_("View/send faxes always in this format"));
        
        comboTargetFormat = new JComboBox(MultiFileConverter.targetFormats.keySet().toArray());
        comboSendMode = new JComboBox(MultiFileMode.values());
        
        ActionListener panelViewListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              checkPanelViewEnabled();
            }  
        };
        checkCreateSingleFile.addActionListener(panelViewListener);
        comboSendMode.addActionListener(panelViewListener);
        
        addWithLabel(panelView, comboSendMode, _("Send multiple files as:"), "1,1,f,c");
        panelView.add(checkCreateSingleFile, "3,1,f,c");
        addWithLabel(panelView, comboTargetFormat, _("Format for viewing/sending:"), "1,3,f,c");
        panelView.add(checkCreateAlwaysAsTargetFormat, "3,3,f,c");
        
        return panelView;
    }
    
    void checkPanelViewEnabled() {
        boolean enable = (checkCreateSingleFile.isSelected() || 
                comboSendMode.getSelectedItem() != MultiFileMode.NONE);
        checkCreateAlwaysAsTargetFormat.setEnabled(enable);
        comboTargetFormat.setEnabled(enable);
    }
    
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
    
    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#loadSettings(yajhfc.FaxOptions)
     */
    public void loadSettings(FaxOptions foEdit) {
        ftfFaxViewer.setText(foEdit.faxViewer);
        ftfPSViewer.setText(foEdit.psViewer);
        ftfPDFViewer.setText(foEdit.pdfViewer);
        ftfGSLocation.setText(foEdit.ghostScriptLocation);
        ftfTIFF2PDFLocation.setText(foEdit.tiff2PDFLocation);
        
        checkCreateAlwaysAsTargetFormat.setSelected(foEdit.alwaysCreateTargetFormat);
        checkCreateSingleFile.setSelected(foEdit.createSingleFilesForViewing);
        checkPDFSameAsPS.setSelected(foEdit.viewPDFAsPS);
        
        comboSendMode.setSelectedItem(foEdit.multiFileSendMode);
        comboTargetFormat.setSelectedItem(foEdit.singleFileFormat);
        
        checkPanelViewEnabled();
        ftfPDFViewer.setEnabled(!foEdit.viewPDFAsPS);
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#saveSettings(yajhfc.FaxOptions)
     */
    public void saveSettings(FaxOptions foEdit) {
        foEdit.faxViewer = ftfFaxViewer.getText();
        foEdit.psViewer = ftfPSViewer.getText();
        foEdit.pdfViewer = ftfPDFViewer.getText();
        foEdit.ghostScriptLocation = ftfGSLocation.getText();
        foEdit.tiff2PDFLocation = ftfTIFF2PDFLocation.getText();

        foEdit.alwaysCreateTargetFormat = checkCreateAlwaysAsTargetFormat.isSelected();
        foEdit.createSingleFilesForViewing = checkCreateSingleFile.isSelected();
        foEdit.viewPDFAsPS = checkPDFSameAsPS.isSelected();

        foEdit.multiFileSendMode = (MultiFileMode)comboSendMode.getSelectedItem();
        foEdit.singleFileFormat = (FileFormat)comboTargetFormat.getSelectedItem();
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        if (!commandLineOK(ftfFaxViewer.getText())) {
            optionsWin.focusComponent(ftfFaxViewer.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the command line for the fax viewer."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!commandLineOK(ftfPSViewer.getText())) {
            optionsWin.focusComponent(ftfPSViewer.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the command line for the PostScript viewer."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (!checkPDFSameAsPS.isSelected() && !commandLineOK(ftfPDFViewer.getText())) {
            optionsWin.focusComponent(ftfPDFViewer.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the command line for the PDF viewer."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        final boolean needsGS = (checkCreateSingleFile.isSelected() || 
                comboSendMode.getSelectedItem() != MultiFileMode.NONE);
        if (needsGS && !commandLineOK(ftfGSLocation.getText())) {
            optionsWin.focusComponent(ftfGSLocation.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the location of the GhostScript executable."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (allowTIFF2PDF && needsGS && !commandLineOK(ftfTIFF2PDFLocation.getText())) {
            optionsWin.focusComponent(ftfTIFF2PDFLocation.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the location of the tiff2pdf executable."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    private boolean commandLineOK(String commandLine) {
        return commandLine != null && commandLine.length() > 0 && 
            Utils.searchExecutableInPath(Utils.extractExecutableFromCmdLine(commandLine)) != null;
    }
    
    static class ExeFileTextField extends FileTextField {
        protected String readTextFieldFileName() {
            return this.getText().replaceAll("(\")?%s(\")?", "").trim();
        };
        
        @Override
        protected void writeTextFieldFileName(String fName) {
            if (fName.contains(" ")) 
                this.setText("\"" + fName + "\" \"%s\"");
            else
                this.setText(fName + " \"%s\"");
        }
        
        public ExeFileTextField() {
            super();
            getJTextField().addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        }
    }
}
