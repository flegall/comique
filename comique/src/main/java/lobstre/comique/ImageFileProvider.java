package lobstre.comique;

import java.io.File;

public interface ImageFileProvider {
    /**
     * Builds/Gets a {@link File} to display
     * 
     * @return a {@link File} instance
     */
    public File getFile ();
}
