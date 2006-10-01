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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class AboutDialog extends JDialog {
    
    public enum Mode { ABOUT, READMES };
    
    private static final String[] readmeFiles = 
            { "README.txt", "FAQ.txt", "COPYING" };
    
    private JPanel jContentPane;
    
    private Box boxButtons;
    private JButton buttonOK;
    
    private JTabbedPane tabMain;
    private JPanel aboutPane;
    //private ArrayList<JScrollPane> scrollTxt;
    private ClipboardPopup clpText;
    
    private void loadFile(JTextArea text, String resName) {
        //URL txtURL = AboutDialog.class.getResource(resName);
        URL txtURL = utils.getLocalizedFile(resName);
        
        try {
            text.setText("");
            
            BufferedReader fIn = new BufferedReader(new InputStreamReader(txtURL.openStream(), Charset.forName("UTF-8")));
            while (true) {
                String line = fIn.readLine();
                if (line != null) {
                    text.append(line + "\n");
                } else
                    break;
            }
            fIn.close();
            text.setCaretPosition(0);
        } catch (IOException e) {
            text.setText("Error loading text file " + resName + ".");
        }
    }
    
    
    private JScrollPane addScrollTxt(String resName) {
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("DialogInput", java.awt.Font.PLAIN, 12));
        text.addMouseListener(clpText);
        loadFile(text, resName);
        
        JScrollPane scroll = new JScrollPane(text);
        return scroll;
        
        /* if (scrollLicense == null) {
            textLicense = new JTextArea();
            textLicense.setEditable(false);
            textLicense.setFont(new Font("DialogInput", java.awt.Font.PLAIN, 12));
            loadFile();
            
            scrollLicense = new JScrollPane(textLicense);          
        }
        return scrollLicense; */
    }
    
    private JScrollPane addSysPropTxt() {
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font("DialogInput", java.awt.Font.PLAIN, 12));
        text.addMouseListener(clpText);

        Properties sp = System.getProperties();
        Enumeration e = sp.propertyNames();
        while (e.hasMoreElements()) {
            String sKey = (String)e.nextElement();
            text.append(sKey + "=" + sp.getProperty(sKey) + "\n");
        }
        text.setCaretPosition(0);
        
        JScrollPane scroll = new JScrollPane(text);
        return scroll;
    }
    
    private JPanel getAboutPane() {
        if (aboutPane == null) {
            double border = 15;
            double[][] dLay = {
                    { border, TableLayout.PREFERRED, border, TableLayout.FILL, border },
                    { border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, 0.25, 0.3, border }
            };
            
            aboutPane = new JPanel(new TableLayout(dLay));
            
            JLabel lblImg = new JLabel(new ImageIcon(AboutDialog.class.getResource("logo-large.png")));
            
            JLabel lblApp = new JLabel("<html>" + utils.AppName + "</html>");
            lblApp.setFont(new Font("Serif", Font.BOLD, 18));
            
            JLabel lblVer = new JLabel(MessageFormat.format(_("Version {0}"), utils.AppVersion));
            lblVer.setFont(new Font("Dialog", Font.ITALIC, 11));
            
            Box boxApp = new Box(BoxLayout.PAGE_AXIS);
            boxApp.add(lblApp);
            boxApp.add(Box.createVerticalStrut(4));
            boxApp.add(lblVer);
            
            JLabel lblCopyright = new JLabel(utils.AppCopyright);
            
            JLabel lblInfo = new JLabel(MessageFormat.format(_("<html>YajHFC is a client for the hylafax fax server (http://www.hylafax.org).<br><br>Homepage: {1} <br>Author: {0} </html>"), utils.AuthorEMail, utils.HomepageURL));
            JLabel lblTranslator = null;
            
            String translator = _("$TRANSLATOR$");
            if (!translator.equals("$TRANSLATOR$")) {
                lblTranslator = new JLabel(MessageFormat.format(_("$LANGUAGE$ translation by {0}"), translator));
            }
            
            aboutPane.add(lblImg, "1, 1");
            aboutPane.add(boxApp, "3, 1, F, C");
            
            aboutPane.add(lblInfo, "1, 3, 3, 3, F, C");
            if (lblTranslator != null)
                aboutPane.add(lblTranslator, "1, 5, 3, 5, F, C");
            aboutPane.add(lblCopyright, "1, 6, 3, 6, F, C");
        }
        return aboutPane;
    }
   
    private Box getBoxButtons() {
        if (boxButtons == null) {
            boxButtons = new Box(BoxLayout.LINE_AXIS);
            
            buttonOK = new JButton(_("OK"));
            buttonOK.addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    dispose();
                };
            });
            
            boxButtons.add(Box.createHorizontalGlue());
            boxButtons.add(buttonOK);
            boxButtons.add(Box.createHorizontalGlue());
        }
        return boxButtons;
    }
    
    private JTabbedPane getTabMain() {
        if (tabMain == null) {
            tabMain = new JTabbedPane(JTabbedPane.BOTTOM);
            
            //tabMain.addTab(_("About"), getAboutPane());
            //tabMain.addTab(_("License"), getScrollLicense());
        }
        return tabMain;
    }
    
    private JPanel getjContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel(new BorderLayout());
            jContentPane.add(getBoxButtons(), BorderLayout.SOUTH);
            jContentPane.add(getTabMain(), BorderLayout.CENTER);
        }
        return jContentPane;
    }
    
    private void initialize() {
        //setResizable(false);
        setModal(true);
        setSize(500, 380);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        clpText = new ClipboardPopup();
        setContentPane(getjContentPane());
        
        getRootPane().setDefaultButton(buttonOK);
        //setLocationByPlatform(true);
    }
    
    private static String _(String key) {
        return utils._(key);
    }
    
    public void setMode(Mode mode) {
        getTabMain().removeAll();
        switch (mode) {
        case ABOUT:
            tabMain.addTab(_("About"), getAboutPane());
            tabMain.addTab(_("License"), addScrollTxt("/COPYING"));
            tabMain.addTab(_("System properties"), addSysPropTxt());
            this.setTitle(MessageFormat.format(_("About {0}"), utils.AppShortName));
            break;
        case READMES:
            for (int i = 0; i < readmeFiles.length; i++)
                tabMain.addTab(readmeFiles[i], addScrollTxt("/" + readmeFiles[i]));
            this.setTitle(_("Documentation"));
            this.setSize(640, 480);
            break;
        default:
            System.err.println("Invalid mode!");
        }
    }
    
    public AboutDialog(Frame owner) {
        super(owner);
        
        initialize();
        setLocationRelativeTo(owner);
    }
}
