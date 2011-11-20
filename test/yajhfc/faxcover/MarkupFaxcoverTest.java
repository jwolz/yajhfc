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
package yajhfc.faxcover;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import yajhfc.PaperSize;
import yajhfc.TestUtils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.phonebook.PBEntryField;
import yajhfc.phonebook.convrules.CompanyRule;
import yajhfc.phonebook.convrules.DefaultPBEntryFieldContainer;
import yajhfc.phonebook.convrules.LocationRule;
import yajhfc.phonebook.convrules.NameRule;
import yajhfc.phonebook.convrules.ZIPCodeRule;

/**
 * @author jonas
 *
 */
public class MarkupFaxcoverTest {

    private MarkupFaxcover faxcover;

    /**
     * Sets up the test fixture. 
     * (Called before every test case method.)
     */
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {
        faxcover = new MarkupFaxcover(null) {
            @Override
            protected void convertMarkupToHyla(File tempFile, OutputStream out)
                    throws IOException, ConversionException {
                throw new RuntimeException("Just a stub.");
            }
        };
        
        faxcover.comments = "Some comments: ÄÖÜ <>&";
        faxcover.coverDate = new Date(110, 10, 12, 13, 14, 15);
        faxcover.companyRule = CompanyRule.DEPARTMENT_COMPANY;
        faxcover.nameRule = NameRule.GIVENNAME_NAME;
        faxcover.locationRule = LocationRule.STREET_LOCATION.generateRule(ZIPCodeRule.ZIPCODE_LOCATION);
        
        faxcover.dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        faxcover.pageCount = 42;
        faxcover.pageSize = PaperSize.A4;
        faxcover.regarding = "Just a Test";
        
        faxcover.fromData = new DefaultPBEntryFieldContainer();
        faxcover.fromData.setField(PBEntryField.Comment, "Sender Comment");
        faxcover.fromData.setField(PBEntryField.Company, "Sender Company");
        faxcover.fromData.setField(PBEntryField.Country, "Sender Country");
        faxcover.fromData.setField(PBEntryField.Department, "Sender Department");
        faxcover.fromData.setField(PBEntryField.EMailAddress, "Sender EMailAddress");
        faxcover.fromData.setField(PBEntryField.FaxNumber, "Sender FaxNumber");
        faxcover.fromData.setField(PBEntryField.GivenName, "Sender GivenName");
        faxcover.fromData.setField(PBEntryField.Location, "Sender Location");
        faxcover.fromData.setField(PBEntryField.Name, "Sender Name");
        faxcover.fromData.setField(PBEntryField.Position, "Sender Position");
        faxcover.fromData.setField(PBEntryField.State, "Sender State");
        faxcover.fromData.setField(PBEntryField.Street, "Sender Street");
        faxcover.fromData.setField(PBEntryField.Title, "Sender Title");
        faxcover.fromData.setField(PBEntryField.VoiceNumber, "Sender VoiceNumber");
        faxcover.fromData.setField(PBEntryField.WebSite, "Sender WebSite");
        faxcover.fromData.setField(PBEntryField.ZIPCode, "Sender ZIP code");
        
        faxcover.toData = new DefaultPBEntryFieldContainer();
        faxcover.toData.setField(PBEntryField.Comment, "Recipient Comment");
        faxcover.toData.setField(PBEntryField.Company, "Recipient Company");
        faxcover.toData.setField(PBEntryField.Country, "Recipient Country");
        faxcover.toData.setField(PBEntryField.Department, "Recipient Department");
        faxcover.toData.setField(PBEntryField.EMailAddress, "Recipient EMailAddress");
        faxcover.toData.setField(PBEntryField.FaxNumber, "Recipient FaxNumber");
        faxcover.toData.setField(PBEntryField.GivenName, "Recipient GivenName");
        faxcover.toData.setField(PBEntryField.Location, "Recipient Location");
        faxcover.toData.setField(PBEntryField.Name, "Recipient Name");
        faxcover.toData.setField(PBEntryField.Position, "Recipient Position");
        faxcover.toData.setField(PBEntryField.State, "Recipient State");
        faxcover.toData.setField(PBEntryField.Street, "Recipient Street");
        faxcover.toData.setField(PBEntryField.Title, "Recipient Title");
        faxcover.toData.setField(PBEntryField.VoiceNumber, "Recipient VoiceNumber");
        faxcover.toData.setField(PBEntryField.WebSite, "Recipient WebSite");
        faxcover.toData.setField(PBEntryField.ZIPCode, "Recipient ZIP code");
    }

    
    /**
     * Test method for {@link yajhfc.faxcover.MarkupFaxcover#replaceTags(java.io.Reader, java.io.Writer)}.
     * @throws IOException 
     */
    @Test
    public void testReplaceTagsBasic() throws IOException {
        String[] testCase = TestUtils.readTestCase("test/yajhfc/faxcover/basictest.txt", true);
        StringWriter out = new StringWriter();
        faxcover.replaceTags(new StringReader(testCase[0]), out);
        
        Assert.assertEquals("Basic test failed:", testCase[1], out.toString());
    }

    /**
     * Test method for {@link yajhfc.faxcover.MarkupFaxcover#replaceTags(java.io.Reader, java.io.Writer)}.
     * @throws IOException 
     */
    @Test
    public void testReplaceTagsIf1() throws IOException {
        faxcover.fromData = new DefaultPBEntryFieldContainer("");
        
        String[] testCase = TestUtils.readTestCase("test/yajhfc/faxcover/iftest1.txt", false);
        StringWriter out = new StringWriter();
        faxcover.replaceTags(new StringReader(testCase[0]), out);
        
        Assert.assertEquals("IF test 1 failed:", testCase[1], out.toString());
    }

}
