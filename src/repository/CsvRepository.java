import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp cơ sở đọc/ghi CSV — các repository cụ thể kế thừa và override parse/toLine.
 */
public abstract class CsvRepository<T> {

    private static final int CSV_IO_MAX_RETRIES = 50;
    private static final long CSV_IO_RETRY_DELAY_MS = 100L;

    private String filePath;

    public CsvRepository(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public abstract String getHeader();

    public abstract String getId(T entity);

    public abstract String toLine(T entity);

    public abstract T parseLine(String line);

    public List<T> findAll() {
        return readAllLines();
    }

    public T findById(String id) {
        for (T entity : readAllLines()) {
            if (getId(entity).equals(id)) {
                return entity;
            }
        }
        return null;
    }

    public void save(T entity) {
        if (entity == null || getId(entity) == null || getId(entity).trim().isEmpty()) {
            throw new IllegalArgumentException("Entity ID cannot be empty.");
        }
        synchronized (filePath.intern()) {
            List<T> all = readAllLines();
            boolean duplicate = all.stream()
                    .anyMatch(existing -> getId(existing).equals(getId(entity)));
            if (duplicate) {
                throw new IllegalArgumentException("Entity already exists: " + getId(entity));
            }
            all.add(entity);
            writeAllLines(all);
        }
    }

    public void update(T entity) {
        synchronized (filePath.intern()) {
            List<T> all = readAllLines();
            boolean found = false;
            for (int i = 0; i < all.size(); i++) {
                if (getId(all.get(i)).equals(getId(entity))) {
                    all.set(i, entity);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Entity not found: " + getId(entity));
            }
            writeAllLines(all);
        }
    }

    public void delete(String id) {
        synchronized (filePath.intern()) {
            List<T> all = readAllLines();
            all.removeIf(entity -> getId(entity).equals(id));
            writeAllLines(all);
        }
    }

    public List<T> readAllLines() {
        synchronized (filePath.intern()) {
            IOException lastError = null;
            for (int attempt = 1; attempt <= CSV_IO_MAX_RETRIES; attempt++) {
                try {
                    return readAllLinesOnce();
                } catch (IOException e) {
                    lastError = e;
                    sleepBeforeRetry(attempt);
                }
            }
            throw new RuntimeException("Failed to read CSV: " + filePath
                    + " (" + lastError.getMessage() + ")", lastError);
        }
    }

    private List<T> readAllLinesOnce() throws IOException {
        List<T> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return list;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    list.add(parseLine(line));
                }
            }
        }
        return list;
    }

    public void writeAllLines(List<T> entities) {
        synchronized (filePath.intern()) {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            IOException lastError = null;
            for (int attempt = 1; attempt <= CSV_IO_MAX_RETRIES; attempt++) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println(getHeader());
                    for (T entity : entities) {
                        writer.println(toLine(entity));
                    }
                    return;
                } catch (IOException e) {
                    lastError = e;
                    sleepBeforeRetry(attempt);
                }
            }
            throw new RuntimeException("Failed to write CSV: " + filePath
                    + " (" + lastError.getMessage() + ")", lastError);
        }
    }

    private void sleepBeforeRetry(int attempt) {
        if (attempt >= CSV_IO_MAX_RETRIES) {
            return;
        }
        try {
            Thread.sleep(CSV_IO_RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("CSV operation interrupted: " + filePath, e);
        }
    }
}
