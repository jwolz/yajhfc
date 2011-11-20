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

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import yajhfc.launch.Launcher2;
import yajhfc.server.ServerOptions;
import yajhfc.ui.YajOptionPane;

public class HylaClientManager {
    protected boolean adminMode;
    protected HylaFAXClient client;
    protected ServerOptions myopts;
    protected String password;
    protected String adminPassword;
    protected String userName;
    protected int transactionCounter;
    protected List<HylaModem> realModems = null;
    protected List<HylaModem> modems = null;
    protected boolean showErrorsUsingGUI = true;
    
    protected static final String modemListFormat = "$$$|%m|%n";
    protected static final String modemListPrefix = "$$$";    
    
    private static final Logger log = Logger.getLogger(HylaClientManager.class.getName());
    
    public HylaClientManager(ServerOptions myopts) {
        super();
        optionsChanged(myopts);
    }

    public void optionsChanged(ServerOptions newOpts) {
        forceLogout();
        
        myopts = newOpts;
        
        realModems = null;
        modems = null;
        
        if (!myopts.askPassword)
            password = myopts.pass.getPassword();
        else
            password = null;
        
        if (!myopts.askAdminPassword)
            adminPassword = myopts.AdminPassword.getPassword();
        else
            adminPassword = null;
        
        if (!myopts.askUsername) 
            userName = myopts.user;
        else 
            userName = null;
    }
    
    public synchronized HylaFAXClient beginServerTransaction(YajOptionPane dialogs) {
        transactionCounter++;
        
        if (Utils.debugMode) {
            log.fine("HylaClientManager -> beginServerTransaction: " + transactionCounter);
        }
        if (client != null  && myopts.useDisconnectedMode && transactionCounter == 1) {
            synchronized (client) {
                int time = 0;
                Thread.yield();
                try {
                    while (client != null && time < 500) {
                        Thread.sleep(50); // Wait that the other user frees the client (Quick and Dirty !!)
                        time += 50;
                    }
                } catch (InterruptedException e) {
                    // NOP
                }
                if (Utils.debugMode && time >= 500) {
                    log.info("In beginServerTransaction: TIMEOUT waiting for other client");
                }
            }
        }
        if (client == null) {
            if (transactionCounter != 1) {
                log.warning("Before forceLogin: transactionCounter = " + transactionCounter);
            }
            return forceLogin(dialogs);
        } else {
            return client;   
        }
    }
    
    public synchronized void endServerTransaction() {
        if (Utils.debugMode) {
            log.fine("HylaClientManager -> endServerTransaction: " + transactionCounter);
        }
        transactionCounter--;
        if (myopts.useDisconnectedMode && transactionCounter <= 0) {
            forceLogout();
        }
    }

    /**
     * Logs in. Returns null if an error occurred.
     */
    public synchronized HylaFAXClient forceLogin(YajOptionPane dialogs) {
        if (Utils.debugMode) {
            log.fine("HylaClientManager -> forceLogin");
        }
        if (client == null) {
            if (userName == null) {
                String[] pwd = dialogs.showPasswordDialog(Utils._("Log in"), Utils._("Please enter the credentials to log into the HylaFAX server:"), myopts.user, true, true);
                if (pwd == null) {
                    return null;
                }
                userName = pwd[0];
                password = pwd[1];
            }
            
            client = new HylaFAXClient();
            synchronized (client) {
                //client.setDebug(Utils.debugMode);
                client.setSocketTimeout(myopts.getParent().socketTimeout);
                client.setCharacterEncoding(myopts.hylaFAXCharacterEncoding);
                try {
                    client.open(myopts.host, myopts.port);
                    if (Utils.debugMode) {
                        log.info("Greeting was: " + client.getGreeting());
                    }
                    

                    while (client.user(userName)) {                
                        if (password == null || password.length() == 0) {

                            String[] pwd = dialogs.showPasswordDialog(Utils._("User password"), Utils._("Please enter the user password:"), userName, false, false);
                            if (pwd == null || pwd[1].length() == 0) { // User cancelled
                                client.quit();
                                //doErrorCleanup(); // TODO
                                return null;
                            } else
                                try {
                                    client.pass(pwd[1]);
                                    
                                    password = pwd[1]; // password after pass is important for repeated asks
                                    //repeatAsk = false;
                                    break;
                                } catch (ServerResponseException e) {
                                    dialogs.showExceptionDialog(Utils._("An error occured in response to the password:"), e);
                                    //repeatAsk = true;
                                }
                        } else {
                            client.pass(password);
                            break;
                        }
                    } 

                    if (adminMode) {
                        boolean authOK = false;
                        if (adminPassword == null || adminPassword.length() == 0) {
                            do {
                                String[] pwd = dialogs.showPasswordDialog(Utils._("Admin password"), Utils._("Please enter the administrative password:"), userName, false, false);
                                if (pwd == null || pwd[1].length() == 0) { // User cancelled
                                    break; //Continue in "normal" mode
                                } else
                                    try {
                                        client.admin(pwd[1]);
                                        adminPassword = pwd[1];
                                        authOK = true;
                                    } catch (ServerResponseException e) {
                                        dialogs.showExceptionDialog(Utils._("An error occured in response to the password:"), e);
                                        authOK = false;
                                    }
                            } while (!authOK);
                        } else {
                            client.admin(adminPassword);
                            authOK = true; // No error => authOK
                        }
                        ;
                        adminMode = authOK;
                    }

                    client.setPassive(myopts.pasv);
                    client.tzone(myopts.tzone.getTimezone());
                    return client;
                } catch (ServerResponseException sre) {
                    if (showErrorsUsingGUI) {
                        dialogs.showExceptionDialog(Utils._("The HylaFAX server responded with an error:"), sre);
                    } else {
                        log.log(Level.WARNING, "The HylaFAX server responded with an error:", sre);
                    }
                    return null;
                } catch (UnknownHostException uhe) {
                    if (showErrorsUsingGUI) {
                        dialogs.showExceptionDialog(Utils._("The server's host name was not found:"), uhe);
                    } else {
                        log.log(Level.WARNING, "The server's host name was not found:", uhe);
                    }
                    return null;
                } catch (Exception e) {
                    if (showErrorsUsingGUI) {
                        dialogs.showExceptionDialog(Utils._("An error occured connecting to the server:"), e);
                    } else {
                        log.log(Level.WARNING, "An error occured connecting to the server:", e);
                    }
                    return null;
                }
            }
        } else {
            return client;
        }
    }
    
    public synchronized void forceLogout() {
        if (Utils.debugMode) {
            log.fine("HylaClientManager -> forceLogout");
        }
        if (transactionCounter != 0) {
            log.warning("In forceLogout: transactionCounter = " + transactionCounter);
            transactionCounter = 0;
        }
        if (client != null) {
            try {
                client.quit();
            } catch (Exception e) {
                if (Utils.debugMode)
                    log.log(Level.INFO, "On client.quit():", e);
            }
            client = null;
        }
    }

    public boolean isAdminMode() {
        return adminMode;
    }

    public synchronized void setAdminMode(boolean adminMode) {
        if (adminMode != this.adminMode) 
        {
            this.adminMode = adminMode;
            forceLogout();
        }
    }
    
    public synchronized List<HylaModem> getModems() {
        if (modems == null) {
            modems = new ArrayList<HylaModem>();
            if (myopts.useCustomModems) {
                for (String modemString : myopts.customModems) {
                    modems.add(new HylaModem(modemString));
                }
            } else {
                modems.addAll(HylaModem.defaultModems);
                modems.addAll(getRealModems());
            }
        }
        return modems;
    }
        
    public synchronized List<HylaModem> getRealModems() {
        if (realModems == null) {
            HylaFAXClient hyfc = beginServerTransaction(Launcher2.application.getDialogUI());
            if (hyfc == null) {
                realModems = Collections.emptyList();
                return realModems;
            }
            
            Vector<?> status;
            try {
                synchronized (hyfc) {
                    String oldModemFmt = hyfc.mdmfmt();

                    hyfc.mdmfmt(modemListFormat);
                    status = hyfc.getList("status");

                    hyfc.mdmfmt(oldModemFmt);
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error fetching modem list: ", e);
                realModems = Collections.emptyList();
                return realModems;
            } finally {
                endServerTransaction();
            }
            
            realModems = new ArrayList<HylaModem>();
            for (Object o : status) {
                String line = (String)o;
                if (line.startsWith(modemListPrefix)) { // Is a line describing a modem
                    String[] fields = Utils.fastSplit(line, '|');
                    if (fields.length < 2) {
                        log.log(Level.WARNING, "Invalid modem \"" + line + "\".");                            
                    } else {
                        realModems.add(new HylaModem(fields[1], fields.length >= 3 ? fields[2] : ""));
                    }
                }
            }
        }
        return realModems;
    }
    
    public String getUser() {
        return userName;
    }
    
    public void setShowErrorsUsingGUI(boolean showErrorsUsingGUI) {
        this.showErrorsUsingGUI = showErrorsUsingGUI;
    }
    
    public boolean isShowErrorsUsingGUI() {
        return showErrorsUsingGUI;
    }
}
