package lobstre.comique;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
                    public File getFile () {
                        return f;
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
            
            final File parentDir = makeTempDir ();
            for (int i = 1; entries.hasMoreElements (); i++) {
                entries.nextElement ();
                final int finalI = i;
                providers.add (new ImageFileProvider() {
                    @Override
                    public File getFile () {
                        try {
                            // Make a new zipFile and jump to the correct entry
                            final ZipFile zf = new ZipFile (droppedFile);
                            final Enumeration<? extends ZipEntry> es = zf.entries ();
                            ZipEntry ze = null;
                            for (int j = 0; j < finalI; j++) {
                                ze = es.nextElement ();
                            }
                            
                            // Prepare the temporary file
                            final File file = createTempFile (parentDir, getFileName (ze.getName ()));
                            
                            // Unzip
                            byte[] buffer = new byte [100*1000];
                            final InputStream is = zf.getInputStream (ze);
                            final FileOutputStream fos = new FileOutputStream(file);
                            int size;
                            try {
                                while ((size = is.read (buffer, 0, buffer.length)) != -1) {
                                    fos.write (buffer, 0, size);
                                }
                            } finally {
                                fos.close ();
                                is.close ();
                            }
                            
                            return file;
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
            final File parentDir = makeTempDir ();
            for (int i = 0; i < headers.size (); i++) {
                final int finalI = i;
                providers.add (new ImageFileProvider() {
                    @Override
                    public File getFile () {
                        try {
                            final Archive archive = new Archive (droppedFile);
                            final List<FileHeader> headers = archive.getFileHeaders ();
                            FileHeader fh = headers.get (finalI);
                            final String fileName = getFileName (fh.getFileNameString ());
                            final File imgFile = createTempFile (parentDir, fileName);
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

    private static File makeTempDir () throws IOException {
        final File parentDir = File.createTempFile ("comique", "avec-ms");
        parentDir.delete ();
        parentDir.mkdir ();
        parentDir.deleteOnExit ();
        return parentDir;
    }

    private static File createTempFile (final File parentDir, final String fileName) throws IOException {
        final File imgFile = new File (parentDir, fileName);
        imgFile.createNewFile ();
        imgFile.deleteOnExit ();
        return imgFile;
    }

    private static String getFileName (final String fileNameString) {
        final String fileName = fileNameString
                .replace ('/', '_')
                .replace ('\\', '_');
        return fileName;
    }
}
