import java.util.List;
import java.util.Scanner;

/**
 * MainView — giao diện CLI chính.
 * Cấu trúc 7 khu vực theo Use Case Diagram.
 * View chỉ hiển thị / nhận input — toàn bộ logic ủy thác cho Controller.
 */
public class MainView {

    // ── ANSI ─────────────────────────────────────────────────────────────
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED    = "\u001B[31m";
    private static final String DIM    = "\u001B[2m";

    // ── Controllers ───────────────────────────────────────────────────────
    private final AuthController         authController;
    private final EmployeeController     employeeController;
    private final DepartmentController   departmentController;
    private final AttendanceController   attendanceController;
    private final LeaveController        leaveController;
    private final PayrollController      payrollController;
    private final ReportController       reportController;
    private final SimulationController   simulationController;

    private final Scanner scanner;

    /** Tài khoản đang đăng nhập (null = chưa login). */
    private UserAccount currentUser = null;

    // ─────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────

    public MainView(AuthController authController,
                    EmployeeController employeeController,
                    DepartmentController departmentController,
                    AttendanceController attendanceController,
                    LeaveController leaveController,
                    PayrollController payrollController,
                    ReportController reportController,
                    SimulationController simulationController) {
        this.authController       = authController;
        this.employeeController   = employeeController;
        this.departmentController = departmentController;
        this.attendanceController = attendanceController;
        this.leaveController      = leaveController;
        this.payrollController    = payrollController;
        this.reportController     = reportController;
        this.simulationController = simulationController;
        this.scanner = new Scanner(System.in);
    }

    // ─────────────────────────────────────────
    // ENTRY POINT
    // ─────────────────────────────────────────

    public void run() {
        printBanner();

        // Bắt buộc login trước khi vào Main Menu
        if (!handleLogin()) {
            printError("Đăng nhập thất bại. Thoát chương trình.");
            scanner.close();
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Choose").trim();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> showEmployeeManagement();
                case "3" -> showAttendanceManagement();
                case "4" -> showLeaveManagement();
                case "5" -> showPayrollManagement();
                case "6" -> showReports();
                case "7" -> showSyncAndSimulation();
                case "0" -> {
                    printSuccess("Goodbye! / Tạm biệt!");
                    running = false;
                }
                default  -> printError("Invalid choice. Please try again.");
            }
        }

        scanner.close();
    }

    // ═════════════════════════════════════════
    // 1. LOGIN
    // ═════════════════════════════════════════

    private boolean handleLogin() {
        printSectionHeader("LOGIN");

        int attempts = 0;
        while (attempts < 3) {
            String username = prompt("Username");
            String password = promptPassword("Password");

            try {
                UserAccount user = authController.login(username, password);
                this.currentUser = user;
                printSuccess("Login successful! Welcome, " + user.getUsername()
                        + " [" + user.getRole() + "]");
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
                "Add Employee",
                "Update Employee",
                "Delete Employee",
                "Search Employee",
                "View Employee Detail",
                "---",
                "Add Department",
                "Update Department",
                "Delete Department",
                "Search Department",
                "View Departments",
                "---",
                "Save CSV Data",
                "Load CSV Data",
                "Generate Test Dataset"
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
                case "11" -> handleSaveCsv();
                case "12" -> handleLoadCsv();
                case "13" -> handleGenerateTestDataset();
                case "0"  -> back = true;
                default   -> printError("Invalid choice.");
            }
        }
    }

    // ── Employee CRUD ─────────────────────────

    private void handleAddEmployee() {
        printSectionHeader("ADD EMPLOYEE");
        try {
            String name         = prompt("Full name");
            String email        = prompt("Email");
            String departmentId = prompt("Department ID");
            System.out.println("  Employment type:  1. FULLTIME   2. PARTTIME");
            EmployeeType type   = prompt("Choose").equals("2")
                                  ? EmployeeType.PARTTIME : EmployeeType.FULLTIME;
            double baseSalary   = promptDouble("Base salary (VND, 0 = default)");

            Employee created = employeeController.addEmployee(
                    name, email, departmentId, type, baseSalary);
            printSuccess("Employee added successfully!");
            printEmployeeDetail(created);
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
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
            double salary       = (salaryStr != null) ? parseDouble(salaryStr) : -1;

            Employee updated = employeeController.updateEmployee(
                    id, name, email, departmentId, salary);
            printSuccess("Updated successfully!");
            printEmployeeDetail(updated);
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleDeleteEmployee() {
        printSectionHeader("DELETE EMPLOYEE");
        String id = prompt("Employee ID");
        try {
            printEmployeeDetail(employeeController.getEmployeeById(id));
            if (confirm("Confirm delete?")) {
                employeeController.deleteEmployee(id);
                printSuccess("Employee " + id + " deleted.");
            } else {
                printInfo("Cancelled.");
            }
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleSearchEmployee() {
        printSectionHeader("SEARCH EMPLOYEE");
        System.out.println("  1. Search by name");
        System.out.println("  2. Search by department");
        System.out.println("  3. Filter by type (FULLTIME/PARTTIME)");

        switch (prompt("Choose").trim()) {
            case "1" -> {
                String kw = prompt("Keyword");
                List<Employee> res = employeeController.searchByName(kw);
                printEmployeeTable(res);
            }
            case "2" -> {
                String deptId = prompt("Department ID");
                try {
                    List<Employee> res = employeeController.getEmployeesByDepartment(deptId);
                    printEmployeeTable(res);
                } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
            }
            case "3" -> {
                System.out.println("  1. FULLTIME   2. PARTTIME");
                EmployeeType type = prompt("Choose").equals("2")
                                    ? EmployeeType.PARTTIME : EmployeeType.FULLTIME;
                printEmployeeTable(employeeController.getEmployeesByType(type));
            }
            default -> printError("Invalid choice.");
        }
    }

    private void handleViewEmployeeDetail() {
        printSectionHeader("VIEW EMPLOYEE DETAIL");
        String id = prompt("Employee ID");
        try {
            printEmployeeDetail(employeeController.getEmployeeById(id));
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    // ── Department CRUD ───────────────────────

    private void handleAddDepartment() {
        printSectionHeader("ADD DEPARTMENT");
        try {
            String name      = prompt("Department name");
            String managerId = promptOptional("Manager employee ID (Enter to skip)");
            Department dept  = departmentController.addDepartment(name, managerId);
            printSuccess("Department added: " + dept.getId() + " — " + dept.getName());
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleUpdateDepartment() {
        printSectionHeader("UPDATE DEPARTMENT");
        String id = prompt("Department ID");
        try {
            Department d = departmentController.getDepartmentById(id);
            printDepartmentDetail(d);
            printInfo("Press Enter to keep current value.");
            String name      = promptOptional("New name");
            String managerId = promptOptional("New manager ID");
            Department updated = departmentController.updateDepartment(id, name, managerId);
            printSuccess("Updated: " + updated.getId() + " — " + updated.getName());
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleDeleteDepartment() {
        printSectionHeader("DELETE DEPARTMENT");
        String id = prompt("Department ID");
        try {
            Department d = departmentController.getDepartmentById(id);
            printDepartmentDetail(d);
            if (confirm("Confirm delete?")) {
                departmentController.deleteDepartment(id);
                printSuccess("Department " + id + " deleted.");
            } else {
                printInfo("Cancelled.");
            }
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleSearchDepartment() {
        printSectionHeader("SEARCH DEPARTMENT");
        String keyword = prompt("Department name keyword");
        try {
            List<Department> list = departmentController.searchByName(keyword);
            if (list.isEmpty()) {
                printInfo("No departments found.");
                return;
            }
            printDepartmentTable(list);
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleViewDepartments() {
        printSectionHeader("ALL DEPARTMENTS");
        List<Department> list = departmentController.getAllDepartments();
        if (list.isEmpty()) {
            printInfo("No departments yet.");
            return;
        }
        printDepartmentTable(list);
        printInfo("Total: " + list.size() + " departments.");
    }

    // ── CSV Data ──────────────────────────────

    private void handleSaveCsv() {
        printSectionHeader("SAVE CSV DATA");
        try {
            employeeController.saveCsv();
            departmentController.saveCsv();
            printSuccess("All data saved to CSV files.");
        } catch (Exception ex) {
            printError("Save failed: " + ex.getMessage());
        }
    }

    private void handleLoadCsv() {
        printSectionHeader("LOAD CSV DATA");
        try {
            employeeController.loadCsv();
            departmentController.loadCsv();
            printSuccess("Data loaded from CSV files.");
        } catch (Exception ex) {
            printError("Load failed: " + ex.getMessage());
        }
    }

    private void handleGenerateTestDataset() {
        printSectionHeader("GENERATE TEST DATASET");
        String countStr = promptOptional("Number of employees to generate (Enter = 50)");
        int count = (countStr != null) ? parseInt(countStr, 50) : 50;
        if (confirm("Generate " + count + " test employees?")) {
            try {
                employeeController.generateTestDataset(count);
                printSuccess("Generated " + count + " test employees.");
            } catch (Exception ex) {
                printError("Generation failed: " + ex.getMessage());
            }
        }
    }

    // ═════════════════════════════════════════
    // 3. ATTENDANCE MANAGEMENT
    // ═════════════════════════════════════════

    private void showAttendanceManagement() {
        boolean back = false;
        while (!back) {
            printSubMenu("ATTENDANCE MANAGEMENT", new String[]{
                "Check In",
                "Check Out",
                "View Attendance Record",
                "View Attendance Summary",
                "Submit Attendance Adjustment Request",
                "Review Attendance Adjustment Request",
                "Approve Attendance Adjustment",
                "Reject Attendance Adjustment"
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
            String employeeId = prompt("Employee ID");
            AttendanceRecord record = attendanceController.checkIn(employeeId);
            printSuccess("Check-in recorded for " + employeeId
                    + " [" + record.getYearMonth() + "]");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleCheckOut() {
        printSectionHeader("CHECK OUT");
        try {
            String employeeId = prompt("Employee ID");
            double overtimeHours = promptDouble("Overtime hours (0 if none)");
            AttendanceRecord record = attendanceController.checkOut(employeeId, overtimeHours);
            printSuccess("Check-out recorded. Work days: " + record.getWorkDays()
                    + ", Overtime: " + record.getOvertimeHours() + "h");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleViewAttendanceRecord() {
        printSectionHeader("VIEW ATTENDANCE RECORD");
        try {
            String employeeId = prompt("Employee ID");
            String yearMonth  = prompt("Year-Month (YYYY-MM)");
            AttendanceRecord record = attendanceController.getRecord(employeeId, yearMonth);
            if (record == null) {
                printInfo("No attendance record found for " + employeeId + " / " + yearMonth);
            } else {
                printAttendanceDetail(record);
            }
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleViewAttendanceSummary() {
        printSectionHeader("VIEW ATTENDANCE SUMMARY");
        try {
            String yearMonth = prompt("Year-Month (YYYY-MM)");
            List<AttendanceRecord> list = attendanceController.getSummaryByMonth(yearMonth);
            if (list.isEmpty()) {
                printInfo("No records for " + yearMonth);
                return;
            }
            printAttendanceTable(list);
            printInfo("Total records: " + list.size());
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleSubmitAttendanceAdjustment() {
        printSectionHeader("SUBMIT ATTENDANCE ADJUSTMENT REQUEST");
        try {
            String employeeId = prompt("Employee ID");
            String yearMonth  = prompt("Year-Month (YYYY-MM)");
            String reason     = prompt("Reason for adjustment");
            int    newWorkDays    = parseInt(prompt("Adjusted work days"), -1);
            double newOvertime    = promptDouble("Adjusted overtime hours");
            attendanceController.submitAdjustmentRequest(
                    employeeId, yearMonth, reason, newWorkDays, newOvertime);
            printSuccess("Adjustment request submitted.");
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleReviewAttendanceAdjustment() {
        printSectionHeader("REVIEW ATTENDANCE ADJUSTMENT REQUEST");
        try {
            List<?> pending = attendanceController.getPendingAdjustments();
            if (pending.isEmpty()) {
                printInfo("No pending adjustment requests.");
                return;
            }
            pending.forEach(r -> System.out.println("  " + r));
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleApproveAttendanceAdjustment() {
        printSectionHeader("APPROVE ATTENDANCE ADJUSTMENT");
        try {
            String requestId = prompt("Request ID");
            attendanceController.approveAdjustment(
                    requestId, currentUser.getId());
            printSuccess("Adjustment approved.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleRejectAttendanceAdjustment() {
        printSectionHeader("REJECT ATTENDANCE ADJUSTMENT");
        try {
            String requestId = prompt("Request ID");
            String reason    = prompt("Rejection reason");
            attendanceController.rejectAdjustment(
                    requestId, currentUser.getId(), reason);
            printSuccess("Adjustment rejected.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            printError(ex.getMessage());
        }
    }

    // ═════════════════════════════════════════
    // 4. LEAVE MANAGEMENT
    // ═════════════════════════════════════════

    private void showLeaveManagement() {
        boolean back = false;
        while (!back) {
            printSubMenu("LEAVE MANAGEMENT", new String[]{
                "Submit Leave Request",
                "Approve Leave Request",
                "Reject Leave Request",
                "View Leave Balance"
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
            String empId     = prompt("Employee ID");
            System.out.println("  Leave type:  1. ANNUAL   2. SICK   3. UNPAID");
            LeaveType type   = switch (prompt("Choose")) {
                case "2" -> LeaveType.SICK;
                case "3" -> LeaveType.UNPAID;
                default  -> LeaveType.ANNUAL;
            };
            String startDate = prompt("Start date (YYYY-MM-DD)");
            String endDate   = prompt("End date   (YYYY-MM-DD)");
            String reason    = prompt("Reason");

            LeaveRequest req = leaveController.submitLeaveRequest(
                    empId, type, startDate, endDate, reason);
            printSuccess("Leave request submitted: " + req.getLeaveId()
                    + " (" + req.getDays() + " days)");
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleApproveLeaveRequest() {
        printSectionHeader("APPROVE LEAVE REQUEST");

        // Show pending list first
        List<LeaveRequest> pending = leaveController.getPendingRequests();
        if (pending.isEmpty()) {
            printInfo("No pending leave requests.");
            return;
        }
        printLeaveTable(pending);

        try {
            String leaveId = prompt("Leave Request ID to approve");
            leaveController.approveLeaveRequest(leaveId, currentUser.getId());
            printSuccess("Leave request " + leaveId + " approved.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleRejectLeaveRequest() {
        printSectionHeader("REJECT LEAVE REQUEST");

        List<LeaveRequest> pending = leaveController.getPendingRequests();
        if (pending.isEmpty()) {
            printInfo("No pending leave requests.");
            return;
        }
        printLeaveTable(pending);

        try {
            String leaveId = prompt("Leave Request ID to reject");
            leaveController.rejectLeaveRequest(leaveId, currentUser.getId());
            printSuccess("Leave request " + leaveId + " rejected.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleViewLeaveBalance() {
        printSectionHeader("VIEW LEAVE BALANCE");
        try {
            String empId = prompt("Employee ID");
            List<LeaveBalance> balances = leaveController.getLeaveBalance(empId);
            if (balances.isEmpty()) {
                printInfo("No leave balance records for " + empId);
                return;
            }
            System.out.printf(BOLD + "%-15s %-10s %-8s %-8s %-10s%n" + RESET,
                    "Employee", "Type", "Total", "Used", "Remaining");
            System.out.println("─".repeat(55));
            for (LeaveBalance b : balances) {
                System.out.printf("%-15s %-10s %-8d %-8d %-10d%n",
                        b.getEmployeeId(),
                        b.getLeaveType(),
                        b.getTotalLeaveDays(),
                        b.getUsedLeaveDays(),
                        b.getRemainingLeaveDays());
            }
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    // ═════════════════════════════════════════
    // 5. PAYROLL MANAGEMENT
    // ═════════════════════════════════════════

    private void showPayrollManagement() {
        boolean back = false;
        while (!back) {
            printSubMenu("PAYROLL MANAGEMENT", new String[]{
                "Process Monthly Payroll",
                "View Payroll History",
                "Configure Payroll Rules"
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
            printInfo("Processing payroll for " + yearMonth + "...");

            PayrollRun run = payrollController.processMonthlyPayroll(
                    yearMonth, currentUser.getId());

            printSuccess("Payroll processed successfully!");
            printPayrollRunSummary(run);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleViewPayrollHistory() {
        printSectionHeader("VIEW PAYROLL HISTORY");
        try {
            String empId = promptOptional("Employee ID (Enter = all employees)");
            List<PayrollEntry> entries = (empId != null)
                    ? payrollController.getPayrollByEmployee(empId)
                    : payrollController.getAllPayrollEntries();

            if (entries.isEmpty()) {
                printInfo("No payroll records found.");
                return;
            }

            System.out.printf(BOLD + "%-20s %-12s %-18s %-12s%n" + RESET,
                    "Entry ID", "Employee", "Net Salary (VND)", "Status");
            System.out.println("─".repeat(65));
            for (PayrollEntry e : entries) {
                System.out.printf("%-20s %-12s %-18s %-12s%n",
                        e.getId(),
                        e.getEmployeeId(),
                        String.format("%,.0f", e.getNetSalary()),
                        e.getStatus());
            }
            System.out.println("─".repeat(65));
            printInfo("Total: " + entries.size() + " entries.");
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleConfigurePayrollRules() {
        printSectionHeader("CONFIGURE PAYROLL RULES");
        try {
            PayrollRule current = payrollController.getPayrollRule();
            System.out.println("  Current configuration:");
            printPayrollRule(current);
            printInfo("Press Enter to keep current value.");

            String wdStr  = promptOptional("Standard working days [" + current.getStandardWorkingDays() + "]");
            String hpStr  = promptOptional("Working hours per day  [" + current.getWorkingHoursPerDay() + "]");
            String otStr  = promptOptional("Overtime multiplier    [" + current.getOvertimeMultiplier() + "]");
            String bonStr = promptOptional("Attendance bonus (VND) [" + current.getAttendanceBonus() + "]");
            String taxRStr= promptOptional("Tax rate (0.0-1.0)     [" + current.getTaxRate() + "]");
            String taxTStr= promptOptional("Tax threshold (VND)    [" + current.getTaxThreshold() + "]");

            PayrollRule updated = new PayrollRule(
                    wdStr  != null ? parseInt(wdStr,  current.getStandardWorkingDays()) : current.getStandardWorkingDays(),
                    hpStr  != null ? parseInt(hpStr,  current.getWorkingHoursPerDay())  : current.getWorkingHoursPerDay(),
                    otStr  != null ? parseDoubleVal(otStr,  current.getOvertimeMultiplier()) : current.getOvertimeMultiplier(),
                    bonStr != null ? parseDoubleVal(bonStr, current.getAttendanceBonus())    : current.getAttendanceBonus(),
                    taxRStr!= null ? parseDoubleVal(taxRStr,current.getTaxRate())            : current.getTaxRate(),
                    taxTStr!= null ? parseDoubleVal(taxTStr,current.getTaxThreshold())       : current.getTaxThreshold()
            );

            payrollController.updatePayrollRule(updated);
            printSuccess("Payroll rules updated!");
            printPayrollRule(updated);
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    // ═════════════════════════════════════════
    // 6. REPORTS
    // ═════════════════════════════════════════

    private void showReports() {
        boolean back = false;
        while (!back) {
            printSubMenu("REPORTS", new String[]{
                "Generate Payroll Report",
                "Generate Attendance Report",
                "Generate Simulation Comparison Report",
                "Export CSV Result",
                "Import CSV Result"
            });

            switch (prompt("Choose").trim()) {
                case "1" -> handleGeneratePayrollReport();
                case "2" -> handleGenerateAttendanceReport();
                case "3" -> handleGenerateSimulationReport();
                case "4" -> handleExportCsvResult();
                case "5" -> handleImportCsvResult();
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    private void handleGeneratePayrollReport() {
        printSectionHeader("GENERATE PAYROLL REPORT");
        try {
            String yearMonth = prompt("Year-Month (YYYY-MM)");
            String report    = reportController.generatePayrollReport(yearMonth);
            System.out.println(report);
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleGenerateAttendanceReport() {
        printSectionHeader("GENERATE ATTENDANCE REPORT");
        try {
            String yearMonth = prompt("Year-Month (YYYY-MM)");
            String report    = reportController.generateAttendanceReport(yearMonth);
            System.out.println(report);
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleGenerateSimulationReport() {
        printSectionHeader("GENERATE SIMULATION COMPARISON REPORT");
        try {
            String report = reportController.generateSimulationComparisonReport();
            System.out.println(report);
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleExportCsvResult() {
        printSectionHeader("EXPORT CSV RESULT");
        try {
            String path = promptOptional("Output file path (Enter = reports/export.csv)");
            if (path == null) path = "reports/export.csv";
            reportController.exportCsv(path);
            printSuccess("Exported to: " + path);
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleImportCsvResult() {
        printSectionHeader("IMPORT CSV RESULT");
        try {
            String path = prompt("Input file path");
            reportController.importCsv(path);
            printSuccess("Imported from: " + path);
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    // ═════════════════════════════════════════
    // 7. SYNCHRONIZATION & SIMULATION
    // ═════════════════════════════════════════

    private void showSyncAndSimulation() {
        boolean back = false;
        while (!back) {
            printSubMenu("SYNCHRONIZATION & SIMULATION", new String[]{
                "Run Payroll Simulation",
                "Select Sync Mode",
                "Measure TPS / Elapsed Time",
                "Detect Double Payment",
                "Detect Wrong Leave Deduction"
            });

            switch (prompt("Choose").trim()) {
                case "1" -> handleRunPayrollSimulation();
                case "2" -> handleSelectSyncMode();
                case "3" -> handleMeasureTps();
                case "4" -> handleDetectDoublePayment();
                case "5" -> handleDetectWrongLeaveDeduction();
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    private void handleRunPayrollSimulation() {
        printSectionHeader("RUN PAYROLL SIMULATION");
        try {
            String yearMonth = prompt("Year-Month (YYYY-MM)");
            int threads      = parseInt(promptOptional("Number of threads (Enter = 10)"), 10);
            printInfo("Running simulation with " + threads + " threads...");

            PayrollRun run = simulationController.runSimulation(yearMonth, threads);
            printSuccess("Simulation complete!");
            printPayrollRunSummary(run);
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleSelectSyncMode() {
        printSectionHeader("SELECT SYNC MODE");
        System.out.println("  1. NO_LOCK      — No synchronization (demo race condition)");
        System.out.println("  2. FILE_LOCK    — Lock entire CSV file");
        System.out.println("  3. SYNCHRONIZED — Per-employee Java synchronized");
        System.out.println("  4. OPTIMISTIC   — Version-based optimistic locking");

        String choice = prompt("Choose sync mode");
        String mode   = switch (choice) {
            case "1" -> "NO_LOCK";
            case "2" -> "FILE_LOCK";
            case "3" -> "SYNCHRONIZED";
            case "4" -> "OPTIMISTIC";
            default  -> null;
        };

        if (mode == null) {
            printError("Invalid choice.");
            return;
        }

        try {
            simulationController.setSyncMode(mode);
            printSuccess("Sync mode set to: " + mode);
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleMeasureTps() {
        printSectionHeader("MEASURE TPS / ELAPSED TIME");
        try {
            PayrollRun latest = simulationController.getLatestRun();
            if (latest == null) {
                printInfo("No simulation run yet. Run a simulation first.");
                return;
            }
            System.out.printf("  %-20s: %s%n",    "Year-Month",      latest.getYearMonth());
            System.out.printf("  %-20s: %s%n",    "Mechanism",       latest.getMechanism());
            System.out.printf("  %-20s: %d ms%n", "Elapsed Time",    latest.getElapsedMs());
            System.out.printf("  %-20s: %.2f%n",  "TPS",             latest.getTps());
            System.out.printf("  %-20s: %d%n",    "Success Count",   latest.getSuccessCount());
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleDetectDoublePayment() {
        printSectionHeader("DETECT DOUBLE PAYMENT");
        try {
            List<String> suspects = simulationController.detectDoublePayment();
            if (suspects.isEmpty()) {
                printSuccess("No double payments detected.");
            } else {
                printError("Double payment detected for " + suspects.size() + " employee(s):");
                suspects.forEach(s -> System.out.println("  ⚠ " + s));
            }
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
    }

    private void handleDetectWrongLeaveDeduction() {
        printSectionHeader("DETECT WRONG LEAVE DEDUCTION");
        try {
            List<String> suspects = simulationController.detectWrongLeaveDeduction();
            if (suspects.isEmpty()) {
                printSuccess("No wrong leave deductions detected.");
            } else {
                printError("Wrong leave deduction detected for " + suspects.size() + " employee(s):");
                suspects.forEach(s -> System.out.println("  ⚠ " + s));
            }
        } catch (Exception ex) {
            printError(ex.getMessage());
        }
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
        String user = (currentUser != null)
                ? DIM + " [" + currentUser.getUsername() + " | " + currentUser.getRole() + "]" + RESET
                : "";
        System.out.println(BOLD + "\n=========================================" + user);
        System.out.println(" EMPLOYEE PAYROLL MANAGEMENT SYSTEM");
        System.out.println("=========================================");
        System.out.println("  1. Login");
        System.out.println("  2. Employee Management");
        System.out.println("  3. Attendance Management");
        System.out.println("  4. Leave Management");
        System.out.println("  5. Payroll Management");
        System.out.println("  6. Reports");
        System.out.println("  7. Synchronization & Simulation");
        System.out.println("  ─────────────────────────────");
        System.out.println("  0. Exit");
        System.out.println("=========================================" + RESET);
    }

    /**
     * Sub-menu động — tự đánh số, tự xử lý dấu "---" thành separator.
     */
    private void printSubMenu(String title, String[] items) {
        System.out.println(BOLD + "\n========== " + title + " ==========" + RESET);
        int menuNum = 1;
        for (String item : items) {
            if (item.equals("---")) {
                System.out.println("  ----------------------------");
            } else {
                System.out.printf("  %2d. %s%n", menuNum++, item);
            }
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
        System.out.printf(BOLD + "%-12s %-20s %-25s %-10s %-10s%n" + RESET,
                "ID", "Name", "Email", "Dept", "Type");
        System.out.println("─".repeat(80));
        for (Employee e : list) {
            System.out.printf("%-12s %-20s %-25s %-10s %-10s%n",
                    e.getId(), trunc(e.getName(), 19), trunc(e.getEmail(), 24),
                    e.getDepartmentId(), e.getEmploymentType());
        }
        System.out.println("─".repeat(80));
        printInfo("Found: " + list.size() + " employee(s).");
    }

    private void printEmployeeDetail(Employee e) {
        System.out.println(CYAN + "┌─── Employee Detail ──────────────────────────┐" + RESET);
        printRow("ID",           e.getId());
        printRow("Version",      "v" + e.getVersion());
        printRow("Name",         e.getName());
        printRow("Email",        e.getEmail());
        printRow("Department",   e.getDepartmentId());
        printRow("Type",         String.valueOf(e.getEmploymentType()));
        printRow("Base Salary",  String.format("%,.0f VND", e.getBaseSalary()));
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printDepartmentTable(List<Department> list) {
        System.out.printf(BOLD + "%-12s %-25s %-15s%n" + RESET, "ID", "Name", "Manager ID");
        System.out.println("─".repeat(55));
        for (Department d : list) {
            System.out.printf("%-12s %-25s %-15s%n",
                    d.getId(), trunc(d.getName(), 24), d.getManagerId());
        }
        System.out.println("─".repeat(55));
    }

    private void printDepartmentDetail(Department d) {
        System.out.println(CYAN + "┌─── Department Detail ─────────────────────────┐" + RESET);
        printRow("ID",         d.getId());
        printRow("Name",       d.getName());
        printRow("Manager ID", d.getManagerId());
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printAttendanceDetail(AttendanceRecord r) {
        System.out.println(CYAN + "┌─── Attendance Record ─────────────────────────┐" + RESET);
        printRow("ID",             r.getId());
        printRow("Employee ID",    r.getEmployeeId());
        printRow("Year-Month",     r.getYearMonth());
        printRow("Work Days",      String.valueOf(r.getWorkDays()));
        printRow("Overtime Hours", r.getOvertimeHours() + "h");
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printAttendanceTable(List<AttendanceRecord> list) {
        System.out.printf(BOLD + "%-15s %-12s %-10s %-10s %-12s%n" + RESET,
                "ID", "Employee", "Month", "WorkDays", "Overtime(h)");
        System.out.println("─".repeat(62));
        for (AttendanceRecord r : list) {
            System.out.printf("%-15s %-12s %-10s %-10d %-12.1f%n",
                    trunc(r.getId(), 14), r.getEmployeeId(),
                    r.getYearMonth(), r.getWorkDays(), r.getOvertimeHours());
        }
        System.out.println("─".repeat(62));
    }

    private void printLeaveTable(List<LeaveRequest> list) {
        System.out.printf(BOLD + "%-15s %-12s %-10s %-12s %-12s %-10s%n" + RESET,
                "Leave ID", "Employee", "Type", "Start", "End", "Status");
        System.out.println("─".repeat(75));
        for (LeaveRequest r : list) {
            System.out.printf("%-15s %-12s %-10s %-12s %-12s %-10s%n",
                    r.getLeaveId(), r.getEmployeeId(), r.getLeaveType(),
                    r.getStartDate(), r.getEndDate(), r.getStatus());
        }
        System.out.println("─".repeat(75));
    }

    private void printPayrollRunSummary(PayrollRun run) {
        System.out.println(CYAN + "┌─── Payroll Run Summary ───────────────────────┐" + RESET);
        printRow("Run ID",          run.getId());
        printRow("Year-Month",      run.getYearMonth());
        printRow("Mechanism",       run.getMechanism());
        printRow("Elapsed Time",    run.getElapsedMs() + " ms");
        printRow("TPS",             String.format("%.2f", run.getTps()));
        printRow("Success Count",   String.valueOf(run.getSuccessCount()));
        printRow("Double Payments", String.valueOf(run.getDoublePaymentCount()));
        printRow("Wrong Leave",     String.valueOf(run.getWrongLeaveCount()));
        System.out.println(CYAN + "└───────────────────────────────────────────────┘" + RESET);
    }

    private void printPayrollRule(PayrollRule r) {
        System.out.println(CYAN + "┌─── Payroll Rule ──────────────────────────────┐" + RESET);
        printRow("Standard Work Days",  String.valueOf(r.getStandardWorkingDays()));
        printRow("Hours/Day",           String.valueOf(r.getWorkingHoursPerDay()));
        printRow("Overtime Multiplier", String.valueOf(r.getOvertimeMultiplier()) + "x");
        printRow("Attendance Bonus",    String.format("%,.0f VND", r.getAttendanceBonus()));
        printRow("Tax Rate",            (r.getTaxRate() * 100) + "%");
        printRow("Tax Threshold",       String.format("%,.0f VND", r.getTaxThreshold()));
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
            printError("This field is required.");
        }
    }

    /** Ẩn password (fallback nếu Console null trong IDE). */
    private String promptPassword(String label) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] pw = console.readPassword(BOLD + label + ": " + RESET);
            return new String(pw).trim();
        }
        // IDE fallback
        return prompt(label);
    }

    /** Trả về null nếu nhấn Enter (field tùy chọn). */
    private String promptOptional(String label) {
        System.out.print(BOLD + label + ": " + RESET);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    private double promptDouble(String label) {
        while (true) {
            String raw = prompt(label);
            try { return Double.parseDouble(raw.replace(",", "")); }
            catch (NumberFormatException e) { printError("Please enter a valid number."); }
        }
    }

    private boolean confirm(String question) {
        System.out.print(YELLOW + question + " (yes/no): " + RESET);
        String ans = scanner.nextLine().trim().toLowerCase();
        return ans.equals("yes") || ans.equals("y");
    }

    private String trunc(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 2) + "..";
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return fallback; }
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.replace(",", "")); }
        catch (Exception e) { return -1; }
    }

    private double parseDoubleVal(String s, double fallback) {
        try { return Double.parseDouble(s.replace(",", "")); }
        catch (Exception e) { return fallback; }
    }
}