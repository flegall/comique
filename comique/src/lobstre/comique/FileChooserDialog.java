package lobstre.comique;

import java.awt.BorderLayout;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import lobstre.comique.util.filedrop.FileDrop;

/**
 * Dialog displaying a drop-zone for choosing a file 
 */
public class FileChooserDialog {
    
    /**
     * Builds a {@link FileChooserDialog} instance
     * @param screenRes an array containing the screen resolution
     */
    public FileChooserDialog (final int[] screenRes) {
        this.screenRes = screenRes;
    }
    
    /**
     * Shows the dialog
     */
    public void show () {
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                final JFrame frame = new JFrame ("ComiqueInternal file chooser");
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
    }
    
    /**
     * Gets the selected file
     * @return a {@link File} instance
     */
    public File getFile () {
        try {
            return queue.take ();
        } catch (InterruptedException e) {
            e.printStackTrace ();
            return null;
        }
    }
    
    private final int[] screenRes;
    private final BlockingQueue<File> queue = new SynchronousQueue<File> ();
}
