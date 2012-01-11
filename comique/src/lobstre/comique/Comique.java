package lobstre.comique;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Comique {
    public static void main (final String[] args) {
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment ();
        final GraphicsDevice gd = ge.getDefaultScreenDevice ();
        final int width = gd.getDisplayMode ().getWidth ();
        final int height = gd.getDisplayMode ().getHeight ();
        System.out.println (width);
        System.out.println (height);
    }
}
