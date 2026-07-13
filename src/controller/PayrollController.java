import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.YearMonth;

public class PayrollController {

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final PayrollRuleRepository ruleRepo;
    private final PayrollEntryRepository entryRepo;
    private final PayrollRunRepository runRepo;
    private final LeaveRequestRepository leaveRequestRepo;
    private ReportView view;

    public PayrollController(EmployeeRepository employeeRepo,
                             AttendanceRepository attendanceRepo,
                             PayrollRuleRepository ruleRepo,
                             PayrollEntryRepository entryRepo,
                             PayrollRunRepository runRepo) {
        this(employeeRepo, attendanceRepo, ruleRepo, entryRepo, runRepo, new LeaveRequestRepository());
    }

    public PayrollController(EmployeeRepository employeeRepo,
                             AttendanceRepository attendanceRepo,
                             PayrollRuleRepository ruleRepo,
                             PayrollEntryRepository entryRepo,
                             PayrollRunRepository runRepo,
                             LeaveRequestRepository leaveRequestRepo) {
        this.employeeRepo = employeeRepo;
        this.attendanceRepo = attendanceRepo;
        this.ruleRepo = ruleRepo;
        this.entryRepo = entryRepo;
        this.runRepo = runRepo;
        this.leaveRequestRepo = leaveRequestRepo;
    }

    public PayrollController(ReportView view) {
        this.employeeRepo = null;
        this.attendanceRepo = null;
        this.ruleRepo = null;
        this.entryRepo = null;
        this.runRepo = null;
        this.leaveRequestRepo = null;
        this.view = view;
    }

    // ─────────────────────────────────────────
    // Core payroll processing
    // ─────────────────────────────────────────

   public synchronized List<PayrollEntry> runPayroll(String yearMonth) {
        List<PayrollEntry> results = new ArrayList<>();
        PayrollRule rule = ruleRepo.getConfig();
        List<Employee> employees = employeeRepo.findAll();
        List<PayrollEntry> allEntries = entryRepo.findAll();

        Map<String, AttendanceRecord> attendanceByEmployee = new HashMap<>();
        for (AttendanceRecord record : attendanceRepo.findByMonth(yearMonth)) {
            attendanceByEmployee.put(record.getEmployeeId(), record);
        }

        Map<String, Integer> entryIndexByEmployee = new HashMap<>();
        for (int i = 0; i < allEntries.size(); i++) {
            PayrollEntry entry = allEntries.get(i);
            if (yearMonth.equals(entry.extractYearMonth())) {
                entryIndexByEmployee.put(entry.getEmployeeId(), i);
            }
        }

        String[] parts = yearMonth.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid year-month. Expected YYYY-MM: " + yearMonth);
        }
        String year = parts[0];
        String month = parts[1];

        for (Employee emp : employees) {
            AttendanceRecord attendance = attendanceByEmployee.get(emp.getId());
            if (attendance == null) {
                continue;
            }

            String entryId = "PR_" + emp.getId() + "_" + month + "_" + year;

            Integer existingIndex = entryIndexByEmployee.get(emp.getId());
            PayrollEntry existing = existingIndex != null ? allEntries.get(existingIndex) : null;
            if (existing != null && existing.getStatus() == PayrollStatus.PROCESSED) {
                continue;
            }

            AttendanceRecord payableAttendance = buildPayableAttendance(attendance, rule, yearMonth);
            double netSalary = emp.calculateSalary(payableAttendance, rule);
            PayrollEntry entry = new PayrollEntry(entryId, 0, emp.getId(), netSalary, PayrollStatus.PENDING);
            entry.process();

            if (existingIndex != null) {
                allEntries.set(existingIndex, entry);
            } else {
                entryIndexByEmployee.put(emp.getId(), allEntries.size());
                allEntries.add(entry);
            }
            results.add(entry);
        }

        if (!results.isEmpty()) {
            synchronized (entryRepo.getFilePath().intern()) {
                entryRepo.writeAllLines(allEntries);
            }
        }
        return results;
    }

    private AttendanceRecord buildPayableAttendance(AttendanceRecord attendance,
                                                      PayrollRule rule,
                                                      String yearMonth) {
        int paidLeaveDays = countPaidLeaveDays(attendance.getEmployeeId(), yearMonth);
        int payableDays = Math.min(rule.getStandardWorkingDays(),
                attendance.getWorkDays() + paidLeaveDays);
        return new AttendanceRecord(
                attendance.getId(), attendance.getVersion(), attendance.getEmployeeId(),
                attendance.getYearMonth(), payableDays, attendance.getOvertimeHours());
    }

    private int countPaidLeaveDays(String employeeId, String yearMonth) {
        if (leaveRequestRepo == null) return 0;
        YearMonth target = YearMonth.parse(yearMonth);
        int total = 0;
        for (LeaveRequest request : leaveRequestRepo.findByEmployee(employeeId)) {
            if (request.getStatus() != LeaveStatus.APPROVED || request.getPaidLeaveDays() <= 0) {
                continue;
            }
            int paidRemaining = request.getPaidLeaveDays();
            for (LocalDate date = request.getStartDate();
                 !date.isAfter(request.getEndDate()) && paidRemaining > 0;
                 date = date.plusDays(1)) {
                if (YearMonth.from(date).equals(target)) total++;
                paidRemaining--;
            }
        }
        return total;
    }

    /**
     * Xử lý lương tháng, đo thời gian chạy và lưu kết quả vào PayrollRun.
     * Được MainView gọi: payrollController.processMonthlyPayroll(yearMonth, userId)
     */
    public PayrollRun processMonthlyPayroll(String yearMonth, String userId) {
        long start = System.currentTimeMillis();
        List<PayrollEntry> processed = runPayroll(yearMonth);
        long elapsed = System.currentTimeMillis() - start;

        double tps = elapsed > 0 ? (processed.size() * 1000.0 / elapsed) : processed.size();
        String runId = "RUN_" + yearMonth + "_" + System.currentTimeMillis();

        PayrollRun run = new PayrollRun(
                runId, 0, yearMonth, "DEFAULT",
                elapsed, processed.size(), 0, 0, tps);

        runRepo.save(run);
        return run;
    }

    // ─────────────────────────────────────────
    // Query entries
    // ─────────────────────────────────────────

    public List<PayrollEntry> getAllEntries() {
        return entryRepo.findAll();
    }

    public List<PayrollEntry> getAllPayrollEntries() {
        return entryRepo.findAll();
    }

    public List<PayrollEntry> getPayrollByEmployee(String employeeId) {
        List<PayrollEntry> result = new ArrayList<>();
        for (PayrollEntry entry : entryRepo.findAll()) {
            if (entry.getEmployeeId() != null && entry.getEmployeeId().equals(employeeId)) {
                result.add(entry);
            }
        }
        return result;
    }

    public List<PayrollEntry> getEntriesByMonth(String yearMonth) {
        List<PayrollEntry> all = entryRepo.findAll();
        List<PayrollEntry> result = new ArrayList<>();
        for (PayrollEntry entry : all) {
            if (entry.extractYearMonth().equals(yearMonth)) {
                result.add(entry);
            }
        }
        return result;
    }

    // ─────────────────────────────────────────
    // Payroll rule config
    // ─────────────────────────────────────────

    public PayrollRule getPayrollRule() {
        return ruleRepo.getConfig();
    }

    public void updatePayrollRule(PayrollRule rule) {
        ruleRepo.updateConfig(rule);
    }

    // ─────────────────────────────────────────
    // Other helpers
    // ─────────────────────────────────────────

    public Employee findEmployeeById(String employeeId) {
        return employeeRepo.findById(employeeId);
    }

    public int processSingleThreadPayroll(String[] employees) {
        int count = 0;
        for (String emp : employees) {
            System.out.println("[Single-Thread] Week 5 File I/O scanning for: " + emp);
            count++;
        }
        if (view != null) {
            view.renderReport(count, 3, "Single-Thread");
        }
        return count;
    }

    public int processLeaveApproval(int currentBalance, int requestDays) {
        if (currentBalance >= requestDays) {
            System.out.println("[Leave Flow] Logic validated -> APPROVED.");
            return requestDays;
        }
        System.out.println("[Leave Flow] Logic validated -> REJECTED.");
        return 0;
    }
}
/*```java
import java.util.ArrayList;
import java.util.List;


public class PayrollController {

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final PayrollRuleRepository ruleRepo;
    private final PayrollEntryRepository entryRepo;
    private ReportView view;

    public PayrollController(EmployeeRepository employeeRepo,
                             AttendanceRepository attendanceRepo,
                             PayrollRuleRepository ruleRepo,
                             PayrollEntryRepository entryRepo) {
        this.employeeRepo = employeeRepo;
        this.attendanceRepo = attendanceRepo;
        this.ruleRepo = ruleRepo;
        this.entryRepo = entryRepo;
    }

    public PayrollController(ReportView view) {
        this.employeeRepo = null;
        this.attendanceRepo = null;
        this.ruleRepo = null;
        this.entryRepo = null;
        this.view = view;
    }

    public List<PayrollEntry> runPayroll(String yearMonth) {
        List<PayrollEntry> results = new ArrayList<>();
        PayrollRule rule = ruleRepo.getConfig();
        List<Employee> employees = employeeRepo.findAll();

        for (Employee emp : employees) {
            AttendanceRecord attendance = attendanceRepo.findByEmployeeAndMonth(emp.getId(), yearMonth);
            if (attendance == null) {
                continue;
            }

            String[] parts = yearMonth.split("-");
            String year = parts[0];
            String month = parts[1];
            String entryId = "PR_" + emp.getId() + "_" + month + "_" + year;

            PayrollEntry existing = entryRepo.findByEmployeeAndMonth(emp.getId(), yearMonth);
            if (existing != null && existing.getStatus() == PayrollStatus.PROCESSED) {
                continue;
            }

            double netSalary = emp.calculateSalary(attendance, rule);
            PayrollEntry entry = new PayrollEntry(entryId, 0, emp.getId(), netSalary, PayrollStatus.PENDING);
            entry.process();

            if (existing != null) {
                entryRepo.update(entry);
            } else {
                entryRepo.save(entry);
            }
            results.add(entry);
        }
        return results;
    }

    public List<PayrollEntry> getAllEntries() {
        return entryRepo.findAll();
    }

    public List<PayrollEntry> getEntriesByMonth(String yearMonth) {
        List<PayrollEntry> all = entryRepo.findAll();
        List<PayrollEntry> result = new ArrayList<>();
        for (PayrollEntry entry : all) {
            if (entry.extractYearMonth().equals(yearMonth)) {
                result.add(entry);
            }
        }
        return result;
    }

   
    public Employee findEmployeeById(String employeeId) {
        return employeeRepo.findById(employeeId);
    }

  
    public int processSingleThreadPayroll(String[] employees) {
        int count = 0;
        for (String emp : employees) {
            System.out.println("[Single-Thread] Week 5 File I/O scanning for: " + emp);
            count++;
        }
        if (view != null) {
            view.renderReport(count, 3, "Single-Thread");
        }
        return count;
    }

    public int processLeaveApproval(int currentBalance, int requestDays) {
        if (currentBalance >= requestDays) {
            System.out.println("[Leave Flow] Logic validated -> APPROVED.");
            return requestDays;
        }
        System.out.println("[Leave Flow] Logic validated -> REJECTED.");
        return 0;
    }

    public PayrollRun processSingleThreadPayroll(String yearMonth, String id) {
        
        throw new UnsupportedOperationException("Unimplemented method 'processSingleThreadPayroll'");
    }
}
``` */
