package repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Attendance CSV Repository Test Suite")
public class AttendanceCsvRepositoryTest {

    private static final String FILE_PATH = "data/attendance.csv";

    @Test
    @DisplayName("Read attendance.csv with at least 12000 rows")
    public void testReadAttendanceCsvWith12000Rows() throws IOException {
        int count = countRows();

        System.out.println("======================================");
        System.out.println("TEST: Read attendance.csv");
        System.out.println("File path: " + FILE_PATH);
        System.out.println("Total data rows: " + count);
        System.out.println("Required rows: 12000");
        System.out.println("Result: " + (count >= 12000 ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertTrue(count >= 12000, "attendance.csv must contain at least 12000 rows");
    }

    @Test
    @DisplayName("Read attendance.csv under 1 second")
    public void testReadAttendanceCsvUnderOneSecond() throws IOException {
        long start = System.nanoTime();

        int count = countRows();

        long end = System.nanoTime();
        double elapsedMs = (end - start) / 1_000_000.0;

        System.out.println("======================================");
        System.out.println("TEST: CSV Reading Performance");
        System.out.println("File path: " + FILE_PATH);
        System.out.println("Total data rows: " + count);
        System.out.printf("Reading time: %.3f ms%n", elapsedMs);
        System.out.println("Limit: < 1000 ms");
        System.out.println("Result: " + (elapsedMs < 1000 ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertTrue(count >= 12000, "attendance.csv must contain at least 12000 rows");
        assertTrue(elapsedMs < 1000, "Reading attendance.csv should take less than 1 second");
    }

    private int countRows() throws IOException {
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            // Bỏ dòng header nếu file có header
            br.readLine();

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
        }

        return count;
    }
}