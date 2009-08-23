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
package yajhfc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

/**
 * @author jonas
 *
 */
public final class TestUtils {

    /**
     * Reads a test case from the specified file
     * @param fileName
     * @param trim trim spaces on every line
     * @return the test cases as Strings: Element 0 is the input, Element 1 the expected output
     * @throws IOException 
     */
    public static String[] readTestCase(String fileName, boolean trim) throws IOException {
        BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        int lineCount = 0;
        String line;
        StringBuilder[] out = { new StringBuilder(), new StringBuilder() };
        
        while ((line = inReader.readLine()) != null) {
            if (!line.startsWith("#")) {
                if (trim) {
                    line = line.trim();
                }
                out[lineCount % 2].append(line);
                out[lineCount % 2].append('\n');
                lineCount++;
            }
        }
        inReader.close();
       
        return new String[] { out[0].toString(), out[1].toString()};
    }
    
    
    public static void compareTestCases(String msgPrefix, String input, String expectedOutput, String actualOutput) {
        String[] inputLines = Utils.fastSplit(input, '\n');
        String[] expectedLines = Utils.fastSplit(expectedOutput, '\n');
        String[] actualLines = Utils.fastSplit(actualOutput, '\n');
        
        Assert.assertEquals("Invalid input", inputLines.length, expectedLines.length);
        Assert.assertEquals("Number of output lines is wrong", expectedLines.length, actualLines.length);
        
        for (int i = 0; i < Math.min(inputLines.length, Math.min(expectedLines.length, actualLines.length)); i++) {
            Assert.assertEquals(msgPrefix + inputLines[i], expectedLines[i], actualLines[i]);
        }
    }
}
