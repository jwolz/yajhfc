package yajhfc.phonebook;
/*
 * YAJHFC - Yet another Java Hylafax client
 * Copyright (C) 2005-2007 Jonas Wolz
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

public class PhoneBookException extends Exception {
    
    private boolean alreadyDisplayed;
    
    public boolean messageAlreadyDisplayed() {
        return alreadyDisplayed;
    }
    
    public PhoneBookException(String message, boolean alreadyDisplayed) {
        super(message);
        this.alreadyDisplayed = alreadyDisplayed;
    }

    public PhoneBookException(Throwable cause, boolean alreadyDisplayed) {
        super(cause.getMessage(), cause);
        this.alreadyDisplayed = alreadyDisplayed;
    }

    public PhoneBookException(String message, Throwable cause, boolean alreadyDisplayed) {
        super(message, cause);
        this.alreadyDisplayed = alreadyDisplayed;
    }

}
