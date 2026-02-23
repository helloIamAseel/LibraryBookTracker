/**
 * Thrown when an ISBN is not exactly 13 digits or contains non-numeric characters.
 */
public class InvalidISBNException extends BookCatalogException {

    /**
     * @param message a description of the invalid ISBN error
     */
    public InvalidISBNException(String message) {
        super(message);
    }
}
