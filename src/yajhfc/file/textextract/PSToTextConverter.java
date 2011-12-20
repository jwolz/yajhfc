package yajhfc.file.textextract;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.file.FileFormat;

public class PSToTextConverter extends ExternalCommandToTextConverter {
    private static final Logger log = Logger.getLogger(PSToTextConverter.class.getName());

    public PSToTextConverter() {
        streamEncoding = "ISO8859-1";
        acceptsMultipleFiles = true;
    }
    
    @Override
    protected void buildCommandLine(List<String> commandLine, File[] inputFiles) {
        File gsFile = new File(Utils.getFaxOptions().ghostScriptLocation);
        String pstotextPath = Utils.getFaxOptions().pstotextPath;
        
        commandLine.add(pstotextPath);
        commandLine.add("-gs");
        commandLine.add(gsFile.getName());
        for (File file : inputFiles) {
            commandLine.add(file.getAbsolutePath());
        }
    }
    
    @Override
    protected void modifyEnvironment(Map<String, String> env) {
        File gsFile = new File(Utils.getFaxOptions().ghostScriptLocation).getAbsoluteFile();
        
        // Append ghostscript directory to path
        String newPath = env.get("PATH") + File.pathSeparator + gsFile.getParent();
        if (Utils.debugMode)
            log.fine("New PATH is: " + newPath);
        env.put("PATH", newPath);
    }

    @Override
    public String getDescription() {
        return "pstotext";
    }

    @Override
    public FileFormat[] getAllowedInputFormats() {
        return new FileFormat[] { FileFormat.PostScript, FileFormat.PDF };
    }

}
