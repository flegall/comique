package lobstre.comique;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;


/**
 * Progress Dialog implementation
 */
public class ProgressDialog {
    /**
     * Builds a {@link ProgressDialog} instance
     * 
     * @param screenRes
     *            an array containing the screen resolution
     */
    public ProgressDialog (final int[] screenRes) {
        this.screenRes = screenRes;
    }

    /**
     * Shows the dialog :)
     */
    public void show () {
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                jf.setSize(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
                jf.setLocation(screenRes[0] / 2 - PROGRESS_BAR_WIDTH / 2, screenRes[1] / 2 - PROGRESS_BAR_HEIGHT / 2);
                jf.setResizable(false);

                final JPanel pane = new JPanel ();
                pane.setLayout (new FlowLayout ());
                progressBar.setValue (0);
                progressBar.setStringPainted (true);
                pane.add (progressBar);
                jf.setContentPane (pane);
                jf.setVisible (true);
                jf.setAlwaysOnTop (true);
            }
        });
    }

    /**
     * Gets the {@link ProgressListener} instance :)
     * 
     * @return a {@link ProgressListener}
     */
    public ProgressListener getProgressListener () {
        return new ProgressListener () {
            @Override
            public void progressed (final int processedPages, final int totalPages) {
                SwingUtilities.invokeLater (new Runnable () {
                    @Override
                    public void run () {
                        progressBar.setValue (processedPages);
                        progressBar.setMaximum (totalPages);
                    }
                });
            }
        };
    }

    /**
     * Hides the dialog
     */
    public void hide () {
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                jf.setVisible(false);
                jf.dispose();
            }
        });
    }

    private final int[] screenRes;
    private final JFrame jf = new JFrame ("Comique - loading");
    private final JProgressBar progressBar = new JProgressBar ();

    private static final int PROGRESS_BAR_WIDTH = 200;
    private static final int PROGRESS_BAR_HEIGHT = 60;
}
