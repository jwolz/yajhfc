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
 */

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

public class IntVerifier extends InputVerifier {
    public int min;
    public int max;
    public boolean allowEmpty;
    
    @Override
    public boolean verify(JComponent input) {
        String text = ((JTextComponent)input).getText();
        if (text == null || text.length() == 0) {
        	return allowEmpty;
        }
        try {
			int val = Integer.parseInt(text);
            return ((val >= min) && (val <= max));
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public IntVerifier() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    public IntVerifier(int min, int max) {
    	this(min,max,false);
    }
    
    public IntVerifier(int min, int max, boolean allowEmpty) {
        super();
        this.min = min;
        this.max = max;
        this.allowEmpty = allowEmpty;
    }
}
