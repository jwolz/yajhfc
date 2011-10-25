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
package yajhfc.file;

/**
 * @author jonas
 *
 */
public class TIFFDitherMultiFileConverter extends TIFFMultiFileConverter {

    private static final String[] altAdditionalGSParams;
    static {
        // Build the gs params by appending "our" params to that of the super class
        String[] myargs = {
                "stocht.ps",
                "-c",
                "<< /HalftoneMode 1 >> setuserparams",
        };
        altAdditionalGSParams = new String[myargs.length+additionalGSParams.length];
        for (int i=0; i<altAdditionalGSParams.length; i++) {
            int j = i-additionalGSParams.length;
            if (j<0) {
                altAdditionalGSParams[i] = additionalGSParams[i];
            } else {
                altAdditionalGSParams[i] = myargs[j];
            }
        }
    }
    
    @Override
    protected String[] getAdditionalGSParams() {
        return altAdditionalGSParams;
    }
}
