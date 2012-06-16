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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import yajhfc.FaxOptions;
import yajhfc.FileTextField;
import yajhfc.Utils;
import yajhfc.file.MultiFileConvFormat;
import yajhfc.util.ClipboardPopup;
import yajhfc.util.ComponentEnabler;

/**
 * @author jonas
 *
 */
public class PathAndViewPanel extends AbstractOptionsPanel<FaxOptions> {

    /**
     * Set this to false to disable the "tiff2pdf location" entry field
     */
    public static boolean requireTIFF2PDF = true;
    
    FileTextField ftfFaxViewer, ftfPSViewer;
    FileTextField ftfPDFViewer, ftfGSLocation, ftfTIFF2PDFLocation;
    JCheckBox checkCreateSingleFile, checkCreateAlwaysAsTargetFormat, checkCreateAlwaysAsTargetFormatView;
    JCheckBox checkUseTiffPaperSize;
    JCheckBox checkCustomFaxViewer, checkCustomPSViewer, checkCustomPDFViewer;
    JComboBox comboSendMode, comboTargetFormat, comboTargetFormatView;
    
    public PathAndViewPanel() {
        super(false);
    }
    
    @Override
    protected void createOptionsUI() {
        setLayout(new TableLayout(new double[][] {
                {OptionsWin.border, 0.5, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.FILL, OptionsWin.border}
        }));
        
        JPanel panelPaths = createPanelPaths();
        JPanel panelView = createPanelView();
        JPanel panelSend = createPanelSend();

        add(panelPaths, "1,1,3,1,f,f");
        add(panelView, "1,3,f,f");
        add(panelSend, "3,3,f,f");
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

//        checkPDFSameAsPS = new JCheckBox(_("Same as PostScript viewer"));
//        checkPDFSameAsPS.setSelected(true);
//        checkPDFSameAsPS.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                ftfPDFViewer.setEnabled(!checkPDFSameAsPS.isSelected());
//            }
//        });
                
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
        checkUseTiffPaperSize = new JCheckBox(_("Always use paper size from YajHFC options in the PDF"));
        checkUseTiffPaperSize.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        
        checkCustomFaxViewer = new JCheckBox(_("Use custom fax viewer"));
        ComponentEnabler.installOn(checkCustomFaxViewer, true, ftfFaxViewer, labelTIFF);
        checkCustomPSViewer = new JCheckBox(_("Use custom PostScript viewer"));
        ComponentEnabler.installOn(checkCustomPSViewer, true, ftfPSViewer, labelPS);
        checkCustomPDFViewer = new JCheckBox(_("Use custom PDF viewer"));
        ComponentEnabler.installOn(checkCustomPDFViewer, true, ftfPDFViewer, labelPDF);
        
        //ftfTIFF2PDFLocation.setEnabled(allowTIFF2PDF);
        //labelTIFF2PDF.setEnabled(allowTIFF2PDF);
        //checkUseTiffPaperSize.setEnabled(allowTIFF2PDF);
        
        Dimension filler = new Dimension(OptionsWin.border, OptionsWin.border);
        panelPaths.add(checkCustomFaxViewer);
        panelPaths.add(labelTIFF);
        panelPaths.add(ftfFaxViewer);
        panelPaths.add(Box.createRigidArea(filler));
        panelPaths.add(new JSeparator());
        panelPaths.add(Box.createRigidArea(filler));
        panelPaths.add(checkCustomPSViewer);
        panelPaths.add(labelPS);
        panelPaths.add(ftfPSViewer);
        panelPaths.add(Box.createRigidArea(filler));
        panelPaths.add(new JSeparator());
        panelPaths.add(Box.createRigidArea(filler));
        panelPaths.add(checkCustomPDFViewer);
        panelPaths.add(labelPDF);
        //panelPaths.add(checkPDFSameAsPS);
        panelPaths.add(ftfPDFViewer);
        panelPaths.add(Box.createRigidArea(filler));
        panelPaths.add(new JSeparator());
        panelPaths.add(Box.createRigidArea(filler));
        panelPaths.add(labelGS);
        panelPaths.add(ftfGSLocation);
        panelPaths.add(Box.createRigidArea(filler));
        panelPaths.add(labelTIFF2PDF);
        panelPaths.add(ftfTIFF2PDFLocation);
        panelPaths.add(checkUseTiffPaperSize);
        panelPaths.add(Box.createVerticalGlue());
        return panelPaths;
    }

    private JPanel createPanelView() {
        final int rowCount = 7;
        double[][] dLay =  {
                {OptionsWin.border, TableLayout.FILL, OptionsWin.border},
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
        panelView.setBorder(BorderFactory.createTitledBorder(_("View settings")));
        
        checkCreateSingleFile = new JCheckBox("<html>" + _("View faxes as single file (needs GhostScript+tiff2pdf)") + "</html>");
        checkCreateAlwaysAsTargetFormatView = new JCheckBox(_("View faxes always in this format"));
        
        comboTargetFormatView = new JComboBox(MultiFileConvFormat.values());
        
        ActionListener panelViewListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              checkPanelViewEnabled();
            }  
        };
        checkCreateSingleFile.addActionListener(panelViewListener);
        
        panelView.add(checkCreateSingleFile, "1,0,1,1,f,c");
        Utils.addWithLabel(panelView, comboTargetFormatView, _("Format for viewing:"), "1,3,f,c");
        panelView.add(checkCreateAlwaysAsTargetFormatView, "1,4,f,c");
        
        return panelView;
    }
    
    private JPanel createPanelSend() {
        final int rowCount = 7;
        double[][] dLay =  {
                {OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                new double[rowCount]
        };
        final double rowH = 2.0/(rowCount-1);
        dLay[1][rowCount-1] = OptionsWin.border;
        for (int i=0; i < rowCount-1; i++) {
            dLay[1][i] = ((i%2) == 0) ? rowH : TableLayout.PREFERRED; 
        }
        dLay[1][rowCount-3] = TableLayout.FILL;
        
        JPanel panelSend = new JPanel(false);
        panelSend.setLayout(new TableLayout(dLay));
        panelSend.setBorder(BorderFactory.createTitledBorder(_("Send settings")));
        
        checkCreateAlwaysAsTargetFormat = new JCheckBox(_("Send faxes always in this format"));
        
        comboTargetFormat = new JComboBox(MultiFileConvFormat.values());
        comboSendMode = new JComboBox(MultiFileMode.values());
        
        ActionListener panelSendListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              checkPanelSendEnabled();
            }  
        };
        comboSendMode.addActionListener(panelSendListener);
        
        Utils.addWithLabel(panelSend, comboSendMode, _("Send multiple files as:"), "1,1,f,c");
        Utils.addWithLabel(panelSend, comboTargetFormat, _("Format for sending:"), "1,3,f,c");
        panelSend.add(checkCreateAlwaysAsTargetFormat, "1,4,f,c");
        
        return panelSend;
    }
    
    void checkPanelViewEnabled() {
        boolean enable = (checkCreateSingleFile.isSelected());
        checkCreateAlwaysAsTargetFormatView.setEnabled(enable);
        comboTargetFormatView.setEnabled(enable);
    }
    
    void checkPanelSendEnabled() {
        boolean enable = (comboSendMode.getSelectedItem() != MultiFileMode.NONE);
        checkCreateAlwaysAsTargetFormat.setEnabled(enable);
        comboTargetFormat.setEnabled(enable);
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
        
        checkUseTiffPaperSize.setSelected(foEdit.usePaperSizeForTIFF2Any);
        checkCreateAlwaysAsTargetFormat.setSelected(foEdit.alwaysCreateTargetFormat);
        checkCreateAlwaysAsTargetFormatView.setSelected(foEdit.alwaysCreateTargetFormatForViewing);
        checkCreateSingleFile.setSelected(foEdit.createSingleFilesForViewing);
        //checkPDFSameAsPS.setSelected(foEdit.viewPDFAsPS);
        checkCustomFaxViewer.setSelected(foEdit.useCustomFaxViewer);
        checkCustomPSViewer.setSelected(foEdit.useCustomPSViewer);
        checkCustomPDFViewer.setSelected(foEdit.useCustomPDFViewer);
        
        comboSendMode.setSelectedItem(foEdit.multiFileSendMode);
        comboTargetFormat.setSelectedItem(foEdit.singleFileFormat);
        comboTargetFormatView.setSelectedItem(foEdit.singleFileFormatForViewing);
        
        checkPanelViewEnabled();
        checkPanelSendEnabled();
        //ftfPDFViewer.setEnabled(!foEdit.viewPDFAsPS);
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

        foEdit.usePaperSizeForTIFF2Any = checkUseTiffPaperSize.isSelected();
        foEdit.alwaysCreateTargetFormat = checkCreateAlwaysAsTargetFormat.isSelected();
        foEdit.alwaysCreateTargetFormatForViewing = checkCreateAlwaysAsTargetFormatView.isSelected();
        foEdit.createSingleFilesForViewing = checkCreateSingleFile.isSelected();
        //foEdit.viewPDFAsPS = checkPDFSameAsPS.isSelected();
        foEdit.useCustomFaxViewer = checkCustomFaxViewer.isSelected();
        foEdit.useCustomPSViewer = checkCustomPSViewer.isSelected();
        foEdit.useCustomPDFViewer = checkCustomPDFViewer.isSelected();

        foEdit.multiFileSendMode = (MultiFileMode)comboSendMode.getSelectedItem();
        foEdit.singleFileFormat = (MultiFileConvFormat)comboTargetFormat.getSelectedItem();
        foEdit.singleFileFormatForViewing = (MultiFileConvFormat)comboTargetFormatView.getSelectedItem();
    }

    /* (non-Javadoc)
     * @see yajhfc.options.OptionsPage#validateSettings(yajhfc.options.OptionsWin)
     */
    public boolean validateSettings(OptionsWin optionsWin) {
        if (checkCustomFaxViewer.isSelected() && !commandLineOK(ftfFaxViewer.getText())) {
            optionsWin.focusComponent(ftfFaxViewer.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the command line for the fax viewer."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (checkCustomPSViewer.isSelected() && !commandLineOK(ftfPSViewer.getText())) {
            optionsWin.focusComponent(ftfPSViewer.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the command line for the PostScript viewer."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (checkCustomPDFViewer.isSelected() && !commandLineOK(ftfPDFViewer.getText())) {
            optionsWin.focusComponent(ftfPDFViewer.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the command line for the PDF viewer."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        final boolean needsGS = (checkCreateSingleFile.isSelected() || 
                comboSendMode.getSelectedItem() != MultiFileMode.NONE);
        if (needsGS && !executableOK(ftfGSLocation.getText())) {
            optionsWin.focusComponent(ftfGSLocation.getJTextField());
            JOptionPane.showMessageDialog(optionsWin, _("Please enter the location of the GhostScript executable."), _("Error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (requireTIFF2PDF && needsGS && !executableOK(ftfTIFF2PDFLocation.getText())) {
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
    
    private boolean executableOK(String commandLine) {
        return commandLine != null && commandLine.length() > 0 && 
            Utils.searchExecutableInPath(commandLine) != null;
    }
    
    static class ExeFileTextField extends FileTextField {
        protected String readTextFieldFileName() {
            //return this.getText().replaceAll("(\")?%s(\")?", "").trim();
            return Utils.extractExecutableFromCmdLine(getText());
        }
        
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
