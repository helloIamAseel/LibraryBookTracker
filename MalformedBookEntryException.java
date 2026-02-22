/**
 * Thrown when a catalog entry has missing fields, empty fields,
 * or an invalid copies value (not a positive integer).
 */
public class MalformedBookEntryException extends BookCatalogException {

    /**
     * Constructs a new MalformedBookEntryException with the given message.
     *
     * @param message a description of what is malformed in the entry
     */
    public MalformedBookEntryException(String message) {
        super(message);
    }
}
