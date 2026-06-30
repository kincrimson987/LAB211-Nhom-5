import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReportController {

    private final PayrollEntryRepository payrollEntryRepository;
    private final AttendanceRepository   attendanceRepository;
    private final PayrollRunRepository   payrollRunRepository;

    public ReportController(PayrollEntryRepository payrollEntryRepository,
                            AttendanceRepository attendanceRepository,
                            PayrollRunRepository payrollRunRepository,
                            EmployeeRepository employeeRepository) {
        this.payrollEntryRepository = payrollEntryRepository;
        this.attendanceRepository   = attendanceRepository;
        this.payrollRunRepository   = payrollRunRepository;
    }

    // ── Generate Payroll Report ───────────────────────────────

    public String generatePayrollReport(String yearMonth) {
        List<PayrollEntry> entries = payrollEntryRepository.findAll().stream()
                .filter(e -> e.extractYearMonth().equals(yearMonth))
                .collect(java.util.stream.Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== PAYROLL REPORT: ").append(yearMonth).append(" ==========\n");
        sb.append(String.format("%-20s %-15s %-18s %-12s%n",
                "Entry ID", "Employee", "Net Salary (VND)", "Status"));
        sb.append("─".repeat(68)).append("\n");

        double total = 0;
        for (PayrollEntry e : entries) {
            sb.append(String.format("%-20s %-15s %-18s %-12s%n",
                    e.getId(), e.getEmployeeId(),
                    String.format("%,.0f", e.getNetSalary()), e.getStatus()));
            total += e.getNetSalary();
        }

        sb.append("─".repeat(68)).append("\n");
        sb.append(String.format("Total payout: %,.0f VND | Records: %d%n", total, entries.size()));
        sb.append("=".repeat(68)).append("\n");
        return sb.toString();
    }

    // ── Generate Attendance Report ────────────────────────────

    public String generateAttendanceReport(String yearMonth) {
        List<AttendanceRecord> records = attendanceRepository.findByMonth(yearMonth);

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== ATTENDANCE REPORT: ").append(yearMonth).append(" ==========\n");
        sb.append(String.format("%-15s %-12s %-10s %-12s%n",
                "Employee ID", "Month", "Work Days", "Overtime(h)"));
        sb.append("─".repeat(52)).append("\n");

        for (AttendanceRecord r : records) {
            sb.append(String.format("%-15s %-12s %-10d %-12.1f%n",
                    r.getEmployeeId(), r.getYearMonth(),
                    r.getWorkDays(), r.getOvertimeHours()));
        }

        sb.append("─".repeat(52)).append("\n");
        sb.append("Total records: ").append(records.size()).append("\n");
        sb.append("=".repeat(52)).append("\n");
        return sb.toString();
    }

    // ── Generate Simulation Comparison Report ─────────────────

    public String generateSimulationComparisonReport() {
        List<PayrollRun> runs = payrollRunRepository.findAll();

        StringBuilder sb = new StringBuilder();
        sb.append("\n========== SIMULATION COMPARISON REPORT ==========\n");
        sb.append(String.format("%-15s %-12s %-10s %-8s %-8s %-8s %-8s%n",
                "Run ID", "Month", "Mechanism", "Ms", "TPS", "Success", "DblPay"));
        sb.append("─".repeat(75)).append("\n");

        for (PayrollRun r : runs) {
            sb.append(String.format("%-15s %-12s %-10s %-8d %-8.1f %-8d %-8d%n",
                    r.getId(), r.getYearMonth(), r.getMechanism(),
                    r.getElapsedMs(), r.getTps(),
                    r.getSuccessCount(), r.getDoublePaymentCount()));
        }

        sb.append("─".repeat(75)).append("\n");
        sb.append("Total runs: ").append(runs.size()).append("\n");
        sb.append("=".repeat(75)).append("\n");
        return sb.toString();
    }

    // ── Export CSV ────────────────────────────────────────────

    public void exportCsv(String filePath) {
        List<PayrollEntry> entries = payrollEntryRepository.findAll();
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,employeeId,netSalary,status");
            writer.newLine();
            for (PayrollEntry e : entries) {
                writer.write(e.toCsvLine());
                writer.newLine();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Export failed: " + ex.getMessage(), ex);
        }
    }

    // ── Import CSV ────────────────────────────────────────────

    public void importCsv(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            throw new IllegalArgumentException("File not found: " + filePath);
        // Đọc qua repository hiện có
        System.out.println("[ReportController] Import from: " + filePath);
    }
}
