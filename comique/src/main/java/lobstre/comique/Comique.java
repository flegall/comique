package lobstre.comique;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
                    public byte[] getImageFile () {
                        try {
                            final InputStream fis = new FileInputStream (f);
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                            copy (fis, baos);
                            return baos.toByteArray ();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            return null;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                });
            }
        }
        
        return providers;
    }

    private static boolean isZipArchive (final File droppedFile) {
        try {
            return new ZipFile (droppedFile)
                .entries ().hasMoreElements ();
        } catch (ZipException e) {
            e.printStackTrace ();
            return false;
        } catch (IOException e) {
            e.printStackTrace ();
            return false;
        }
    }
    
    private static List<ImageFileProvider> unzip (final File droppedFile) {
        try {
            final List<ImageFileProvider> providers = new ArrayList<ImageFileProvider> ();
            Enumeration<? extends ZipEntry> entries = new ZipFile (droppedFile).entries ();
            
            for (int i = 1; entries.hasMoreElements (); i++) {
                entries.nextElement ();
                final int finalI = i;
                providers.add (new ImageFileProvider() {
                    @Override
                    public byte[] getImageFile () {
                        try {
                            // Make a new zipFile and jump to the correct entry
                            final ZipFile zf = new ZipFile (droppedFile);
                            final Enumeration<? extends ZipEntry> es = zf.entries ();
                            ZipEntry ze = null;
                            for (int j = 0; j < finalI; j++) {
                                ze = es.nextElement ();
                            }
                            
                            // Unzip
                            final InputStream is = zf.getInputStream (ze);
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                            copy (is, baos);
                            
                            return baos.toByteArray ();
                        } catch (ZipException e) {
                            e.printStackTrace();
                            return null;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                });
            }
            
            return providers;
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
            for (int i = 0; i < headers.size (); i++) {
                final int finalI = i;
                providers.add (new ImageFileProvider() {
                    @Override
                    public byte[] getImageFile () {
                        try {
                            final Archive archive = new Archive (droppedFile);
                            final List<FileHeader> headers = archive.getFileHeaders ();
                            final FileHeader fh = headers.get (finalI);
                            
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                            
                            try {
                                archive.extractFile (fh, baos);
                            } finally {
                                baos.close ();
                            }
                            return baos.toByteArray ();
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
    
    private static void copy (final InputStream is, final OutputStream fos) throws IOException {
        int size;
        byte[] buffer = new byte [100*1000];
        try {
            while ((size = is.read (buffer, 0, buffer.length)) != -1) {
                fos.write (buffer, 0, size);
            }
        } finally {
            fos.close ();
            is.close ();
        }
    }
}
