import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

import javax.swing.UIDefaults;
import javax.swing.UIManager;


public class FillUIProps {

    private static final String header = 
        "# This file contains message IDs to translate standard Java Swing dialogs\n" +
        "\n"+
        "msgid \"\"\n"+
        "msgstr \"\"\n"+
        "\"MIME-Version: 1.0\\n\"\n"+        
        "\"Content-Type: text/plain; charset=utf-8\\n\"\n"+
        "\"Content-Transfer-Encoding: 8bit\\n\"\n\n";

    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception{
        InputStream in;
        OutputStream out;
        
        if (args.length >= 1) {
            in = new FileInputStream(args[0]);
        } else {
            in = System.in;
        }
        
        if (args.length >= 2) {
            out = new FileOutputStream(args[1]);
        } else {
            out = System.out;
        }

        BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
        //Properties props = new Properties();
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
        UIDefaults uiDefs = UIManager.getDefaults();
        Locale loc = Locale.ENGLISH;
        
        outWriter.write(header);
        
        String line;
        while ((line = inReader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("#")) {
                Object val = uiDefs.get(line, loc);
                if (val instanceof String) {
                    //props.put(line, val);
                    outWriter.write("# English text: \"" + val + "\"\n");
                    outWriter.write("msgid \"" + line + "\"\n");
                    outWriter.write("msgstr \"\"\n\n");
                } else {
                    System.err.println("Invalid property: " + line);
                }
            } 
        }
        inReader.close();
        outWriter.close();
        
        //props.store(out, "Auto generated from " + in);
        //out.close();
    }

}
