package lobstre.comique;

/**
 * Progression listener interface
 */
public interface ProgressListener {
    /**
     * Called when making progress
     * @param processedPages the number of processed pages
     * @param totalPages the total page count
     */
    void progressed (int processedPages, int totalPages);
}