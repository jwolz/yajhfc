package yajhfc.file.textextract;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yajhfc.FaxOptions;
import yajhfc.PlatformInfo;
import yajhfc.Utils;
import yajhfc.file.FileFormat;

public class PSToTextConverter extends ExternalCommandToTextConverter {
    private static final Logger log = Logger.getLogger(PSToTextConverter.class.getName());

    public PSToTextConverter() {
        this(Utils.getFaxOptions());
    }
    
    public PSToTextConverter(FaxOptions options) {
        super("ISO8859-1", true, options);
    }
    
    @Override
    protected void buildCommandLine(List<String> commandLine, File[] inputFiles) {
        File gsFile = new File(options.ghostScriptLocation);
        String pstotextPath = options.pstotextPath;
        
        commandLine.add(pstotextPath);
        commandLine.add("-gs");
        commandLine.add(gsFile.getName());
        for (File file : inputFiles) {
            commandLine.add(file.getAbsolutePath());
        }
    }
    
    @Override
    protected void modifyEnvironment(Map<String, String> env) {
        File gsFile = new File(options.ghostScriptLocation).getAbsoluteFile();
        
        // Append ghostscript directory to path
        String pathKey = "PATH";
        if (PlatformInfo.IS_WINDOWS) {
            // Find the key for PATH, as it might be written "Path" or so
            for (String key : env.keySet()) {
                if (pathKey.equalsIgnoreCase(key)) {
                    pathKey = key;
                    break;
                }
            }
        } 
        String newPath = env.get(pathKey) + File.pathSeparator + gsFile.getParent();
        if (Utils.debugMode)
            log.fine("New PATH is: " + newPath);
        env.put(pathKey, newPath);
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
