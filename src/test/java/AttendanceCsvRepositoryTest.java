
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Attendance CSV Repository Test Suite")
public class AttendanceCsvRepositoryTest {

    @Test
    @DisplayName("Read and validate all CSV files in the data directory under 1 second")
    public void testReadAndValidateAllCsvFiles() throws IOException {
        long startTime = System.nanoTime();

        File dataDir = new File("data");
        assertTrue(dataDir.exists() && dataDir.isDirectory(), "The 'data' directory must exist");

        File[] files = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        assertNotNull(files, "Should find CSV files in 'data' directory");
        assertTrue(files.length > 0, "There should be at least one CSV file in 'data'");

        // Sort files to have stable output order
        Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));

        int totalErrors = 0;

        System.out.println("================================================================================");
        System.out.println("TEST: Read and Validate All CSV Files in 'data/' folder");
        System.out.println("================================================================================");

        for (File file : files) {
            System.out.println("\n------------------------------------------------------------");
            System.out.println("Analyzing File: " + file.getPath());
            System.out.println("------------------------------------------------------------");

            int totalLines = 0;
            int dataRows = 0;
            int fileErrors = 0;
            boolean hasHeader = false;
            int expectedColCount = -1;
            String[] headerNames = null;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    totalLines++;
                    
                    if (line.trim().isEmpty()) {
                        System.out.println("  [LINE " + totalLines + "] WARNING: Empty line found.");
                        continue;
                    }

                    // Check if it's the first line
                    if (totalLines == 1) {
                        // Heuristic: check if the first line is a header
                        String normalized = line.toLowerCase();
                        hasHeader = normalized.startsWith("id,")
                                 || normalized.startsWith("balanceid,")
                                 || normalized.startsWith("leaveid,");

                        if (hasHeader) {
                            headerNames = line.split(",", -1);
                            for (int i = 0; i < headerNames.length; i++) {
                                headerNames[i] = headerNames[i].trim();
                            }
                            expectedColCount = headerNames.length;
                            System.out.println("  Detected Header: " + line);
                            System.out.println("  Expected Column Count: " + expectedColCount);
                            continue; // Skip validation on the header line itself
                        } else {
                            // No header, treat first line as data and infer expected column count
                            String[] firstRowParts = line.split(",", -1);
                            expectedColCount = firstRowParts.length;
                            System.out.println("  No Header detected. Expected Column Count inferred from first row: " + expectedColCount);
                        }
                    }

                    dataRows++;
                    String[] parts = line.split(",", -1);

                    // 1. Column count mismatch
                    if (parts.length != expectedColCount) {
                        System.out.println("  [LINE " + totalLines + "] ERROR: Column count mismatch! Expected " 
                                + expectedColCount + " but got " + parts.length + ". Line content: \"" + line + "\"");
                        fileErrors++;
                        continue;
                    }

                    // 2. Validate empty cells/missing values
                    for (int i = 0; i < parts.length; i++) {
                        String cellValue = parts[i].trim();
                        if (cellValue.isEmpty()) {
                            String colIdentifier = (headerNames != null && i < headerNames.length) 
                                    ? headerNames[i] 
                                    : "Column Index " + i;

                            // Special check: approvedBy in leave_requests.csv is optional
                            if (file.getName().equals("leave_requests.csv") && colIdentifier.equals("approvedBy")) {
                                // This is optional, we can skip reporting it as an error
                                continue;
                            }

                            System.out.println("  [LINE " + totalLines + "] ERROR: Missing value for '" + colIdentifier + "'");
                            fileErrors++;
                        }
                    }
                }
            }

            System.out.println("  Analysis Completed for " + file.getName());
            System.out.println("  Total Data Records: " + dataRows);
            System.out.println("  Total Missing/Invalid Values: " + fileErrors);
            
            totalErrors += fileErrors;
        }

        long endTime = System.nanoTime();
        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("\n================================================================================");
        System.out.println("VALIDATION SUMMARY");
        System.out.println("Total CSV files scanned: " + files.length);
        System.out.println("Total errors across all files: " + totalErrors);
        System.out.printf("Total processing time: %.3f ms (Limit: < 1000 ms)%n", elapsedMs);
        System.out.println("Result: " + (totalErrors == 0 && elapsedMs < 1000.0 ? "PASSED" : "FAILED"));
        System.out.println("================================================================================");

        assertEquals(0, totalErrors, "There should be no missing values or syntax errors in any CSV file (except known optional fields)");
        assertTrue(elapsedMs < 1000.0, "Reading and validating all CSV files in 'data/' should take less than 1 second (1000 ms), but took " + elapsedMs + " ms");
    }

    @Test
    @DisplayName("Compare data files with backup and report differences")
    public void testCompareAndReportChanges() throws IOException {
        File dataDir = new File("data");
        File backupDir = new File("data_backup");

        if (!dataDir.exists() || !dataDir.isDirectory()) {
            System.out.println("[INFO] No 'data' folder found. Skipping comparison.");
            return;
        }

        // Check if the user requested to accept/update changes
        boolean acceptChanges = "true".equalsIgnoreCase(System.getProperty("acceptChanges"));

        if (acceptChanges) {
            System.out.println("================================================================================");
            System.out.println("[INFO] System property -DacceptChanges=true detected.");
            System.out.println("[INFO] Syncing 'data_backup/' baseline with current 'data/' files...");
            System.out.println("================================================================================");

            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // Remove old baseline files
            File[] oldBackupFiles = backupDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            if (oldBackupFiles != null) {
                for (File f : oldBackupFiles) {
                    f.delete();
                }
            }

            // Copy current files to baseline
            File[] filesToBackup = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            if (filesToBackup != null) {
                for (File f : filesToBackup) {
                    File dest = new File(backupDir, f.getName());
                    Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            System.out.println("[SUCCESS] New data files have been accepted as the baseline!");
            System.out.println("================================================================================");
            return;
        }

        // If backup does not exist, create it and copy original files
        if (!backupDir.exists()) {
            backupDir.mkdirs();
            File[] files = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            if (files != null) {
                for (File f : files) {
                    File dest = new File(backupDir, f.getName());
                    Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            System.out.println("================================================================================");
            System.out.println("[INFO] No existing backup found. Created baseline copy of 'data/' in 'data_backup/'");
            System.out.println("================================================================================");
            return;
        }

        File[] currentFiles = dataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        File[] backupFiles = backupDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

        List<String> allFileNames = new ArrayList<>();
        if (currentFiles != null) {
            for (File f : currentFiles) {
                if (!allFileNames.contains(f.getName())) {
                    allFileNames.add(f.getName());
                }
            }
        }
        if (backupFiles != null) {
            for (File f : backupFiles) {
                if (!allFileNames.contains(f.getName())) {
                    allFileNames.add(f.getName());
                }
            }
        }

        // Sort file names for stable printing
        allFileNames.sort(String::compareTo);

        System.out.println("================================================================================");
        System.out.println("COMPARE DATA CHANGES WITH BASELINE (data/ vs data_backup/)");
        System.out.println("================================================================================");

        int totalFilesChanged = 0;

        for (String filename : allFileNames) {
            File currentFile = new File(dataDir, filename);
            File backupFile = new File(backupDir, filename);

            if (currentFile.exists() && !backupFile.exists()) {
                System.out.println("\n[FILE ADDED]: " + filename);
                totalFilesChanged++;
            } else if (!currentFile.exists() && backupFile.exists()) {
                System.out.println("\n[FILE DELETED]: " + filename);
                totalFilesChanged++;
            } else {
                // Exists in both, compare contents
                boolean hasDiff = diffCsvFiles(backupFile, currentFile);
                if (hasDiff) {
                    totalFilesChanged++;
                }
            }
        }

        System.out.println("\n================================================================================");
        System.out.println("SUMMARY: " + totalFilesChanged + " file(s) modified/added/deleted.");
        System.out.println("  Tip: To accept these changes, run the test with: -DacceptChanges=true");
        System.out.println("  Or simply delete the 'data_backup/' folder and run test again.");
        System.out.println("================================================================================");
    }

    private boolean diffCsvFiles(File backupFile, File currentFile) throws IOException {
        String filename = currentFile.getName();
        
        // Parse header and records of backup file
        String[] backupHeaders = null;
        java.util.LinkedHashMap<String, RecordInfo> backupRecords = new java.util.LinkedHashMap<>();
        boolean backupHasHeader = false;
        int backupLineNum = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(backupFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                backupLineNum++;
                if (line.trim().isEmpty()) continue;
                
                if (backupLineNum == 1) {
                    String norm = line.toLowerCase();
                    backupHasHeader = norm.startsWith("id,") || norm.startsWith("balanceid,") || norm.startsWith("leaveid,");
                    if (backupHasHeader) {
                        backupHeaders = line.split(",", -1);
                        for (int i = 0; i < backupHeaders.length; i++) {
                            backupHeaders[i] = backupHeaders[i].trim();
                        }
                        continue;
                    }
                }
                
                String[] parts = line.split(",", -1);
                String id = parts[0].trim();
                backupRecords.put(id, new RecordInfo(backupLineNum, parts, line));
            }
        }

        // Parse header and records of current file
        String[] currentHeaders = null;
        java.util.LinkedHashMap<String, RecordInfo> currentRecords = new java.util.LinkedHashMap<>();
        boolean currentHasHeader = false;
        int currentLineNum = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                currentLineNum++;
                if (line.trim().isEmpty()) continue;
                
                if (currentLineNum == 1) {
                    String norm = line.toLowerCase();
                    currentHasHeader = norm.startsWith("id,") || norm.startsWith("balanceid,") || norm.startsWith("leaveid,");
                    if (currentHasHeader) {
                        currentHeaders = line.split(",", -1);
                        for (int i = 0; i < currentHeaders.length; i++) {
                            currentHeaders[i] = currentHeaders[i].trim();
                        }
                        continue;
                    }
                }
                
                String[] parts = line.split(",", -1);
                String id = parts[0].trim();
                currentRecords.put(id, new RecordInfo(currentLineNum, parts, line));
            }
        }

        // Find diffs
        List<String> added = new ArrayList<>();
        List<String> deleted = new ArrayList<>();
        List<String> modified = new ArrayList<>();
        List<String> modifiedDetails = new ArrayList<>();

        for (String id : currentRecords.keySet()) {
            if (!backupRecords.containsKey(id)) {
                added.add(id);
            } else {
                RecordInfo backupInfo = backupRecords.get(id);
                RecordInfo currentInfo = currentRecords.get(id);
                String[] bParts = backupInfo.parts;
                String[] cParts = currentInfo.parts;
                
                if (bParts.length != cParts.length) {
                    modified.add(id);
                    modifiedDetails.add(String.format("    - Column count changed: %d -> %d\n", bParts.length, cParts.length));
                } else {
                    boolean itemModified = false;
                    StringBuilder itemDiff = new StringBuilder();
                    for (int i = 0; i < bParts.length; i++) {
                        String bVal = bParts[i].trim();
                        String cVal = cParts[i].trim();
                        if (!bVal.equals(cVal)) {
                            itemModified = true;
                            String colName = (currentHeaders != null && i < currentHeaders.length) ? currentHeaders[i] : "Column " + i;
                            itemDiff.append(String.format("      * Field '%s' changed: '%s' -> '%s'\n", colName, bVal, cVal));
                        }
                    }
                    if (itemModified) {
                        modified.add(id);
                        modifiedDetails.add(String.format("    - ID: %s (at Line %d)\n%s", id, currentInfo.lineNumber, itemDiff.toString()));
                    }
                }
            }
        }

        for (String id : backupRecords.keySet()) {
            if (!currentRecords.containsKey(id)) {
                deleted.add(id);
            }
        }

        boolean hasDiff = !added.isEmpty() || !deleted.isEmpty() || !modified.isEmpty();
        
        if (hasDiff) {
            System.out.println("\n[FILE MODIFIED]: " + filename);
            if (!added.isEmpty()) {
                System.out.println("  Added Records (" + added.size() + "):");
                for (String id : added) {
                    RecordInfo info = currentRecords.get(id);
                    System.out.println("    + ID: " + id + " (Line " + info.lineNumber + "): \"" + info.originalLine + "\"");
                }
            }
            if (!deleted.isEmpty()) {
                System.out.println("  Deleted Records (" + deleted.size() + "):");
                for (String id : deleted) {
                    RecordInfo info = backupRecords.get(id);
                    System.out.println("    - ID: " + id + " (was Line " + info.lineNumber + "): \"" + info.originalLine + "\"");
                }
            }
            if (!modified.isEmpty()) {
                System.out.println("  Modified Records (" + modified.size() + "):");
                for (String detail : modifiedDetails) {
                    System.out.print(detail);
                }
            }
        }

        return hasDiff;
    }

    private static class RecordInfo {
        int lineNumber;
        String[] parts;
        String originalLine;

        RecordInfo(int lineNumber, String[] parts, String originalLine) {
            this.lineNumber = lineNumber;
            this.parts = parts;
            this.originalLine = originalLine;
        }
    }
}