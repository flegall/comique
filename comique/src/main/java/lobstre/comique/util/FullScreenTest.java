package lobstre.comique.util;


import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

import com.apple.eawt.AppEvent.FullScreenEvent;
import com.apple.eawt.*;

public class FullScreenTest {
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { createFrame(); }
        });
    }

    static void createFrame() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle(System.getProperty("java.version"));

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createPanel(), BorderLayout.CENTER);

        // turns on the fullscreen window titlebar widget in the upper right corner
        FullScreenUtilities.setWindowCanFullScreen(frame, true);

        // useful for re-adjusting content or hiding/showing palette windows
        FullScreenUtilities.addFullScreenListenerTo(frame, new FullScreenAdapter() {
            public void windowExitingFullScreen(FullScreenEvent e) {
                System.out.println("exiting");
            }
            public void windowExitedFullScreen(FullScreenEvent e) {
                System.out.println("exited");
            }
            public void windowEnteringFullScreen(FullScreenEvent e) {
                System.out.println("entering");
            }
            public void windowEnteredFullScreen(FullScreenEvent e) {
                System.out.println("entered");
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    static Component createPanel() {
        final JPanel panel = new JPanel(new FlowLayout());

        // toggle FullScreen from a toolbar button
        panel.add(new JButton(new AbstractAction("Full Screen Me!") {
            public void actionPerformed(ActionEvent e) {
                Application.getApplication().requestToggleFullScreen(
                        (Window)panel.getTopLevelAncestor());
            }
        }));

        return panel;
    }
}