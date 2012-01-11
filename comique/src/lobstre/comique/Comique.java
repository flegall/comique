package lobstre.comique;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Comique {
    public static void main (final String[] args) {
        final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment ().getDefaultScreenDevice ();
        final int width = gd.getDisplayMode ().getWidth ();
        final int height = gd.getDisplayMode ().getHeight ();
        System.out.println (width);
        System.out.println (height);
    }
}
