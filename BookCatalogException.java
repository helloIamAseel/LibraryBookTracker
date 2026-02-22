/**
 * Base exception for all Library Book Tracker errors.
 * All custom exceptions in this program extend this class.
 */
public class BookCatalogException extends Exception {

    /**
     * Constructs a new BookCatalogException with the given message.
     *
     * @param message a description of the error
     */
    public BookCatalogException(String message) {
        super(message);
    }
}
