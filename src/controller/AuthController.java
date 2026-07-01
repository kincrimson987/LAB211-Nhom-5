
/**
 * AuthController — lớp cha xử lý login chung.
 * Các subclass override canAccess() theo quyền riêng.
 * Không dùng protected — subclass truy cập qua getter.
 */
public abstract class AuthController {

    private final UserAccount account;

    public AuthController(UserAccount account) {
        this.account = account;
    }

    // ── Getters ───────────────────────────────────────────────

    public String getUsername() {
        return account.getUsername();
    }

    public String getRole() {
        return account.getRole();
    }

    public String getEmployeeId() {
        return account.getEmployeeId();
    }

    public UserAccount getAccount() {
        return account;
    }

    // ── Abstract — subclass bắt buộc override ────────────────

    public abstract boolean canAccess(String feature);

    // ── Kiểm tra quyền — throw nếu không đủ ─────────────────

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

    // ── Static factory login ──────────────────────────────────

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
            default -> throw new IllegalStateException(
                    "Unknown role: " + account.getRole());
        };
    }
}
