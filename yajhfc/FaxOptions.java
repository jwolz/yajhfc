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

import java.awt.Point;
import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import yajhfc.send.SendWinStyle;

public class FaxOptions {
    public FaxStringProperty tzone = null;
    public String host;
    public int port;
    public boolean pasv;
    public String user;
    public String pass;
    
    public final FmtItemList recvfmt;
    public final FmtItemList sentfmt;
    public final FmtItemList sendingfmt;
    
    public String faxViewer;
    public String psViewer;
    
    public String notifyAddress;
    public FaxStringProperty notifyWhen = null;
    public FaxIntProperty resolution = null;
    public PaperSize paperSize = null;
    
    public int maxTry; 
    public int maxDial;
    public int killTime = 180;
    
    public Rectangle mainWinBounds, phoneWinBounds, customFilterBounds = null;
    public Point sendWinPos, optWinPos;
    public String recvColState, sentColState, sendingColState/*, recvReadState*/;
    public int mainwinLastTab;
    
    public int statusUpdateInterval, tableUpdateInterval;
    
    //public String lastPhonebook;
    
    public String FromFaxNumber;
    public String FromVoiceNumber;
    public String FromName;
    public String FromLocation;
    public String FromCompany;
    public String CustomCover;
    public String FromEMail = "";
    public boolean useCover, useCustomCover;
    
    //public FaxIntProperty newFaxAction = utils.newFaxActions[3];
    public int newFaxAction = utils.NEWFAX_BEEP | utils.NEWFAX_TOFRONT;
    public boolean pclBug = false;
    public boolean askPassword = false, askAdminPassword = true;
    public String AdminPassword = "";
    
    public String recvFilter = null, sentFilter = null, sendingFilter = null;
    
    public String defaultCover = null;
    public boolean useCustomDefaultCover = false;
    
    public YajLanguage locale = utils.AvailableLocales[0];
    
    // Offset for displayed date values in seconds:
    public int dateOffsetSecs = 0;
    
    public boolean preferRenderedTIFF = false;
    public boolean markFailedJobs = true;
    public boolean showRowNumbers = false;
    public boolean adjustColumnWidths = true;
    
    public String lookAndFeel = LOOKANDFEEL_SYSTEM;
    public static final String LOOKANDFEEL_SYSTEM = "!system!";
    public static final String LOOKANDFEEL_CROSSPLATFORM = "!crossplatform!"; 
    
    public ArrayList<String> phoneBooks = new ArrayList<String>();
    public int lastSelectedPhonebook = 0;
    
    public boolean useDisconnectedMode = false;
    public String defaultModem = "any";
    public boolean regardingAsUsrKey = true;
    public String lastSavePath = "";
    
    public SendWinStyle sendWinStyle = SendWinStyle.SIMPLIFIED;
    
    public FaxOptions() {
        this.host = "";
        this.port = 4559;
        this.user = System.getProperty("user.name");
        this.pass = "";
        this.pasv = true;
        this.tzone = utils.timezones[0];
        
        this.recvfmt = new FmtItemList(utils.recvfmts, utils.requiredRecvFmts);
        this.recvfmt.add(utils.recvfmts[0]);  // Y
        this.recvfmt.add(utils.recvfmts[16]); // s
        this.recvfmt.add(utils.recvfmts[4]);  // e
        this.recvfmt.add(utils.recvfmts[6]);  // h
        this.recvfmt.add(utils.recvfmts[13]); // p
        this.recvfmt.add(utils.recvfmt_FileName);
        //this.recvFileCol = 2;
        this.recvColState = String.valueOf(this.recvfmt.indexOf(utils.recvfmt_FileName) + 1); // 0 means: no sorting
        
        this.sentfmt = new FmtItemList(utils.jobfmts, utils.requiredSentFmts);
        this.sentfmt.add(utils.jobfmts[40]); //o
        this.sentfmt.add(utils.jobfmts[30]); //e 
        this.sentfmt.add(utils.jobfmts[44]);       
        this.sentfmt.add(utils.jobfmts[45]);
        this.sentfmt.add(utils.jobfmt_JobID);
        this.sentfmt.add(utils.jobfmt_Jobstate);
        this.sentfmt.add(utils.jobfmts[51]);
        
        this.sendingfmt = new FmtItemList(utils.jobfmts, utils.requiredSendingFmts);
        this.sendingfmt.addAll(this.sentfmt);
        
        this.sentColState = this.sendingColState = "";
        
        String sysname = System.getProperty("os.name");
        if (sysname.startsWith("Windows")) {
            this.psViewer = "rundll32.exe URL.DLL,FileProtocolHandler \"%s\"";//"gsview32.exe";
            if (sysname.indexOf("XP") >= 0 || sysname.indexOf("Vista") >= 0) 
                this.faxViewer = "rundll32.exe shimgvw.dll,ImageView_Fullscreen %s";
            else
                this.faxViewer = "rundll32.exe URL.DLL,FileProtocolHandler \"%s\"";//"kodakimg.exe";
        } else {
            Map<String,String> env = System.getenv();
            if ("true".equals(env.get("KDE_FULL_SESSION"))) {
                this.faxViewer = "kfmclient exec %s";
                this.psViewer = "kfmclient exec %s";
            } else {
                String gnome = env.get("GNOME_DESKTOP_SESSION_ID");
                if (gnome != null && gnome.length() > 0) {
                    this.faxViewer = "gnome-open %s";
                    this.psViewer = "gnome-open %s";
                } else {
                    this.faxViewer = "kfax %s";
                    this.psViewer = "gv %s";                    
                }
            }
        }
        
        this.resolution = utils.resolutions[0];
        this.paperSize = utils.papersizes[0];
        this.notifyWhen = utils.notifications[2];
        this.notifyAddress = this.user +  "@localhost"; //+ this.host;
        this.maxTry = 6;
        this.maxDial = 12;  
        
        mainWinBounds = null;
        optWinPos = null;
        sendWinPos = null;
        phoneWinBounds = null;
        mainwinLastTab = 0;
        
        statusUpdateInterval = 2000;
        tableUpdateInterval = 6000;
        //recvReadState = "";
        
        //lastPhonebook = "";
        
        FromFaxNumber = "";
        FromVoiceNumber = "";
        FromName = user;
        FromLocation = "";
        FromCompany = "";
        
        useCover = false;
        useCustomCover = false;
        CustomCover = "";
    }
    
    
    private static final char sep = '|';
    //private static final String sepregex = "\\|";
    
    @SuppressWarnings("unchecked")
    public void storeToFile(String fileName) {
        Properties p = new Properties();
        java.lang.reflect.Field[] f = FaxOptions.class.getFields();
        
        for (int i = 0; i < f.length; i++) {
            try {
                if (Modifier.isStatic(f[i].getModifiers())) // || Modifier.isFinal(f[i].getModifiers()))
                    continue;
                
                Object val = f[i].get(this);
                if (val == null)
                    continue;
                
                String name = f[i].getName();
                if ((val instanceof String) || (val instanceof Integer) || (val instanceof Boolean))
                    p.setProperty(name, val.toString());
                else if (val instanceof MyManualMapObject) 
                    p.setProperty(name, ((MyManualMapObject)val).getKey().toString());
                else if (val instanceof FmtItemList) {
                    p.setProperty(name, ((FmtItemList)val).saveToString());
                } else /*if (val instanceof Vector) {
                    Vector vec = (Vector)val;
                    StringBuilder saveval = new StringBuilder();
                    for (int j = 0; j < vec.size(); j++) 
                        saveval.append(((FmtItem)vec.get(j)).fmt).append(sep);
                    p.setProperty(name, saveval.toString());
                } else*/ if (val instanceof Rectangle) {
                    Rectangle rval = (Rectangle)val;
                    p.setProperty(name, "" + rval.x + sep + rval.y + sep + rval.width + sep + rval.height);
                } else if (val instanceof Point) {
                    Point pval = (Point)val;
                    p.setProperty(name, "" + pval.x + sep + pval.y);
                } else if (val instanceof List) {
                    List lst = (List)val;
                    int idx = 0;
                    for (Object o : lst) {
                        p.setProperty(name + "." + (++idx), (String)o);
                    }
                } else if (val instanceof Enum) {
                    p.setProperty(name, ((Enum)val).name());
                } else {
                    utils.printWarning("Unknown field type " + val.getClass().getName());
                }
            } catch (Exception e) {
                utils.printWarning("Exception reading field: ", e);
            }
        }
        
        try {
            FileOutputStream filout = new FileOutputStream(fileName);
            p.store(filout, utils.AppShortName + " " + utils.AppVersion + " configuration file");
            filout.close();
        } catch (Exception e) {
            utils.printWarning("Couldn't save file '" + fileName + "': ", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadFromFile(String fileName) {
        if (utils.debugMode) {
            utils.debugOut.println("Loading prefs from " + fileName);
        }
        Properties p = new Properties();
        //System.err.println(fileName);
        try {
            FileInputStream filin = new FileInputStream(fileName);
            p.load(filin);
            filin.close();
        } catch (FileNotFoundException e) {
            return; // No file yet
        } catch (IOException e) {
            utils.printWarning("Error reading file '" + fileName + "': " , e);
            return;
        }
        if (utils.debugMode) {
            utils.debugOut.println("---- BEGIN preferences dump");
            utils.dumpProperties(p, utils.debugOut, "pass", "AdminPassword");
            utils.debugOut.println("---- END preferences dump");
        }
        // Clear all lists:
        phoneBooks.clear();
        
        Enumeration<?> e = p.propertyNames();
        while (e.hasMoreElements()) {
            String propName = (String)e.nextElement();
            
            // Special case for old "lastPhonebook" property
            if (propName.equals("lastPhonebook")) {
                phoneBooks.add(p.getProperty(propName));
                continue;
            }
            
            try {
                String fName;
                int pntIdx = propName.indexOf('.');
                if (pntIdx >= 0) { //Lists
                    fName = propName.substring(0, pntIdx); // Cut off parts right of point
                } else {
                    fName = propName;
                }
                
                Field f = FaxOptions.class.getField(fName);
                Class<?> fcls = f.getType();
                if (String.class.isAssignableFrom(fcls))
                    f.set(this, p.getProperty(fName));
                else if (Integer.TYPE.isAssignableFrom(fcls))
                    f.setInt(this, Integer.parseInt(p.getProperty(fName)));
                else if (Boolean.TYPE.isAssignableFrom(fcls))
                    f.setBoolean(this, Boolean.parseBoolean(p.getProperty(fName)));
                else if (MyManualMapObject.class.isAssignableFrom(fcls)) {
                    MyManualMapObject[] dataarray;
                    
                    if (fName.equals("tzone")) 
                        dataarray = utils.timezones;
                    else if (fName.equals("notifyWhen"))
                        dataarray = utils.notifications;
                    else if (fName.equals("resolution"))
                        dataarray = utils.resolutions;
                    else if (fName.equals("paperSize"))
                        dataarray = utils.papersizes;
                    /*else if (fName.equals("newFaxAction"))
                        dataarray = utils.newFaxActions;*/
                    else if (fName.equals("locale"))
                        dataarray = utils.AvailableLocales;
                    else {
                        utils.printWarning("Unknown MyManualMapObject field: " + fName);
                        continue;
                    }
                    Object res = utils.findInArray(dataarray, p.getProperty(fName));
                    if (res != null)
                        f.set(this, res);
                    else
                        utils.printWarning("Unknown value for MyManualMapObject field " + fName);
                }
                else if (FmtItemList.class.isAssignableFrom(fcls)) {
                    FmtItemList fim = (FmtItemList)f.get(this);
                    fim.loadFromString(p.getProperty(fName));
                } else /*if (Vector.class.isAssignableFrom(fcls)) {
                    String[] fields = utils.fastSplit(p.getProperty(fName), sep);
                    FmtItem[] dataarray, required;
                    Vector<FmtItem> vecres = new Vector<FmtItem>();
                    
                    if (fName.equals("recvfmt")) {
                        dataarray = utils.recvfmts;
                        required = utils.requiredRecvFmts;
                    } else if (fName.equals("sentfmt")) {
                        dataarray = utils.jobfmts;
                        required = utils.requiredSentFmts;
                    } else if (fName.equals("sendingfmt")) {
                        dataarray = utils.jobfmts;
                        required = utils.requiredSendingFmts;
                    } else {
                        System.err.println("Unknown vector field name: " + fName);
                        continue;
                    }
                    for (int i=0; i < fields.length; i++) {
                        FmtItem res = (FmtItem)utils.findInArray(dataarray, fields[i]);
                        if (res == null) 
                            System.err.println("FmtItem for " + fields[i] + "not found.");
                        else 
                            if (!vecres.contains(res))
                                vecres.add(res);
                    }
                    
                    utils.addUniqueToVec(vecres, required);
                    f.set(this, vecres);
                } else */ if (Rectangle.class.isAssignableFrom(fcls)) {
                    String [] v =  utils.fastSplit(p.getProperty(fName), sep);
                    f.set(this, new Rectangle(Integer.parseInt(v[0]), Integer.parseInt(v[1]), Integer.parseInt(v[2]), Integer.parseInt(v[3])));
                } else if (Point.class.isAssignableFrom(fcls)) {
                    String [] v =  utils.fastSplit(p.getProperty(fName), sep);
                    f.set(this, new Point(Integer.parseInt(v[0]), Integer.parseInt(v[1])));
                } else if (List.class.isAssignableFrom(fcls)) {
                    List lst = (List)f.get(this);
                    lst.add(p.getProperty(propName));
                } else if (Enum.class.isAssignableFrom(fcls)) {
                    f.set(this, Enum.valueOf((Class<? extends Enum>)fcls, p.getProperty(propName)));
                } else {
                    utils.printWarning("Unknown field type " + fcls.getName());
                }
            } catch (Exception e1) {
                utils.printWarning("Couldn't load setting for " + propName + ": ", e1);
            }
        }
    }
    
    public static String getDefaultConfigFileName() {
        return utils.getConfigDir() +  "settings";
    }
}
