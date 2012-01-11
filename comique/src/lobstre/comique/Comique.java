package lobstre.comique;

import java.io.File;

import lobstre.comique.util.Helper;

public class Comique {
    public static void main (final String[] args) {
        if (args.length != 1) {
            System.out.println ("Usage : Comique directory");
            return;
        }
        
        final String dirName = args [0];
        final File file = new File (dirName);
        
        if (!file.exists ()) {
            System.out.println ("No such directory : " + file.getAbsolutePath ());
            return;
        }
        if (!file.isDirectory ()) {
            System.out.println  (file.getAbsolutePath () + " is not a directory");
            return;
        }
        
        final int[] result = Helper.getScreenResolution ();
        System.out.println (result [0]);
    }
}
