/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2018 Jonas Wolz <info@yajhfc.de>
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
package yajhfc.options;

import javax.swing.JLabel;
import javax.swing.JTextField;

import info.clearthought.layout.TableLayout;
import yajhfc.FaxOptions;
import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class PhoneNumberPanel extends AbstractOptionsPanel<FaxOptions> {

    JTextField textLongDistancePrefix, textIntlPrefix, textAreaCode, textCountryCode;
    
    public PhoneNumberPanel() {
        super(false);
    }

    public void loadSettings(FaxOptions foEdit) {
        textLongDistancePrefix.setText(foEdit.longDistanceCallPrefix);
        textIntlPrefix.setText(foEdit.internationalCallPrefix);
        textAreaCode.setText(foEdit.areaCode);
        textCountryCode.setText(foEdit.countryCode);
    }

    public void saveSettings(FaxOptions foEdit) {
        foEdit.longDistanceCallPrefix = textLongDistancePrefix.getText();
        foEdit.internationalCallPrefix = textIntlPrefix.getText();
        foEdit.areaCode = textAreaCode.getText();
        foEdit.countryCode = textCountryCode.getText();
    }

    @Override
    protected void createOptionsUI() {
        double[][] tablelay = {
                {OptionsWin.border, 0.5, OptionsWin.border, TableLayout.FILL, OptionsWin.border},
                {OptionsWin.border, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.PREFERRED, TableLayout.PREFERRED, OptionsWin.border, TableLayout.FILL, OptionsWin.border }
        };
        this.setLayout(new TableLayout(tablelay));
        
        textLongDistancePrefix = new JTextField();
        textIntlPrefix = new JTextField();
        textAreaCode = new JTextField();
        textCountryCode = new JTextField();
        
        JLabel infoLabel = new JLabel("<html>" + Utils._("Here you can set information on how to translate phone numbers to canonic format (e.g. +49 1234 567890). Currently this is only used to look up names from the phone book.") + "</html>");
        
        add(infoLabel, "1,1,3,1,f,f");
        
        Utils.addWithLabel(this, textCountryCode, Utils._("Country Code"), "1,4");
        Utils.addWithLabel(this, textAreaCode, Utils._("Area Code"), "3,4");
        Utils.addWithLabel(this, textLongDistancePrefix, Utils._("Long Distance Call Prefix"), "1,7");
        Utils.addWithLabel(this, textIntlPrefix, Utils._("International Call Prefix"), "3,7");
    }

}
