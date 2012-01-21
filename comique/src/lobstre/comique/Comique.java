package lobstre.comique;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import lobstre.comique.util.Helper;
import lobstre.comique.util.Helper.ProgressListener;

public class Comique {
    public static void main (final String[] args) {
        if (args.length != 1) {
            System.out.println ("Usage : Comique directory");
            return;
        }
        
        final String dirName = args [0];
        final File directory = new File (dirName);
        
        if (!directory.exists ()) {
            System.out.println ("No such directory : " + directory.getAbsolutePath ());
            return;
        }
        if (!directory.isDirectory ()) {
            System.out.println  (directory.getAbsolutePath () + " is not a directory");
            return;
        }
        
        final int[] screenRes = Helper.getScreenResolution ();
        final JProgressBar[] progressBar = new JProgressBar [1];
        final JFrame[] jFrame = new JFrame [1];
        
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                jFrame[0] = new JFrame ("Comique");
                jFrame[0].setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                jFrame[0].setSize (PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
                jFrame[0].setLocation (screenRes[0] / 2  - PROGRESS_BAR_WIDTH / 2, 
                        screenRes[1] / 2 - PROGRESS_BAR_HEIGHT / 2);
                jFrame[0].setResizable (false);
                
                final JPanel pane = new JPanel ();
                pane.setLayout(new FlowLayout());
                progressBar[0] = new JProgressBar(0, 1000);
                progressBar[0].setValue(0);
                progressBar[0].setStringPainted(true);
                pane.add(progressBar[0]);
                jFrame[0].setContentPane (pane);
                jFrame[0].setVisible (true);
            }
        });
        

        final Map<Integer, BufferedImage> images = Helper.loadFiles (
                directory, 
                screenRes [0], 
                new ProgressListener() {
                    @Override
                    public void progressed (
                            final int processedPages, 
                            final int totalPages) {
                        SwingUtilities.invokeLater (new Runnable () {
                            @Override
                            public void run () {
                                progressBar[0].setValue (processedPages);
                                progressBar[0].setMaximum (totalPages);
                            }
                        });
                    }
                });
        
        System.out.println ("Done: " + images.size () + " images loaded!");
        
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                jFrame[0].setVisible (false);
            }
        });
    }
    
    private static final int PROGRESS_BAR_WIDTH = 200;
    private static final int PROGRESS_BAR_HEIGHT = 60;
}
