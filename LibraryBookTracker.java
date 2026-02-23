import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class LibraryBookTracker {

    private static int validRecords  = 0;
    private static int searchResults = 0;
    private static int booksAdded    = 0;
    private static int errorCount    = 0;

    private static Path errorLogPath;

    /**
     * @param args command-line arguments where args[0] is the catalog file
     *              and args[1] is the operation
     */
    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            System.out.println("Thank you for using the Library Book Tracker.")));

        try {
            if (args.length < 2) {
                throw new InsufficientArgumentsException(
                    "Need at least 2 arguments: <catalogFile.txt> <operation>");
            }

            String catalogPath = args[0];
            if (!catalogPath.endsWith(".txt")) {
                throw new InvalidFileNameException(
                    "Catalog file must end with .txt, got: " + catalogPath);
            }

            Path filePath = Paths.get(catalogPath);
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            Path dir = filePath.getParent() != null ? filePath.getParent() : Paths.get(".");
            errorLogPath = dir.resolve("errors.log");

            List<Book> catalog = new ArrayList<>();
            List<String> rawLines = Files.readAllLines(filePath);

            for (String line : rawLines) {
                if (line.trim().isEmpty()) continue;
                try {
                    Book b = parseLine(line);
                    catalog.add(b);
                    validRecords++;
                } catch (BookCatalogException e) {
                    logError(line, e);
                    errorCount++;
                }
            }

            String operation = args[1];

            if (isISBN(operation)) {
                doISBNSearch(catalog, operation);
            } else if (isNewRecord(operation)) {
                doAddBook(catalog, filePath, operation);
            } else {
                doTitleSearch(catalog, operation);
            }

        } catch (InsufficientArgumentsException | InvalidFileNameException e) {
            System.out.println("Error: " + e.getMessage());
            errorCount++;
        } catch (IOException e) {
            System.out.println("File I/O error: " + e.getMessage());
            errorCount++;
        }

        System.out.println();
        System.out.println("Valid records processed : " + validRecords);
        System.out.println("Search results          : " + searchResults);
        System.out.println("Books added             : " + booksAdded);
        System.out.println("Errors encountered      : " + errorCount);
    }

    /**
     * @param line the raw text line from the catalog file
     * @return a valid Book object constructed from the line
     * @throws MalformedBookEntryException if any field is missing, empty, or copies is invalid
     * @throws InvalidISBNException if the ISBN is not exactly 13 numeric digits
     */
    public static Book parseLine(String line) throws BookCatalogException {
        String[] parts = line.split(":", -1);

        if (parts.length != 4) {
            throw new MalformedBookEntryException(
                "Expected 4 fields (Title:Author:ISBN:Copies), got " + parts.length);
        }

        String title     = parts[0].trim();
        String author    = parts[1].trim();
        String isbn      = parts[2].trim();
        String copiesStr = parts[3].trim();

        if (title.isEmpty()) {
            throw new MalformedBookEntryException("Title is empty");
        }
        if (author.isEmpty()) {
            throw new MalformedBookEntryException("Author is empty");
        }
        if (!isbn.matches("\\d{13}")) {
            throw new InvalidISBNException(
                "ISBN must be exactly 13 digits, got: '" + isbn + "'");
        }

        int copies;
        try {
            copies = Integer.parseInt(copiesStr);
        } catch (NumberFormatException e) {
            throw new MalformedBookEntryException("Copies is not an integer: '" + copiesStr + "'");
        }
        if (copies <= 0) {
            throw new MalformedBookEntryException("Copies must be > 0, got: " + copies);
        }

        return new Book(title, author, isbn, copies);
    }

    /**
     * @param arg the operation argument from the command line
     * @return true if the argument is exactly 13 digits, false otherwise
     */
    public static boolean isISBN(String arg) {
        return arg.matches("\\d{13}");
    }

    /**
     * @param arg the operation argument from the command line
     * @return true if the argument contains exactly 3 colons, false otherwise
     */
    public static boolean isNewRecord(String arg) {
        long colons = arg.chars().filter(c -> c == ':').count();
        return colons == 3;
    }

    /**
     * @param catalog the list of books loaded from the catalog file
     * @param keyword the title keyword to search for
     */
    public static void doTitleSearch(List<Book> catalog, String keyword) {
        String lowerKey = keyword.toLowerCase();
        List<Book> matches = new ArrayList<>();
        for (Book b : catalog) {
            if (b.getTitle().toLowerCase().contains(lowerKey)) {
                matches.add(b);
            }
        }

        if (matches.isEmpty()) {
            System.out.println("No books found matching title keyword: " + keyword);
            MalformedBookEntryException e = new MalformedBookEntryException(
                "No results for title keyword: " + keyword);
            logError(keyword, e);
            errorCount++;
        } else {
            printHeader();
            for (Book b : matches) {
                printBook(b);
                searchResults++;
            }
        }
    }

    /**
     * @param catalog the list of books loaded from the catalog file
     * @param isbn    the 13-digit ISBN to search for
     * @throws DuplicateISBNException if more than one book with the same ISBN is found
     */
    public static void doISBNSearch(List<Book> catalog, String isbn) {
        List<Book> matches = new ArrayList<>();
        for (Book b : catalog) {
            if (b.getIsbn().equals(isbn)) {
                matches.add(b);
            }
        }

        try {
            if (matches.size() > 1) {
                throw new DuplicateISBNException(
                    "More than one book found with ISBN: " + isbn);
            }
            if (matches.isEmpty()) {
                System.out.println("No book found with ISBN: " + isbn);
            } else {
                printHeader();
                printBook(matches.get(0));
                searchResults++;
            }
        } catch (DuplicateISBNException e) {
            System.out.println("Error: " + e.getMessage());
            logError(isbn, e);
            errorCount++;
        }
    }

    /**
     * @param catalog  the list of books currently in the catalog
     * @param filePath the path to the catalog file to write to
     * @param record   the new book record string in Title:Author:ISBN:Copies format
     * @throws IOException if the file cannot be written
     */
    public static void doAddBook(List<Book> catalog, Path filePath, String record) throws IOException {
        try {
            Book newBook = parseLine(record);

            catalog.add(newBook);
            catalog.sort(Comparator.comparing(b -> b.getTitle().toLowerCase()));

            List<String> lines = new ArrayList<>();
            for (Book b : catalog) {
                lines.add(b.toFileString());
            }
            Files.write(filePath, lines);

            System.out.println("Book added successfully:");
            printHeader();
            printBook(newBook);
            booksAdded++;

        } catch (BookCatalogException e) {
            System.out.println("Error adding book: " + e.getMessage());
            logError(record, e);
            errorCount++;
        }
    }

    /**
     * Prints the table header row with column names in aligned format.
     */
    public static void printHeader() {
        System.out.printf("%-30s %-20s %-15s %5s%n",
            "Title", "Author", "ISBN", "Copies");
        System.out.println("-".repeat(72));
    }

    /**
     * @param b the Book object to print
     */
    public static void printBook(Book b) {
        System.out.printf("%-30s %-20s %-15s %5d%n",
            b.getTitle(), b.getAuthor(), b.getIsbn(), b.getCopies());
    }

    /**
     * @param offendingText the text that caused the error (a bad catalog line or bad argument)
     * @param e             the exception that was thrown, used for its class name and message
     */
    public static void logError(String offendingText, Exception e) {
        if (errorLogPath == null) return;

        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String logLine = String.format("[%s] INVALID INPUT: \"%s\" - %s: %s%n",
            timestamp,
            offendingText,
            e.getClass().getSimpleName(),
            e.getMessage());

        try (FileWriter fw = new FileWriter(errorLogPath.toFile(), true)) {
            fw.write(logLine);
        } catch (IOException ioEx) {
            System.out.println("Warning: could not write to error log: " + ioEx.getMessage());
        }
    }
}
