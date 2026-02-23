/**
 * Thrown when the catalog file name does not end with ".txt".
 */
public class InvalidFileNameException extends BookCatalogException {

    /**
     * @param message a description of the invalid file name error
     */
    public InvalidFileNameException(String message) {
        super(message);
    }
}
