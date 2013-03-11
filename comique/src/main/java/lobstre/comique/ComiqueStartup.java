package lobstre.comique;

import java.io.File;
import java.io.IOException;

public class ComiqueStartup {
    public static void main (final String[] args) {
        final String[] commands = prepareCommands (args);

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

    /**
     * Gets an array of command line arguments in order to start comique reader.
     * 
     * @param args
     *            the comique arguments
     * @return an array of command line arguments
     */
    public static String[] prepareCommands (final String[] args) {
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
        final String[] commands = new String [5 + args.length]; 
        commands[0] = javaBinDir + File.separator + "java";
        commands[1] = "-cp";
        commands[2] = classPath;
        commands[3] = memoryOption;
        commands[4] = Comique.class.getName ();
        for (int i = 0; i < args.length; i++) {
            commands [5+i] = args [i];
        }
        return commands;
    }
}
