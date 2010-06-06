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
package yajhfc.logconsole;

import static yajhfc.Utils._;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import yajhfc.Utils;
import yajhfc.file.FileFormat;
import yajhfc.launch.Launcher2;
import yajhfc.logconsole.SwingLogHandler.LogListener;
import yajhfc.util.CancelAction;
import yajhfc.util.ExampleFileFilter;
import yajhfc.util.ExcDialogAbstractAction;
import yajhfc.util.ExceptionDialog;
import yajhfc.util.SafeJFileChooser;

/**
 * @author jonas
 *
 */
public class LogConsole extends JFrame implements LogListener {
    static final Logger log = Logger.getLogger(LogConsole.class.getName());
    
    JTextArea textLog;
    Action actCopy, actSave;
    JComboBox comboLevel;
    //JCheckBox checkDebug;
    SimpleFormatter formatter = new SimpleFormatter();
    
    private static final int border = 8;
    
    /**
     * @throws HeadlessException
     */
    public LogConsole() throws HeadlessException {
        super(_("YajHFC log console"));
        initialize();
    }


    private void initialize() {
        actCopy = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                StringSelection contents = new StringSelection(textLog.getText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, contents);
            }
        };
        actCopy.putValue(Action.NAME, _("Copy"));
        actCopy.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Copy"));
        
        actSave = new ExcDialogAbstractAction() {
            @Override
            protected void actualActionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new SafeJFileChooser();
                if (Utils.getFaxOptions().lastSavePath.length() > 0) {
                    fileChooser.setCurrentDirectory(new File(Utils.getFaxOptions().lastSavePath));
                }
                fileChooser.resetChoosableFileFilters();
                FileFilter txtFilter = new ExampleFileFilter(FileFormat.PlainText.getPossibleExtensions(), FileFormat.PlainText.getDescription());
                FileFilter logFilter = new ExampleFileFilter("log", Utils._("Log files"));
                fileChooser.addChoosableFileFilter(txtFilter);
                fileChooser.addChoosableFileFilter(logFilter);
                fileChooser.setFileFilter(txtFilter);
                
                if (fileChooser.showSaveDialog(LogConsole.this) == JFileChooser.APPROVE_OPTION) {
                    Utils.getFaxOptions().lastSavePath = fileChooser.getCurrentDirectory().getAbsolutePath();
                    
                    File selectedFile = Utils.getSelectedFileFromSaveChooser(fileChooser);
                    try {
                        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(selectedFile));
                        writer.write(textLog.getText());
                        writer.close();
                    } catch (Exception e1) {
                        ExceptionDialog.showExceptionDialog(LogConsole.this, Utils._("Error saving the log file"), e1);
                    }
                }
            }
        };
        actSave.putValue(Action.NAME, _("Save")+"...");
        actSave.putValue(Action.SMALL_ICON, Utils.loadIcon("general/Save"));
        
        CancelAction actClose = new CancelAction(this, _("Close"));
        
        JPanel contentPane = new JPanel(new BorderLayout());
        textLog = new JTextArea();
        textLog.setEditable(false);
        textLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JPanel buttonPanel = new JPanel(new GridLayout(2,3,border,border));
        
        Level[] logLevels = {
                Level.ALL,
                Level.FINEST,
                Level.FINER,
                Level.FINE,
                Level.CONFIG,
                Level.INFO,
                Level.WARNING,
                Level.SEVERE,
                Level.OFF
        };
        comboLevel = new JComboBox(logLevels);
        comboLevel.setSelectedItem(Launcher2.swingLogHandler.getLevel());
        comboLevel.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               Launcher2.swingLogHandler.setLevel((Level)comboLevel.getSelectedItem());
            } 
        });
        
//        checkDebug = new JCheckBox(_("Show debug messages"));
//        checkDebug.setSelected(Utils.debugMode);
//        checkDebug.addActionListener(new ActionListener() {
//           public void actionPerformed(ActionEvent e) {
//               log.info("User set debugMode to " + checkDebug.isSelected());
//               Utils.debugMode = checkDebug.isSelected();
//           } 
//        });
        
        buttonPanel.add(Box.createRigidArea(new Dimension(border, border)));
        JLabel levelLabel = new JLabel("Log level:");
        levelLabel.setAlignmentX(RIGHT_ALIGNMENT);
        levelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        buttonPanel.add(levelLabel);
        buttonPanel.add(comboLevel);
        
        buttonPanel.add(new JButton(actCopy));
        buttonPanel.add(new JButton(actSave));
        buttonPanel.add(actClose.createCancelButton());
        
        contentPane.add(new JScrollPane(textLog, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
        setSize(800, 600);
        Utils.setDefWinPos(this);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Launcher2.swingLogHandler.removeLogListener(LogConsole.this);
                dispose();
            }  
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        Launcher2.swingLogHandler.addLogListenerAndPublishBuffer(LogConsole.this);
    }
        
    public void recordPublished(LogRecord record) {
        textLog.append(formatter.format(record));
        textLog.append("\n");
    }
}
