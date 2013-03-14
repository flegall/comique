package lobstre.comique.util;

import lobstre.comique.util.filedrop.FileDrop;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.*;

@SuppressWarnings("serial")
public class FullScreenComplex {

    private final JScrollPane jsp;
    private final Renderer r;

    public FullScreenComplex() {
        r = new Renderer();
        jsp = new JScrollPane(r);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void enableFullScreenMode(Window window) {
        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class params[] = new Class[]{Window.class, Boolean.TYPE};
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, window, true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void requestToggleFullScreen(Window window)
    {
        try {
            Class appClass = Class.forName("com.apple.eawt.Application");
            Class params[] = new Class[]{};

            Method getApplication = appClass.getMethod("getApplication", params);
            Object application = getApplication.invoke(appClass);
            Method requestToggleFulLScreen = application.getClass().getMethod("requestToggleFullScreen", Window.class);

            requestToggleFulLScreen.invoke(application, window);
        } catch (Exception e) {
            System.out.println("An exception occurred while trying to toggle full screen mode");
        }
    }

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        FullScreenComplex fs = new FullScreenComplex();
        fs.show();
    }

    public void show() throws InvocationTargetException, InterruptedException {
        final JFrame[] res = new JFrame[]{null};
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                final JFrame jf = new JFrame ();
                jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jf.setSize(new Dimension(600, 400));

                enableFullScreenMode(jf);
                SwingUtilities.invokeLater(new Runnable (){
                    @Override
                    public void run() {
                        requestToggleFullScreen(jf);
                    }
                });

                jf.setVisible(true);


                jsp.setBorder(null);
                jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                jf.add (jsp);

                new FileDrop(System.out, r, new FileDrop.Listener () {
                    public void filesDropped (File[] files) {
                        System.out.println(files.toString());
                        }
                    });

                r.addMouseMotionListener(mouseListener);
                r.addMouseListener(mouseListener);
                r.addMouseWheelListener(mouseListener);

                r.getToolkit().addAWTEventListener (keyListener, AWTEvent.KEY_EVENT_MASK);
            }
        });
    }

    private final class Renderer extends JComponent {
        @Override
        public Dimension getPreferredSize () {
            return new Dimension (1440, 4000);
        }

        @Override
        protected void paintComponent (final Graphics g) {
            super.paintComponent (g);

            Graphics g2 = g.create();
            for (int y = 0; y < 4000; y+=20) {
                if ((y/20)%2 == 1) {
                    g2.setColor (Color.YELLOW);
                } else {
                    g2.setColor (Color.RED);
                }
                g2.fillRect (0, y, 1440, 20);
            }

            g2.dispose();
        }

        private Renderer () {
        }

        private static final long serialVersionUID = 1L;
    }

    private final MouseAdapter mouseListener = new MouseAdapter () {
        @Override
        public void mouseDragged(final MouseEvent e) {
        }
        @Override
        public void mousePressed (MouseEvent e) {
        }

        @Override
        public void mouseWheelMoved (MouseWheelEvent e) {
        }
    };

    private final AWTEventListener keyListener = new AWTEventListener() {
        @Override
        public void eventDispatched (AWTEvent event) {
        }
    };
}
