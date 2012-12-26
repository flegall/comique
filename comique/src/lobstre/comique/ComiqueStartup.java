package lobstre.comique;

import java.io.File;
import java.io.IOException;

public class ComiqueStartup {
    public static void main (final String[] args) {
        // Determine java /bin dir
        final String javaHome = System.getProperty ("java.home");
        final File javaDir = new File (javaHome);
        final File javaBinDir = new File (javaDir, "bin");
        
        // Determine current class path
        final String classPath = System.getProperty ("java.class.path");
        
        // Determine maximum memory
        // 64 bits architecture allows larger memory windows.
        final String osArch = System.getProperty ("os.arch");
        final String memoryOption;
        if (osArch.endsWith ("64")) {
            memoryOption = "-Xmx32000M";
        } else {
            memoryOption = "-Xmx1000M";
        }
        
        // Prepare commands
        final String[] commands = new String [] {
            javaBinDir + File.separator + "java",
            "-cp",
            classPath,
            memoryOption,
            Comique.class.getName (),
        };

        // Execute the subprocess
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
