package lobstre.comique;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import lobstre.comique.util.Helper;
import lobstre.comique.util.Helper.ProgressListener;

public class Comique {
    public static boolean loadFiles = true;

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
            System.out.println (directory.getAbsolutePath () + " is not a directory");
            return;
        }

        final int[] screenRes = Helper.getScreenResolution ();
        final JProgressBar[] progressBar = new JProgressBar[1];
        final JFrame[] jFrame = new JFrame[1];

        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                jFrame [0] = new JFrame ("Comique");
                jFrame [0].setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

                jFrame [0].setSize (PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
                jFrame [0].setLocation (screenRes [0] / 2 - PROGRESS_BAR_WIDTH / 2, screenRes [1] / 2 - PROGRESS_BAR_HEIGHT / 2);
                jFrame [0].setResizable (false);

                final JPanel pane = new JPanel ();
                pane.setLayout (new FlowLayout ());
                progressBar [0] = new JProgressBar (0, 1000);
                progressBar [0].setValue (0);
                progressBar [0].setStringPainted (true);
                pane.add (progressBar [0]);
                jFrame [0].setContentPane (pane);
                jFrame [0].setVisible (true);
            }
        });

        final Map<Integer, BufferedImage> images;
        if (loadFiles) {
            images = Helper.loadFiles (directory, screenRes [0], new ProgressListener () {
                @Override
                public void progressed (final int processedPages, final int totalPages) {
                    SwingUtilities.invokeLater (new Runnable () {
                        @Override
                        public void run () {
                            progressBar [0].setValue (processedPages);
                            progressBar [0].setMaximum (totalPages);
                        }
                    });
                }
            });

            System.out.println ("Done: " + images.size () + " images loaded!");
        } else {
            images = Collections.emptyMap ();
        }

        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                jFrame [0].setVisible (false);

                JFrame jf = new JFrame ("Comique");
                jf.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
                jf.setUndecorated (true);
                jf.setSize (jf.getToolkit ().getScreenSize ());
                jf.setLocationRelativeTo (null);
                jf.setExtendedState (Frame.MAXIMIZED_BOTH);
                jf.validate ();
                jf.setVisible (true);

                int totalHeight = 0;
                for (BufferedImage bi : images.values ()) {
                    totalHeight += bi.getHeight ();
                }
                final int height = totalHeight;

                final JComponent jc = new JComponent() {
                    @Override
                    protected void paintComponent (Graphics g) {
                        super.paintComponent (g);
                        
                        int y = 0;
                        for (BufferedImage bi : images.values ()) {
                            g.drawImage (bi, 0, y, null);
                            y += bi.getHeight ();
                        }
                    }
                    
                    @Override
                    public Dimension getPreferredSize () {
                        return new Dimension (screenRes[0], height);
                    }

                    private static final long serialVersionUID = 1L;
                };
                final JScrollPane jsp = new JScrollPane (jc);
                jsp.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                jf.setContentPane (jsp);
            }
        });
    }

    private static final int PROGRESS_BAR_WIDTH = 200;
    private static final int PROGRESS_BAR_HEIGHT = 60;
}
