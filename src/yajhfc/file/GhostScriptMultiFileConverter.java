package yajhfc.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import yajhfc.PaperSize;
import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.util.ExternalProcessExecutor;

/**
 * A multi file converter using Ghost Script as filter
 * @author jonas
 *
 */
public abstract class GhostScriptMultiFileConverter extends MultiFileConverter {

    /**
     * The number of lines of error output to show in the message to the user
     */
    private static final int LINES_OF_ERROR_OUTPUT = 20;

    private static final Logger log = Logger.getLogger(PDFMultiFileConverter.class.getName());
    /**
     * Parameters always passed to gs
     */
    protected static final String[] defaultGSParams = {
        "-q",
        "-dBATCH",
        "-dSAFER",
        "-dNOPAUSE"
    };
    
    /**
     * Return value for getAdditionalGSParams() when no additional parameters are required
     */
    protected static final String[] NO_ADDITIONAL_PARAMETERS = new String[0];

    public GhostScriptMultiFileConverter() {
        super();
    }

    @Override
    public void convertMultiplePSorPDFFiles(File[] files, File targetFile, PaperSize paperSize) throws IOException, ConversionException {
        String gsPath = Utils.getFaxOptions().ghostScriptLocation;

        String[] additionalParams = getAdditionalGSParams();
        List<String> cmdList = new ArrayList<String>(5 + additionalParams.length + defaultGSParams.length + 2*files.length);

        cmdList.add(gsPath);
        for (String param : defaultGSParams) {
            cmdList.add(param);
        }
        cmdList.add("-sDEVICE=" + getGSDevice());
        cmdList.add("-sOutputFile=" + targetFile.getAbsolutePath());
        cmdList.add("-sPAPERSIZE=" + paperSize.name().toLowerCase());
        cmdList.add(calcResolution(paperSize));
        for (String param : additionalParams) {
            cmdList.add(param);
        }
        for (File file : files) {
            cmdList.add("-f");
            cmdList.add(file.getAbsolutePath());
        }

        ExternalProcessExecutor.quoteCommandLine(cmdList);
        if (Utils.debugMode) {
            log.fine("Ghostscript command line:");
            for (String cmd : cmdList) {
                log.fine(cmd);
            }
        }

        Process gs = new ProcessBuilder(cmdList).redirectErrorStream(true).start();
        gs.getOutputStream().close();
        BufferedReader bufR = new BufferedReader(new InputStreamReader(gs.getInputStream()));
        String line;
        LinkedList<String> tail = new LinkedList<String>();
        while ((line = bufR.readLine()) != null) {
            log.info("gs output: " + line);
            tail.offer(line);
            while (tail.size() > LINES_OF_ERROR_OUTPUT) {
                tail.poll();
            }
        }
        bufR.close();
        try {
            int exitVal = gs.waitFor();
            if (exitVal != 0) {
                StringBuilder excText = new StringBuilder();
                excText.append("Non-zero exit code of GhostScript (").append(exitVal).append("):\n");
                for (String text : tail) {
                    excText.append(text).append('\n');
                }
                throw new ConversionException(excText.toString());
            }
        } catch (InterruptedException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Returns the GhostScript "-r" parameter
     * @param paperSize
     * @return
     */
    protected String calcResolution(PaperSize paperSize) {
        return "-r196";
    }

    /**
     * The GS device to use
     * @return
     */
    protected abstract String getGSDevice();

    /**
     * Additional GhostScript parameters to pass
     * @return
     */
    protected abstract String[] getAdditionalGSParams();

    /**
     * The FileFormat produced by this converter
     */
    public abstract FileFormat getTargetFormat();
}