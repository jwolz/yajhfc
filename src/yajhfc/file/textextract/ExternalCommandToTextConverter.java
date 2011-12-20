package yajhfc.file.textextract;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import yajhfc.Utils;
import yajhfc.file.FileConverter.ConversionException;
import yajhfc.util.ArrayCharSequence;
import yajhfc.util.ExternalProcessExecutor;

public abstract class ExternalCommandToTextConverter extends HylaToTextConverter {
    /**
     * The number of lines of error output to show in the message to the user
     */
    private static final int LINES_OF_ERROR_OUTPUT = 20;
    
    private static final Logger log = Logger.getLogger(ExternalCommandToTextConverter.class.getName());
    
    /**
     * Encoding of the stream produced by the external command
     */
    protected String streamEncoding = "ISO8859-1";
    
    /**
     * Determines if the command can be called with all files or has to be called once per file
     */
    protected boolean acceptsMultipleFiles = true;
    
    /**
     * Adds the command line argument to the specified list in order to create the command line to execute
     * @param commandLine
     */
    protected abstract void buildCommandLine(List<String> commandLine, File[] inputFiles);

    /**
     * Modifies the environment of the child process before starting it.
     * @param env
     */
    protected void modifyEnvironment(Map<String,String> env) {
        // Do nothing by default
    }
    
    @Override
    public CharSequence[] convertToText(File[] input) throws ConversionException, IOException {       
        if (acceptsMultipleFiles) {
            return new CharSequence[] { callCommandFor(input) };
        } else {
            CharSequence[] output = new CharSequence[input.length];
            for (int i=0; i<input.length; i++) {
                output[i] = callCommandFor(new File[] { input[i] }); 
            }
            return output;
        }
    }

    protected CharSequence callCommandFor(File[] input) throws ConversionException, IOException { 
        if (!acceptsMultipleFiles && input.length != 1) {
            throw new UnsupportedOperationException("!acceptsMultipleFiles && input.length != 1");
        }
        
        List<String> cmdList = new ArrayList<String>();
        buildCommandLine(cmdList, input);

        ExternalProcessExecutor.quoteCommandLine(cmdList);
        if (Utils.debugMode) {
            log.fine(cmdList.get(0) + " command line:");
            for (String cmd : cmdList) {
                log.fine(cmd);
            }
        }

        ProcessBuilder childBuilder = new ProcessBuilder(cmdList);
        modifyEnvironment(childBuilder.environment());        
        Process child = childBuilder.start();

        InputStreamReader r = new InputStreamReader(child.getInputStream(), streamEncoding);
        ArrayCharSequence csq = ArrayCharSequence.readCompletely(r);
        r.close();

        // Close the child's stdin
        child.getOutputStream().close();

        BufferedReader bufR = new BufferedReader(new InputStreamReader(child.getErrorStream()));
        String line;
        LinkedList<String> tail = new LinkedList<String>();
        while ((line = bufR.readLine()) != null) {
            log.info(cmdList.get(0) + " output: " + line);
            tail.offer(line);
            while (tail.size() > LINES_OF_ERROR_OUTPUT) {
                tail.poll();
            }
        }
        bufR.close();
        try {
            int exitVal = child.waitFor();
            if (exitVal != 0) {
                StringBuilder excText = new StringBuilder();
                excText.append("Non-zero exit code of ").append(cmdList.get(0)).append(" (").append(exitVal).append("):\n");
                for (String text : tail) {
                    excText.append(text).append('\n');
                }
                throw new ConversionException(excText.toString());
            }
        } catch (InterruptedException e) {
            throw new ConversionException(e);
        }

        return csq;
    }
}
