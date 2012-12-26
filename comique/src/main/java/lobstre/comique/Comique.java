package lobstre.comique;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import lobstre.comique.util.Helper;

public class Comique {
    public static void main (final String[] args) {
        final int[] screenRes = Helper.getScreenResolution ();
        
        final FileChooserDialog fcd = new FileChooserDialog (screenRes);
        fcd.show ();
        final File droppedFile = fcd.getFile ();
        
        final File directory;
        if (droppedFile.isDirectory ()) {
            directory = droppedFile;
        } else {
            directory = droppedFile.getParentFile ();
        }

        final ProgressDialog pd = new ProgressDialog (screenRes);
        pd.show ();

        final Map<Integer, BufferedImage> images = Helper.loadFiles (directory, screenRes [0], pd.getProgressListener ());

        pd.hide ();
        System.out.println ("Done: " + images.size () + " images loaded!");

        final ComiqueReader cr = new ComiqueReader (images, screenRes);
        cr.show ();
    }
}
