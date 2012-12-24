package lobstre.comique;

import java.io.File;
import java.io.IOException;

public class Comique {
    public static void main (final String[] args) {
        final String javaHome = System.getProperty ("java.home");
        final File javaDir = new File (javaHome);
        final File javaBinDir = new File (javaDir, "bin");
        
        final String classPath = System.getProperty ("java.class.path");
        
        final String[] commands = new String [] {
            javaBinDir + File.separator + "java",
            "-cp",
            classPath,
            "-Xmx1024M",
            ComiqueInternal.class.getName (),
        };

        try {
            final Process process = Runtime.getRuntime ().exec (commands);
            int read;
            while (-1 != (read = process.getErrorStream ().read ())) {
                System.err.write (read);
            }
            while (-1 != (read = process.getInputStream ().read ())) {
                System.out.write (read);
            }
     
            int exitValue = process.exitValue ();
            System.out.println ("Exit Value : " + exitValue);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
