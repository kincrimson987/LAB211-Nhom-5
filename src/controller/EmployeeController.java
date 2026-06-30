import java.util.List;
import java.util.UUID;

/**
 * Controller xử lý toàn bộ logic nghiệp vụ cho Employee.
 * MainView gọi các method này — không trực tiếp thao tác Repository.
 */
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeController(EmployeeRepository employeeRepository,
                              DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    // ─────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────

    /**
     * Thêm nhân viên mới.
     *
     * @param name         Họ tên (bắt buộc)
     * @param email        Email (bắt buộc, phải unique)
     * @param departmentId ID phòng ban (phải tồn tại)
     * @param type         FULLTIME hoặc PARTTIME
     * @param baseSalary   Lương cơ bản (>= 0)
     * @return Employee vừa tạo
     */
    public Employee addEmployee(String name,
                                String email,
                                String departmentId,
                                EmployeeType type,
                                double baseSalary) {
        // Validate
        validateName(name);
        validateEmail(email);
        checkEmailUnique(email, null);
        validateDepartmentExists(departmentId);
        validateBaseSalary(baseSalary);

        // Tạo đối tượng đúng kiểu
        Employee employee;
        if (type == EmployeeType.FULLTIME) {
            employee = new FullTimeEmployee(
                    generateId(), 1L, name.trim(), email.trim(), departmentId, baseSalary);
        } else {
            employee = new PartTimeEmployee(
                    generateId(), 1L, name.trim(), email.trim(), departmentId, baseSalary);
        }

        employeeRepository.save(employee);
        return employee;
    }

    // ─────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────

    /** Lấy toàn bộ danh sách nhân viên. */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Tìm nhân viên theo ID.
     *
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    public Employee getEmployeeById(String id) {
        Employee employee = employeeRepository.findById(id);
        if (employee == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id);
        }
        return employee;
    }

    /**
     * Lấy danh sách nhân viên theo phòng ban.
     *
     * @param departmentId ID phòng ban (phải tồn tại)
     */
    public List<Employee> getEmployeesByDepartment(String departmentId) {
        validateDepartmentExists(departmentId);
        return employeeRepository.findByDepartment(departmentId);
    }

    /**
     * Lọc nhân viên theo loại hợp đồng.
     */
    public List<Employee> getEmployeesByType(EmployeeType type) {
        if (type == null) {
            throw new IllegalArgumentException("Loại nhân viên không được để trống.");
        }
        return employeeRepository.findByType(type);
    }

    /**
     * Tìm kiếm nhân viên theo tên (không phân biệt hoa thường).
     */
    public List<Employee> searchByName(String keyword) {
        return employeeRepository.searchByName(keyword);
    }

    // ─────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────

    /**
     * Cập nhật thông tin nhân viên.
     * Chỉ cập nhật các trường được truyền vào (null = giữ nguyên).
     *
     * @param id           ID nhân viên cần cập nhật
     * @param name         Tên mới (null = giữ nguyên)
     * @param email        Email mới (null = giữ nguyên)
     * @param departmentId Phòng ban mới (null = giữ nguyên)
     * @param baseSalary   Lương mới (< 0 = giữ nguyên)
     * @return Employee sau khi cập nhật
     */
    public Employee updateEmployee(String id,
                                   String name,
                                   String email,
                                   String departmentId,
                                   double baseSalary) {
        Employee existing = getEmployeeById(id); // throws nếu không tồn tại

        if (name != null) {
            validateName(name);
            existing.setName(name.trim());
        }

        if (email != null) {
            validateEmail(email);
            checkEmailUnique(email.trim(), id); // cho phép giữ email cũ của chính mình
            existing.setEmail(email.trim());
        }

        if (departmentId != null) {
            validateDepartmentExists(departmentId);
            existing.setDepartmentId(departmentId);
        }

        if (baseSalary >= 0) {
            validateBaseSalary(baseSalary);
            existing.setBaseSalary(baseSalary);
        }

        existing.setVersion(existing.getVersion() + 1);
        employeeRepository.update(existing);
        return existing;
    }

    // ─────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────

    /**
     * Xóa nhân viên theo ID.
     *
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    public void deleteEmployee(String id) {
        getEmployeeById(id); // throws nếu không tồn tại
        employeeRepository.delete(id);
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────

    private String generateId() {
        return "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhân viên không được để trống.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (!email.trim().contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ: " + email);
        }
    }

    /**
     * Kiểm tra email chưa được dùng bởi nhân viên khác.
     *
     * @param email     Email cần kiểm tra
     * @param excludeId ID nhân viên hiện tại (null nếu tạo mới)
     */
    private void checkEmailUnique(String email, String excludeId) {
        boolean duplicate = employeeRepository.findAll().stream()
                .anyMatch(e -> e.getEmail() != null
                        && e.getEmail().equalsIgnoreCase(email)
                        && !e.getId().equals(excludeId));

        if (duplicate) {
            throw new IllegalArgumentException("Email đã được sử dụng: " + email);
        }
    }

    private void validateDepartmentExists(String departmentId) {
        if (departmentId == null || departmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID phòng ban không được để trống.");
        }
        Department dept = departmentRepository.findById(departmentId);
        if (dept == null) {
            throw new IllegalArgumentException("Phòng ban không tồn tại: " + departmentId);
        }
    }

    private void validateBaseSalary(double baseSalary) {
        if (baseSalary < 0) {
            throw new IllegalArgumentException("Lương cơ bản không được âm.");
        }
    }
}