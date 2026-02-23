/**
 * Thrown when a catalog entry has missing fields, empty fields,
 * or an invalid copies value (less than 0).
 */
public class MalformedBookEntryException extends BookCatalogException {

    /**
     * @param message a description of what is malformed in the entry
     */
    public MalformedBookEntryException(String message) {
        super(message);
    }
}
