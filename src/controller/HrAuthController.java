
import java.util.Set;


public class HrAuthController extends AuthController {

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
            // Payroll
            "PROCESS_MONTHLY_PAYROLL", "VIEW_PAYROLL_HISTORY",
            "CONFIGURE_PAYROLL_RULES",
            // Reports
            "GENERATE_PAYROLL_REPORT", "GENERATE_ATTENDANCE_REPORT",
            "GENERATE_SIMULATION_REPORT", "EXPORT_CSV_RESULT", "IMPORT_CSV_RESULT",
            // Synchronization and simulation
            "SELECT_SYNC_MECHANISM", "RUN_PAYROLL_SIMULATION", "MEASURE_TPS",
            "DETECT_DOUBLE_PAYMENT", "DETECT_WRONG_LEAVE_DEDUCTION"
    );

    public HrAuthController(UserAccount account) {
        super(account);
    }

    @Override
    public boolean canAccess(String feature) {
        return ALLOWED.contains(feature.toUpperCase());
    }
}
