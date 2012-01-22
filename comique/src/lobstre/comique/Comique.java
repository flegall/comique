package lobstre.comique;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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

        final Map<Integer, BufferedImage> images = Helper.loadFiles (directory, screenRes [0], new ProgressListener () {
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

        SwingUtilities.invokeLater (new Runnable () {
            private JScrollPane jsp;
            private JComponent jc;
            
            @Override
            public void run () {
                jFrame [0].setVisible (false);
                jFrame [0].dispose ();

                final JFrame jf = new JFrame ("Comique");
                jf.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
                jf.setUndecorated (true);
                jf.setSize (jf.getToolkit ().getScreenSize ());
                jf.setLocationRelativeTo (null);
                jf.setExtendedState (Frame.MAXIMIZED_BOTH);
                jf.validate ();
                jf.setVisible (true);

                int totalHeight = 0;
                for (final BufferedImage bi : images.values ()) {
                    totalHeight += bi.getHeight ();
                }
                final int height = totalHeight;

                jc = new JComponent() {
                    @Override
                    protected void paintComponent (final Graphics g) {
                        super.paintComponent (g);
                        
                        int y = 0;
                        for (final BufferedImage bi : images.values ()) {
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
                
                
                jsp = new JScrollPane (jc);
                jsp.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                jsp.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                jf.setContentPane (jsp);
                
                
                final MouseAdapter mouseListener = new MouseAdapter () {
                    @Override
                    public void mouseDragged(final MouseEvent e) {
                        final int eventY = e.getYOnScreen ();
                        
                        final int diffY = - eventY + y;
                        
                        translate (diffY);
                        
                        y = eventY;
                    }
                    @Override
                    public void mousePressed (MouseEvent e) {
                        y = e.getYOnScreen ();
                    }
                    
                    @Override
                    public void mouseWheelMoved (MouseWheelEvent e) {
                        final int wheelRotation = e.getWheelRotation ();
                        smoothScroll (wheelRotation);
                    }
                    
                    private int y;
                };
                
                jc.addMouseMotionListener (mouseListener);
                jc.addMouseListener (mouseListener);
                jc.addMouseWheelListener (mouseListener);
                
                jc.getToolkit ().addAWTEventListener (new AWTEventListener() {
                    @Override
                    public void eventDispatched (AWTEvent event) {
                        if (event instanceof KeyEvent) {
                            final KeyEvent e = (KeyEvent) event;
                            if (e.getKeyCode () == KeyEvent.VK_ESCAPE) {
                                System.exit (0);
                            }
                            if (e.getKeyCode () == KeyEvent.VK_UP) {
                                smoothScroll (-1);
                            }
                            if (e.getKeyCode () == KeyEvent.VK_DOWN) {
                                smoothScroll (+1);
                            }
                        }
                    }
                }, AWTEvent.KEY_EVENT_MASK);
            }

            private void smoothScroll (final int wheelRotation) {
                final int[] counter = new int [1];
                counter[0] = 40;
                SwingUtilities.invokeLater (new Runnable () {
                    @Override
                    public void run () {
                        if (counter[0] > 0) {
                            translate (wheelRotation);
                            counter[0]--;
                            SwingUtilities.invokeLater (this);
                        }
                    }
                });
            }
            
            private void translate (final int diffY) {
                final Point vp = jsp.getViewport ().getViewPosition ();
                vp.translate (0, diffY);
                jc.scrollRectToVisible (new Rectangle (vp, jsp.getViewport ().getSize ()));
            }
        });
    }

    private static final int PROGRESS_BAR_WIDTH = 200;
    private static final int PROGRESS_BAR_HEIGHT = 60;
}
