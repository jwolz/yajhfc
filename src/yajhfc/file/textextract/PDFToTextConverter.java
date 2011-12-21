package yajhfc.file.textextract;

import java.io.File;
import java.util.List;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.file.FileFormat;

public class PDFToTextConverter extends ExternalCommandToTextConverter {

    public PDFToTextConverter() {
        this(Utils.getFaxOptions());
    }
    
    public PDFToTextConverter(FaxOptions options) {
        super("UTF-8", false, options);
    }
    
    @Override
    protected void buildCommandLine(List<String> commandLine, File[] inputFiles) {
        String pdftotextPath = options.pdftotextPath; 
        
        commandLine.add(pdftotextPath);
        commandLine.add("-enc");
        commandLine.add("UTF-8");
        commandLine.add(inputFiles[0].getAbsolutePath());
        commandLine.add("-");
    }

    @Override
    public String getDescription() {
        return "pdftotext";
    }

    @Override
    public FileFormat[] getAllowedInputFormats() {
        return new FileFormat[] { FileFormat.PDF };
    }

}
