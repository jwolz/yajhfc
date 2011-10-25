package yajhfc;
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
 */


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import yajhfc.util.SafeJFileChooser;

//Text field with Button and FileChooser
public class FileTextField extends JComponent implements ActionListener {
    
    protected JTextField jTextField;
    protected JButton jButton;
    private JFileChooser fileChooser;
    protected FileFilter[] fileFilters;
    private boolean filtersChanged = true;
    protected int fileSelectionMode = JFileChooser.FILES_ONLY;
    protected File currentDirectory = null;
    
    protected JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new SafeJFileChooser();
        }
        return fileChooser;
    }
    
    public void actionPerformed(ActionEvent e) {
        JFileChooser jFileChooser = getFileChooser();

        configureFileChooser(jFileChooser);
        if (currentDirectory != null) {
            jFileChooser.setCurrentDirectory(currentDirectory);
        }
        
        jFileChooser.setSelectedFile(new File(readTextFieldFileName()));
        if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentDirectory = jFileChooser.getCurrentDirectory();
            handleUserSelection(jFileChooser);
        }
    }

    protected void configureFileChooser(JFileChooser jFileChooser) {
        if (fileFilters != null && fileFilters.length > 0 && filtersChanged) {
            // Apply file filters:
            jFileChooser.resetChoosableFileFilters();

            for (FileFilter ff : fileFilters)
                jFileChooser.addChoosableFileFilter(ff);

            FileFilter allf = jFileChooser.getAcceptAllFileFilter();
            jFileChooser.removeChoosableFileFilter(allf);
            jFileChooser.addChoosableFileFilter(allf);
            jFileChooser.setFileFilter(fileFilters[0]);
            filtersChanged = false;
        }
        
        jFileChooser.setFileSelectionMode(fileSelectionMode);
    }

    protected void handleUserSelection(JFileChooser jFileChooser) {
        writeTextFieldFileName(jFileChooser.getSelectedFile().getPath());
    }
    
    protected String readTextFieldFileName() {
        return getText();
    }
    
    protected void writeTextFieldFileName(String fName) {
        setText(fName);
    }
    
    public String getText() {
        return jTextField.getText();
    }
    
    public void setText(String text) {
        jTextField.setText(text);
    }
    
    public JTextField getJTextField() {
        return jTextField;
    }
    
    public JButton getJButton() {
        return jButton;
    }
    
    
    public int getFileSelectionMode() {
        return fileSelectionMode;
    }

    public void setFileSelectionMode(int fileSelectionMode) {
        this.fileSelectionMode = fileSelectionMode;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public FileFilter[] getFileFilters() {
        return fileFilters;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        jButton.setEnabled(enabled);
        jTextField.setEnabled(enabled);
    }
    
    /**
     * Adds a list of file filters; the "all files" filter is included as the last option.
     * The first one is used as a default.
     */
    public void setFileFilters(FileFilter... filters) {
        fileFilters = filters;
        filtersChanged = true;
    }
    
    public FileTextField() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        jTextField = new JTextField();
        
        jButton = new JButton(Utils.loadIcon("general/Open"));
        jButton.setToolTipText(Utils._("Choose a file using a dialog"));
        jButton.addActionListener(this);
        
        Dimension d = jButton.getPreferredSize();
        Dimension d2 = jTextField.getPreferredSize();
        if (d2.height > d.height)
            d.height = d2.height;
        else
            d2.height = d.height;
        d2.width = Integer.MAX_VALUE;
        
        jButton.setMaximumSize(d);
        jTextField.setMaximumSize(d2);
        
        add(jTextField);
        add(jButton);
    }
}
