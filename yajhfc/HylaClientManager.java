package yajhfc;

import gnu.hylafax.HylaFAXClient;
import gnu.inet.ftp.ServerResponseException;

import java.awt.Window;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JFrame;

public class HylaClientManager {
    protected boolean adminMode;
    protected HylaFAXClient client;
    protected FaxOptions myopts;
    protected String password;
    protected String adminPassword;
    protected String lastUser;
    
    public HylaClientManager(FaxOptions myopts, JFrame mainWin) {
        super();
        this.myopts = myopts;
        optionsChanged();
    }

    public void optionsChanged() {
        forceLogout();
        if (!myopts.askPassword)
            password = myopts.pass;
        else
            password = null;
        
        if (!myopts.askAdminPassword)
            adminPassword = myopts.AdminPassword;
        else
            adminPassword = null;
    }
    
    public HylaFAXClient beginServerTransaction(Window owner) {
        if (utils.debugMode) {
            utils.debugOut.println("HylaClientManager -> beginServerTransaction");
        }
        if (client == null) {
            return forceLogin(owner);
        } else {
            return client;   
        }
    }
    
    public void endServerTransaction() {
        if (utils.debugMode) {
            utils.debugOut.println("HylaClientManager -> endServerTransaction");
        }
        if (myopts.useDisconnectedMode) {
            forceLogout();
        }
    }
    
    /**
     * Logs in. Returns null if an error occurred.
     */
    public HylaFAXClient forceLogin(Window owner) {
        if (utils.debugMode) {
            utils.debugOut.println("HylaClientManager -> forceLogin");
        }
        if (client == null)
        {
            client = new HylaFAXClient();
            client.setDebug(utils.debugMode);
            try {
                client.open(myopts.host, myopts.port);
                
                while (client.user(myopts.user)) {                
                        if (password == null) {
                            
                            String pwd = PasswordDialog.showPasswordDialogThreaded(owner, utils._("User password"), MessageFormat.format(utils._("Please enter the password for user \"{0}\"."), myopts.user));
                            if (pwd == null) { // User cancelled
                                client.quit();
                                //doErrorCleanup(); // TODO
                                return null;
                            } else
                                try {
                                    client.pass(pwd);
                                    password = pwd;
                                    //repeatAsk = false;
                                    break;
                                } catch (ServerResponseException e) {
                                    ExceptionDialog.showExceptionDialogThreaded(owner, utils._("An error occured in response to the password:"), e);
                                    //repeatAsk = true;
                                }
                        } else {
                            client.pass(password);
                            break;
                        }
                } 
                
                if (adminMode) {
                    boolean authOK = false;
                    if (adminPassword == null) {
                        do {
                            String pwd = PasswordDialog.showPasswordDialogThreaded(owner, utils._("Admin password"), MessageFormat.format(utils._("Please enter the administrative password for user \"{0}\"."), myopts.user));
                            if (pwd == null) { // User cancelled
                                break; //Continue in "normal" mode
                            } else
                                try {
                                    client.admin(pwd);
                                    adminPassword = pwd;
                                    authOK = true;
                                } catch (ServerResponseException e) {
                                    ExceptionDialog.showExceptionDialogThreaded(owner, utils._("An error occured in response to the password:"), e);
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
                client.tzone(myopts.tzone.type);
                
                client.rcvfmt(myopts.recvfmt.getFormatString());
                
                return client;
            } catch (Exception e) {
                ExceptionDialog.showExceptionDialogThreaded(owner, utils._("An error occured connecting to the server:"), e);
                return null;
            }
        } else {
            return client;
        }
    }
    
    public void forceLogout() {
        if (utils.debugMode) {
            utils.debugOut.println("HylaClientManager -> forceLogout");
        }
        if (client != null) {
            try {
                client.quit();
            } catch (IOException e) {
                if (utils.debugMode)
                    e.printStackTrace(utils.debugOut);
            } catch (ServerResponseException e) {
                if (utils.debugMode)
                    e.printStackTrace(utils.debugOut);
            }
            client = null;
        }
    }

    public boolean isAdminMode() {
        return adminMode;
    }

    public void setAdminMode(boolean adminMode, Window owner) {
        this.adminMode = adminMode;
        if (client != null) 
        {
            client = null;
            forceLogin(owner);
        }
    }
}
