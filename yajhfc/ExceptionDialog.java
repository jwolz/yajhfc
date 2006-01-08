package yajhfc;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class ExceptionDialog extends JDialog implements ActionListener {

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
                { border, 400, border },
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
        
        contentPane.add(lblText, "1, 1");
        if (lblExceptionText != null)
            contentPane.add(lblExceptionText, "1, 3");
        contentPane.add(boxButtons, "1, 7");
        
        this.setResizable(false);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setContentPane(contentPane);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnOK) {
            dispose();
        } else if (e.getSource() == btnDetails) {
            detailState = !detailState;
            if (detailState) {
                contentPane.add(strutStacktrace, "1, 4");
                contentPane.add(scrollStacktrace, "1, 5");
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
        this.pack();
    }
    
    public ExceptionDialog(Frame owner, String title, String message, Exception exc) {
        super(owner, title, true);
        initialize(message, exc);
        this.setLocationRelativeTo(owner);
        this.pack();
    }

    public static void showExceptionDialog(Frame owner, String title, String message, Exception exc) {
        ExceptionDialog eDlg = new ExceptionDialog(owner, title, message, exc);
        eDlg.setVisible(true);
        eDlg.pack();
        eDlg.validate();
    }

    public static void showExceptionDialog(Frame owner, String message, Exception exc) {
        showExceptionDialog(owner, utils._("Error"), message, exc);
    }
    
    public static void showExceptionDialog(Dialog owner, String title, String message, Exception exc) {
        ExceptionDialog eDlg = new ExceptionDialog(owner, title, message, exc);
        eDlg.setVisible(true);
        eDlg.pack();
        eDlg.validate();
    }

    public static void showExceptionDialog(Dialog owner, String message, Exception exc) {
        showExceptionDialog(owner, utils._("Error"), message, exc);
    }
}
