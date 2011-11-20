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
import java.net.URL;

import yajhfc.file.EditorPaneFileConverter;
import yajhfc.file.FileConverter.ConversionException;

/**
 * @author jonas
 *
 */
public class HTMLFaxcover extends MarkupFaxcover {

    /**
     * @param coverTemplate
     */
    public HTMLFaxcover(URL coverTemplate) {
        super(coverTemplate);
        encodeNonASCIIAsEntity = true;
        newLineReplacement = "<br>";
        aposReplacement = "'";
        encoding = "iso-8859-1";
    }

    /* (non-Javadoc)
     * @see yajhfc.faxcover.MarkupFaxcover#convertMarkupToHyla(java.io.File, java.io.OutputStream)
     */
    @Override
    protected void convertMarkupToHyla(File tempFile, OutputStream out)
            throws IOException, ConversionException {
        URL inURL = tempFile.toURI().toURL();

        EditorPaneFileConverter.HTML_CONVERTER.convertToHylaFormat(inURL, out, pageSize, coverTemplate);
    }
    
//    // Testing code:
//    public static void main(String[] args) throws Exception {
//        System.out.println("Creating cover page...");
//        Faxcover cov = new HTMLFaxcover(new URL("file:/home/jonas/java/yajhfc/extra/cover/Coverpage example.html"));
//
//        cov.comments = "foo\niniun iunuini uinini <tag> ninuin iuniuniu 9889hz h897h789 bnin uibiubui ubuib uibub ubiu bib bib ib uib i \nbar";
//        cov.fromCompany = "foo Ü&Ö OHG";
//        cov.fromFaxNumber = "989898";
//        cov.fromLocation = "Bardorf";
//        cov.fromVoiceNumber = "515616";
//        cov.fromMailAddress = "a@bc.de";
//
//
//        cov.pageCount = 55;
//        cov.pageSize = Utils.papersizes[0];
//        cov.regarding = "Test fax";
//        cov.sender = "Werner Meißner";
//
//        cov.toCompany = "Bâr GmbH & Co. KGaA";
//        cov.toFaxNumber = "87878787";
//        cov.toLocation = "Foostädtle";
//        cov.toName = "Otto Müller";
//        cov.toVoiceNumber = "4545454";
//
//        try {
//            String outName = "/tmp/testHTML.ps";
//            cov.makeCoverSheet(new FileOutputStream(outName));
//            Runtime.getRuntime().exec(new String[] { "gv", outName } );
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

}
