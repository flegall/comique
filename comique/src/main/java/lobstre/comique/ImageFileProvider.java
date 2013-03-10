package lobstre.comique;


public interface ImageFileProvider {
    /**
     * Builds/Gets an array of byte containing the file to display
     * 
     * @return a byte array
     */
    public byte[] getImageFile ();
}
