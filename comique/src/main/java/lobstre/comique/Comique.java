package lobstre.comique;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lobstre.comique.util.Helper;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class Comique {
    public static void main (final String[] args) {
        final int[] screenRes = Helper.getScreenResolution ();
        
        final FileChooserDialog fcd = new FileChooserDialog (screenRes);
        fcd.show ();
        final File droppedFile = fcd.getFile ();
        
        final List<ImageFileProvider> providers;
        if (droppedFile.isDirectory ()) {
            providers = listFiles (droppedFile);
        } else if (isRarArchive (droppedFile)) {
            providers = unrar (droppedFile);
            if (null == providers) {
                System.exit (-1);
            }
        } else if (isZipArchive (droppedFile)) {
            providers = unzip (droppedFile);
        } else {
            providers = listFiles (droppedFile.getParentFile ());
        }

        final ProgressDialog pd = new ProgressDialog (screenRes);
        pd.show ();

        final Map<Integer, BufferedImage> images = FileLoadingHelper.loadFiles (null, providers, screenRes [0], pd.getProgressListener ());

        pd.hide ();
        System.out.println ("Done: " + images.size () + " images loaded!");

        final ComiqueReader cr = new ComiqueReader (images, screenRes);
        cr.show ();
    }
    
    private static List<ImageFileProvider> listFiles (File parentFile) {
        final List<ImageFileProvider> providers = new ArrayList<ImageFileProvider> ();
        
        for (final File f : parentFile.listFiles ()) {
            if (f.isDirectory ()) {
                providers.addAll (listFiles (f));
            } else {
                providers.add (new ImageFileProvider() {
                    @Override
                    public File getFile () {
                        return f;
                    }
                });
            }
        }
        
        return providers;
    }

    private static boolean isZipArchive (File droppedFile) {
        return false;
    }
    
    private static List<ImageFileProvider> unzip (File droppedFile) {
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
    
    private static List<ImageFileProvider> unrar (final File droppedFile) {
        try {
            final List<ImageFileProvider> providers = new ArrayList<ImageFileProvider> ();
            
            final Archive archive = new Archive (droppedFile);
            final List<FileHeader> headers = archive.getFileHeaders ();
            final File parentDir = File.createTempFile ("comique", "avec-ms");
            parentDir.delete ();
            parentDir.mkdir ();
            parentDir.deleteOnExit ();
            for (int i = 0; i < headers.size (); i++) {
                final int finalI = i;
                providers.add (new ImageFileProvider() {
                    @Override
                    public File getFile () {
                        try {
                            final Archive archive = new Archive (droppedFile);
                            final List<FileHeader> headers = archive.getFileHeaders ();
                            FileHeader fh = headers.get (finalI);
                            final String fileName = fh.getFileNameString ()
                                    .replace ('/', '_')
                                    .replace ('\\', '_');
                            final File imgFile = new File (parentDir, fileName);
                            imgFile.createNewFile ();
                            imgFile.deleteOnExit ();
                            final FileOutputStream fos = new FileOutputStream (imgFile);
                            try {
                                archive.extractFile (fh, fos);
                            } finally {
                                fos.close ();
                            }
                            return imgFile;
                        } catch (RarException e) {
                            e.printStackTrace ();
                            return null;
                        } catch (IOException e) {
                            e.printStackTrace ();
                            return null;
                        }
                    }
                });
            }
            return providers;
        } catch (RarException e) {
            e.printStackTrace ();
            return null;
        } catch (IOException e) {
            e.printStackTrace ();
            return null;
        }
    }
}
