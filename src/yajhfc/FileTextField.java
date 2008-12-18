package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005 Jonas Wolz
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
    
    private JTextField jTextField;
    private JButton jButton;
    private JFileChooser fileChooser;
    private FileFilter[] fileFilters;
    private int fileSelectionMode = JFileChooser.FILES_ONLY;
    private File currentDirectory = null;
    
    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new SafeJFileChooser();
        }
        return fileChooser;
    }
    
    public void actionPerformed(ActionEvent e) {
        JFileChooser jFileChooser = getFileChooser();

        if (fileFilters != null && fileFilters.length > 0) {
            // Apply file filters:
            jFileChooser.resetChoosableFileFilters();

            for (FileFilter ff : fileFilters)
                jFileChooser.addChoosableFileFilter(ff);

            FileFilter allf = jFileChooser.getAcceptAllFileFilter();
            jFileChooser.removeChoosableFileFilter(allf);
            jFileChooser.addChoosableFileFilter(allf);
            jFileChooser.setFileFilter(fileFilters[0]);
        }
        
        jFileChooser.setFileSelectionMode(fileSelectionMode);
        if (currentDirectory != null) {
            jFileChooser.setCurrentDirectory(currentDirectory);
        }
        
        jFileChooser.setSelectedFile(new File(readTextFieldFileName()));
        if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentDirectory = jFileChooser.getCurrentDirectory();
            writeTextFieldFileName(jFileChooser.getSelectedFile().getPath());
        }
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
