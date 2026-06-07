import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Performance test: đọc 12,000 dòng attendance.csv trong < 1 giây.
 * Dùng AttendanceRepository (BufferedReader trong CsvRepository).
 */
public class PerformanceTest {

    public static void main(String[] args) throws Exception {
        File tmp = File.createTempFile("attendance_perf_", ".csv");
        tmp.deleteOnExit();

        try (PrintWriter writer = new PrintWriter(new FileWriter(tmp))) {
            writer.println("id,version,employeeId,yearMonth,workDays,overtimeHours");
            for (int i = 0; i < 12000; i++) {
                String id = String.format("A%06d", i);
                String line = String.format("%s,0,E%04d,2024-05,20,2.5", id, i % 1000);
                writer.println(line);
            }
        }

        AttendanceRepository repo = new AttendanceRepository(tmp.getAbsolutePath());

        long start = System.currentTimeMillis();
        int count = repo.findAll().size();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Read count=" + count + ", elapsedMs=" + elapsed);

        if (count < 12000) {
            System.err.println("[FAIL] Expected 12000 lines, got " + count);
            System.exit(1);
        }

        if (elapsed <= 1000) {
            System.out.println("[PASS] Read 12k lines in " + elapsed + " ms");
        } else {
            System.err.println("[FAIL] Read too slow: " + elapsed + " ms");
            System.exit(1);
        }
    }
}
