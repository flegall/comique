package lobstre.comique;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

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
        
        final List<BufferedImage> images = new ArrayList<BufferedImage> ();
        final File[] files = file.listFiles ();
        for (final File f : files) {
            try {
                images.add (ImageIO.read(f));
            } catch (final IOException e) {
                e.printStackTrace();
                continue;
            }
        }
        
        final int[] result = Helper.getScreenResolution ();
        System.out.println (result [0]);
    }
}
