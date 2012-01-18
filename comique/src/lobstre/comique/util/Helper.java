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

import javax.imageio.ImageIO;

public class Helper {

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
     * Loads all files
     * 
     * @param directory
     *            a directory {@link File}
     * @param width
     *            the display width
     * @return a {@link Map} of {@link Integer} page key to
     *         {@link BufferedImage} images
     */
    public static Map<Integer, BufferedImage> loadFiles (final File directory, final int width) {
        // CPUs
        final int cpus = Runtime.getRuntime ().availableProcessors ();
        final ExecutorService es = Executors.newFixedThreadPool (cpus);

        // Loading all images
        final Map<Integer, BufferedImage> images = new ConcurrentSkipListMap<Integer, BufferedImage> ();
        final File[] files = directory.listFiles ();
        for (int i = 0; i < files.length; i++) {
            final File f = files [i];
            final int pageId = i;
            es.submit (new Runnable () {
                @Override
                public void run () {
                    try {
                        final BufferedImage sourceImage;
                        // Silly concurrency bug in JVM : http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6986863
                        // Remove synchronized when fixed :(
                        synchronized (Helper.class) {
                            sourceImage = ImageIO.read (f);
                        }
                        
                        final double srcWidth = sourceImage.getWidth ();
                        final double srcHeight = sourceImage.getHeight ();
                        final double screenWidth = width;
                        final double desiredHeight = srcHeight * screenWidth / srcWidth;
                        final int height = (int) Math.ceil (desiredHeight);
                        System.out.println (height);
                        
                        final Image scaledSource = sourceImage.getScaledInstance (width, height, Image.SCALE_SMOOTH);
                        final BufferedImage targetImage = new BufferedImage (width, height, sourceImage.getType ());
                        targetImage.createGraphics ().drawImage (scaledSource, 0, 0, null);
                        images.put (pageId, targetImage);
                        System.out.println (pageId);
                    } catch (final IOException e) {
                        e.printStackTrace ();
                    } catch (final Throwable t) {
                        t.printStackTrace ();
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
