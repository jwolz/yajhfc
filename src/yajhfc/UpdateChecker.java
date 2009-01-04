/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2009 Jonas Wolz
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
package yajhfc;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import yajhfc.util.ProgressWorker;
import yajhfc.util.ProgressWorker.ProgressUI;


/**
 * @author jonas
 *
 */
public class UpdateChecker {
    
    static final Logger log = Logger.getLogger(UpdateChecker.class.getName());
    private static final String UPDATE_URL = "http://yajhfc.berlios.de/updatecheck/versioninfo.xml"; //"file:/home/jonas/java/yajhfc/update.xml";
    private static final String RELEASE_DATE_FORMAT = "yyyy-MM-dd";
    private static final long CHECK_INTERVAL = 7 * 24 * 3600 * 1000; // Check weekly
    
    protected YajHFCVersion currentVersion = null;
    protected Date releaseDate = null;
    protected URI infoURI = null;
    
    /**
     * Shows an info dialog for the new version. This method assumes that there has been
     * an update check by checkForUpdates() earlier.
     * @param parent
     * @return true to remind again, false otherwise
     */
    public boolean showInfoDialog(final Window parent, final boolean askForRemindAgain) {
        final String text = 
            Utils._("There is a new version of YajHFC available!") + "\n\n" +
            Utils._("Version") + ": " + currentVersion + '\n' +
            Utils._("Release date") + ": " + DateKind.getInstanceFromKind(DateKind.DATE_ONLY).format(releaseDate);
        final JButton websiteButton = new JButton(Utils._("Go to website"));
        websiteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DesktopManager.getDefault().safeBrowse(infoURI, parent);
            }
        });
        Object[] options;
        if (askForRemindAgain) {
            options = new Object[] {
                    websiteButton,
                    Utils._("Remind me again"),
                    Utils._("Do not remind me again")
            };
        } else {
            options = new Object[] {
                    websiteButton,
                    Utils._("Close")
            };
        }

        int result = JOptionPane.showOptionDialog(parent, text, Utils._("Check for update"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (result == JOptionPane.CLOSED_OPTION || result == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks for an update. Returns true if a newer version is available.
     * If this method returns true, currentVersion, releaseDate and infoURL are guaranteed to be non-null
     * @return
     * @throws ParserConfigurationException
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws DOMException
     * @throws ParseException
     * @throws URISyntaxException 
     */
    public boolean checkForUpdates() throws ParserConfigurationException, MalformedURLException, IOException, SAXException, DOMException, ParseException, URISyntaxException {
        log.info("Checking for updates from URL " + UPDATE_URL);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream urlStream = new URL(UPDATE_URL).openStream();
        Document doc = builder.parse(urlStream);
        urlStream.close();
        
        Element root = doc.getDocumentElement();
        NodeList nl = root.getChildNodes();
        
        currentVersion = null;
        releaseDate = null;
        infoURI = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Node item = nl.item(i);
            if ((item.getNodeType() == Node.ELEMENT_NODE)) {
                String nodeName = item.getNodeName();
                String content = item.getTextContent().trim();
                if ("currentVersion".equals(nodeName)) {
                    log.fine("Found currentVersion tag: " + content);
                    currentVersion = new YajHFCVersion(content);
                } else if ("releaseDate".equals(nodeName)) {
                    log.fine("Found releaseDate tag: " + content);
                    releaseDate = new SimpleDateFormat(RELEASE_DATE_FORMAT).parse(content);
                } else if ("infoURL".equals(nodeName)) {
                    log.fine("Found infoURL tag: " + content);
                    infoURI = new URI(content);
                } else {
                    log.warning("Unknown element \"" + nodeName + "\" with content: " + content);
                }
            }
        }
        
        if (currentVersion == null || releaseDate == null || infoURI == null) {
            throw new ParseException("Not all required fields found in XML.", -1);
        } else {
            log.fine(String.format("Successfully loaded update XML: currentVersion=\"%s\", releaseDate=\"%tF\", infoURL=\"%s\"", currentVersion, releaseDate, infoURI));
            
            YajHFCVersion myVersion = new YajHFCVersion();
            if (myVersion.compareTo(currentVersion) < 0) {
                // Newer version available...
                log.info("A newer version is available: local: " + myVersion + "; remote: " + currentVersion); 
                return true;
            } else {
                log.info("No newer version available: local: " + myVersion + "; remote: " + currentVersion);
                return false;
            }
        }
    }
    
    public static void startSilentUpdateCheck() {
        final FaxOptions fo = Utils.getFaxOptions();
            
        if (System.currentTimeMillis() - fo.lastUpdateCheck > CHECK_INTERVAL) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        final UpdateChecker uc = new UpdateChecker();
                        if (uc.checkForUpdates()) {
                            if ("".equals(fo.lastSeenUpdateVersion) ||
                                    new YajHFCVersion(fo.lastSeenUpdateVersion).compareTo(uc.currentVersion) >= 0) { 
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        if (uc.showInfoDialog(Launcher2.application, true)) {
                                            fo.lastSeenUpdateVersion = "";
                                        } else  {
                                            fo.lastSeenUpdateVersion = uc.currentVersion.toString();
                                        }
                                    } 
                                });
                            }
                        }
                        fo.lastUpdateCheck = System.currentTimeMillis();
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Exception checking for updates:", e);
                    }
                }
            };
            t.start();
        }
    }
    
    public static void doGUIUpdateCheck(Window parent, ProgressUI progressUI) {
        ProgressWorker worker = new ProgressWorker() {
            private UpdateChecker uc;
            private Boolean checkResult;
            
            @Override
            public void doWork() {
                updateNote(Utils._("Connecting to server..."));
                uc = new UpdateChecker();
                try {
                    checkResult = Boolean.valueOf(uc.checkForUpdates());
                } catch (Exception e) {
                    showExceptionDialog(Utils._("Error checking for updates:"), e);
                    checkResult = null;
                } 
                Utils.getFaxOptions().lastUpdateCheck = System.currentTimeMillis();
            }
            
            @Override
            protected void pMonClosed() {
                if (checkResult != null) {
                    if (checkResult.booleanValue()) {
                        uc.showInfoDialog((Window)this.parent, false);
                    } else {
                        JOptionPane.showMessageDialog(this.parent, Utils._("Your version of YajHFC is up to date."), Utils._("Check for update"), JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        };
        worker.setProgressMonitor(progressUI);
        worker.startWork(parent, Utils._("Checking for updates..."));
    }
}
