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
