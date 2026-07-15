import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Repository đọc/ghi payroll_entries.csv.
 * Cung cấp CRUD + 4 cơ chế đồng bộ chống double payment.
 */
public class PayrollEntryRepository extends CsvRepository<PayrollEntry> {

    /*
     * private static final String DEFAULT_PATH = "data/payroll_entries.csv";
     * private static final String EMPLOYEES_PATH = "data/employees.csv";
     * private static final int OPTIMISTIC_MAX_RETRIES = 10;
     * private static final long OPTIMISTIC_BASE_BACKOFF_MS = 50L;
     * 
     * public PayrollEntryRepository() {
     * this(DEFAULT_PATH);
     * }
     * 
     * public PayrollEntryRepository(String filePath) {
     * super(filePath);
     * }
     */
    private String EMPLOYEES_PATH = "data/employees.csv";

    private int OPTIMISTIC_MAX_RETRIES = 10;

    private long OPTIMISTIC_BASE_BACKOFF_MS = 50L;

    public PayrollEntryRepository() {
        super("data/payroll_entries.csv");
    }

    public PayrollEntryRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        File file = new File(getFilePath());
        if (!file.exists()) {
            return new PayrollEntry().getCsvHeader();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            return header != null ? header : new PayrollEntry().getCsvHeader();
        } catch (IOException e) {
            return new PayrollEntry().getCsvHeader();
        }
    }

    @Override
    public String getId(PayrollEntry entity) {
        return entity.getId();
    }

    @Override
    public String toLine(PayrollEntry entity) {
        return entity.toCsvLine();
    }

    @Override
    public PayrollEntry parseLine(String line) {
        PayrollEntry pe = new PayrollEntry();
        pe.fromCsvLine(line);
        return pe;
    }

    // ==================== TÌM KIẾM ====================

    public PayrollEntry findByEmployeeAndMonth(String empId, String yearMonth) {
        for (PayrollEntry entry : findAll()) {
            if (entry.getEmployeeId().equals(empId)
                    && entry.extractYearMonth().equals(yearMonth)) {
                return entry;
            }
        }
        return null;
    }

    public List<PayrollEntry> findByStatus(PayrollStatus status) {
        return findAll().stream()
                .filter(entry -> entry.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<PayrollEntry> findByDeptAndMonth(String deptId, String yearMonth) {
        Set<String> employeeIds = loadEmployeeIdsByDepartment(deptId);
        return findAll().stream()
                .filter(entry -> employeeIds.contains(entry.getEmployeeId()))
                .filter(entry -> entry.extractYearMonth().equals(yearMonth))
                .collect(Collectors.toList());
    }

    public long countProcessedByEmployee(String employeeId) {
        return findAll().stream()
                .filter(entry -> entry.getEmployeeId().equals(employeeId))
                .filter(entry -> entry.getStatus() == PayrollStatus.PROCESSED)
                .count();
    }

    // ==================== 4 CƠ CHẾ ĐỒNG BỘ ====================

    /**
     * Cơ chế 1 — NO_LOCK: cố ý không đồng bộ để Simulator chứng minh double
     * payment.
     */
    public boolean processWithNoLock(String entryId, String processedBy) {
        PayrollEntry entry = findById(entryId);
        if (entry == null)
            return false;
        if (entry.getStatus() == PayrollStatus.PROCESSED)
            return false;

        try {
            Thread.sleep(5L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        markProcessed(entry);
        update(entry);
        return true;
    }

    /**
     * Cơ chế 2 — FILE_LOCK: khóa toàn bộ file CSV.
     */
    public boolean processWithFileLock(String entryId, String processedBy) {
        synchronized (getFilePath().intern()) {
            try (RandomAccessFile raf = new RandomAccessFile(getFilePath(), "rw");
                    FileChannel channel = raf.getChannel();
                    FileLock lock = channel.lock()) {

            // Trên Windows, khi channel đang khóa file thì không thể mở lại cùng
            // file bằng FileReader/FileWriter. Đọc và ghi trực tiếp qua channel
            // đang giữ khóa để tránh lỗi "locked a portion of the file".
            if (channel.size() > Integer.MAX_VALUE) {
                throw new IOException("Payroll CSV is too large to process safely.");
            }

            ByteBuffer input = ByteBuffer.allocate((int) channel.size());
            channel.position(0);
            while (input.hasRemaining() && channel.read(input) != -1) {
                // Đọc hết nội dung trong cùng file channel.
            }
            input.flip();
            String content = StandardCharsets.UTF_8.decode(input).toString();
            String[] lines = content.split("\\R");

            String header = lines.length > 0 && !lines[0].trim().isEmpty()
                    ? lines[0]
                    : new PayrollEntry().getCsvHeader();
            List<PayrollEntry> all = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                if (!lines[i].trim().isEmpty()) {
                    all.add(parseLine(lines[i]));
                }
            }
            PayrollEntry entry = findInList(all, entryId);
            if (entry == null)
                return false;
            if (entry.getStatus() == PayrollStatus.PROCESSED)
                return false;

            markProcessed(entry);
            StringBuilder output = new StringBuilder(header).append(System.lineSeparator());
            for (PayrollEntry item : all) {
                output.append(toLine(item)).append(System.lineSeparator());
            }

            ByteBuffer bytes = StandardCharsets.UTF_8.encode(output.toString());
            channel.truncate(0);
            channel.position(0);
            while (bytes.hasRemaining()) {
                channel.write(bytes);
            }
            channel.force(true);
                return true;
            } catch (IOException e) {
                throw new RuntimeException("File lock failed for entry: " + entryId, e);
            }
        }
    }

    /**
     * Cơ chế 3 — SYNCHRONIZED: khóa per-employee qua empId.intern().
     */
    public boolean processWithSync(String entryId, String processedBy) {
        PayrollEntry snapshot = findById(entryId);
        if (snapshot == null)
            return false;

        synchronized (snapshot.getEmployeeId().intern()) {
            PayrollEntry entry = findById(entryId);
            if (entry == null)
                return false;
            if (entry.getStatus() == PayrollStatus.PROCESSED)
                return false;

            markProcessed(entry);
            update(entry);
            return true;
        }
    }

    /**
     * Cơ chế 4 — OPTIMISTIC: version field + retry với exponential backoff.
     */
    public boolean processWithOptimistic(String entryId, String processedBy) {
        for (int attempt = 0; attempt < OPTIMISTIC_MAX_RETRIES; attempt++) {
            PayrollEntry entry = findById(entryId);
            if (entry == null)
                return false;
            if (entry.getStatus() == PayrollStatus.PROCESSED)
                return false;

            long expectedVersion = entry.getVersion();
            markProcessed(entry);

            if (updateIfVersionMatch(entry, expectedVersion)) {
                return true;
            }

            sleepWithBackoff(attempt);
        }
        return false;
    }

    /**
     * Ghi entry chỉ khi version trong file vẫn khớp expectedVersion.
     */
    boolean updateIfVersionMatch(PayrollEntry updated, long expectedVersion) {
        synchronized (getFilePath().intern()) {
            File lockFile = new File(getFilePath());
            try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
                 FileChannel channel = raf.getChannel();
                 FileLock lock = channel.lock()) {

                List<PayrollEntry> all = new ArrayList<>();
                String header = raf.readLine();
                String line;
                while ((line = raf.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        all.add(parseLine(line));
                    }
                }

                boolean updatedAny = false;
                for (int i = 0; i < all.size(); i++) {
                    PayrollEntry current = all.get(i);
                    if (!current.getId().equals(updated.getId())) {
                        continue;
                    }
                    if (current.getVersion() != expectedVersion)
                        return false;
                    all.set(i, updated);
                    updatedAny = true;
                    break;
                }

                if (!updatedAny) {
                    return false;
                }

                raf.setLength(0);
                raf.seek(0);
                raf.writeBytes(header != null ? header : new PayrollEntry().getCsvHeader());
                raf.writeBytes(System.lineSeparator());
                for (PayrollEntry entry : all) {
                    raf.writeBytes(entry.toCsvLine());
                    raf.writeBytes(System.lineSeparator());
                }
                return true;
            } catch (IOException e) {
                throw new RuntimeException("Optimistic update failed for entry: " + updated.getId(), e);
            }
        }
    }

    // ==================== HELPERS ====================

    /**
     * Tìm entry trong list theo entryId — instance method thay vì static.
     */
    private PayrollEntry findInList(List<PayrollEntry> entries, String entryId) {
        for (PayrollEntry entry : entries) {
            if (entry.getId().equals(entryId)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Đánh dấu entry là đã xử lý — instance method thay vì static.
     */
    private void markProcessed(PayrollEntry entry) {
        entry.setStatus(PayrollStatus.PROCESSED);
        entry.setVersion(entry.getVersion() + 1);
    }

    private void sleepWithBackoff(int attempt) {
        try {
            Thread.sleep(OPTIMISTIC_BASE_BACKOFF_MS * (1L << attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Set<String> loadEmployeeIdsByDepartment(String deptId) {
        Set<String> ids = new HashSet<>();
        File empFile = new File(EMPLOYEES_PATH);
        if (!empFile.exists())
            return ids;

        try (BufferedReader reader = new BufferedReader(new FileReader(empFile))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[4].trim().equals(deptId)) {
                    ids.add(parts[0].trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read employees CSV", e);
        }
        return ids;
    }
    public void processWithFileLock(String filePath, Runnable saveAction) throws Exception {
        FileLockManager.executeWithLock(filePath, () -> {
            saveAction.run();
            return null;
        });
    }
}
