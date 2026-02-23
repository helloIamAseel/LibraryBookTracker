/**
 * Thrown when fewer than two command-line arguments are provided.
 */
public class InsufficientArgumentsException extends BookCatalogException {

    /**
     * @param message a description of the missing arguments error
     */
    public InsufficientArgumentsException(String message) {
        super(message);
    }
}
