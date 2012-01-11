package lobstre.comique.util;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Helper {

    /**
     * Gets the screen resolution in an integer array.
     * 
     * @return an int array containing two entries, the width and the height in
     *         pixels.
     */
    public static int[] getScreenResolution () {
        final int[] result = new int[2];
        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment ();
        final GraphicsDevice gd = ge.getDefaultScreenDevice ();
        result [0] = gd.getDisplayMode ().getWidth ();
        result [1] = gd.getDisplayMode ().getHeight ();
        return result;
    }

}
