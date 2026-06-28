import java.util.ArrayList;
import java.util.List;

/**
 * Tuần 5 — PayrollController (NO_LOCK, đơn luồng)
 *
 * Controller chịu trách nhiệm:
 * - Lấy danh sách nhân viên, chấm công, quy tắc lương từ Repository
 * - Gọi employee.calculateSalary() trực tiếp qua đa hình (Polymorphism)
 * - Ghi PayrollEntry vào PayrollEntryRepository
 *
 * Không sử dụng Thread, Lock, hay Synchronized.
 * Không dùng SalaryCalculator — logic tính lương đã nằm trong từng lớp Employee.
 */
public class PayrollController {

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final PayrollRuleRepository ruleRepo;
    private final PayrollEntryRepository entryRepo;
    // Không cần SalaryCalculator nữa — gọi employee.calculateSalary() trực tiếp

    public PayrollController(EmployeeRepository employeeRepo,
                             AttendanceRepository attendanceRepo,
                             PayrollRuleRepository ruleRepo,
                             PayrollEntryRepository entryRepo) {
        this.employeeRepo = employeeRepo;
        this.attendanceRepo = attendanceRepo;
        this.ruleRepo = ruleRepo;
        this.entryRepo = entryRepo;
    }

    /**
     * Chạy tính lương cho tất cả nhân viên trong 1 tháng.
     * Sử dụng NO_LOCK (đơn luồng, không đồng bộ).
     *
     * @param yearMonth ví dụ: "2024-01"
     * @return danh sách PayrollEntry đã được tạo
     */
    public List<PayrollEntry> runPayroll(String yearMonth) {
        List<PayrollEntry> results = new ArrayList<>();

        // 1. Lấy quy tắc lương
        PayrollRule rule = ruleRepo.getConfig();

        // 2. Lấy danh sách nhân viên
        List<Employee> employees = employeeRepo.findAll();

        // 3. Duyệt từng nhân viên, tính lương
        for (Employee emp : employees) {
            // Lấy chấm công của nhân viên trong tháng
            AttendanceRecord attendance = attendanceRepo.findByEmployeeAndMonth(
                    emp.getId(), yearMonth);

            // Nếu không có chấm công thì bỏ qua
            if (attendance == null) {
                continue;
            }

            // Tạo entryId theo format: PR_empId_month_year
            // Ví dụ: yearMonth = "2024-01" -> month = "01", year = "2024"
            String[] parts = yearMonth.split("-");
            String year = parts[0];
            String month = parts[1];
            String entryId = "PR_" + emp.getId() + "_" + month + "_" + year;

            // Kiểm tra xem đã tính lương cho nhân viên này chưa (tránh double payment)
            PayrollEntry existing = entryRepo.findByEmployeeAndMonth(emp.getId(), yearMonth);
            if (existing != null && existing.getStatus() == PayrollStatus.PROCESSED) {
                // Đã tính lương rồi, bỏ qua
                continue;
            }

            // Tính lương trực tiếp qua đa hình — FullTimeEmployee hoặc PartTimeEmployee
            // tự quyết định cách tính của mình (không cần SalaryCalculator)
            double netSalary = emp.calculateSalary(attendance, rule);

            // Tạo PayrollEntry mới
            PayrollEntry entry = new PayrollEntry(entryId, 0, emp.getId(), netSalary, PayrollStatus.PENDING);

            // Chốt lương (đánh dấu PROCESSED)
            entry.process();

            // Ghi vào repository (NO_LOCK — không đồng bộ)
            if (existing != null) {
                // Nếu đã có entry PENDING thì update
                entryRepo.update(entry);
            } else {
                // Nếu chưa có thì save mới
                entryRepo.save(entry);
            }

            results.add(entry);
        }

        return results;
    }

    /**
     * Lấy tất cả PayrollEntry đã có.
     */
    public List<PayrollEntry> getAllEntries() {
        return entryRepo.findAll();
    }

    /**
     * Lấy PayrollEntry theo tháng (lọc theo yearMonth trong entryId).
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
     * Tìm tên nhân viên theo ID (hỗ trợ cho View hiển thị).
     */
    public Employee findEmployeeById(String employeeId) {
        return employeeRepo.findById(employeeId);
    }
}
