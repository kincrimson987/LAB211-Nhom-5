import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Controller xử lý toàn bộ logic nghiệp vụ cho Employee.
 * MainView gọi các method này — không trực tiếp thao tác Repository.
 */
public class EmployeeController {
    private static final int EMPLOYEE_ID_SEED = 1200;
    private static final String EMPLOYEE_SEQUENCE_FILE = "data/employee_sequence.txt";

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public EmployeeController(EmployeeRepository employeeRepository,
                              DepartmentRepository departmentRepository) {
        this(employeeRepository, departmentRepository, null, null);
    }

    public EmployeeController(EmployeeRepository employeeRepository,
                              DepartmentRepository departmentRepository,
                              UserAccountRepository userAccountRepository) {
        this(employeeRepository, departmentRepository, userAccountRepository, null);
    }

    public EmployeeController(EmployeeRepository employeeRepository,
                              DepartmentRepository departmentRepository,
                              UserAccountRepository userAccountRepository,
                              LeaveBalanceRepository leaveBalanceRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userAccountRepository = userAccountRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
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
        String normalizedDepartmentId = normalizeDepartmentId(departmentId);
        validateDepartmentExists(normalizedDepartmentId);
        validateBaseSalary(baseSalary);
        if (type == null) {
            throw new IllegalArgumentException("Employee type cannot be empty.");
        }

        // Tạo đối tượng đúng kiểu
        Employee employee;
        if (type == EmployeeType.FULLTIME) {
            employee = new FullTimeEmployee(
                    generateId(), 1L, name.trim(), email.trim(), normalizedDepartmentId, baseSalary);
        } else {
            employee = new PartTimeEmployee(
                    generateId(), 1L, name.trim(), email.trim(), normalizedDepartmentId, baseSalary);
        }

        employeeRepository.save(employee);
        UserAccount createdAccount = null;
        LeaveBalance createdBalance = null;
        try {
            if (userAccountRepository != null) {
                createdAccount = buildEmployeeAccount(employee);
                userAccountRepository.save(createdAccount);
            }
            if (leaveBalanceRepository != null
                    && leaveBalanceRepository.findByEmployeeAndType(
                            employee.getId(), LeaveType.PAID_LEAVE) == null) {
                createdBalance = new LeaveBalance(
                        "LB_" + employee.getId() + "_PAID_LEAVE",
                        employee.getId(), LeaveType.PAID_LEAVE, 18);
                leaveBalanceRepository.save(createdBalance);
            }
        } catch (RuntimeException ex) {
            if (createdBalance != null
                    && leaveBalanceRepository.findById(createdBalance.getId()) != null) {
                leaveBalanceRepository.delete(createdBalance.getId());
            }
            if (createdAccount != null
                    && userAccountRepository.findById(createdAccount.getId()) != null) {
                userAccountRepository.delete(createdAccount.getId());
            }
            employeeRepository.delete(employee.getId());
            throw new IllegalStateException(
                    "Related employee data could not be created; employee creation was rolled back.", ex);
        }
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
        String normalizedId = normalizeEmployeeId(id);
        Employee employee = employeeRepository.findById(normalizedId);
        if (employee == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + normalizedId);
        }
        return employee;
    }

    public UserAccount getAccountByEmployeeId(String employeeId) {
        return userAccountRepository == null
                ? null
                : userAccountRepository.findByEmployeeId(normalizeEmployeeId(employeeId));
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
            String normalizedDepartmentId = normalizeDepartmentId(departmentId);
            validateDepartmentExists(normalizedDepartmentId);
            existing.setDepartmentId(normalizedDepartmentId);
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
        Employee employee = getEmployeeById(id);
        UserAccount account = userAccountRepository == null
                ? null
                : userAccountRepository.findByEmployeeId(employee.getId());
        List<LeaveBalance> balances = leaveBalanceRepository == null
                ? java.util.Collections.emptyList()
                : leaveBalanceRepository.findByEmployee(employee.getId());
        employeeRepository.delete(employee.getId());
        try {
            if (account != null) {
                userAccountRepository.delete(account.getId());
            }
            for (LeaveBalance balance : balances) {
                leaveBalanceRepository.delete(balance.getId());
            }
        } catch (RuntimeException ex) {
            if (employeeRepository.findById(employee.getId()) == null) {
                employeeRepository.save(employee);
            }
            if (account != null && userAccountRepository.findById(account.getId()) == null) {
                userAccountRepository.save(account);
            }
            for (LeaveBalance balance : balances) {
                if (leaveBalanceRepository.findById(balance.getId()) == null) {
                    leaveBalanceRepository.save(balance);
                }
            }
            throw new IllegalStateException(
                    "Related employee data could not be deleted; employee deletion was rolled back.", ex);
        }
    }

    // ─────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────

    private String generateId() {
        int nextNumber = getNextEmployeeNumber();
        saveLastEmployeeNumber(nextNumber);
        return String.format("E%04d", nextNumber);
    }

    private UserAccount buildEmployeeAccount(Employee employee) {
        if (userAccountRepository.findByEmployeeId(employee.getId()) != null) {
            throw new IllegalArgumentException("Employee already has an account: " + employee.getId());
        }
        String username = employee.getId().toLowerCase(java.util.Locale.ROOT);
        if (userAccountRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        return new UserAccount(generateUserAccountId(), 1L,
                username, username + "@123", "EMPLOYEE", true, employee.getId());
    }

    private String generateUserAccountId() {
        int max = userAccountRepository.findAll().stream()
                .map(UserAccount::getId)
                .filter(id -> id != null && id.matches("U\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(1)))
                .max()
                .orElse(0);
        return String.format("U%04d", max + 1);
    }

    private String normalizeEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty.");
        }
        return employeeId.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String normalizeDepartmentId(String departmentId) {
        if (departmentId == null || departmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Department ID cannot be empty.");
        }
        return departmentId.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private int getNextEmployeeNumber() {
        int currentMax = employeeRepository.findAll().stream()
                .map(Employee::getId)
                .mapToInt(this::extractEmployeeNumber)
                .max()
                .orElse(0);
        int storedLast = readLastEmployeeNumber();
        int lastUsed = Math.max(Math.max(currentMax, storedLast), EMPLOYEE_ID_SEED);
        return lastUsed + 1;
    }

    private int extractEmployeeNumber(String employeeId) {
        if (employeeId == null || !employeeId.matches("E\\d{4,}")) {
            return -1;
        }
        return Integer.parseInt(employeeId.substring(1));
    }

    private int readLastEmployeeNumber() {
        try {
            Path path = Path.of(EMPLOYEE_SEQUENCE_FILE);
            if (!Files.exists(path)) {
                return 0;
            }
            String value = Files.readString(path).trim();
            if (value.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (IOException | NumberFormatException ex) {
            return 0;
        }
    }

    private void saveLastEmployeeNumber(int number) {
        try {
            Path path = Path.of(EMPLOYEE_SEQUENCE_FILE);
            Path parent = path.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, String.valueOf(number));
        } catch (IOException ex) {
            throw new RuntimeException("Khong the luu bo dem ma nhan vien.", ex);
        }
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
