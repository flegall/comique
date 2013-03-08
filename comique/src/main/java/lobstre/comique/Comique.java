package lobstre.comique;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

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
        } else if (isRarArchive (droppedFile)) {
            directory = unrar (droppedFile);
            if (null == directory) {
                System.exit (-1);
            }
        } else if (isZipArchive (droppedFile)) {
            directory = unzip (droppedFile);
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
    
    private static boolean isZipArchive (File droppedFile) {
        return false;
    }
    
    private static File unzip (File droppedFile) {
        return null;
    }
    
    private static boolean isRarArchive (final File droppedFile) {
        try {
            final Archive archive = new Archive (droppedFile);
            final List<FileHeader> headers = archive.getFileHeaders ();
            return headers.size () > 0;
        } catch (RarException e) {
            e.printStackTrace ();
            return false;
        } catch (IOException e) {
            e.printStackTrace ();
            return false;
        }
    }
    
    private static File unrar (File droppedFile) {
        try {
            final Archive archive = new Archive (droppedFile);
            final List<FileHeader> headers = archive.getFileHeaders ();
            final File parentDir = File.createTempFile ("comique", "avec-ms");
            parentDir.delete ();
            parentDir.mkdir ();
            parentDir.deleteOnExit ();
            for (FileHeader fh : headers) {
                final String fileName = fh.getFileNameString ()
                        .replace ('/', '_')
                        .replace ('\\', '_');
                final File imgFile = new File (parentDir, fileName);
                imgFile.createNewFile ();
                final FileOutputStream fos = new FileOutputStream (imgFile);
                try {
                    archive.extractFile (fh, fos);
                } finally {
                    fos.close ();
                }
                parentDir.deleteOnExit ();
            }
            return parentDir;
        } catch (RarException e) {
            e.printStackTrace ();
            return null;
        } catch (IOException e) {
            e.printStackTrace ();
            return null;
        }
    }
}
