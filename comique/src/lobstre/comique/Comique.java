package lobstre.comique;

import lobstre.comique.util.Helper;

public class Comique {
    public static void main (final String[] args) {
        final int[] result = Helper.getScreenResolution ();
        System.out.println (result [0]);
    }
}
