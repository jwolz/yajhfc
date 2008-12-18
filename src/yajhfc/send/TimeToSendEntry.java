/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2008 Jonas Wolz
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

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

import yajhfc.DateKind;
import yajhfc.Utils;

/**
 * @author jonas
 *
 */
public class TimeToSendEntry extends JPanel {
    public enum TTSType {
        NOW(Utils._("Now")),
        IN_MINUTES(Utils._("In (timespan)")),
        EXACT_TIME(Utils._("At (exact time)"));
        
        private final String description;
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return description;
        }
        
        private TTSType(String desc) {
            this.description = desc;
        }
    }
    
    protected JComboBox comboTTSType;
    protected JSpinner spinMinutes;
    protected JFormattedTextField spinnerTextField;
    protected AbstractFormatterFactory nowFactory = new DefaultFormatterFactory(new AbstractFormatter() {
        @Override
        public Object stringToValue(String text) throws ParseException {
            return new Date();
        }
        @Override
        public String valueToString(Object value) throws ParseException {
            return Utils._("Now");
        }
        
    });
    protected AbstractFormatterFactory dateFactory = new DefaultFormatterFactory(new DateFormatter(DateKind.getInstanceFromKind(DateKind.DATE_AND_TIME)));
    protected AbstractFormatterFactory minutesFactory; 
    protected long startTime;
    
    /**
     * Return the selected date or null if the user selected send "Now"
     * @return
     */
    public Date getSelection() {
        switch ((TTSType)comboTTSType.getSelectedItem()) {
        case NOW:
        default:
            return null;
        case EXACT_TIME:
            return (Date)spinMinutes.getValue();
        case IN_MINUTES:
            return new Date(System.currentTimeMillis() - startTime + ((Date)spinMinutes.getValue()).getTime());
        }
    }
    
    
    void configureForItem(TTSType item) {
        switch (item) {
        case NOW:
            spinMinutes.setEnabled(false);
            spinnerTextField.setFormatterFactory(nowFactory);
            break;
        case IN_MINUTES:
            spinMinutes.setEnabled(true);
            spinnerTextField.setFormatterFactory(minutesFactory);
            break;
        case EXACT_TIME:
            spinMinutes.setEnabled(true);
            spinnerTextField.setFormatterFactory(dateFactory);
            break;
        }
    }
    
    /**
     * 
     */
    public TimeToSendEntry() {
        super(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        comboTTSType = new JComboBox(TTSType.values());
        comboTTSType.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                configureForItem((TTSType)e.getItem());
            }
        });
        
        // Model time is in seconds
        Date startDate = new Date(startTime = ((System.currentTimeMillis() + 30000) / 60000) * 60000);
        minutesFactory = new DefaultFormatterFactory(new MinutesFormatter(startDate));
        spinMinutes = new JSpinner(new SpinnerDateModel(startDate, startDate, null, Calendar.MINUTE));
        JSpinner.DefaultEditor editor = new JSpinner.DefaultEditor(spinMinutes);
        spinnerTextField = editor.getTextField();
        spinnerTextField.setEditable(true);
        spinMinutes.setEditor(editor);
        
        comboTTSType.setMaximumSize(comboTTSType.getPreferredSize());
        add(comboTTSType);
        add(Box.createRigidArea(new Dimension(6,6)));
        add(spinMinutes);
        
        comboTTSType.setSelectedItem(TTSType.NOW);
        spinMinutes.setEnabled(false);
        configureForItem(TTSType.NOW);
    }


}
