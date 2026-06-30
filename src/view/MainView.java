
import java.util.List;
import java.util.Scanner;

public class MainView {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String DIM    = "\u001B[2m";

    private final UserAccountRepository  userRepo;
    private final EmployeeController     employeeController;
    private final DepartmentController   departmentController;
    private final AttendanceController   attendanceController;
    private final LeaveController        leaveController;
    private final PayrollController      payrollController;
    private final ReportController       reportController;
    private final SimulationController   simulationController;

    private final Scanner scanner;
    private AuthController currentSession = null;

    public MainView(UserAccountRepository userRepo,
                    EmployeeController employeeController,
                    DepartmentController departmentController,
                    AttendanceController attendanceController,
                    LeaveController leaveController,
                    PayrollController payrollController,
                    ReportController reportController,
                    SimulationController simulationController) {
        this.userRepo             = userRepo;
        this.employeeController   = employeeController;
        this.departmentController = departmentController;
        this.attendanceController = attendanceController;
        this.leaveController      = leaveController;
        this.payrollController    = payrollController;
        this.reportController     = reportController;
        this.simulationController = simulationController;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        printBanner();
        if (!handleLogin()) {
            printError("Login failed. Exiting.");
            scanner.close();
            return;
        }
        boolean running = true;
        while (running) {
            printMainMenu();
            switch (prompt("Choose").trim()) {
                case "1" -> handleLogin();
                case "2" -> {
                    if (checkAccess("ADD_EMPLOYEE")) showEmployeeManagement();
                }
                case "3" -> {
                    if (checkAccess("CHECK_IN")) showAttendanceManagement();
                }
                case "4" -> {
                    if (checkAccess("SUBMIT_LEAVE_REQUEST")) showLeaveManagement();
                }
                case "5" -> {
                    if (checkAccess("PROCESS_MONTHLY_PAYROLL")) showPayrollManagement();
                }
                case "6" -> {
                    if (checkAccess("GENERATE_PAYROLL_REPORT")) showReports();
                }
                case "7" -> {
                    if (checkAccess("RUN_PAYROLL_SIMULATION")) showSyncAndSimulation();
                }
                case "0" -> { printSuccess("Goodbye!"); running = false; }
                default  -> printError("Invalid choice.");
            }
        }
        scanner.close();
    }

    /**
     * Kiểm tra quyền — in lỗi và trả về false nếu role không đủ quyền.
     * Dùng để chặn cả việc vào menu lớn lẫn từng action con bên trong.
     */
    private boolean checkAccess(String feature) {
        if (currentSession == null) {
            printError("You must login first.");
            return false;
        }
        if (!currentSession.canAccess(feature)) {
            printError("Access denied for role [" + currentSession.getRole()
                    + "]. This feature requires higher permission.");
            return false;
        }
        return true;
    }

    // ═════════════════════════════════════════
    // 1. LOGIN
    // ═════════════════════════════════════════

    private boolean handleLogin() {
        printSectionHeader("LOGIN");
        int attempts = 0;
        while (attempts < 3) {
            String username = prompt("Username");
            String password = prompt("Password");
            try {
                currentSession = AuthController.login(userRepo, username, password);
                printSuccess("Welcome, " + currentSession.getUsername()
                        + " [" + currentSession.getRole() + "]");
                return true;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                attempts++;
                printError(ex.getMessage()
                        + (attempts < 3 ? " (" + (3 - attempts) + " attempts left)" : ""));
            }
        }
        return false;
    }

    // ═════════════════════════════════════════
    // 2. EMPLOYEE MANAGEMENT
    // ═════════════════════════════════════════

    private void showEmployeeManagement() {
        boolean back = false;
        while (!back) {
            printSubMenu("EMPLOYEE MANAGEMENT", new String[]{
                "Add Employee", "Update Employee", "Delete Employee",
                "Search Employee", "View Employee Detail", "---",
                "Add Department", "Update Department", "Delete Department",
                "Search Department", "View Departments", "---",
                "Save CSV Data", "Load CSV Data", "Generate Test Dataset"
            });
            switch (prompt("Choose").trim()) {
                case "1"  -> handleAddEmployee();
                case "2"  -> handleUpdateEmployee();
                case "3"  -> handleDeleteEmployee();
                case "4"  -> handleSearchEmployee();
                case "5"  -> handleViewEmployeeDetail();
                case "6"  -> handleAddDepartment();
                case "7"  -> handleUpdateDepartment();
                case "8"  -> handleDeleteDepartment();
                case "9"  -> handleSearchDepartment();
                case "10" -> handleViewDepartments();
                case "11" -> printInfo("Data is auto-saved via Repository.");
                case "12" -> printInfo("Data is auto-loaded via Repository.");
                case "13" -> printInfo("Generate test dataset: not implemented yet.");
                case "0"  -> back = true;
                default   -> printError("Invalid choice.");
            }
        }
    }

    private void handleAddEmployee() {
        printSectionHeader("ADD EMPLOYEE");
        try {
            String name         = prompt("Full name");
            String email        = prompt("Email");
            String departmentId = prompt("Department ID");
            System.out.println("  1. FULLTIME   2. PARTTIME");
            EmployeeType type = prompt("Choose").equals("2")
                    ? EmployeeType.PARTTIME : EmployeeType.FULLTIME;
            double baseSalary = promptDouble("Base salary (VND, 0 = default)");
            Employee created = employeeController.addEmployee(name, email, departmentId, type, baseSalary);
            printSuccess("Employee added!");
            printEmployeeDetail(created);
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleUpdateEmployee() {
        printSectionHeader("UPDATE EMPLOYEE");
        String id = prompt("Employee ID");
        try {
            printEmployeeDetail(employeeController.getEmployeeById(id));
            printInfo("Press Enter to keep current value.");
            String name         = promptOptional("New name");
            String email        = promptOptional("New email");
            String departmentId = promptOptional("New department ID");
            String salaryStr    = promptOptional("New base salary");
            double salary       = salaryStr != null ? parseDoubleVal(salaryStr, -1) : -1;
            Employee updated = employeeController.updateEmployee(id, name, email, departmentId, salary);
            printSuccess("Updated!");
            printEmployeeDetail(updated);
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleDeleteEmployee() {
        printSectionHeader("DELETE EMPLOYEE");
        String id = prompt("Employee ID");
        try {
            printEmployeeDetail(employeeController.getEmployeeById(id));
            if (confirm("Confirm delete?")) {
                employeeController.deleteEmployee(id);
                printSuccess("Deleted.");
            } else { printInfo("Cancelled."); }
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleSearchEmployee() {
        printSectionHeader("SEARCH EMPLOYEE");
        System.out.println("  1. By name   2. By department   3. By type");
        switch (prompt("Choose").trim()) {
            case "1" -> printEmployeeTable(employeeController.searchByName(prompt("Keyword")));
            case "2" -> {
                try { printEmployeeTable(employeeController.getEmployeesByDepartment(prompt("Department ID"))); }
                catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
            }
            case "3" -> {
                System.out.println("  1. FULLTIME   2. PARTTIME");
                EmployeeType type = prompt("Choose").equals("2") ? EmployeeType.PARTTIME : EmployeeType.FULLTIME;
                printEmployeeTable(employeeController.getEmployeesByType(type));
            }
            default -> printError("Invalid choice.");
        }
    }

    private void handleViewEmployeeDetail() {
        printSectionHeader("VIEW EMPLOYEE DETAIL");
        try { printEmployeeDetail(employeeController.getEmployeeById(prompt("Employee ID"))); }
        catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleAddDepartment() {
        printSectionHeader("ADD DEPARTMENT");
        try {
            String name      = prompt("Department name");
            String managerId = promptOptional("Manager ID (Enter to skip)");
            Department dept  = departmentController.addDepartment(name, managerId);
            printSuccess("Department added: " + dept.getId() + " — " + dept.getName());
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleUpdateDepartment() {
        printSectionHeader("UPDATE DEPARTMENT");
        String id = prompt("Department ID");
        try {
            printDepartmentDetail(departmentController.getDepartmentById(id));
            Department updated = departmentController.updateDepartment(
                    id, promptOptional("New name"), promptOptional("New manager ID"));
            printSuccess("Updated: " + updated.getName());
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleDeleteDepartment() {
        printSectionHeader("DELETE DEPARTMENT");
        String id = prompt("Department ID");
        try {
            printDepartmentDetail(departmentController.getDepartmentById(id));
            if (confirm("Confirm delete?")) { departmentController.deleteDepartment(id); printSuccess("Deleted."); }
            else printInfo("Cancelled.");
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleSearchDepartment() {
        printSectionHeader("SEARCH DEPARTMENT");
        try {
            List<Department> list = departmentController.searchByName(prompt("Keyword"));
            if (list.isEmpty()) printInfo("No departments found.");
            else printDepartmentTable(list);
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleViewDepartments() {
        printSectionHeader("ALL DEPARTMENTS");
        List<Department> list = departmentController.getAllDepartments();
        if (list.isEmpty()) printInfo("No departments yet.");
        else { printDepartmentTable(list); printInfo("Total: " + list.size()); }
    }

    // ═════════════════════════════════════════
    // 3. ATTENDANCE MANAGEMENT
    // ═════════════════════════════════════════

    private void showAttendanceManagement() {
        boolean back = false;
        while (!back) {
            printSubMenu("ATTENDANCE MANAGEMENT", new String[]{
                "Check In", "Check Out", "View Attendance Record",
                "View Attendance Summary", "Submit Attendance Adjustment Request",
                "Review Attendance Adjustment Request",
                "Approve Attendance Adjustment", "Reject Attendance Adjustment"
            });
            switch (prompt("Choose").trim()) {
                case "1" -> handleCheckIn();
                case "2" -> handleCheckOut();
                case "3" -> handleViewAttendanceRecord();
                case "4" -> handleViewAttendanceSummary();
                case "5" -> handleSubmitAttendanceAdjustment();
                case "6" -> handleReviewAttendanceAdjustment();
                case "7" -> handleApproveAttendanceAdjustment();
                case "8" -> handleRejectAttendanceAdjustment();
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    private void handleCheckIn() {
        printSectionHeader("CHECK IN");
        try {
            AttendanceRecord r = attendanceController.checkIn(prompt("Employee ID"));
            printSuccess("Checked in. Work days: " + r.getWorkDays());
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleCheckOut() {
        printSectionHeader("CHECK OUT");
        try {
            AttendanceRecord r = attendanceController.checkOut(
                    prompt("Employee ID"), promptDouble("Overtime hours (0 if none)"));
            printSuccess("Checked out. Overtime: " + r.getOvertimeHours() + "h");
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleViewAttendanceRecord() {
        printSectionHeader("VIEW ATTENDANCE RECORD");
        try {
            AttendanceRecord r = attendanceController.getRecord(
                    prompt("Employee ID"), prompt("Year-Month (YYYY-MM)"));
            if (r == null) printInfo("No record found.");
            else printAttendanceDetail(r);
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleViewAttendanceSummary() {
        printSectionHeader("VIEW ATTENDANCE SUMMARY");
        List<AttendanceRecord> list = attendanceController.getSummaryByMonth(prompt("Year-Month (YYYY-MM)"));
        if (list.isEmpty()) printInfo("No records found.");
        else printAttendanceTable(list);
    }

    private void handleSubmitAttendanceAdjustment() {
        printSectionHeader("SUBMIT ATTENDANCE ADJUSTMENT");
        try {
            String empId     = prompt("Employee ID");
            String yearMonth = prompt("Year-Month (YYYY-MM)");

            // Hiển thị số liệu hiện tại để HR biết đang sửa từ đâu
            AttendanceRecord current = attendanceController.getRecord(empId, yearMonth);
            int curWorkDays = (current != null) ? current.getWorkDays() : 0;
            double curOvertime = (current != null) ? current.getOvertimeHours() : 0;

            System.out.println("  Current: Work Days = " + curWorkDays
                    + ", Overtime = " + curOvertime + "h");
            printInfo("Press Enter to keep current value.");

            String reason = prompt("Reason");

            String workDaysStr = promptOptional("New work days [" + curWorkDays + "]");
            int workDays = (workDaysStr != null) ? parseInt(workDaysStr, curWorkDays) : curWorkDays;

            String overtimeStr = promptOptional("New overtime hours [" + curOvertime + "]");
            double overtime = (overtimeStr != null) ? parseDoubleVal(overtimeStr, curOvertime) : curOvertime;

            attendanceController.submitAdjustmentRequest(empId, yearMonth, reason, workDays, overtime);
            printSuccess("Adjustment submitted: " + curWorkDays + " → " + workDays + " days");
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleReviewAttendanceAdjustment() {
        printSectionHeader("REVIEW ATTENDANCE ADJUSTMENT");
        List<AttendanceRecord> list = attendanceController.getPendingAdjustments();
        if (list.isEmpty()) printInfo("No pending adjustments.");
        else printAttendanceTable(list);
    }

    private void handleApproveAttendanceAdjustment() {
        if (!checkAccess("APPROVE_ATTENDANCE_ADJUSTMENT")) return;
        printSectionHeader("APPROVE ATTENDANCE ADJUSTMENT");
        try {
            attendanceController.approveAdjustment(
                    prompt("Record ID"), currentSession.getAccount().getId());
            printSuccess("Approved.");
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleRejectAttendanceAdjustment() {
        if (!checkAccess("REJECT_ATTENDANCE_ADJUSTMENT")) return;
        printSectionHeader("REJECT ATTENDANCE ADJUSTMENT");
        try {
            attendanceController.rejectAdjustment(
                    prompt("Record ID"), currentSession.getAccount().getId(), prompt("Reason"));
            printSuccess("Rejected.");
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    // ═════════════════════════════════════════
    // 4. LEAVE MANAGEMENT
    // ═════════════════════════════════════════

    private void showLeaveManagement() {
        boolean back = false;
        while (!back) {
            printSubMenu("LEAVE MANAGEMENT", new String[]{
                "Submit Leave Request", "Approve Leave Request",
                "Reject Leave Request", "View Leave Balance"
            });
            switch (prompt("Choose").trim()) {
                case "1" -> handleSubmitLeaveRequest();
                case "2" -> handleApproveLeaveRequest();
                case "3" -> handleRejectLeaveRequest();
                case "4" -> handleViewLeaveBalance();
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    private void handleSubmitLeaveRequest() {
        printSectionHeader("SUBMIT LEAVE REQUEST");
        try {
            String empId = prompt("Employee ID");
            System.out.println("  1. ANNUAL   2. SICK   3. UNPAID");
            LeaveType type = switch (prompt("Choose")) {
                case "2" -> LeaveType.SICK;
                case "3" -> LeaveType.UNPAID;
                default  -> LeaveType.ANNUAL;
            };
            // Gọi đúng tên method: submit() thay vì submitLeaveRequest()
            LeaveRequest req = leaveController.submit(
                    empId, type,
                    java.time.LocalDate.parse(prompt("Start date (YYYY-MM-DD)")),
                    java.time.LocalDate.parse(prompt("End date (YYYY-MM-DD)")),
                    prompt("Reason"));
            printSuccess("Submitted: " + req.getLeaveId() + " (" + req.getDays() + " days)");
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    private void handleApproveLeaveRequest() {
        if (!checkAccess("APPROVE_LEAVE_REQUEST")) return;
        printSectionHeader("APPROVE LEAVE REQUEST");
        List<LeaveRequest> pending = leaveController.getPendingRequests();
        if (pending.isEmpty()) { printInfo("No pending requests."); return; }
        printLeaveTable(pending);
        try {
            // Gọi đúng tên method: approve() với LockMechanism
            leaveController.approve(prompt("Leave ID"), currentSession.getAccount().getId(),
                    LockMechanism.NO_LOCK);
            printSuccess("Approved.");
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    private void handleRejectLeaveRequest() {
        if (!checkAccess("REJECT_LEAVE_REQUEST")) return;
        printSectionHeader("REJECT LEAVE REQUEST");
        List<LeaveRequest> pending = leaveController.getPendingRequests();
        if (pending.isEmpty()) { printInfo("No pending requests."); return; }
        printLeaveTable(pending);
        try {
            // Gọi đúng tên method: reject()
            leaveController.reject(prompt("Leave ID"), currentSession.getAccount().getId());
            printSuccess("Rejected.");
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    private void handleViewLeaveBalance() {
        printSectionHeader("VIEW LEAVE BALANCE");
        try {
            // Gọi đúng tên method: getBalancesByEmployee()
            List<LeaveBalance> balances = leaveController.getBalancesByEmployee(prompt("Employee ID"));
            if (balances.isEmpty()) { printInfo("No balance records found."); return; }
            System.out.printf(BOLD + "%-15s %-10s %-8s %-8s %-10s%n" + RESET,
                    "Employee", "Type", "Total", "Used", "Remaining");
            System.out.println("─".repeat(55));
            for (LeaveBalance b : balances) {
                System.out.printf("%-15s %-10s %-8d %-8d %-10d%n",
                        b.getEmployeeId(), b.getLeaveType(),
                        b.getTotalLeaveDays(), b.getUsedLeaveDays(), b.getRemainingLeaveDays());
            }
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    // ═════════════════════════════════════════
    // 5. PAYROLL MANAGEMENT
    // ═════════════════════════════════════════

    private void showPayrollManagement() {
        boolean back = false;
        while (!back) {
            printSubMenu("PAYROLL MANAGEMENT", new String[]{
                "Process Monthly Payroll", "View Payroll History", "Configure Payroll Rules"
            });
            switch (prompt("Choose").trim()) {
                case "1" -> handleProcessMonthlyPayroll();
                case "2" -> handleViewPayrollHistory();
                case "3" -> handleConfigurePayrollRules();
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    private void handleProcessMonthlyPayroll() {
        printSectionHeader("PROCESS MONTHLY PAYROLL");
        try {
            String yearMonth = prompt("Year-Month (YYYY-MM)");
            printInfo("Processing...");
            PayrollRun run = payrollController.processMonthlyPayroll(
                    yearMonth, currentSession.getAccount().getId());
            printSuccess("Done!");
            printPayrollRunSummary(run);
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    private void handleViewPayrollHistory() {
        printSectionHeader("VIEW PAYROLL HISTORY");
        try {
            String empId = promptOptional("Employee ID (Enter = all)");
            List<PayrollEntry> entries = empId != null
                    ? payrollController.getPayrollByEmployee(empId)
                    : payrollController.getAllPayrollEntries();
            if (entries.isEmpty()) { printInfo("No records."); return; }

            // Hiện thêm cột Type để thấy rõ 2 luồng tính lương đa hình
            System.out.printf(BOLD + "%-20s %-12s %-10s %-18s %-12s%n" + RESET,
                    "Entry ID", "Employee", "Type", "Net Salary (VND)", "Status");
            System.out.println("─".repeat(78));
            for (PayrollEntry e : entries) {
                Employee emp = payrollController.findEmployeeById(e.getEmployeeId());
                String type = (emp != null) ? String.valueOf(emp.getEmploymentType()) : "?";
                System.out.printf("%-20s %-12s %-10s %-18s %-12s%n",
                        e.getId(), e.getEmployeeId(), type,
                        String.format("%,.0f", e.getNetSalary()), e.getStatus());
            }
            System.out.println("─".repeat(78));

            // Giải thích công thức khác nhau giữa 2 loại
            printInfo("FULLTIME: base + overtime + attendance bonus - tax");
            printInfo("PARTTIME: base + overtime (no bonus, no tax)");
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    private void handleConfigurePayrollRules() {
        if (!checkAccess("CONFIGURE_PAYROLL_RULES")) return;
        printSectionHeader("CONFIGURE PAYROLL RULES");
        try {
            PayrollRule cur = payrollController.getPayrollRule();
            printPayrollRule(cur);
            printInfo("Press Enter to keep current value.");
            PayrollRule updated = new PayrollRule(
                    parseInt(promptOptional("Standard work days [" + cur.getStandardWorkingDays() + "]"), cur.getStandardWorkingDays()),
                    parseInt(promptOptional("Hours/day [" + cur.getWorkingHoursPerDay() + "]"), cur.getWorkingHoursPerDay()),
                    parseDoubleVal(promptOptional("Overtime multiplier [" + cur.getOvertimeMultiplier() + "]"), cur.getOvertimeMultiplier()),
                    parseDoubleVal(promptOptional("Attendance bonus [" + cur.getAttendanceBonus() + "]"), cur.getAttendanceBonus()),
                    parseDoubleVal(promptOptional("Tax rate [" + cur.getTaxRate() + "]"), cur.getTaxRate()),
                    parseDoubleVal(promptOptional("Tax threshold [" + cur.getTaxThreshold() + "]"), cur.getTaxThreshold())
            );
            payrollController.updatePayrollRule(updated);
            printSuccess("Payroll rules updated!");
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    // ═════════════════════════════════════════
    // 6. REPORTS
    // ═════════════════════════════════════════

    private void showReports() {
        boolean back = false;
        while (!back) {
            printSubMenu("REPORTS", new String[]{
                "Generate Payroll Report", "Generate Attendance Report",
                "Generate Simulation Comparison Report", "Export CSV Result", "Import CSV Result"
            });
            switch (prompt("Choose").trim()) {
                case "1" -> { try { System.out.println(reportController.generatePayrollReport(prompt("Year-Month (YYYY-MM)"))); } catch (Exception ex) { printError(ex.getMessage()); } }
                case "2" -> { try { System.out.println(reportController.generateAttendanceReport(prompt("Year-Month (YYYY-MM)"))); } catch (Exception ex) { printError(ex.getMessage()); } }
                case "3" -> { try { System.out.println(reportController.generateSimulationComparisonReport()); } catch (Exception ex) { printError(ex.getMessage()); } }
                case "4" -> { try { String p = promptOptional("Path (Enter = reports/export.csv)"); reportController.exportCsv(p != null ? p : "reports/export.csv"); printSuccess("Exported."); } catch (Exception ex) { printError(ex.getMessage()); } }
                case "5" -> { try { reportController.importCsv(prompt("File path")); printSuccess("Imported."); } catch (Exception ex) { printError(ex.getMessage()); } }
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    // ═════════════════════════════════════════
    // 7. SYNC & SIMULATION
    // ═════════════════════════════════════════

    private void showSyncAndSimulation() {
        boolean back = false;
        while (!back) {
            printSubMenu("SYNCHRONIZATION & SIMULATION", new String[]{
                "Run Payroll Simulation", "Select Sync Mode",
                "Measure TPS / Elapsed Time", "Detect Double Payment",
                "Detect Wrong Leave Deduction"
            });
            switch (prompt("Choose").trim()) {
                case "1" -> handleRunSimulation();
                case "2" -> handleSelectSyncMode();
                case "3" -> handleMeasureTps();
                case "4" -> { try { List<String> s = simulationController.detectDoublePayment(); if (s.isEmpty()) printSuccess("No double payments."); else { printError("Double payments: " + s.size()); s.forEach(x -> System.out.println("  ⚠ " + x)); } } catch (Exception ex) { printError(ex.getMessage()); } }
                case "5" -> { try { List<String> s = simulationController.detectWrongLeaveDeduction(); if (s.isEmpty()) printSuccess("No wrong deductions."); else { printError("Wrong deductions: " + s.size()); s.forEach(x -> System.out.println("  ⚠ " + x)); } } catch (Exception ex) { printError(ex.getMessage()); } }
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    private void handleRunSimulation() {
        printSectionHeader("RUN PAYROLL SIMULATION");
        try {
            String yearMonth = prompt("Year-Month (YYYY-MM)");
            int threads = parseInt(promptOptional("Threads (Enter = 10)"), 10);
            printInfo("Running with " + threads + " threads, mode: " + simulationController.getCurrentSyncMode());
            PayrollRun run = simulationController.runSimulation(yearMonth, threads);
            printSuccess("Done!");
            printPayrollRunSummary(run);
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    private void handleSelectSyncMode() {
        printSectionHeader("SELECT SYNC MODE");
        System.out.println("  1. NO_LOCK      2. FILE_LOCK");
        System.out.println("  3. SYNCHRONIZED 4. OPTIMISTIC");
        String mode = switch (prompt("Choose")) {
            case "1" -> "NO_LOCK";
            case "2" -> "FILE_LOCK";
            case "3" -> "SYNCHRONIZED";
            case "4" -> "OPTIMISTIC";
            default  -> null;
        };
        if (mode == null) { printError("Invalid choice."); return; }
        simulationController.setSyncMode(mode);
        printSuccess("Mode set to: " + mode);
    }

    private void handleMeasureTps() {
        printSectionHeader("MEASURE TPS / ELAPSED TIME");
        PayrollRun r = simulationController.getLatestRun();
        if (r == null) { printInfo("No simulation run yet."); return; }
        printPayrollRunSummary(r);
    }

    // ═════════════════════════════════════════
    // DISPLAY HELPERS
    // ═════════════════════════════════════════

    private void printBanner() {
        System.out.println(BOLD + CYAN);
        System.out.println("=========================================");
        System.out.println("  EMPLOYEE PAYROLL MANAGEMENT SYSTEM");
        System.out.println("=========================================");
        System.out.println(RESET);
    }

    private void printMainMenu() {
        String user = currentSession != null
                ? DIM + " [" + currentSession.getUsername() + " | " + currentSession.getRole() + "]" + RESET : "";
        System.out.println(BOLD + "\n=========================================" + user);
        System.out.println(" EMPLOYEE PAYROLL MANAGEMENT SYSTEM");
        System.out.println("=========================================");
        System.out.println("  1. Login\n  2. Employee Management\n  3. Attendance Management");
        System.out.println("  4. Leave Management\n  5. Payroll Management\n  6. Reports");
        System.out.println("  7. Synchronization & Simulation\n  ─────────────────────────────");
        System.out.println("  0. Exit\n=========================================" + RESET);
    }

    private void printSubMenu(String title, String[] items) {
        System.out.println(BOLD + "\n========== " + title + " ==========" + RESET);
        int n = 1;
        for (String item : items) {
            if (item.equals("---")) System.out.println("  ----------------------------");
            else System.out.printf("  %2d. %s%n", n++, item);
        }
        System.out.println("   0. Back");
        System.out.println(BOLD + "═".repeat(title.length() + 22) + RESET);
    }

    private void printSectionHeader(String title) {
        System.out.println(BOLD + CYAN + "\n▶ " + title + RESET);
        System.out.println("─".repeat(45));
    }

    private void printEmployeeTable(List<Employee> list) {
        if (list.isEmpty()) { printInfo("No employees found."); return; }
        System.out.printf(BOLD + "%-12s %-20s %-25s %-10s %-10s%n" + RESET, "ID","Name","Email","Dept","Type");
        System.out.println("─".repeat(80));
        for (Employee e : list)
            System.out.printf("%-12s %-20s %-25s %-10s %-10s%n",
                    e.getId(), trunc(e.getName(),19), trunc(e.getEmail(),24), e.getDepartmentId(), e.getEmploymentType());
        System.out.println("─".repeat(80));
        printInfo("Found: " + list.size());
    }

    private void printEmployeeDetail(Employee e) {
        System.out.println(CYAN + "┌─── Employee Detail ──────────────────────────┐" + RESET);
        printRow("ID", e.getId()); printRow("Name", e.getName()); printRow("Email", e.getEmail());
        printRow("Department", e.getDepartmentId()); printRow("Type", String.valueOf(e.getEmploymentType()));
        printRow("Base Salary", String.format("%,.0f VND", e.getBaseSalary()));
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printDepartmentTable(List<Department> list) {
        System.out.printf(BOLD + "%-12s %-25s %-15s%n" + RESET, "ID","Name","Manager ID");
        System.out.println("─".repeat(55));
        for (Department d : list)
            System.out.printf("%-12s %-25s %-15s%n", d.getId(), trunc(d.getName(),24), d.getManagerId());
        System.out.println("─".repeat(55));
    }

    private void printDepartmentDetail(Department d) {
        System.out.println(CYAN + "┌─── Department ────────────────────────────────┐" + RESET);
        printRow("ID", d.getId()); printRow("Name", d.getName()); printRow("Manager", d.getManagerId());
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printAttendanceDetail(AttendanceRecord r) {
        System.out.println(CYAN + "┌─── Attendance Record ─────────────────────────┐" + RESET);
        printRow("ID", r.getId()); printRow("Employee", r.getEmployeeId());
        printRow("Month", r.getYearMonth()); printRow("Work Days", String.valueOf(r.getWorkDays()));
        printRow("Overtime", r.getOvertimeHours() + "h");
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printAttendanceTable(List<AttendanceRecord> list) {
        System.out.printf(BOLD + "%-15s %-12s %-10s %-10s %-12s%n" + RESET, "ID","Employee","Month","WorkDays","Overtime(h)");
        System.out.println("─".repeat(62));
        for (AttendanceRecord r : list)
            System.out.printf("%-15s %-12s %-10s %-10d %-12.1f%n",
                    trunc(r.getId(),14), r.getEmployeeId(), r.getYearMonth(), r.getWorkDays(), r.getOvertimeHours());
        System.out.println("─".repeat(62));
    }

    private void printLeaveTable(List<LeaveRequest> list) {
        System.out.printf(BOLD + "%-15s %-12s %-10s %-12s %-12s %-10s%n" + RESET, "Leave ID","Employee","Type","Start","End","Status");
        System.out.println("─".repeat(75));
        for (LeaveRequest r : list)
            System.out.printf("%-15s %-12s %-10s %-12s %-12s %-10s%n",
                    r.getLeaveId(), r.getEmployeeId(), r.getLeaveType(), r.getStartDate(), r.getEndDate(), r.getStatus());
        System.out.println("─".repeat(75));
    }

    private void printPayrollRunSummary(PayrollRun r) {
        System.out.println(CYAN + "┌─── Payroll Run Summary ───────────────────────┐" + RESET);
        printRow("Run ID", r.getId()); printRow("Month", r.getYearMonth());
        printRow("Mechanism", r.getMechanism()); printRow("Elapsed", r.getElapsedMs() + " ms");
        printRow("TPS", String.format("%.2f", r.getTps()));
        printRow("Success", String.valueOf(r.getSuccessCount()));
        printRow("Double Pay", String.valueOf(r.getDoublePaymentCount()));
        printRow("Wrong Leave", String.valueOf(r.getWrongLeaveCount()));
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printPayrollRule(PayrollRule r) {
        System.out.println(CYAN + "┌─── Payroll Rule ──────────────────────────────┐" + RESET);
        printRow("Work Days", String.valueOf(r.getStandardWorkingDays()));
        printRow("Hours/Day", String.valueOf(r.getWorkingHoursPerDay()));
        printRow("OT Multiplier", r.getOvertimeMultiplier() + "x");
        printRow("Bonus", String.format("%,.0f VND", r.getAttendanceBonus()));
        printRow("Tax Rate", (r.getTaxRate() * 100) + "%");
        printRow("Tax Threshold", String.format("%,.0f VND", r.getTaxThreshold()));
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printRow(String label, String value) {
        System.out.printf("│ %-20s: %-24s│%n", label, trunc(value, 24));
    }

    private void printSuccess(String msg) { System.out.println(GREEN  + "✔ " + msg + RESET); }
    private void printError(String msg)   { System.out.println(RED    + "✘ " + msg + RESET); }
    private void printInfo(String msg)    { System.out.println(YELLOW + "ℹ " + msg + RESET); }

    // ═════════════════════════════════════════
    // INPUT HELPERS
    // ═════════════════════════════════════════

    private String prompt(String label) {
        while (true) {
            System.out.print(BOLD + label + ": " + RESET);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            printError("Required field.");
        }
    }

    private String promptOptional(String label) {
        System.out.print(BOLD + label + ": " + RESET);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    private double promptDouble(String label) {
        while (true) {
            try { return Double.parseDouble(prompt(label).replace(",", "")); }
            catch (NumberFormatException e) { printError("Enter a valid number."); }
        }
    }

    private boolean confirm(String q) {
        System.out.print(YELLOW + q + " (yes/no): " + RESET);
        String ans = scanner.nextLine().trim().toLowerCase();
        return ans.equals("yes") || ans.equals("y");
    }

    private String trunc(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }

    private int parseInt(String s, int fallback) {
        if (s == null) return fallback;
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return fallback; }
    }

    private double parseDoubleVal(String s, double fallback) {
        if (s == null) return fallback;
        try { return Double.parseDouble(s.replace(",", "")); }
        catch (Exception e) { return fallback; }
    }
}