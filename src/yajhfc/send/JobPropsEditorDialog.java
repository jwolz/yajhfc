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
package yajhfc.send;

import java.awt.Dialog;
import java.util.Map;


import yajhfc.Utils;
import yajhfc.util.MapEditorDialog;

/**
 * @author jonas
 *
 */
public class JobPropsEditorDialog extends MapEditorDialog {
    public JobPropsEditorDialog(Dialog owner, Map<String,String> mapToEdit) {
        super(owner, Utils._("Job properties"), mapToEdit);
    }

    /**
     * Available job properties as listed by the "JPARM" command
     */
    static final String[] availableProperties = {
        "BEGBR",
        "BEGST",
        "CHOPTHRESHOLD",
        "CLIENT",
        "COMMENTS",
        "COMMID",
        "DATAFORMAT",
        "DIALSTRING",
        "DONEOP",
        "EXTERNAL",
        "FAXNUMBER",
        "FROMCOMPANY",
        "FROMLOCATION",
        "FROMUSER",
        "FROMVOICE",
        "GROUPID",
        "JOBID",
        "JOBINFO",
        "JOBTYPE",
        "LASTTIME",
        "MAXDIALS",
        "MAXTRIES",
        "MINBR",
        "MODEM",
        "NDIALS",
        "NOTIFYADDR",
        "NOTIFY",
        "NPAGES",
        "NTRIES",
        "OWNER",
        "PAGECHOP",
        "PAGELENGTH",
        "PAGEWIDTH",
        "PASSWD",
        "REGARDING",
        "RETRYTIME",
        "SCHEDPRI",
        "SENDTIME",
        "STATE",
        "STATUS",
        "STATUSCODE",
        "SUBADDR",
        "TAGLINE",
        "TOCOMPANY",
        "TOLOCATION",
        "TOTDIALS",
        "TOTPAGES",
        "TOTTRIES",
        "TOUSER",
        "TOVOICE",
        "TSI",
        "USECONTCOVER",
        "USEECM",
        "USETAGLINE",
        "USEXVRES",
        "USRKEY",
        "VRES",
    };

    /**
     * @return the available Properties to edit
     */
    protected String[] getAvailableProperties() {
        return availableProperties;
    }

    protected String getCaption() {
        return Utils._("This dialog allows you to set HylaFAX job properties directly. Use it only if you know what you are doing!");
    }
}
