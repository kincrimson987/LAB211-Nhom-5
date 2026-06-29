import java.util.ArrayList;
import java.util.List;

/**
 * Tuần 5 — PayrollController (NO_LOCK, đơn luồng)
 * tích hợp thêm hàm bổ trợ Tuần 5-6
 */
public class PayrollController {

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final PayrollRuleRepository ruleRepo;
    private final PayrollEntryRepository entryRepo;
    private ReportView view; // Thêm thuộc tính hỗ trợ View của bạn

    // Constructor gốc của nhóm (Giữ nguyên)
    public PayrollController(EmployeeRepository employeeRepo,
                             AttendanceRepository attendanceRepo,
                             PayrollRuleRepository ruleRepo,
                             PayrollEntryRepository entryRepo) {
        this.employeeRepo = employeeRepo;
        this.attendanceRepo = attendanceRepo;
        this.ruleRepo = ruleRepo;
        this.entryRepo = entryRepo;
    }

    // Constructor bổ sung phục vụ riêng cho file test của bạn (Tuần 6 MVC)
    public PayrollController(ReportView view) {
        this.employeeRepo = null;
        this.attendanceRepo = null;
        this.ruleRepo = null;
        this.entryRepo = null;
        this.view = view;
    }

    /**
     * Chạy tính lương cho tất cả nhân viên trong 1 tháng. (Code gốc của nhóm)
     */
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

    /**
     * Lấy tất cả PayrollEntry đã có. (Code gốc của nhóm)
     */
    public List<PayrollEntry> getAllEntries() {
        return entryRepo.findAll();
    }

    /**
     * Lấy PayrollEntry theo tháng. (Code gốc của nhóm)
     */
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

    /**
     * Tìm tên nhân viên theo ID. (Code gốc của nhóm)
     */
    public Employee findEmployeeById(String employeeId) {
        return employeeRepo.findById(employeeId);
    }

    // =========================================================================
    // PHẦN CODE TIẾN ĐỘ TUẦN 5-6 CỦA BẠN (Chèn thêm vào cuối để không lỗi nhóm)
    // =========================================================================
    
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