import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimulationController {

    private final PayrollEntryRepository payrollEntryRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PayrollRunRepository   payrollRunRepository;
    private final AttendanceRepository   attendanceRepository;
    private final EmployeeRepository     employeeRepository;
    private final PayrollRuleRepository  payrollRuleRepository;

    private String currentSyncMode = "NO_LOCK"; // mặc định

    public SimulationController(PayrollEntryRepository payrollEntryRepository,
                                LeaveBalanceRepository leaveBalanceRepository,
                                PayrollRunRepository payrollRunRepository,
                                AttendanceRepository attendanceRepository,
                                EmployeeRepository employeeRepository,
                                PayrollRuleRepository payrollRuleRepository) {
        this.payrollEntryRepository = payrollEntryRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.payrollRunRepository   = payrollRunRepository;
        this.attendanceRepository   = attendanceRepository;
        this.employeeRepository     = employeeRepository;
        this.payrollRuleRepository  = payrollRuleRepository;
    }

    // ── Select Sync Mode ──────────────────────────────────────

    public void setSyncMode(String mode) {
        List<String> valid = List.of("NO_LOCK", "FILE_LOCK", "SYNCHRONIZED", "OPTIMISTIC");
        if (!valid.contains(mode))
            throw new IllegalArgumentException("Invalid sync mode: " + mode);
        this.currentSyncMode = mode;
    }

    public String getCurrentSyncMode() {
        return currentSyncMode;
    }

    // ── Run Payroll Simulation ────────────────────────────────

    public PayrollRun runSimulation(String yearMonth, int threadCount) {
    if (yearMonth == null || yearMonth.trim().isEmpty())
            throw new IllegalArgumentException("Year-Month cannot be empty.");
        if (threadCount <= 0) threadCount = 1;

        PayrollRule rule = payrollRuleRepository.getConfig();
        List<Employee> employees = employeeRepository.findAll();
        List<PayrollEntry> entries = prepareEntries(employees, yearMonth, rule);

        long startTime = System.currentTimeMillis();
        int successCount = 0;

        // Chạy theo cơ chế đã chọn
        for (PayrollEntry entry : entries) {
            boolean ok = processEntry(entry.getId(), "SIMULATION");
            if (ok) successCount++;
        }

        long elapsedMs = System.currentTimeMillis() - startTime;
        double tps = elapsedMs > 0 ? (successCount * 1000.0 / elapsedMs) : successCount;

        // Phát hiện lỗi
        int doublePayment = detectDoublePayment().size();
        int wrongLeave    = detectWrongLeaveDeduction().size();

        PayrollRun run = new PayrollRun(
                "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                1L, yearMonth, currentSyncMode, elapsedMs,
                successCount, doublePayment, wrongLeave, tps);
        payrollRunRepository.save(run);
        return run;
    }

    // ── Measure TPS ───────────────────────────────────────────

    public PayrollRun getLatestRun() {
        return payrollRunRepository.findLatest();
    }

    // ── Detect Double Payment ─────────────────────────────────

    public List<String> detectDoublePayment() {
        List<String> suspects = new ArrayList<>();
        List<Employee> employees = employeeRepository.findAll();

        for (Employee emp : employees) {
            long count = payrollEntryRepository.countProcessedByEmployee(emp.getId());
            if (count > 1) {
                suspects.add(emp.getId() + " (" + emp.getName() + ") - " + count + " processed entries");
            }
        }
        return suspects;
    }

    // ── Detect Wrong Leave Deduction ──────────────────────────

    public List<String> detectWrongLeaveDeduction() {
        List<String> suspects = new ArrayList<>();
        List<LeaveBalance> balances = leaveBalanceRepository.findAll();

        for (LeaveBalance b : balances) {
            int calc = b.getTotalLeaveDays() - b.getUsedLeaveDays();
            if (calc != b.getRemainingLeaveDays()) {
                suspects.add(b.getEmployeeId() + " [" + b.getLeaveType() + "]"
                        + " expected=" + calc
                        + " actual=" + b.getRemainingLeaveDays());
            }
        }
        return suspects;
    }

    // ── Helpers ───────────────────────────────────────────────

    private List<PayrollEntry> prepareEntries(List<Employee> employees,
                                              String yearMonth, PayrollRule rule) {
        List<PayrollEntry> entries = new ArrayList<>();
        String[] yearMonthParts = yearMonth.split("-");
        String year = yearMonthParts[0];
        String month = yearMonthParts[1];
        for (Employee emp : employees) {
            AttendanceRecord att = attendanceRepository
                    .findByEmployeeAndMonth(emp.getId(), yearMonth);
            if (att == null) continue;

            double netSalary = emp.calculateSalary(att, rule);
            String entryId   = "PR_" + emp.getId() + "_" + month + "_" + year;

            PayrollEntry existing = payrollEntryRepository.findById(entryId);
            if (existing == null) {
                PayrollEntry entry = new PayrollEntry(
                        entryId, 1L, emp.getId(), netSalary, PayrollStatus.PENDING);
                payrollEntryRepository.save(entry);
                entries.add(entry);
            } else {
                existing.setStatus(PayrollStatus.PENDING);
                existing.setVersion(existing.getVersion() + 1);
                payrollEntryRepository.update(existing);
                entries.add(existing);
            }
        }
        return entries;
    }

    private boolean processEntry(String entryId, String processedBy) {
        return switch (currentSyncMode) {
            case "FILE_LOCK"    -> payrollEntryRepository.processWithFileLock(entryId, processedBy);
            case "SYNCHRONIZED" -> payrollEntryRepository.processWithSync(entryId, processedBy);
            case "OPTIMISTIC"   -> payrollEntryRepository.processWithOptimistic(entryId, processedBy);
            default             -> payrollEntryRepository.processWithNoLock(entryId, processedBy);
        };
    }
}
