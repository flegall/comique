package lobstre.comique;

import java.io.File;

public interface ImageFileProvider {
    /**
     * Builds/Gets a {@link File} to display
     * 
     * @return a {@link File} instance
     */
    public File getFile ();

    /**
     * Builds/Gets an array of byte containing the file to display
     * 
     * @return a byte array
     */
    public byte[] getImageFile ();
}
