import java.util.Set;

/**
 * EmployeeAuthController - quyen Employee theo Use Case.
 */
public class EmployeeAuthController extends AuthController {

    private static final Set<String> ALLOWED = Set.of(
            "CHECK_IN", "CHECK_OUT",
            "VIEW_ATTENDANCE_RECORD", "VIEW_ATTENDANCE_SUMMARY",
            "SUBMIT_ATTENDANCE_ADJUSTMENT",
            "SUBMIT_LEAVE_REQUEST", "VIEW_LEAVE_BALANCE",
            "VIEW_PAYROLL_HISTORY"
    );

    public EmployeeAuthController(UserAccount account) {
        super(account);
    }

    @Override
    public boolean canAccess(String feature) {
        return ALLOWED.contains(feature.toUpperCase());
    }
}
