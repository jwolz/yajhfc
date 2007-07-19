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
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class ExceptionDialog extends JDialog implements ActionListener, ComponentListener {

    private JLabel lblText, lblExceptionText;
    private JScrollPane scrollStacktrace;
    private JTextArea textStacktrace;
    private JButton btnOK, btnDetails;
    private JPanel contentPane;
    private Box boxButtons/*, boxLabels*/;
    private Component strutStacktrace;
    private ClipboardPopup clpDef;
    private boolean detailState = false;
    
    private void initialize(String message, Exception exc) {
        final int border = 12;
        double[][] dLay = {
                { border, TableLayout.PREFERRED, border, 400, border },
                { border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border }
        };
        
        contentPane = new JPanel(new TableLayout(dLay));
        
        boxButtons = Box.createHorizontalBox();
        btnOK = new JButton(utils._("OK"));
        btnOK.addActionListener(this);
        btnDetails = new JButton(utils._("Details") + " >>");
        btnDetails.addActionListener(this);
        
        boxButtons.add(Box.createHorizontalGlue());
        boxButtons.add(btnOK);
        boxButtons.add(Box.createHorizontalStrut(border));
        boxButtons.add(btnDetails);
        boxButtons.add(Box.createHorizontalGlue());
        
        JLabel lblIcon = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        
        lblText = new JLabel("<html>" + message + "</html>");
        
        if (exc.getLocalizedMessage() != null) {
            lblExceptionText = new JLabel("<html>" + exc.getLocalizedMessage() + "</html>");
        } else
            lblExceptionText = null;
        
        strutStacktrace = Box.createVerticalStrut(20);
        
        StringWriter stringBuf = new StringWriter();
        exc.printStackTrace(new PrintWriter(stringBuf));
        
        clpDef = new ClipboardPopup();
        
        textStacktrace = new JTextArea(stringBuf.toString());
        textStacktrace.setFont(new Font("DialogInput", Font.PLAIN, 12));
        textStacktrace.setEditable(false);
        textStacktrace.addMouseListener(clpDef);
        textStacktrace.setRows(8);
        textStacktrace.setColumns(40);
        
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
        lblText.addComponentListener(this);
        if (lblExceptionText != null)
            lblExceptionText.addComponentListener(this);
        
        if (utils.debugMode) {
            utils.debugOut.println("EXCEPTION occured: " + message);
            exc.printStackTrace(utils.debugOut);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOK) {
            dispose();
        } else if (e.getSource() == btnDetails) {
            detailState = !detailState;
            if (detailState) {
                contentPane.add(strutStacktrace, "1, 4, 3, 4");
                contentPane.add(scrollStacktrace, "1, 5, 3, 5");
            } else {
                contentPane.remove(strutStacktrace);
                contentPane.remove(scrollStacktrace);
            }
            btnDetails.setText(utils._("Details") + (detailState ? " <<" : " >>"));
            this.pack();
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

    public void componentHidden(ComponentEvent e) {
        //  not used   
    }

    public void componentMoved(ComponentEvent e) {
        //  not used   
    }

    public void componentResized(ComponentEvent e) {
        this.pack();    
    }

    public void componentShown(ComponentEvent e) {
        // not used
    }

}
