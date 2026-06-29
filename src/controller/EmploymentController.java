import java.util.Set;

// ═══════════════════════════════════════════════════════════════
// BASE CLASS
// ═══════════════════════════════════════════════════════════════

/**
 * AuthController — lớp cha xử lý login/logout chung.
 * Các subclass override canAccess() theo quyền riêng.
 */
abstract class AuthController {

    private final UserAccount account;

    AuthController(UserAccount account) {
        this.account = account;
    }

    // ── Thông tin session ─────────────────────────────────────

    public String getUsername() {
        return account.getUsername();
    }

    public String getRole() {
        return account.getRole();
    }

    public UserAccount getAccount() {
        return account;
    }

    /**
     * Subclass override — kiểm tra quyền truy cập feature.
     */
    public abstract boolean canAccess(String feature);

    /**
     * Throw nếu không có quyền.
     * MainView gọi trước khi thực hiện action.
     */
    public void requireAccess(String feature) {
        if (!canAccess(feature)) {
            throw new IllegalStateException(
                    "Access denied: [" + getRole() + "] cannot access [" + feature + "]");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{username='" + getUsername()
                + "', role='" + getRole() + "'}";
    }

    // ── Static factory — tạo đúng subclass sau khi login ─────

    /**
     * Login — validate rồi trả về đúng subclass theo role.
     *
     * @param userRepository Repository tìm tài khoản
     * @param username       Tên đăng nhập
     * @param password       Mật khẩu
     * @return AuthController đúng role (Admin/Hr/Employee)
     */
    public static AuthController login(UserAccountRepository userRepository,
                                       String username,
                                       String password) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Username cannot be empty.");
        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException("Password cannot be empty.");

        UserAccount account = userRepository.findByUsername(username.trim());

        if (account == null)
            throw new IllegalArgumentException("Account not found: " + username);
        if (!account.isActive())
            throw new IllegalStateException("Account is disabled: " + username);
        if (!account.checkPassword(password))
            throw new IllegalStateException("Incorrect password.");

        return switch (account.getRole().toUpperCase()) {
            case "ADMIN"    -> new AdminAuthController(account);
            case "HR"       -> new HrAuthController(account);
            case "EMPLOYEE" -> new EmployeeAuthController(account);
            default -> throw new IllegalStateException("Unknown role: " + account.getRole());
        };
    }
}

// ═══════════════════════════════════════════════════════════════
// ADMIN — full quyền
// ═══════════════════════════════════════════════════════════════

class AdminAuthController extends AuthController {

    AdminAuthController(UserAccount account) {
        super(account);
    }

    @Override
    public boolean canAccess(String feature) {
        return true; // Admin được tất cả
    }
}

// ═══════════════════════════════════════════════════════════════
// HR — Employee Mgmt + Attendance + Leave + Payroll + Reports
// ═══════════════════════════════════════════════════════════════

class HrAuthController extends AuthController {

    private static final Set<String> ALLOWED = Set.of(
            // Employee Management
            "ADD_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE",
            "SEARCH_EMPLOYEE", "VIEW_EMPLOYEE_DETAIL",
            "ADD_DEPARTMENT", "UPDATE_DEPARTMENT", "DELETE_DEPARTMENT",
            "SEARCH_DEPARTMENT", "VIEW_DEPARTMENTS",
            "SAVE_CSV_DATA", "LOAD_CSV_DATA", "GENERATE_TEST_DATASET",
            // Attendance
            "CHECK_IN", "CHECK_OUT",
            "VIEW_ATTENDANCE_RECORD", "VIEW_ATTENDANCE_SUMMARY",
            "SUBMIT_ATTENDANCE_ADJUSTMENT", "REVIEW_ATTENDANCE_ADJUSTMENT",
            "APPROVE_ATTENDANCE_ADJUSTMENT", "REJECT_ATTENDANCE_ADJUSTMENT",
            // Leave
            "SUBMIT_LEAVE_REQUEST", "VIEW_LEAVE_BALANCE",
            "APPROVE_LEAVE_REQUEST", "REJECT_LEAVE_REQUEST",
            // Payroll — không có CONFIGURE_PAYROLL_RULES
            "PROCESS_MONTHLY_PAYROLL", "VIEW_PAYROLL_HISTORY",
            // Reports
            "GENERATE_PAYROLL_REPORT", "GENERATE_ATTENDANCE_REPORT",
            "GENERATE_SIMULATION_REPORT", "EXPORT_CSV_RESULT", "IMPORT_CSV_RESULT"
    );

    HrAuthController(UserAccount account) {
        super(account);
    }

    @Override
    public boolean canAccess(String feature) {
        return ALLOWED.contains(feature.toUpperCase());
    }
}

// ═══════════════════════════════════════════════════════════════
// EMPLOYEE — chỉ Check In/Out + Submit Leave + View
// ═══════════════════════════════════════════════════════════════

class EmployeeAuthController extends AuthController {

    private static final Set<String> ALLOWED = Set.of(
            "CHECK_IN", "CHECK_OUT",
            "VIEW_ATTENDANCE_RECORD", "VIEW_ATTENDANCE_SUMMARY",
            "SUBMIT_ATTENDANCE_ADJUSTMENT",
            "SUBMIT_LEAVE_REQUEST", "VIEW_LEAVE_BALANCE"
    );

    EmployeeAuthController(UserAccount account) {
        super(account);
    }

    @Override
    public boolean canAccess(String feature) {
        return ALLOWED.contains(feature.toUpperCase());
    }
}