

import java.util.Set;

/**
import java.util.Set;

/**
 * EmployeeAuthController — quyền Employee theo Use Case.
 * Chỉ được: Check In/Out, Submit Leave, View Attendance/Leave Balance.
 */
public class EmployeeAuthController extends AuthController {

    private static final Set<String> ALLOWED = Set.of(
            "CHECK_IN", "CHECK_OUT",
            "VIEW_ATTENDANCE_RECORD", "VIEW_ATTENDANCE_SUMMARY",
            "SUBMIT_ATTENDANCE_ADJUSTMENT",
            "SUBMIT_LEAVE_REQUEST", "VIEW_LEAVE_BALANCE"
    );

    public EmployeeAuthController(UserAccount account) {
        super(account);
    }

    @Override
    public boolean canAccess(String feature) {
        return ALLOWED.contains(feature.toUpperCase());
    }
}