package lobstre.comique;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import lobstre.comique.util.Helper;

public class Comique {
    public static void main (final String[] args) {
        if (args.length != 1) {
            System.out.println ("Usage : Comique directory");
            return;
        }
        
        final String dirName = args [0];
        final File directory = new File (dirName);
        
        if (!directory.exists ()) {
            System.out.println ("No such directory : " + directory.getAbsolutePath ());
            return;
        }
        if (!directory.isDirectory ()) {
            System.out.println  (directory.getAbsolutePath () + " is not a directory");
            return;
        }
        
        final int[] result = Helper.getScreenResolution ();
        final Map<Integer, BufferedImage> images = Helper.loadFiles (directory, result [0]);
        
        System.out.println (images.size ());
        
    }
}
