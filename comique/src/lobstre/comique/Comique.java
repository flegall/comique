package lobstre.comique;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import lobstre.comique.util.Helper;
import lobstre.comique.util.filedrop.FileDrop;

public class Comique {
    public static void main (final String[] args) {
        final int[] screenRes = Helper.getScreenResolution ();
        
        final BlockingQueue<File> queue = new SynchronousQueue<File> ();

        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                final JFrame frame = new JFrame ("Comique file chooser");
                final JLabel text = new JLabel ("Drop here a file or directory you'd like to read...");
                text.setHorizontalAlignment (SwingConstants.CENTER);
                frame.getContentPane ().add (text, BorderLayout.CENTER);

                final int width = 300;
                final int height = 80;
                frame.setSize (width, height);
                frame.setLocation (screenRes [0] / 2 - width / 2, screenRes [1] / 2 - height / 2);
                frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
                frame.setVisible (true);

                new FileDrop (System.out, text, new FileDrop.Listener () {
                    public void filesDropped (File[] files) {
                        if (files.length > 0) {
                            queue.offer (files[0]);
                            frame.setVisible (false);
                            frame.dispose ();
                        }
                    } 
                }); 

            }
        });

        final File droppedFile;
        try {
            droppedFile = queue.take ();
        } catch (InterruptedException e) {
            e.printStackTrace ();
            return;
        }
        
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
