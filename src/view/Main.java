/**
 * Entry point — khởi tạo toàn bộ dependency theo thứ tự:
 *   Repository → Controller → View
 */
public class Main {

    public static void main(String[] args) {

        // ── 1. Repositories (tầng Data) ─────────────────────────────────
        UserAccountRepository    userRepo        = new UserAccountRepository();
        EmployeeRepository       employeeRepo    = new EmployeeRepository();
        DepartmentRepository     departmentRepo  = new DepartmentRepository();
        AttendanceRepository     attendanceRepo  = new AttendanceRepository();
        LeaveRequestRepository   leaveReqRepo    = new LeaveRequestRepository();
        LeaveBalanceRepository   leaveBalRepo    = new LeaveBalanceRepository();
        PayrollEntryRepository   payrollEntryRepo= new PayrollEntryRepository();
        PayrollRuleRepository    payrollRuleRepo = new PayrollRuleRepository();
        PayrollRunRepository     payrollRunRepo  = new PayrollRunRepository();

        // ── 2. Controllers (tầng Logic) ──────────────────────────────────
        AuthController       authController       = new AuthController(userRepo);
        EmployeeController   employeeController   = new EmployeeController(employeeRepo, departmentRepo);
        DepartmentController departmentController = new DepartmentController(departmentRepo);
        AttendanceController attendanceController = new AttendanceController(attendanceRepo, employeeRepo);
        LeaveController      leaveController      = new LeaveController(leaveReqRepo, leaveBalRepo, employeeRepo);
        PayrollController    payrollController    = new PayrollController(
                payrollEntryRepo, payrollRuleRepo, payrollRunRepo,
                attendanceRepo, employeeRepo, leaveBalRepo);
        ReportController     reportController     = new ReportController(
                payrollEntryRepo, attendanceRepo, payrollRunRepo, employeeRepo);
        SimulationController simulationController = new SimulationController(
                payrollEntryRepo, leaveBalRepo, payrollRunRepo,
                attendanceRepo, employeeRepo, payrollRuleRepo);

        // ── 3. View (tầng Presentation) ──────────────────────────────────
        MainView view = new MainView(
                authController,
                employeeController,
                departmentController,
                attendanceController,
                leaveController,
                payrollController,
                reportController,
                simulationController
        );

        // ── 4. Chạy ứng dụng ─────────────────────────────────────────────
        view.run();
    }
}