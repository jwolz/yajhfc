package yajhfc.util;
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

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;

import yajhfc.Utils;

// For use with a number model that limits the number of seconds shown.
public class SpinnerDateOffsetEditor extends javax.swing.JSpinner.DefaultEditor {
    
    protected static class DateOffsetFormatter extends JFormattedTextField.AbstractFormatter {
        /**
         * Number of columns used in the format:
         */
        public static final int columns = 10;
        
        @Override
        public Object stringToValue(String text) throws ParseException {
            int hours, minutes = 0, seconds = 0, secpos, sign;
            
            int pos = text.indexOf(':');
            
            if (pos < 0) 
                pos = text.length();
            
            try {
                char c = text.charAt(0);
                if (c == '+') {
                    sign = 1;
                    hours = Integer.parseInt(text.substring(1, pos));
                } else if (c == '-') {
                    sign = -1;
                    hours = Integer.parseInt(text.substring(1, pos));
                } else {
                    sign = 1;
                    hours = Integer.parseInt(text.substring(0, pos));
                }
            } catch (NumberFormatException ex) {
                throw new ParseException("Invalid hour value", 0);
            }
            if (pos < text.length()) {
                secpos = text.indexOf(':', pos+1);
                if (secpos < 0) 
                    secpos = text.length();
                
                try {
                    minutes = Integer.parseInt(text.substring(pos + 1, secpos));
                } catch (NumberFormatException ex) {
                    throw new ParseException("Invalid minutes value", pos+1);
                }
                if (secpos < text.length()) {
                    try {
                        seconds = Integer.parseInt(text.substring(secpos + 1));
                    } catch (NumberFormatException ex) {
                        throw new ParseException("Invalid seconds value", secpos+1);
                    }
                }
            }
            return sign * (((hours * 60) + minutes) * 60 + seconds);
        }
        
        @Override
        public String valueToString(Object value) throws ParseException {
            int val = (Integer)value;
            int seconds = Math.abs(val);
            
            int minutes = seconds / 60;
            seconds %= 60;
            if (seconds < 0)
                seconds += 60;
            
            int hours = minutes / 60;
            minutes %= 60;
            if (minutes < 0) 
                minutes += 60;
            
            return String.format(Utils.getLocale(), "%c%02d:%02d:%02d", (val >= 0) ? '+' : '-', hours, minutes, seconds);
        }    
      
    }
    

    
    public SpinnerDateOffsetEditor(JSpinner spinner) {
        super(spinner);
        JFormattedTextField ftf = getTextField();
        ftf.setFormatterFactory(new DefaultFormatterFactory(new DateOffsetFormatter()));
        //ftf.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter()));
        ftf.setEditable(true);
        ftf.setColumns(DateOffsetFormatter.columns);
    }

    /**
     * Returns a number model for input of offsets between -24 and +24 hours.
     * @return A number model.
     */
    public static SpinnerModel getDefaultModel() {
        return new SpinnerNumberModel(0, -24*3600, 24*3600, 3600);
    }
    
    /**
     * Returns a JSpinner using this Editor and a 
     * fitting number model.
     * @return
     */
    public static JSpinner createJSpinner() {
        JSpinner rv = new JSpinner(getDefaultModel());
        rv.setEditor(new SpinnerDateOffsetEditor(rv));
        return rv;
    }
}
