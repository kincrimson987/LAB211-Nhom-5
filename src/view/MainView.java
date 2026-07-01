
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
            String choice = prompt("Choose").trim();
            if (isAdmin()) {
                running = handleAdminMainChoice(choice);
            } else if (isEmployee()) {
                running = handleEmployeeMainChoice(choice);
            } else {
                running = handleHrMainChoice(choice);
            }
        }
        scanner.close();
    }

    /**
     * KiÃƒÂ¡Ã‚Â»Ã†â€™m tra quyÃƒÂ¡Ã‚Â»Ã‚Ân ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â in lÃƒÂ¡Ã‚Â»Ã¢â‚¬â€i vÃƒÆ’Ã‚Â  trÃƒÂ¡Ã‚ÂºÃ‚Â£ vÃƒÂ¡Ã‚Â»Ã‚Â false nÃƒÂ¡Ã‚ÂºÃ‚Â¿u role khÃƒÆ’Ã‚Â´ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã‚Â§ quyÃƒÂ¡Ã‚Â»Ã‚Ân.
     * DÃƒÆ’Ã‚Â¹ng Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ chÃƒÂ¡Ã‚ÂºÃ‚Â·n cÃƒÂ¡Ã‚ÂºÃ‚Â£ viÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡c vÃƒÆ’Ã‚Â o menu lÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºn lÃƒÂ¡Ã‚ÂºÃ‚Â«n tÃƒÂ¡Ã‚Â»Ã‚Â«ng action con bÃƒÆ’Ã‚Âªn trong.
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

    private boolean isAdmin() {
        return currentSession != null && "ADMIN".equalsIgnoreCase(currentSession.getRole());
    }

    private boolean isEmployee() {
        return currentSession != null && "EMPLOYEE".equalsIgnoreCase(currentSession.getRole());
    }

    private boolean isHr() {
        return currentSession != null && "HR".equalsIgnoreCase(currentSession.getRole());
    }

    private boolean handleAdminMainChoice(String choice) {
        switch (choice) {
            case "1" -> { if (checkAccess("ADD_EMPLOYEE")) showEmployeeManagement(); }
            case "2" -> { if (checkAccess("CONFIGURE_PAYROLL_RULES")) handleConfigurePayrollRules(); }
            case "3" -> { if (checkAccess("RUN_PAYROLL_SIMULATION")) showSyncAndSimulation(); }
            case "4" -> handleLogout();
            case "0" -> { printSuccess("Goodbye!"); return false; }
            default -> printError("Invalid choice.");
        }
        return true;
    }

    private boolean handleHrMainChoice(String choice) {
        switch (choice) {
            case "1" -> { if (checkAccess("ADD_EMPLOYEE")) showEmployeeManagement(); }
            case "2" -> { if (checkAccess("CHECK_IN")) showAttendanceManagement(); }
            case "3" -> { if (checkAccess("SUBMIT_LEAVE_REQUEST")) showLeaveManagement(); }
            case "4" -> { if (checkAccess("PROCESS_MONTHLY_PAYROLL")) showPayrollManagement(); }
            case "5" -> { if (checkAccess("GENERATE_PAYROLL_REPORT")) showReports(); }
            case "6" -> { if (checkAccess("RUN_PAYROLL_SIMULATION")) showSyncAndSimulation(); }
            case "7" -> handleLogout();
            case "0" -> { printSuccess("Goodbye!"); return false; }
            default -> printError("Invalid choice.");
        }
        return true;
    }

    private boolean handleEmployeeMainChoice(String choice) {
        switch (choice) {
            case "1" -> { if (checkAccess("CHECK_IN")) showAttendanceManagement(); }
            case "2" -> { if (checkAccess("SUBMIT_LEAVE_REQUEST")) showLeaveManagement(); }
            case "3" -> { if (checkAccess("VIEW_PAYROLL_HISTORY")) handleViewPayrollHistory(); }
            case "4" -> handleLogout();
            case "0" -> { printSuccess("Goodbye!"); return false; }
            default -> printError("Invalid choice.");
        }
        return true;
    }

    private void handleLogout() {
        if (currentSession != null) {
            printSuccess("Logged out: " + currentSession.getUsername());
        }
        currentSession = null;
        handleLogin();
    }

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // 1. LOGIN
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

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

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // 2. EMPLOYEE MANAGEMENT
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

    private void showEmployeeManagement() {
        boolean back = false;
        while (!back) {
            String[] items = isHr()
                    ? new String[]{
                        "Add Employee", "Update Employee", "Delete Employee",
                        "Search Employee", "View Employee Detail", "View Employee List", "---",
                        "Search Department", "View Departments", "---",
                        "Save CSV Data", "Load CSV Data", "Generate Test Dataset"
                    }
                    : new String[]{
                        "Add Employee", "Update Employee", "Delete Employee",
                        "Search Employee", "View Employee Detail", "View Employee List",
                        "Create Employee Account", "---",
                        "Add Department", "Update Department", "Delete Department",
                        "Search Department", "View Departments", "---",
                        "Save CSV Data", "Load CSV Data", "Generate Test Dataset"
                    };
            printSubMenu("EMPLOYEE MANAGEMENT", items);
            switch (prompt("Choose").trim()) {
                case "1"  -> handleAddEmployee();
                case "2"  -> handleUpdateEmployee();
                case "3"  -> handleDeleteEmployee();
                case "4"  -> handleSearchEmployee();
                case "5"  -> handleViewEmployeeDetail();
                case "6"  -> handleViewEmployeeList();
                case "7"  -> {
                    if (isHr()) handleSearchDepartment();
                    else handleCreateEmployeeAccount();
                }
                case "8"  -> {
                    if (isHr()) handleViewDepartments();
                    else handleAddDepartment();
                }
                case "9"  -> {
                    if (isHr()) printInfo("Data is auto-saved via Repository.");
                    else handleUpdateDepartment();
                }
                case "10" -> {
                    if (isHr()) printInfo("Data is auto-loaded via Repository.");
                    else handleDeleteDepartment();
                }
                case "11" -> {
                    if (isHr()) printInfo("Generate test dataset: not implemented yet.");
                    else handleSearchDepartment();
                }
                case "12" -> {
                    if (isHr()) printError("Invalid choice.");
                    else handleViewDepartments();
                }
                case "13" -> {
                    if (isHr()) printError("Invalid choice.");
                    else printInfo("Data is auto-saved via Repository.");
                }
                case "14" -> {
                    if (isHr()) printError("Invalid choice.");
                    else printInfo("Data is auto-loaded via Repository.");
                }
                case "15" -> {
                    if (isHr()) printError("Invalid choice.");
                    else printInfo("Generate test dataset: not implemented yet.");
                }
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

    private void handleViewEmployeeList() {
        printSectionHeader("VIEW EMPLOYEE LIST");
        printEmployeeTablePaged(employeeController.getAllEmployees());
    }

    private void handleCreateEmployeeAccount() {
        printSectionHeader("CREATE EMPLOYEE ACCOUNT");
        try {
            String employeeId = prompt("Employee ID");
            Employee employee = employeeController.getEmployeeById(employeeId);
            if (userRepo.findByEmployeeId(employeeId) != null) {
                printError("This employee already has an account.");
                return;
            }

            String username = prompt("Username");
            if (userRepo.findByUsername(username) != null) {
                printError("Username already exists.");
                return;
            }

            String password = prompt("Password");
            UserAccount account = new UserAccount(
                    generateUserAccountId(), 1L, username.trim(), password.trim(),
                    "EMPLOYEE", true, employee.getId());
            userRepo.save(account);
            printSuccess("Account created for employee: " + employee.getId());
            printUserAccountDetail(account);
        } catch (IllegalArgumentException ex) {
            printError(ex.getMessage());
        }
    }

    private void handleAddDepartment() {
        printSectionHeader("ADD DEPARTMENT");
        try {
            String name      = prompt("Department name");
            String managerId = promptOptional("Manager ID (Enter to skip)");
            Department dept  = departmentController.addDepartment(name, managerId);
            printSuccess("Department added: " + dept.getId() + " - " + dept.getName());
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

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // 3. ATTENDANCE MANAGEMENT
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

    private void showAttendanceManagement() {
        boolean back = false;
        while (!back) {
            String[] items = isEmployee()
                    ? new String[]{
                        "Check In", "Check Out", "View Attendance Record",
                        "View Attendance Summary", "Submit Attendance Adjustment Request",
                        "Review Attendance Adjustment Request"
                    }
                    : new String[]{
                        "View Attendance Record", "View Attendance Summary",
                        "Approve Attendance Adjustment", "Reject Attendance Adjustment"
                    };
            printSubMenu("ATTENDANCE MANAGEMENT", items);
            switch (prompt("Choose").trim()) {
                case "1" -> {
                    if (isEmployee()) handleCheckIn();
                    else handleViewAttendanceRecord();
                }
                case "2" -> {
                    if (isEmployee()) handleCheckOut();
                    else handleViewAttendanceSummary();
                }
                case "3" -> {
                    if (isEmployee()) handleViewAttendanceRecord();
                    else handleApproveAttendanceAdjustment();
                }
                case "4" -> {
                    if (isEmployee()) handleViewAttendanceSummary();
                    else handleRejectAttendanceAdjustment();
                }
                case "5" -> {
                    if (isEmployee()) handleSubmitAttendanceAdjustment();
                    else printError("Invalid choice.");
                }
                case "6" -> {
                    if (isEmployee()) handleReviewAttendanceAdjustment();
                    else printError("Invalid choice.");
                }
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    private void handleCheckIn() {
        printSectionHeader("CHECK IN");
        try {
            String employeeId = isEmployee() ? requireLinkedEmployeeId() : prompt("Employee ID");
            if (employeeId == null) return;
            AttendanceRecord r = attendanceController.checkIn(employeeId);
            printSuccess("Checked in. Work days: " + r.getWorkDays());
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleCheckOut() {
        printSectionHeader("CHECK OUT");
        try {
            String employeeId = isEmployee() ? requireLinkedEmployeeId() : prompt("Employee ID");
            if (employeeId == null) return;
            AttendanceRecord r = attendanceController.checkOut(
                    employeeId, promptDouble("Overtime hours (0 if none)"));
            printSuccess("Checked out. Overtime: " + r.getOvertimeHours() + "h");
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleViewAttendanceRecord() {
        printSectionHeader("VIEW ATTENDANCE RECORD");
        try {
            String employeeId = isEmployee() ? requireLinkedEmployeeId() : prompt("Employee ID");
            if (employeeId == null) return;
            AttendanceRecord r = attendanceController.getRecord(
                    employeeId, prompt("Year-Month (YYYY-MM)"));
            if (r == null) printInfo("No record found.");
            else printAttendanceDetail(r);
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleViewAttendanceSummary() {
        printSectionHeader("VIEW ATTENDANCE SUMMARY");
        List<AttendanceRecord> list;
        if (isEmployee()) {
            String employeeId = requireLinkedEmployeeId();
            if (employeeId == null) return;
            list = attendanceController.getRecordsByEmployee(employeeId);
        } else {
            list = attendanceController.getSummaryByMonth(prompt("Year-Month (YYYY-MM)"));
        }
        if (list.isEmpty()) printInfo("No records found.");
        else printAttendanceTablePaged(list);
    }

    private void handleSubmitAttendanceAdjustment() {
        printSectionHeader("SUBMIT ATTENDANCE ADJUSTMENT");
        try {
            String empId = isEmployee() ? requireLinkedEmployeeId() : prompt("Employee ID");
            if (empId == null) return;
            String yearMonth = prompt("Year-Month (YYYY-MM)");

            // HiÃƒÂ¡Ã‚Â»Ã†â€™n thÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ sÃƒÂ¡Ã‚Â»Ã¢â‚¬Ëœ liÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡u hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ HR biÃƒÂ¡Ã‚ÂºÃ‚Â¿t Ãƒâ€žÃ¢â‚¬Ëœang sÃƒÂ¡Ã‚Â»Ã‚Â­a tÃƒÂ¡Ã‚Â»Ã‚Â« Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â¢u
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

            AttendanceAdjustmentRequest request =
                    attendanceController.submitAdjustmentRequest(empId, yearMonth, reason, workDays, overtime);
            printSuccess("Adjustment submitted: " + curWorkDays + " -> " + workDays + " days");
            printInfo("Request ID: " + request.getId());
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleReviewAttendanceAdjustment() {
        printSectionHeader("REVIEW ATTENDANCE ADJUSTMENT");
        List<AttendanceAdjustmentRequest> list;
        if (isEmployee()) {
            String employeeId = requireLinkedEmployeeId();
            if (employeeId == null) return;
            list = attendanceController.getAdjustmentRequestsByEmployee(employeeId);
        } else {
            list = attendanceController.getPendingAdjustments();
        }
        if (list.isEmpty()) printInfo("No pending adjustments.");
        else printAttendanceAdjustmentTablePaged(list);
    }

    private void handleApproveAttendanceAdjustment() {
        if (!checkAccess("APPROVE_ATTENDANCE_ADJUSTMENT")) return;
        printSectionHeader("APPROVE ATTENDANCE ADJUSTMENT");
        try {
            List<AttendanceAdjustmentRequest> records = promptAttendanceAdjustmentRequestsByRange();
            if (records.isEmpty()) {
                printInfo("No adjustment requests found in this range.");
                return;
            }
            printInfo("Browse requests, then press Enter to continue to Request ID.");
            printAttendanceAdjustmentTablePaged(records, "N-next, P-prev, Enter-continue");
            attendanceController.approveAdjustment(
                    prompt("Request ID"), currentSession.getAccount().getId());
            printSuccess("Approved.");
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private void handleRejectAttendanceAdjustment() {
        if (!checkAccess("REJECT_ATTENDANCE_ADJUSTMENT")) return;
        printSectionHeader("REJECT ATTENDANCE ADJUSTMENT");
        try {
            List<AttendanceAdjustmentRequest> records = promptAttendanceAdjustmentRequestsByRange();
            if (records.isEmpty()) {
                printInfo("No adjustment requests found in this range.");
                return;
            }
            printInfo("Browse requests, then press Enter to continue to Request ID.");
            printAttendanceAdjustmentTablePaged(records, "N-next, P-prev, Enter-continue");
            attendanceController.rejectAdjustment(
                    prompt("Request ID"), currentSession.getAccount().getId(), prompt("Reason"));
            printSuccess("Rejected.");
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // 4. LEAVE MANAGEMENT
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

    private void showLeaveManagement() {
        boolean back = false;
        while (!back) {
            String[] items = isEmployee()
                    ? new String[]{
                        "Submit Leave Request", "View Leave Requests", "View Leave Balance"
                    }
                    : new String[]{
                        "Approve Leave Request", "Reject Leave Request"
                    };
            printSubMenu("LEAVE MANAGEMENT", items);
            switch (prompt("Choose").trim()) {
                case "1" -> {
                    if (isEmployee()) handleSubmitLeaveRequest();
                    else handleApproveLeaveRequest();
                }
                case "2" -> {
                    if (isEmployee()) {
                        handleViewLeaveRequests();
                    } else {
                        handleRejectLeaveRequest();
                    }
                }
                case "3" -> {
                    if (isEmployee()) {
                        handleViewLeaveBalance();
                    } else {
                        printError("Invalid choice.");
                    }
                }
                case "4" -> {
                    if (isEmployee()) {
                        printError("Invalid choice.");
                    } else {
                        printError("Invalid choice.");
                    }
                }
                case "0" -> back = true;
                default  -> printError("Invalid choice.");
            }
        }
    }

    private void handleSubmitLeaveRequest() {
        printSectionHeader("SUBMIT LEAVE REQUEST");
        try {
            String empId = isEmployee() ? requireLinkedEmployeeId() : prompt("Employee ID");
            if (empId == null) return;
            if (isEmployee()) {
                printInfo("Employee ID: " + empId);
            }
            System.out.println("  1. ANNUAL   2. SICK   3. UNPAID");
            LeaveType type = switch (prompt("Choose")) {
                case "2" -> LeaveType.SICK;
                case "3" -> LeaveType.UNPAID;
                default  -> LeaveType.ANNUAL;
            };
            // GÃƒÂ¡Ã‚Â»Ã‚Âi Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng tÃƒÆ’Ã‚Âªn method: submit() thay vÃƒÆ’Ã‚Â¬ submitLeaveRequest()
            LeaveRequest req = leaveController.submit(
                    empId, type,
                    java.time.LocalDate.parse(prompt("Start date (YYYY-MM-DD)")),
                    java.time.LocalDate.parse(prompt("End date (YYYY-MM-DD)")),
                    prompt("Reason"));
            printSuccess("Submitted: " + req.getLeaveId() + " (" + req.getDays() + " days)");
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    private void handleViewLeaveRequests() {
        printSectionHeader("VIEW LEAVE REQUESTS");
        String employeeId = requireLinkedEmployeeId();
        if (employeeId == null) return;
        List<LeaveRequest> requests = leaveController.getRequestsByEmployee(employeeId);
        if (requests.isEmpty()) {
            printInfo("No leave requests found.");
            return;
        }
        printLeaveTablePaged(requests);
    }

    private void handleApproveLeaveRequest() {
        if (!checkAccess("APPROVE_LEAVE_REQUEST")) return;
        printSectionHeader("APPROVE LEAVE REQUEST");
        List<LeaveRequest> pending = leaveController.getPendingRequests();
        if (pending.isEmpty()) { printInfo("No pending requests."); return; }
        printLeaveTablePaged(pending);
        try {
            // GÃƒÂ¡Ã‚Â»Ã‚Âi Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng tÃƒÆ’Ã‚Âªn method: approve() vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi LockMechanism
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
        printLeaveTablePaged(pending);
        try {
            // GÃƒÂ¡Ã‚Â»Ã‚Âi Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng tÃƒÆ’Ã‚Âªn method: reject()
            leaveController.reject(prompt("Leave ID"), currentSession.getAccount().getId());
            printSuccess("Rejected.");
        } catch (Exception ex) { printError(ex.getMessage()); }
    }

    private void handleViewLeaveBalance() {
        printSectionHeader("VIEW LEAVE BALANCE");
        try {
            // GÃƒÂ¡Ã‚Â»Ã‚Âi Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Âºng tÃƒÆ’Ã‚Âªn method: getBalancesByEmployee()
            String employeeId = isEmployee() ? requireLinkedEmployeeId() : prompt("Employee ID");
            if (employeeId == null) return;
            List<LeaveBalance> balances = leaveController.getBalancesByEmployee(employeeId);
            if (balances.isEmpty()) { printInfo("No balance records found."); return; }
            printInfo("Leave balance updates after HR approves the leave request.");
            System.out.printf(BOLD + "%-15s %-10s %-8s %-8s %-10s%n" + RESET,
                    "Employee", "Type", "Total", "Used", "Remaining");
            System.out.println("-".repeat(55));
            for (LeaveBalance b : balances) {
                System.out.printf("%-15s %-10s %-8d %-8d %-10d%n",
                        b.getEmployeeId(), b.getLeaveType(),
                        b.getTotalLeaveDays(), b.getUsedLeaveDays(), b.getRemainingLeaveDays());
            }
        } catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // 5. PAYROLL MANAGEMENT
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

    private void showPayrollManagement() {
        boolean back = false;
        while (!back) {
            printSubMenu("PAYROLL MANAGEMENT", new String[]{
                "Process Monthly Payroll", "View Payroll History"
            });
            switch (prompt("Choose").trim()) {
                case "1" -> handleProcessMonthlyPayroll();
                case "2" -> handleViewPayrollHistory();
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
            List<PayrollEntry> entries;
            if (isEmployee()) {
                String empId = requireLinkedEmployeeId();
                if (empId == null) return;
                entries = payrollController.getPayrollByEmployee(empId);
            } else {
                String empId = promptOptional("Employee ID (Enter = all)");
                entries = empId != null
                        ? payrollController.getPayrollByEmployee(empId)
                        : payrollController.getAllPayrollEntries();
            }
            if (entries.isEmpty()) { printInfo("No records."); return; }

            // HiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n thÃƒÆ’Ã‚Âªm cÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t Type Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ thÃƒÂ¡Ã‚ÂºÃ‚Â¥y rÃƒÆ’Ã‚Âµ 2 luÃƒÂ¡Ã‚Â»Ã¢â‚¬Å“ng tÃƒÆ’Ã‚Â­nh lÃƒâ€ Ã‚Â°Ãƒâ€ Ã‚Â¡ng Ãƒâ€žÃ¢â‚¬Ëœa hÃƒÆ’Ã‚Â¬nh
            printPayrollEntryTablePaged(entries);

            // GiÃƒÂ¡Ã‚ÂºÃ‚Â£i thÃƒÆ’Ã‚Â­ch cÃƒÆ’Ã‚Â´ng thÃƒÂ¡Ã‚Â»Ã‚Â©c khÃƒÆ’Ã‚Â¡c nhau giÃƒÂ¡Ã‚Â»Ã‚Â¯a 2 loÃƒÂ¡Ã‚ÂºÃ‚Â¡i
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

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // 6. REPORTS
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

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

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // 7. SYNC & SIMULATION
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

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
                case "4" -> printIssueList("DOUBLE PAYMENT", simulationController.detectDoublePayment());
                case "5" -> printIssueList("WRONG LEAVE DEDUCTION", simulationController.detectWrongLeaveDeduction());
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

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // DISPLAY HELPERS
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

    private void printBanner() {
        System.out.println(BOLD + CYAN);
        System.out.println("=========================================");
        System.out.println("  " + getPortalTitle());
        System.out.println("=========================================");
        System.out.println(RESET);
    }

    private void printMainMenu() {
        String user = currentSession != null
                ? DIM + " [" + currentSession.getUsername() + " | " + currentSession.getRole() + "]" + RESET : "";
        System.out.println(BOLD + "\n=========================================" + user);
        System.out.println(" " + getPortalTitle());
        System.out.println("=========================================");
        if (isAdmin()) {
            System.out.println("  1. Employee Management");
            System.out.println("  2. Configure Payroll Rules");
            System.out.println("  3. Synchronization & Simulation");
            System.out.println("  -----------------------------");
            System.out.println("  4. Log out");
        } else if (isEmployee()) {
            System.out.println("  1. Attendance Management");
            System.out.println("  2. Leave Management");
            System.out.println("  3. Payroll History");
            System.out.println("  -----------------------------");
            System.out.println("  4. Log out");
        } else {
            System.out.println("  1. Employee Management");
            System.out.println("  2. Attendance Management");
            System.out.println("  3. Leave Management");
            System.out.println("  4. Payroll Management");
            System.out.println("  5. Reports");
            System.out.println("  6. Synchronization & Simulation");
            System.out.println("  -----------------------------");
            System.out.println("  7. Log out");
        }
        System.out.println("  0. Exit\n=========================================" + RESET);
    }

    private String getPortalTitle() {
        if (currentSession == null) {
            return "INTERNAL COMPANY PORTAL";
        }
        if (isEmployee()) {
            return "EMPLOYEE SELF SERVICE PORTAL";
        }
        if (isHr()) {
            return "HR OPERATIONS PORTAL";
        }
        return "EMPLOYEE PAYROLL MANAGEMENT SYSTEM";
    }

    private void printSubMenu(String title, String[] items) {
        System.out.println(BOLD + "\n========== " + title + " ==========" + RESET);
        int n = 1;
        for (String item : items) {
            if (item.equals("---")) System.out.println("  ----------------------------");
            else System.out.printf("  %2d. %s%n", n++, item);
        }
        System.out.println("   0. Back");
        System.out.println(BOLD + "-".repeat(title.length() + 22) + RESET);
    }

    private void printSectionHeader(String title) {
        System.out.println(BOLD + CYAN + "\n> " + title + RESET);
        System.out.println("-".repeat(45));
    }

    private void printEmployeeTable(List<Employee> list) {
        if (list.isEmpty()) { printInfo("No employees found."); return; }
        System.out.printf(BOLD + "%-12s %-20s %-25s %-10s %-10s%n" + RESET, "ID","Name","Email","Dept","Type");
        System.out.println("-".repeat(80));
        for (Employee e : list)
            System.out.printf("%-12s %-20s %-25s %-10s %-10s%n",
                    e.getId(), trunc(e.getName(),19), trunc(e.getEmail(),24), e.getDepartmentId(), e.getEmploymentType());
        System.out.println("-".repeat(80));
        printInfo("Found: " + list.size());
    }

    private void printEmployeeTablePaged(List<Employee> list) {
        if (list.isEmpty()) {
            printInfo("No employees found.");
            return;
        }

        final int pageSize = 20;
        int page = 0;
        int totalPages = (list.size() + pageSize - 1) / pageSize;
        while (true) {
            int from = page * pageSize;
            int to = Math.min(from + pageSize, list.size());
            printEmployeeTable(list.subList(from, to));
            printInfo("Page " + (page + 1) + "/" + totalPages + " | Total: " + list.size());
            String choice = promptOptional("N-next, P-prev, Enter-back");
            if (choice == null || choice.equalsIgnoreCase("q")) {
                return;
            }
            if (choice.equalsIgnoreCase("n") && page < totalPages - 1) {
                page++;
            } else if (choice.equalsIgnoreCase("p") && page > 0) {
                page--;
            } else {
                printInfo("No more pages.");
            }
        }
    }

    private void printEmployeeDetail(Employee e) {
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
        printRow("ID", e.getId()); printRow("Name", e.getName()); printRow("Email", e.getEmail());
        printRow("Department", e.getDepartmentId()); printRow("Type", String.valueOf(e.getEmploymentType()));
        printRow("Base Salary", String.format("%,.0f VND", e.getBaseSalary()));
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
    }

    private void printDepartmentTable(List<Department> list) {
        System.out.printf(BOLD + "%-12s %-25s %-15s%n" + RESET, "ID","Name","Manager ID");
        System.out.println("-".repeat(55));
        for (Department d : list)
            System.out.printf("%-12s %-25s %-15s%n", d.getId(), trunc(d.getName(),24), d.getManagerId());
        System.out.println("-".repeat(55));
    }

    private void printDepartmentDetail(Department d) {
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
        printRow("ID", d.getId()); printRow("Name", d.getName()); printRow("Manager", d.getManagerId());
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
    }

    private void printAttendanceDetail(AttendanceRecord r) {
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
        printRow("ID", r.getId()); printRow("Employee", r.getEmployeeId());
        printRow("Month", r.getYearMonth()); printRow("Work Days", String.valueOf(r.getWorkDays()));
        printRow("Overtime", r.getOvertimeHours() + "h");
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
    }

    private void printAttendanceTable(List<AttendanceRecord> list) {
        System.out.printf(BOLD + "%-15s %-12s %-10s %-10s %-12s%n" + RESET, "ID","Employee","Month","WorkDays","Overtime(h)");
        System.out.println("-".repeat(62));
        for (AttendanceRecord r : list)
            System.out.printf("%-15s %-12s %-10s %-10d %-12.1f%n",
                    trunc(r.getId(),14), r.getEmployeeId(), r.getYearMonth(), r.getWorkDays(), r.getOvertimeHours());
        System.out.println("-".repeat(62));
    }

    private void printAttendanceTablePaged(List<AttendanceRecord> list) {
        printAttendanceTablePaged(list, "N-next, P-prev, Enter-back");
    }

    private void printAttendanceTablePaged(List<AttendanceRecord> list, String exitPrompt) {
        if (list.isEmpty()) {
            printInfo("No attendance records found.");
            return;
        }

        final int pageSize = 20;
        int page = 0;
        int totalPages = (list.size() + pageSize - 1) / pageSize;
        while (true) {
            int from = page * pageSize;
            int to = Math.min(from + pageSize, list.size());
            printAttendanceTable(list.subList(from, to));
            printInfo("Page " + (page + 1) + "/" + totalPages + " | Total: " + list.size());
            String choice = promptOptional(exitPrompt);
            if (choice == null || choice.equalsIgnoreCase("q")) {
                return;
            }
            if (choice.equalsIgnoreCase("n") && page < totalPages - 1) {
                page++;
            } else if (choice.equalsIgnoreCase("p") && page > 0) {
                page--;
            } else {
                printInfo("No more pages.");
            }
        }
    }

    private void printAttendanceAdjustmentTable(List<AttendanceAdjustmentRequest> list) {
        System.out.printf(
                BOLD + "%-22s %-10s %-10s %-15s %-16s %-10s%n" + RESET,
                "Request ID", "Employee", "Month", "WorkDays", "Overtime(h)", "Status");
        System.out.println("-".repeat(92));
        for (AttendanceAdjustmentRequest request : list) {
            String workDays = request.getOriginalWorkDays() + " -> " + request.getRequestedWorkDays();
            String overtime = String.format("%.1f -> %.1f",
                    request.getOriginalOvertimeHours(),
                    request.getRequestedOvertimeHours());
            System.out.printf("%-22s %-10s %-10s %-15s %-16s %-10s%n",
                    request.getId(),
                    request.getEmployeeId(),
                    request.getYearMonth(),
                    workDays,
                    overtime,
                    request.getStatus());
        }
        System.out.println("-".repeat(92));
    }

    private void printAttendanceAdjustmentTablePaged(List<AttendanceAdjustmentRequest> list) {
        printAttendanceAdjustmentTablePaged(list, "N-next, P-prev, Enter-back");
    }

    private void printAttendanceAdjustmentTablePaged(List<AttendanceAdjustmentRequest> list, String exitPrompt) {
        if (list.isEmpty()) {
            printInfo("No adjustment requests found.");
            return;
        }

        final int pageSize = 20;
        int page = 0;
        int totalPages = (list.size() + pageSize - 1) / pageSize;
        while (true) {
            int from = page * pageSize;
            int to = Math.min(from + pageSize, list.size());
            printAttendanceAdjustmentTable(list.subList(from, to));
            printInfo("Page " + (page + 1) + "/" + totalPages + " | Total: " + list.size());
            String choice = promptOptional(exitPrompt);
            if (choice == null || choice.equalsIgnoreCase("q")) {
                return;
            }
            if (choice.equalsIgnoreCase("n") && page < totalPages - 1) {
                page++;
            } else if (choice.equalsIgnoreCase("p") && page > 0) {
                page--;
            } else {
                printInfo("No more pages.");
            }
        }
    }

    private void printLeaveTable(List<LeaveRequest> list) {
        System.out.printf(BOLD + "%-15s %-12s %-10s %-12s %-12s %-10s%n" + RESET, "Leave ID","Employee","Type","Start","End","Status");
        System.out.println("-".repeat(75));
        for (LeaveRequest r : list)
            System.out.printf("%-15s %-12s %-10s %-12s %-12s %-10s%n",
                    r.getLeaveId(), r.getEmployeeId(), r.getLeaveType(), r.getStartDate(), r.getEndDate(), r.getStatus());
        System.out.println("-".repeat(75));
    }

    private void printLeaveTablePaged(List<LeaveRequest> list) {
        if (list.isEmpty()) {
            printInfo("No leave requests found.");
            return;
        }

        final int pageSize = 20;
        int page = 0;
        int totalPages = (list.size() + pageSize - 1) / pageSize;
        while (true) {
            int from = page * pageSize;
            int to = Math.min(from + pageSize, list.size());
            printLeaveTable(list.subList(from, to));
            printInfo("Page " + (page + 1) + "/" + totalPages + " | Total: " + list.size());
            String choice = promptOptional("N-next, P-prev, Enter-back");
            if (choice == null || choice.equalsIgnoreCase("q")) {
                return;
            }
            if (choice.equalsIgnoreCase("n") && page < totalPages - 1) {
                page++;
            } else if (choice.equalsIgnoreCase("p") && page > 0) {
                page--;
            } else {
                printInfo("No more pages.");
            }
        }
    }

    private void printPayrollEntryTable(List<PayrollEntry> entries) {
        System.out.printf(BOLD + "%-20s %-12s %-10s %-18s %-12s %-10s%n" + RESET,
                "Entry ID", "Employee", "Month", "Net Salary (VND)", "Status", "Type");
        System.out.println("-".repeat(92));
        for (PayrollEntry entry : entries) {
            Employee employee = payrollController.findEmployeeById(entry.getEmployeeId());
            String type = employee != null ? String.valueOf(employee.getEmploymentType()) : "?";
            System.out.printf("%-20s %-12s %-10s %-18s %-12s %-10s%n",
                    trunc(entry.getId(), 19),
                    entry.getEmployeeId(),
                    entry.extractYearMonth(),
                    String.format("%,.0f", entry.getNetSalary()),
                    entry.getStatus(),
                    type);
        }
        System.out.println("-".repeat(92));
    }

    private void printPayrollEntryTablePaged(List<PayrollEntry> entries) {
        if (entries.isEmpty()) {
            printInfo("No payroll entries found.");
            return;
        }

        final int pageSize = 20;
        int page = 0;
        int totalPages = (entries.size() + pageSize - 1) / pageSize;
        while (true) {
            int from = page * pageSize;
            int to = Math.min(from + pageSize, entries.size());
            printPayrollEntryTable(entries.subList(from, to));
            printInfo("Page " + (page + 1) + "/" + totalPages + " | Total: " + entries.size());
            String choice = promptOptional("N-next, P-prev, Enter-back");
            if (choice == null || choice.equalsIgnoreCase("q")) {
                return;
            }
            if (choice.equalsIgnoreCase("n") && page < totalPages - 1) {
                page++;
            } else if (choice.equalsIgnoreCase("p") && page > 0) {
                page--;
            } else {
                printInfo("No more pages.");
            }
        }
    }

    private void printPayrollRunSummary(PayrollRun r) {
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
        printRow("Run ID", r.getId()); printRow("Month", r.getYearMonth());
        printRow("Mechanism", r.getMechanism()); printRow("Elapsed", r.getElapsedMs() + " ms");
        printRow("TPS", String.format("%.2f", r.getTps()));
        printRow("Success", String.valueOf(r.getSuccessCount()));
        printRow("Double Pay", String.valueOf(r.getDoublePaymentCount()));
        printRow("Wrong Leave", String.valueOf(r.getWrongLeaveCount()));
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
    }

    private void printPayrollRule(PayrollRule r) {
        System.out.println(CYAN + "+---------------- Payroll Rule ----------------+" + RESET);
        printRow("Work Days", String.valueOf(r.getStandardWorkingDays()));
        printRow("Hours/Day", String.valueOf(r.getWorkingHoursPerDay()));
        printRow("OT Multiplier", r.getOvertimeMultiplier() + "x");
        printRow("Bonus", String.format("%,.0f VND", r.getAttendanceBonus()));
        printRow("Tax Rate", (r.getTaxRate() * 100) + "%");
        printRow("Tax Threshold", String.format("%,.0f VND", r.getTaxThreshold()));
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
    }

    private void printUserAccountDetail(UserAccount account) {
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
        printRow("Account ID", account.getId());
        printRow("Username", account.getUsername());
        printRow("Role", account.getRole());
        printRow("Employee ID", account.getEmployeeId());
        printRow("Active", String.valueOf(account.isActive()));
        System.out.println(CYAN + "+----------------------------------------------+" + RESET);
    }

    private void printRow(String label, String value) {
        System.out.printf("| %-20s: %-24s |%n", label, trunc(value, 24));
    }

    private void printSuccess(String msg) { System.out.println(GREEN  + "[OK] " + msg + RESET); }
    private void printError(String msg)   { System.out.println(RED    + "[ERROR] " + msg + RESET); }
    private void printInfo(String msg)    { System.out.println(YELLOW + "[INFO] " + msg + RESET); }

    private void printIssueList(String title, List<String> issues) {
        printSectionHeader(title);
        if (issues.isEmpty()) {
            printSuccess("No issue found.");
            return;
        }
        for (int i = 0; i < issues.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, issues.get(i));
        }
    }

    private List<AttendanceAdjustmentRequest> promptAttendanceAdjustmentRequestsByRange() {
        String fromMonth = prompt("From month (YYYY-MM)");
        String toMonth = prompt("To month (YYYY-MM)");
        List<AttendanceAdjustmentRequest> allRequests = attendanceController.getPendingAdjustments();
        return allRequests.stream()
                .filter(request -> {
                    String yearMonth = request.getYearMonth();
                    return yearMonth != null
                            && yearMonth.compareTo(fromMonth) >= 0
                            && yearMonth.compareTo(toMonth) <= 0;
                })
                .toList();
    }

    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â
    // INPUT HELPERS
    // ÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚ÂÃƒÂ¢Ã¢â‚¬Â¢Ã‚Â

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

    private String generateUserAccountId() {
        int max = 0;
        for (UserAccount account : userRepo.findAll()) {
            String id = account.getId();
            if (id != null && id.matches("U\\d+")) {
                max = Math.max(max, Integer.parseInt(id.substring(1)));
            }
        }
        return String.format("U%03d", max + 1);
    }

    private String requireLinkedEmployeeId() {
        if (currentSession == null) {
            printError("You must login first.");
            return null;
        }
        String employeeId = currentSession.getEmployeeId();
        if (employeeId == null || employeeId.isBlank()) {
            printError("This account is not linked to any employee.");
            return null;
        }
        return employeeId;
    }
}
