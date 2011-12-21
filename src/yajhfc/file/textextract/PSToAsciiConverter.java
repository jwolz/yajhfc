package yajhfc.file.textextract;

import java.io.File;
import java.util.List;

import yajhfc.FaxOptions;
import yajhfc.Utils;
import yajhfc.file.FileFormat;

public class PSToAsciiConverter extends ExternalCommandToTextConverter {
    
    public PSToAsciiConverter() {
        this(Utils.getFaxOptions());
    }
    
    public PSToAsciiConverter(FaxOptions options) {
        super("ISO8859-1", false, options);
    }
    
    @Override
    protected void buildCommandLine(List<String> commandLine, File[] inputFiles) {
        String gsPath = options.ghostScriptLocation;
        
        commandLine.add(gsPath);
        commandLine.add("-q");
        commandLine.add("-dNODISPLAY");
        commandLine.add("-P-");
        commandLine.add("-dSAFER");
        commandLine.add("-dDELAYBIND");
        commandLine.add("-dWRITESYSTEMDICT");
        commandLine.add("-dSIMPLE");
        commandLine.add("-c");
        commandLine.add("save");
        commandLine.add("-f");
        commandLine.add("ps2ascii.ps");
        for (File file : inputFiles) {
            commandLine.add(file.getAbsolutePath());
        }
        commandLine.add("-c");
        commandLine.add("quit");
    }

    @Override
    public String getDescription() {
        return "pstoascii (GhostScript)";
    }

    @Override
    public FileFormat[] getAllowedInputFormats() {
        return new FileFormat[] { FileFormat.PostScript, FileFormat.PDF };
    }

}
