/**
 * Represents a single book entry in the library catalog.
 */
public class Book {
    private String title;
    private String author;
    private String isbn;
    private int copies;

    /**
     * Constructs a new Book with the given details.
     *
     * @param title  the title of the book
     * @param author the author of the book
     * @param isbn   the 13-digit ISBN of the book
     * @param copies the number of available copies
     */
    public Book(String title, String author, String isbn, int copies) {
        this.title  = title;
        this.author = author;
        this.isbn   = isbn;
        this.copies = copies;
    }

    /**
     * Returns the title of the book.
     *
     * @return the book title
     */
    public String getTitle()  { return title; }

    /**
     * Returns the author of the book.
     *
     * @return the book author
     */
    public String getAuthor() { return author; }

    /**
     * Returns the ISBN of the book.
     *
     * @return the 13-digit ISBN string
     */
    public String getIsbn()   { return isbn; }

    /**
     * Returns the number of available copies.
     *
     * @return the number of copies
     */
    public int getCopies() { return copies; }

    /**
     * Formats the book as a catalog file line.
     *
     * @return a string in the format Title:Author:ISBN:Copies
     */
    public String toFileString() {
        return title + ":" + author + ":" + isbn + ":" + copies;
    }
}
