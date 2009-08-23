import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ResourceBundle;


public class FillUIProps {

    private static final String header = 
        "# This file contains message IDs to translate standard Java Swing dialogs\n" +
        "\n"+
        "msgid \"\"\n"+
        "msgstr \"\"\n"+
        "\"MIME-Version: 1.0\\n\"\n"+        
        "\"Content-Type: text/plain; charset=utf-8\\n\"\n"+
        "\"Content-Transfer-Encoding: 8bit\\n\"\n\n";

    private static final String[] resources = {
        "com.sun.swing.internal.plaf.basic.resources.basic",
        "com.sun.swing.internal.plaf.metal.resources.metal",
        "com.sun.java.swing.plaf.windows.resources.windows",
        "com.sun.swing.internal.plaf.synth.resources.synth",
        "com.sun.java.swing.plaf.gtk.resources.gtk",
        "com.sun.java.swing.plaf.motif.resources.motif"
    };
    
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
        //UIDefaults uiDefs = UIManager.getDefaults();
        //Locale loc = Locale.ENGLISH;
        ResourceBundle[] rbs = new ResourceBundle[resources.length];
        for (int i = 0; i < resources.length; i++) {
            rbs[i] = (ResourceBundle)Class.forName(resources[i]).newInstance();
        }
        
        outWriter.write(header);
        
        String line;
        while ((line = inReader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("#")) {
                //Object val = uiDefs.get(line, loc);
                Object val = null;
                for (ResourceBundle rb : rbs) {
                    try {
                      val = rb.getObject(line);
                      if (val != null) {
                          break;
                      }
                    } catch (Exception e) {
                        //System.err.println(e);
                        // Do nothing
                    }
                }
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
