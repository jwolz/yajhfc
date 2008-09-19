package yajhfc;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2006 Jonas Wolz
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

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
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
    private ClipboardPopup clpDef;
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
        btnOK = new JButton(utils._("OK"));
        btnOK.addActionListener(this);
        btnDetails = new JButton(utils._("Details") + " >>");
        btnDetails.addActionListener(this);
        btnCopy = new JButton(utils._("Copy"), utils.loadIcon("general/Copy"));
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
        
        clpDef = new ClipboardPopup();
        
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
        textStacktrace.addMouseListener(clpDef);
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
        
        if (utils.debugMode) {
//            utils.debugOut.println("EXCEPTION occured: " + message);
//            exc.printStackTrace(utils.debugOut);
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
            btnDetails.setText(utils._("Details") + (detailState ? " <<" : " >>"));
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

    public static void showExceptionDialog(Frame owner, String title, String message, Exception exc) {
        ExceptionDialog eDlg = new ExceptionDialog(owner, title, message, exc);
        eDlg.setVisible(true);
    }

    public static void showExceptionDialog(Frame owner, String message, Exception exc) {
        showExceptionDialog(owner, utils._("Error"), message, exc);
    }
    
    public static void showExceptionDialog(Dialog owner, String title, String message, Exception exc) {
        ExceptionDialog eDlg = new ExceptionDialog(owner, title, message, exc);
        eDlg.setVisible(true);
    }

    public static void showExceptionDialog(Dialog owner, String message, Exception exc) {
        showExceptionDialog(owner, utils._("Error"), message, exc);
    }

    public static void showExceptionDialogThreaded(Component owner, String message, Exception exc) {
        try {
            SwingUtilities.invokeAndWait(new DisplayRunnable(owner, exc, message));
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Error showing exception dialog.", e);
        } catch (InvocationTargetException e) {
            log.log(Level.WARNING, "Error showing exception dialog.", e);
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
    public static class DisplayRunnable implements Runnable {
        private Component parent;
        private Exception ex;
        private String msg;
        
        public DisplayRunnable(Component parent, Exception ex, String msg) {
            this.parent = parent;
            this.ex = ex;
            this.msg = msg;
        }
        
        public void run() {
            if (parent instanceof Dialog) {
                showExceptionDialog((Dialog)parent, msg, ex);
            } else if (parent instanceof Frame) {
                showExceptionDialog((Frame)parent, msg, ex);
            } else {
                JOptionPane.showMessageDialog(parent, msg + "\n" + ex.getMessage(), utils._("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
