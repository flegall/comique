package lobstre.comique;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import lobstre.comique.util.Helper;

public class FileLoadingHelper {
    
    public static final boolean LOAD_ONLY_FIVE_PAGES = false;

    private static final String DECODE_AS_BUFFERED_IMAGE_METHODNAME = "decodeAsBufferedImage";
    private static final String CREATE_JPEG_DECODER_METHODNAME = "createJPEGDecoder";
    private static final String COM_SUN_IMAGE_CODEC_JPEG_JPEG_CODEC = "com.sun.image.codec.jpeg.JPEGCodec";
    private static final String COM_SUN_IMAGE_CODEC_JPEG_IMAGE_FORMAT_EXCEPTION = "com.sun.image.codec.jpeg.ImageFormatException";

    /**
     * Loads all files
     * 
     * @param directory
     *            a directory {@link File}
     * @param ifps
     *            a {@link List} of {@link ImageFileProvider} instances
     * @param width
     *            the display width
     * @param progressListener an optional {@link ProgressListener}
     * @return a {@link Map} of {@link Integer} page key to
     *         {@link BufferedImage} images
     */
    public static Map<Integer, BufferedImage> loadFiles (
            final File directory, 
            final List<ImageFileProvider> ifps,
            final int width, 
            final ProgressListener progressListener) {
        
        // CPUs
        final int cpus = Runtime.getRuntime ().availableProcessors ();
        final ExecutorService es = Executors.newFixedThreadPool (cpus);
    
        // Loading all images
        final Map<Integer, BufferedImage> images = new ConcurrentSkipListMap<Integer, BufferedImage> ();
        final int totalPages = ifps.size ();
        
        // Starting 
        if (null != progressListener) {
            progressListener.progressed (0, totalPages);
        }
        
        final AtomicInteger counter = new AtomicInteger (0);
        // Iterating on all files
        for (int i = 0; i < totalPages; i++) {
            final ImageFileProvider ifp = ifps.get (i);
            final int pageId = i;
            es.submit (new Runnable () {
                @Override
                @SuppressWarnings("unused")
                public void run () {
                    try {
                        if (pageId > 5 && LOAD_ONLY_FIVE_PAGES) {
                            return;
                        }
                        
                        // Extract the file
                        final File f = ifp.getFile ();
                        
                        // Decode the image
                        BufferedImage sourceImage;
                        sourceImage = tryDecodeUsingJpegCodec (f);
                        if (null == sourceImage) {
                            // Fall back to traditional reader for other formats
                            // Synchronized due to this bug : 
                            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6986863
                            synchronized (Helper.class) {
                                sourceImage = ImageIO.read (f);
                            }
                        }
                        if (null == sourceImage) {
                            return;
                        }
                        
                        // Eventually resize the image
                        final double srcWidth = sourceImage.getWidth ();
                        if (srcWidth == width) {
                            // Nothing to do :)
                            images.put (pageId, sourceImage);
                        } else {
                            // Computes desired height
                            final double srcHeight = sourceImage.getHeight ();
                            final double desiredHeight = srcHeight * width / srcWidth;
                            final int height = (int) Math.ceil (desiredHeight);
                            final double scaleFactor = (double) width / (double) srcWidth;
                            
                            // Perform scaling
                            BufferedImage scaledImage = new BufferedImage (
                                    width, 
                                    height, 
                                    sourceImage.getType ());
                            final AffineTransform at = new AffineTransform ();
                            at.scale (scaleFactor, scaleFactor);
                            final AffineTransformOp scaleOp = 
                                    new AffineTransformOp (at, AffineTransformOp.TYPE_BICUBIC);
                            scaledImage = scaleOp.filter (sourceImage, scaledImage);
                            
                            // Store the image
                            images.put (pageId, scaledImage);
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

    /**
     * Try decoding using Sun's JPEG Codec, (faster than java ImageIO and
     * doesn't have any concurrency bug)
     * <p>
     * If not available on current JVM or couldn't decode image,, return null.
     * 
     * @param f
     *            a {@link File} instance
     * @return a {@link BufferedImage} if could be decoded, null if not.
     */
    public static BufferedImage tryDecodeUsingJpegCodec (final File f) {
        BufferedInputStream stream = null;
        Class<?> imageFormatExceptionClass = null;
        try {
            imageFormatExceptionClass = Class.forName (COM_SUN_IMAGE_CODEC_JPEG_IMAGE_FORMAT_EXCEPTION);
            stream = new BufferedInputStream (new FileInputStream (f));
            final Class<?> jpegCodec = Class.forName (COM_SUN_IMAGE_CODEC_JPEG_JPEG_CODEC);
            final Method createJPEGDecoder = jpegCodec.getMethod (CREATE_JPEG_DECODER_METHODNAME, InputStream.class);
            final Object decoder = createJPEGDecoder.invoke (null, stream);
            final Method decodeAsBufferedImage = decoder.getClass ().getMethod (DECODE_AS_BUFFERED_IMAGE_METHODNAME);
            return (BufferedImage) decodeAsBufferedImage.invoke (decoder);
        } catch (final ClassNotFoundException e) {
            // Unsupported by this JVM : no need to log, returning null
            return null;
        } catch (final SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (final InvocationTargetException e) {
            if (imageFormatExceptionClass.isInstance (e.getCause ())) {
                // Unsupported file format, returning null
                return null;
            } else {
                e.printStackTrace();
                return null; 
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close ();
                } catch (final IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

}
