/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2011 Jonas Wolz
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
 */
package yajhfc.report;

import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.Attribute;

import yajhfc.AbstractFaxOptions;
import yajhfc.model.JobFormat;
import yajhfc.model.RecvFormat;

/**
 * Example class to save options in the YajHFC settings file if you need to.
 * 
 * To load/save options, you just need to create a subclass of AbstractFaxOptions
 * and add public fields for your options to it.
 * 
 * Then you can call the loadFromProperties/storeToProperties methods 
 * to load/store them to a properties file (e.g. the one returned
 * by Utils.getSettingsProperties()).
 * @author jonas
 *
 */
public class ReportOptions extends AbstractFaxOptions {    
    public final List<RecvFormat> reportRecvColumns = new ArrayList<RecvFormat>();
    public final List<JobFormat> reportSentColumns = new ArrayList<JobFormat>();

    public boolean reportPrintAllPages = true;
    public int reportPageFrom = 1;
    public int reportPageTo = 1;
    public boolean reportUnlimitedThumbs = true;
    public int reportThumbsPerPage = 4;
    public boolean reportViewAfterGeneration = true;
    
    /**
     * The attributes used for printing
     */
    public Attribute[] printAttributes = null;
    
	/**
	 * Call the super constructor with the prefix that should be prepended
	 * to the options name.
	 */
	public ReportOptions() {
		super("sendreport");
	}
}
