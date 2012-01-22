package lobstre.comique.util;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

public class Helper {
    
    public static final boolean LOAD_ONLY_FIVE_PAGES = false;

    /**
     * Gets the screen resolution in an integer array.
     * 
     * @return an int array containing two entries, the width and the height in
     *         pixels.
     */
    public static int[] getScreenResolution () {
        final int[] result = new int[2];
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment ();
        final GraphicsDevice gd = ge.getDefaultScreenDevice ();
        result [0] = gd.getDisplayMode ().getWidth ();
        result [1] = gd.getDisplayMode ().getHeight ();
        return result;
    }
    
    /**
     * Progression listener interface
     */
    public interface ProgressListener {
        /**
         * Called when making progress
         * @param processedPages the number of processed pages
         * @param totalPages the total page count
         */
        void progressed (int processedPages, int totalPages);
    }

    /**
     * Loads all files
     * 
     * @param directory
     *            a directory {@link File}
     * @param width
     *            the display width
     * @param progressListener an optional {@link ProgressListener}
     * @return a {@link Map} of {@link Integer} page key to
     *         {@link BufferedImage} images
     */
    public static Map<Integer, BufferedImage> loadFiles (
            final File directory, 
            final int width, 
            final ProgressListener progressListener) {
        
        // CPUs
        final int cpus = Runtime.getRuntime ().availableProcessors ();
        final ExecutorService es = Executors.newFixedThreadPool (cpus);

        // Loading all images
        final Map<Integer, BufferedImage> images = new ConcurrentSkipListMap<Integer, BufferedImage> ();
        final File[] files = directory.listFiles ();
        final int totalPages = files.length;
        
        // Starting 
        if (null != progressListener) {
            progressListener.progressed (0, totalPages);
        }
        
        final AtomicInteger counter = new AtomicInteger (0);
        // Iterating on all files
        for (int i = 0; i < totalPages; i++) {
            final File f = files [i];
            final int pageId = i;
            es.submit (new Runnable () {
                @Override
                @SuppressWarnings("unused")
                public void run () {
                    try {
                        if (pageId > 5 && LOAD_ONLY_FIVE_PAGES) {
                            return;
                        }
                        final BufferedImage sourceImage;
                        // Silly concurrency bug in jpeg plugin : http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6986863
                        // Remove synchronized when fixed :(
                        synchronized (Helper.class) {
                            sourceImage = ImageIO.read (f);
                        }
                        
                        final double srcWidth = sourceImage.getWidth ();
                        
                        if (srcWidth == width) {
                            // Nothing to do :)
                            images.put (pageId, sourceImage);
                        } else {
                            final double srcHeight = sourceImage.getHeight ();
                            final double desiredHeight = srcHeight * (double) width / srcWidth;
                            final int height = (int) Math.ceil (desiredHeight);
                            
                            final Image scaledSource = sourceImage.getScaledInstance (width, height, Image.SCALE_SMOOTH);
                            final BufferedImage targetImage = new BufferedImage (width, height, sourceImage.getType ());
                            targetImage.createGraphics ().drawImage (scaledSource, 0, 0, null);
                            images.put (pageId, targetImage);
                        }
                    } catch (final IOException e) {
                        e.printStackTrace ();
                    } catch (final Throwable t) {
                        t.printStackTrace ();
                    } finally {
                        final int current = counter.incrementAndGet ();
                        if (null != progressListener) {
                            progressListener.progressed (current, totalPages);
                        }
                    }
                }
            });
        }
        
        // Shutdown worker.
        es.shutdown ();
        try {
            es.awaitTermination (3600L, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        
        return images;
    }

}
