package lobstre.comique.util;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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
     * @return a {@link Map} of {@link Integer} page key to
     *         {@link BufferedImage} images
     */
    public static Map<Integer, BufferedImage> loadFiles (final File directory) {
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
                        images.put (pageId, ImageIO.read (f));
                    } catch (final IOException e) {
                        e.printStackTrace ();
                    }
                }
            });
        }
        
        // Shutdown worker.
        es.shutdown ();
        try {
            es.awaitTermination (3600L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return images;
    }

}
