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
package yajhfc;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a password
 * @author jonas
 *
 */
public class Password {
    private static final Logger log = Logger.getLogger(Password.class.getName());
    
    private String password = null;
    private String obfuscatedPassword = null;
    
    /**
     * Returns the clear text password
     * @return
     */
    public String getPassword() {
        if (password == null) {
            try {
                password = deobfuscatePassword(obfuscatedPassword);
            } catch (UnsupportedEncodingException e) {
                log.log(Level.WARNING, "Error deobfuscating a password: ", e);
                obfuscatedPassword = null;
            }
        }
        return password;
    }
    /**
     * Sets the clear text password
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
        this.obfuscatedPassword = null;
    }
    /**
     * Returns the obfuscated password
     * @return
     */
    public String getObfuscatedPassword() {
        if (obfuscatedPassword == null) {
            try {
                obfuscatedPassword = obfuscatePassword(password);
            } catch (UnsupportedEncodingException e) {
                log.log(Level.WARNING, "Error obfuscating a password: ", e);
                obfuscatedPassword = null;
            }
        }
        return obfuscatedPassword;
    }
    /**
     * Sets the obfuscated password
     * @param obfuscatedPassword
     */
    public void setObfuscatedPassword(String obfuscatedPassword) {
        this.obfuscatedPassword = obfuscatedPassword;
        this.password = null;
    }

    @Override
    public String toString() {
        char[] res = new char[getPassword().length()];
        Arrays.fill(res, '*');
        return new String(res);
    }

    private static Random generator;
    /**
     * Returns a random integer in the interval [1,28]
     * @return
     */
    private static int getRandomInt() {
        if (generator == null) {
            generator = new Random();
        }
        return 1 + generator.nextInt(28);
    }
    
    public static String obfuscatePassword(String clearText) throws UnsupportedEncodingException {
        if (clearText == null || clearText.length() == 0)
            return "";
        
        final byte[] bytes = clearText.getBytes("utf-8");
        final char[] result = new char[bytes.length*2 + 1];
        final int factor = getRandomInt();
        
        result[0] = Character.forDigit(factor, 29);
        for (int i=0; i<bytes.length; i++) {
            final int b = bytes[i] & 0xff;
            
            int lower = ((b + i) * factor) % 29;
            if (lower < 0)
                lower += 29;
            int higher = ((b/29 + i) * factor) % 29;
            if (higher < 0)
                higher += 29;
            
            result[2*i+1] = Character.forDigit(lower, 29);
            result[2*i+2] = Character.forDigit(higher, 29);
        }
        
        return new String(result);
    }
    
    public static String deobfuscatePassword(String obfuscatedText) throws UnsupportedEncodingException {
        if (obfuscatedText == null || obfuscatedText.length() == 0)
            return "";
        
        final byte[] result = new byte[obfuscatedText.length()/2];
        int factor = extEuclid(Character.digit(obfuscatedText.charAt(0), 29), 29)[1] % 29;
        if (factor < 0)
            factor += 29;
        for (int i=0; i<result.length; i++) {
            int l = ((Character.digit(obfuscatedText.charAt(2*i+1), 29) * factor) - i) % 29;
            if (l < 0)
                l += 29;
            int h = ((Character.digit(obfuscatedText.charAt(2*i+2), 29) * factor) - i) % 29;
            if (h < 0)
                h += 29;
            result[i] = (byte)(h*29 + l);
        }
        return new String(result, "utf-8");
    }
    
    /**
     * Extended euclidean algorithm (recursive implementation).
     * Returns a triple (d, s, t) as an array so that 
     * gcd(a,b) = d = s*a + t*b
     * @param a
     * @param b
     * @return
     */
    private static int[] extEuclid(int a, int b) {
        if (b == 0) {
            return new int[] { a, 1, 0 };
        }
        final int[] r = extEuclid(b, a%b);
        final int s = r[1];
        final int t = r[2];
        
        r[1] = t;
        r[2] = s - a/b*t;
        return r;
    }
    
//    public static void main(String[] args) throws IOException {
//        BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
//        String line = inReader.readLine();
//        while (line != null && line.length() > 0) {
//            String obfuscated = obfuscatePassword(line);
//            System.out.println("Obfuscated: " + obfuscated);
//            String clear = deobfuscatePassword(obfuscated);
//            System.out.println("Deobfuscated matches: " + clear.equals(line) + ": " + clear);
//            line = inReader.readLine();
//        }
//    }
}
