/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2013 Jonas Wolz <info@yajhfc.de>
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
package yajhfc.model.servconn.directaccess.fritz;

import java.awt.Window;

import yajhfc.Utils;
import yajhfc.launch.Launcher2;
import yajhfc.model.RecvFormat;
import yajhfc.model.jobq.FTPHylaDirAccessor;
import yajhfc.model.jobq.HylaDirAccessor;
import yajhfc.model.servconn.hylafax.HylaFaxListConnection;
import yajhfc.model.servconn.hylafax.ManagedFaxJobList;
import yajhfc.server.ServerOptions;
import yajhfc.ui.YajOptionPane;
import yajhfc.util.PasswordDialog;

/**
 * @author jonas
 *
 */
public class FritzFaxListConnection extends HylaFaxListConnection {
    protected HylaDirAccessor hyda;
    private FritzFaxConfig config;
    
    public static String showConfigDialog(Window parent, String oldConfig) {
        FritzFaxConfig ffc = new FritzFaxConfig(oldConfig);
        if (FritzConfigDialog.showConfigDialog(parent, ffc))
            return ffc.saveToString();
        else
            return null;
    }
    
    public FritzFaxListConnection(ServerOptions fo, YajOptionPane dialogUI) {
        super(fo, dialogUI);
    }

    @Override
    protected ManagedFaxJobList<RecvFormat> createRecvdList() {
        return new FritzFaxList(this, fo.getParent().recvfmt, fo, null, getConfig().faxPattern, getConfig().faxDateFormat);
    }

    
    @Override
    public void setOptions(ServerOptions so) {
        super.setOptions(so);
        config = null;
        hyda = null;
    }

    
    public HylaDirAccessor getDirAccessor() {
        if (hyda==null) {
            final FritzFaxConfig ffc = getConfig();
            String password, user;
            user = ffc.user;
            if (ffc.alwaysAsk) {
                String[] res = PasswordDialog.showPasswordDialog(Launcher2.application.getFrame(), Utils._("Fritz!Box connection"), Utils._("Please enter user name and password to connect to the Fritz!Box"), user, true);
                if (res == null) {
                    password = "";
                } else {
                    user = res[0];
                    password = res[1];
                }
            } else {
                password = ffc.pass.getPassword();
            }
            hyda = new FTPHylaDirAccessor(ffc.hostname, ffc.port, user, password, ffc.faxboxDir, ffc.passive);
        }
        return hyda;
    }

    /**
     * @return the config
     */
    protected FritzFaxConfig getConfig() {
        if (config==null) {
            config = new FritzFaxConfig(this.fo.connectionConfig);
        }
        return config;
    }


}
