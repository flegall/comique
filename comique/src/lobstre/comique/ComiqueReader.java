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
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

/**
 * Reader 
 */
public class ComiqueReader {

    /**
     * Builds a {@link ComiqueReader} instance
     * @param images a {@link Map} of {@link Integer} to {@link BufferedImage} images
     * @param screenRes an array containing the screen resolution
     */
    public ComiqueReader (final Map<Integer, BufferedImage> images, final int[] screenRes) {
        this.images = images;
        this.screenRes = screenRes;
        this.jc = new Renderer ();
        this.jsp = new JScrollPane (jc);
        int totalHeight = 0;
        for (final BufferedImage bi : images.values ()) {
            this.pagesIndexes.add (totalHeight);
            totalHeight += bi.getHeight ();
        }
        this.height = totalHeight;
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
                
                jc.addMouseMotionListener (mouseListener);
                jc.addMouseListener (mouseListener);
                jc.addMouseWheelListener (mouseListener);
                
                jc.getToolkit ().addAWTEventListener (keyListener, AWTEvent.KEY_EVENT_MASK);
            }
        });
    }
    
    private final class Renderer extends JComponent {
        @Override
        public Dimension getPreferredSize () {
            return new Dimension (screenRes[0], height);
        }

        @Override
        protected void paintComponent (final Graphics g) {
            super.paintComponent (g);
            
            int y = 0;
            for (final BufferedImage bi : images.values ()) {
                g.drawImage (bi, 0, y, null);
                y += bi.getHeight ();
            }
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
        jc.scrollRectToVisible (new Rectangle (vp, jsp.getViewport ().getSize ()));
    }

    private void pageSwitch (boolean up) {
        smoothScrolling = false;
        final Point vp = jsp.getViewport ().getViewPosition ();
        final Integer next;
        if (up) {
            next = this.pagesIndexes.higher (vp.y);
        } else {
            next = this.pagesIndexes.lower (vp.y);
        }
        if (null != next) {
            translate (next - vp.y);
        }
    }

    private boolean smoothScrolling = false;
    private final int[] screenRes;
    private final Map<Integer, BufferedImage> images;
    private final int height;
    private final NavigableSet<Integer> pagesIndexes = new TreeSet<Integer> ();
    
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
            y = e.getYOnScreen ();
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
                    if (e.getKeyCode () == KeyEvent.VK_UP) {
                        smoothScroll (-1, 100);
                    }
                    if (e.getKeyCode () == KeyEvent.VK_DOWN) {
                        smoothScroll (+1, 100);
                    }
                    
                    if (e.getKeyCode () == KeyEvent.VK_PAGE_UP) {
                        pageSwitch (false);
                    }
                    if (e.getKeyCode () == KeyEvent.VK_PAGE_DOWN) {
                        pageSwitch (true);
                    }
                }
            }
        }
    };
    
    private final JScrollPane jsp;
    private final JComponent jc;
}
