package lobstre.comique;

import java.awt.AWTEvent;
import java.awt.Dimension;
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
import java.io.IOException;
import java.util.NavigableMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import lobstre.comique.util.filedrop.FileDrop;

/**
 * Reader 
 */
public class ComiqueReader {

    /**
     * Builds a {@link ComiqueReader} instance
     * @param images a {@link NavigableMap} of {@link Integer} to {@link BufferedImage} images
     * @param screenRes an array containing the screen resolution
     */
    public ComiqueReader (final NavigableMap<Integer, BufferedImage> images, final int[] screenRes) {
        this.images = images;
        this.screenRes = screenRes;
        this.renderer = new Renderer ();
        this.jsp = new JScrollPane (renderer);
        this.currentPage = images.firstKey ();
    }

    public void show () {
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                final JFrame jf = new JFrame ("Comique");
                jf.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
                jf.setUndecorated (true);
                jf.setSize (new Dimension (screenRes[0], screenRes [1]));
                jf.setLocationRelativeTo (null);
                jf.setExtendedState (Frame.MAXIMIZED_BOTH);
                jf.validate ();
                jf.setVisible (true);

                jsp.setBorder (null);
                jsp.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                jsp.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                jf.setContentPane (jsp);
                
                new FileDrop (System.out, renderer, new FileDrop.Listener () {
                    public void filesDropped (File[] files) {
                        if (files.length > 0) {
                            final String[] args = new String[] {files[0].getPath ()};
                            final String[] cmds = ComiqueStartup.prepareCommands (args);
                            try {
                                Runtime.getRuntime ().exec (cmds);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }
                            System.exit (0);
                        }
                    } 
                }); 
                
                renderer.addMouseMotionListener (mouseListener);
                renderer.addMouseListener (mouseListener);
                renderer.addMouseWheelListener (mouseListener);
                
                renderer.getToolkit ().addAWTEventListener (keyListener, AWTEvent.KEY_EVENT_MASK);
            }
        });
    }
    
    private final class Renderer extends JComponent {
        @Override
        public Dimension getPreferredSize () {
            final BufferedImage currentImage = images.get (currentPage);
            return new Dimension (screenRes[0], currentImage.getHeight ());
        }

        @Override
        protected void paintComponent (final Graphics g) {
            super.paintComponent (g);
            
            final BufferedImage bi = images.get (currentPage);
            g.drawImage (bi, 0, 0, null);
        }

        private Renderer () {
        }

        private static final long serialVersionUID = 1L;
    }
    
    private void smoothScroll (final int direction, int ticks) {
        smoothScrolling = true;
        final int[] counter = new int [1];
        counter[0] = ticks;
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                if (smoothScrolling) {
                    if (counter[0] > 0) {
                        translate (direction);
                        counter[0]--;
                        SwingUtilities.invokeLater (this);
                    }
                }
            }
        });
    }

    private void translate (final int diffY) {
        final Point vp = jsp.getViewport ().getViewPosition ();
        vp.translate (0, diffY);
        renderer.scrollRectToVisible (new Rectangle (vp, jsp.getViewport ().getSize ()));
    }

    private void pageSwitch (boolean up) {
        Integer nextKey;
        if (up) {
            nextKey = images.higherKey (this.currentPage);
        } else {
            nextKey = images.lowerKey (this.currentPage);
        }
        
        smoothScrolling = false;
        if (null != nextKey) {
            currentPage = nextKey;
            renderer.repaint ();
            renderer.invalidate ();
            renderer.scrollRectToVisible (
                new Rectangle (
                        new Point (), 
                        jsp.getViewport ().getSize ()));
        }
    }

    private boolean smoothScrolling = false;
    private int currentPage;
    private final int[] screenRes;
    private final NavigableMap<Integer, BufferedImage> images;
    
    private final MouseAdapter mouseListener = new MouseAdapter () {
        @Override
        public void mouseDragged(final MouseEvent e) {
            final int eventY = e.getYOnScreen ();
            final int diffY = - eventY + y;
            
            smoothScrolling = false;
            translate (diffY);
            
            y = eventY;
        }
        @Override
        public void mousePressed (MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton (e)) {
                y = e.getYOnScreen ();
            }
            
            if (SwingUtilities.isRightMouseButton (e)) {
                pageSwitch (true);
            }
            if (SwingUtilities.isMiddleMouseButton (e)) {
                pageSwitch (false);
            }
        }
        
        @Override
        public void mouseWheelMoved (MouseWheelEvent e) {
            final int wheelRotation = e.getWheelRotation ();
            smoothScroll (wheelRotation, 50);
        }
        
        private int y;
    };
    
    private final AWTEventListener keyListener = new AWTEventListener() {
        @Override
        public void eventDispatched (AWTEvent event) {
            if (event instanceof KeyEvent) {
                final KeyEvent e = (KeyEvent) event;
                if (e.getID () == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode () == KeyEvent.VK_ESCAPE) {
                        System.exit (0);
                    }
                    
                    if (e.getKeyCode () == KeyEvent.VK_UP
                        || e.getKeyCode () == KeyEvent.VK_K) {
                        smoothScroll (-1, 100);
                    }
                    if (e.getKeyCode () == KeyEvent.VK_DOWN
                        || e.getKeyCode () == KeyEvent.VK_J
                        || e.getKeyCode () == KeyEvent.VK_SPACE) {
                        smoothScroll (+1, 100);
                    }
                    
                    if (e.getKeyCode () == KeyEvent.VK_PAGE_UP 
                        || e.getKeyCode () == KeyEvent.VK_LEFT
                        || e.getKeyCode () == KeyEvent.VK_P
                        || e.getKeyCode () == KeyEvent.VK_BACK_SPACE) {
                        pageSwitch (false);
                    }
                    if (e.getKeyCode () == KeyEvent.VK_PAGE_DOWN
                        || e.getKeyCode () == KeyEvent.VK_RIGHT
                        || e.getKeyCode () == KeyEvent.VK_N
                        || e.getKeyCode () == KeyEvent.VK_ENTER) {
                        pageSwitch (true);
                    }
                }
            }
        }
    };
    
    private final JScrollPane jsp;
    private final Renderer renderer;
}
