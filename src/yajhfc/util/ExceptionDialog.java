package yajhfc.util;
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

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import yajhfc.Utils;
import yajhfc.launch.Launcher2;

public class ExceptionDialog extends JDialog implements ActionListener {
    private final static int border = 12;
    private final static int maxTextWidth = 400;
    private static final int maxTextLines = 10;
    
    private JLabel lblText, lblExceptionText;
    private JScrollPane scrollStacktrace;
    private JTextArea textStacktrace;
    private JButton btnOK, btnDetails, btnCopy;
    private JPanel contentPane;
    private Box boxButtons/*, boxLabels*/;
    private Component strutStacktrace;
    private boolean detailState = false;
    private String fullMessage;
    
    private static final Logger log = Logger.getLogger(ExceptionDialog.class.getName());
    
    private void initialize(String message, Exception exc) {
        double[][] dLay = {
                { border, TableLayout.PREFERRED, border, TableLayout.FILL, border },
                { border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, border, TableLayout.PREFERRED, border }
        };
        
        contentPane = new JPanel(new TableLayout(dLay));
        
        boxButtons = Box.createHorizontalBox();
        btnOK = new JButton(Utils._("OK"));
        btnOK.addActionListener(this);
        btnDetails = new JButton(Utils._("Details") + " >>");
        btnDetails.addActionListener(this);
        btnCopy = new JButton(Utils._("Copy"), Utils.loadIcon("general/Copy"));
        btnCopy.addActionListener(this);
        
        boxButtons.add(Box.createHorizontalGlue());
        boxButtons.add(btnOK);
        boxButtons.add(Box.createHorizontalStrut(border));
        boxButtons.add(btnDetails);
        boxButtons.add(Box.createHorizontalStrut(border));
        boxButtons.add(btnCopy);
        boxButtons.add(Box.createHorizontalGlue());
        
        JLabel lblIcon = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        
        lblText = new JLabel("<html>" + message + "</html>");
        adjustTextLabelSize(lblText);
        
        String localizedMessage = exc.getLocalizedMessage();
        if (localizedMessage == null) {
            localizedMessage = exc.getMessage();
        }
        if (localizedMessage != null) {
            lblExceptionText = new JLabel("<html>" + localizedMessage + "</html>");
            lblExceptionText.setVerticalAlignment(SwingConstants.TOP);
            adjustTextLabelSize(lblExceptionText);
        } else {
            lblExceptionText = null;
        }
        
        strutStacktrace = Box.createVerticalStrut(20);
        
        StringWriter stringBuf = new StringWriter();
        exc.printStackTrace(new PrintWriter(stringBuf));
        
        //Append some system info:
        stringBuf.append("\n\n");
        stringBuf.append(Utils.AppShortName).append(' ').append(Utils.AppVersion).append('\n');
        stringBuf.append("Java ")
            .append(System.getProperty("java.version")).append(" (")
            .append(System.getProperty("java.vendor")).append(")\n")
            .append(System.getProperty("java.runtime.name")).append(' ')
            .append(System.getProperty("java.runtime.version")).append('\n')
            .append(System.getProperty("java.vm.name")).append('\n');
        stringBuf
            .append(System.getProperty("os.name")).append(' ')
            .append(System.getProperty("os.version")).append(" (")
            .append(System.getProperty("os.arch")).append(")\n");
        
        String stacktrace = stringBuf.toString();
        
        StringBuilder sb = new StringBuilder();
        sb.append(message).append('\n');
        if (localizedMessage != null)
            sb.append(localizedMessage).append('\n');
        sb.append('\n');
        sb.append(stacktrace);
        fullMessage = sb.toString();
        
        textStacktrace = new JTextArea(stacktrace);
        textStacktrace.setFont(new Font("DialogInput", Font.PLAIN, 12));
        textStacktrace.setEditable(false);
        textStacktrace.addMouseListener(ClipboardPopup.DEFAULT_POPUP);
        textStacktrace.setRows(8);
        //textStacktrace.setColumns(40);
        
        scrollStacktrace = new JScrollPane(textStacktrace, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        /*
        boxLabels = Box.createVerticalBox();
        boxLabels.add(lblText);
        boxLabels.add(Box.createVerticalStrut(border));
        if (lblExceptionText != null) {
            boxLabels.add(lblExceptionText);
            boxLabels.add(Box.createVerticalStrut(border));
        }
        boxLabels.add(scrollStacktrace);
        boxLabels.add(strutStacktrace);
        boxLabels.add(boxButtons); 
        
        contentPane.add(Box.createVerticalStrut(border), BorderLayout.NORTH);
        contentPane.add(Box.createVerticalStrut(border), BorderLayout.SOUTH);
        contentPane.add(Box.createHorizontalStrut(border), BorderLayout.WEST);
        contentPane.add(Box.createHorizontalStrut(border), BorderLayout.EAST);
        contentPane.add(boxLabels, BorderLayout.CENTER); */
        
        contentPane.add(lblIcon, "1, 1, 1, 3");
        contentPane.add(lblText, "3, 1");
        if (lblExceptionText != null)
            contentPane.add(lblExceptionText, "3, 3");
        contentPane.add(boxButtons, "1, 7, 3, 7");
        
        this.setResizable(false);
        this.getRootPane().getRootPane().setDefaultButton(btnOK);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(contentPane);
        //this.setLocationByPlatform(true);
        
        this.pack();
//        lblText.addComponentListener(this);
//        if (lblExceptionText != null)
//            lblExceptionText.addComponentListener(this);
        
        if (Utils.debugMode) {
//            Utils.debugOut.println("EXCEPTION occured: " + message);
//            exc.printStackTrace(Utils.debugOut);
            log.log(Level.WARNING, "Exception occurred: " + message, exc);
        }
    }
    
    private void adjustTextLabelSize(JLabel label) {
        // Try to calculate a reasonable height
        Dimension oldPreferred = label.getPreferredSize();
        if (oldPreferred.width > maxTextWidth) {
            int numLines = oldPreferred.width / maxTextWidth + 1;
            if (numLines > maxTextLines) {
                numLines = maxTextLines;
            }
            oldPreferred.height *= numLines;
        }
        oldPreferred.width = maxTextWidth;
        label.setPreferredSize(oldPreferred);
        label.setMinimumSize(oldPreferred);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnOK) {
            dispose();
        } else if (source == btnDetails) {
            detailState = !detailState;
            if (detailState) {
                contentPane.add(strutStacktrace, "1, 4, 3, 4");
                contentPane.add(scrollStacktrace, "1, 5, 3, 5");
            } else {
                contentPane.remove(strutStacktrace);
                contentPane.remove(scrollStacktrace);
            }
            btnDetails.setText(Utils._("Details") + (detailState ? " <<" : " >>"));
            this.setResizable(detailState);
            this.pack();
        } else if (source == btnCopy) {
            StringSelection contents = new StringSelection(fullMessage);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(contents, contents);
        }
    }
    
 
    public ExceptionDialog(Dialog owner, String title, String message, Exception exc) {
        super(owner, title, true);
        initialize(message, exc);
        this.setLocationRelativeTo(owner);
    }
    
    public ExceptionDialog(Frame owner, String title, String message, Exception exc) {
        super(owner, title, true);
        initialize(message, exc);
        this.setLocationRelativeTo(owner);
    }

    public static void showExceptionDialog(Component owner, String message, Exception exc) {
        showExceptionDialog(owner, Utils._("Error"), message, exc);
    }
    
    public static void showExceptionDialog(Component owner, String title, String message, Exception exc) {
        if (SwingUtilities.isEventDispatchThread()) {
            ExceptionDialog eDlg;
            if (!(owner instanceof Window)) {
                if (owner != null) {
                    owner = SwingUtilities.getWindowAncestor(owner);
                }
                if (owner == null) {
                    owner = Launcher2.application.getFrame();
                }
            }


            if (owner instanceof Dialog) {
                eDlg = new ExceptionDialog((Dialog)owner, title, message, exc);
            } else if (owner instanceof Frame) {
                eDlg = new ExceptionDialog((Frame)owner, title, message, exc);
            } else {
                JOptionPane.showMessageDialog(owner, message + "\n" + exc.getMessage(), Utils._("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            eDlg.setVisible(true);
        } else {
            try {
                SwingUtilities.invokeAndWait(new DisplayRunnable(owner, title, message, exc));
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Error showing exception dialog.", e);
            } catch (InvocationTargetException e) {
                log.log(Level.WARNING, "Error showing exception dialog.", e);
            }
        }
    }

    
//    public void componentHidden(ComponentEvent e) {
//        //  not used   
//    }
//
//    public void componentMoved(ComponentEvent e) {
//        //  not used   
//    }
//
//    public void componentResized(ComponentEvent e) {
//        this.pack();    
//    }
//
//    public void componentShown(ComponentEvent e) {
//        // not used
//    }

    /**
     * Implements an Runnable that may be used in conjunction with the SwingUtilities.invoke*()
     * to display an exception dialog from another thread 
     * @author jonas
     *
     */
    static class DisplayRunnable implements Runnable {
        private Component parent;
        private Exception ex;
        private String msg;
        private String title;
        
        public DisplayRunnable(Component parent, String title, String msg, Exception ex) {
            this.parent = parent;
            this.ex = ex;
            this.msg = msg;
            this.title = title;
        }
        
        public void run() {
            showExceptionDialog(parent, title, msg, ex);
        }
    }
}
