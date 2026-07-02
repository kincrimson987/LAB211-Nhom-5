import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportController {

    private final PayrollEntryRepository payrollEntryRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRunRepository payrollRunRepository;

    public ReportController(PayrollEntryRepository payrollEntryRepository,
                            AttendanceRepository attendanceRepository,
                            PayrollRunRepository payrollRunRepository,
                            EmployeeRepository employeeRepository) {
        this.payrollEntryRepository = payrollEntryRepository;
        this.attendanceRepository = attendanceRepository;
        this.payrollRunRepository = payrollRunRepository;
    }

    public String generatePayrollReport(String yearMonth) {
        List<PayrollEntry> entries = getPayrollEntriesByMonth(yearMonth);

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== PAYROLL REPORT: ").append(yearMonth).append(" ==========\n");
        sb.append(String.format("%-20s %-15s %-18s %-12s%n",
                "Entry ID", "Employee", "Net Salary (VND)", "Status"));
        sb.append("-".repeat(68)).append("\n");

        double total = 0;
        for (PayrollEntry entry : entries) {
            sb.append(String.format("%-20s %-15s %-18s %-12s%n",
                    entry.getId(),
                    entry.getEmployeeId(),
                    String.format("%,.0f", entry.getNetSalary()),
                    entry.getStatus()));
            total += entry.getNetSalary();
        }

        sb.append("-".repeat(68)).append("\n");
        sb.append(String.format("Total payout: %,.0f VND | Records: %d%n", total, entries.size()));
        sb.append("=".repeat(68)).append("\n");
        return sb.toString();
    }

    public String generateAttendanceReport(String yearMonth) {
        List<AttendanceRecord> records = getAttendanceRecordsByMonth(yearMonth);

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== ATTENDANCE REPORT: ").append(yearMonth).append(" ==========\n");
        sb.append(String.format("%-15s %-12s %-10s %-12s%n",
                "Employee ID", "Month", "Work Days", "Overtime(h)"));
        sb.append("-".repeat(52)).append("\n");

        for (AttendanceRecord record : records) {
            sb.append(String.format("%-15s %-12s %-10d %-12.1f%n",
                    record.getEmployeeId(),
                    record.getYearMonth(),
                    record.getWorkDays(),
                    record.getOvertimeHours()));
        }

        sb.append("-".repeat(52)).append("\n");
        sb.append("Total records: ").append(records.size()).append("\n");
        sb.append("=".repeat(52)).append("\n");
        return sb.toString();
    }

    public String generateSimulationComparisonReport() {
        List<PayrollRun> runs = getPayrollRuns();

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== SIMULATION COMPARISON REPORT ==========\n");
        sb.append(String.format("%-28s %-10s %-20s %-8s %-8s %-8s %-8s%n",
                "Run ID", "Month", "Mechanism", "Ms", "TPS", "Success", "DblPay"));
        sb.append("-".repeat(100)).append("\n");

        for (PayrollRun run : runs) {
            sb.append(String.format("%-28s %-10s %-20s %-8d %-8.1f %-8d %-8d%n",
                    run.getId(),
                    run.getYearMonth(),
                    run.getMechanism(),
                    run.getElapsedMs(),
                    run.getTps(),
                    run.getSuccessCount(),
                    run.getDoublePaymentCount()));
        }

        sb.append("-".repeat(100)).append("\n");
        sb.append("Total runs: ").append(runs.size()).append("\n");
        sb.append("=".repeat(100)).append("\n");
        return sb.toString();
    }

    public int exportCsv(String filePath) {
        List<PayrollEntry> entries = payrollEntryRepository.findAll();
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(new PayrollEntry().getCsvHeader());
            writer.newLine();
            for (PayrollEntry entry : entries) {
                writer.write(entry.toCsvLine());
                writer.newLine();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Export failed: " + ex.getMessage(), ex);
        }
        return entries.size();
    }

    public int importCsv(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        List<PayrollEntry> imported = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            if (header == null) {
                throw new IllegalArgumentException("CSV file is empty.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                PayrollEntry entry = new PayrollEntry();
                entry.fromCsvLine(line);
                imported.add(entry);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Import failed: " + ex.getMessage(), ex);
        }

        payrollEntryRepository.writeAllLines(imported);
        return imported.size();
    }

    public List<PayrollEntry> getPayrollEntriesByMonth(String yearMonth) {
        return payrollEntryRepository.findAll().stream()
                .filter(entry -> entry.extractYearMonth().equals(yearMonth))
                .collect(Collectors.toList());
    }

    public List<AttendanceRecord> getAttendanceRecordsByMonth(String yearMonth) {
        return attendanceRepository.findByMonth(yearMonth);
    }

    public List<PayrollRun> getPayrollRuns() {
        return payrollRunRepository.findAll().stream()
                .filter(run -> run.getMechanism() != null && !run.getMechanism().isBlank())
                .filter(run -> run.getElapsedMs() > 0
                        || run.getSuccessCount() > 0
                        || run.getDoublePaymentCount() > 0
                        || run.getWrongLeaveCount() > 0
                        || run.getTps() > 0)
                .collect(Collectors.toList());
    }
}
