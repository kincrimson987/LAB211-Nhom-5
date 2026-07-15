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
        List<T> all = readAllLines();
        all.add(entity);
        writeAllLines(all);
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
        List<T> all = readAllLines();
        all.removeIf(entity -> getId(entity).equals(id));
        writeAllLines(all);
    }

    public List<T> readAllLines() {
        synchronized (filePath.intern()) {
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
            } catch (IOException e) {
                throw new RuntimeException("Failed to read CSV: " + filePath, e);
            }
            return list;
        }
    }

    public void writeAllLines(List<T> entities) {
        synchronized (filePath.intern()) {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(getHeader());
                for (T entity : entities) {
                    writer.println(toLine(entity));
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to write CSV: " + filePath, e);
            }
        }
    }
}
