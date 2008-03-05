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
package yajhfc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores the read state of faxes in a local file.
 * @author jonas
 *
 */
public class LocalPersistentReadState extends PersistentReadState {

    protected String fileName;
    
    public LocalPersistentReadState(String fileName) {
        this.fileName = fileName;
    }
    
    /* (non-Javadoc)
     * @see yajhfc.PersistentReadState#loadReadFaxes()
     */
    @Override
    public Set<String> loadReadFaxes() {
        HashSet<String> oldRead = new HashSet<String>();
        
        try {
            BufferedReader bIn = new BufferedReader(new FileReader(fileName));
            
            String line = null;
            while ((line = bIn.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && line.length() > 0) {
                    oldRead.add(line);
                }
            }
            bIn.close();
        } catch (FileNotFoundException e) { 
            // No file yet - keep empty
        } catch (IOException e) {
            utils.printWarning("Error reading read status: ", e);
        }
        
        return oldRead;
    }

    /* (non-Javadoc)
     * @see yajhfc.PersistentReadState#persistReadState(java.util.Collection)
     */
    @Override
    public void persistReadState(Collection<String> readFaxes) {
        try {
            BufferedWriter bOut = new BufferedWriter(new FileWriter(fileName));
            
            bOut.write("# " + utils.AppShortName + " " + utils.AppVersion + " configuration file\n");
            bOut.write("# This file contains a list of faxes considered read\n\n");
            
            for ( String fax : readFaxes ) {
                bOut.write(fax);
                bOut.write('\n');
            }
            bOut.close();
        } catch (IOException e) {
            utils.printWarning("Error storing read state: ", e);
        }
    }

}
