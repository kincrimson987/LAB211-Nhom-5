import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class DataGenerator {
    private static final String DATA_DIR = "data/";
    private static final int NUM_DEPARTMENTS = 10;
    private static final int NUM_EMPLOYEES = 1200;
    private static final int CURRENT_YEAR = 2023;
    private static final int MONTHS_TO_GENERATE = 12;
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        System.out.println("===================================================================");
        System.out.println(" Bắt đầu sinh dữ liệu CSV theo chuẩn UML / Repository Layer        ");
        System.out.println("===================================================================");

        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            generateDepartments();
            generateEmployees();
            generateLeaveBalances();
            generateAttendanceRecords();
            generateLeaveRequests();
            generatePayrollEntries();
            generatePayrollRuns();

            System.out.println("\n[HOÀN TẤT] File CSV đã sinh thành công.");
        } catch (IOException e) {
            System.err.println("Lỗi khi sinh file CSV: " + e.getMessage());
        }
    }

    private static void generateDepartments() throws IOException {
        int totalRows = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "departments.csv"))) {
            writer.println("id,version,name,managerId");

            for (int i = 1; i <= NUM_DEPARTMENTS; i++) {
                String managerId = String.format("E%04d", 1 + RANDOM.nextInt(NUM_EMPLOYEES));

                writer.println(String.format("D%03d,1,Department %d,%s",
                        i, i, managerId));

                totalRows++;
            }
        }

        System.out.printf("[OK] departments.csv     (%d dòng)%n", totalRows);
    }

    private static final String[] FIRST_NAMES = {
            "James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Elizabeth",
            "William", "Linda", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
            "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Lisa", "Daniel", "Nancy",
            "Matthew", "Betty", "Anthony", "Sandra", "Mark", "Margaret", "Donald", "Ashley",
            "Steven", "Kimberly", "Paul", "Emily", "Andrew", "Donna", "Joshua", "Michelle"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", "Garcia",
            "Rodriguez", "Wilson", "Martinez", "Anderson", "Taylor", "Thomas", "Hernandez",
            "Moore", "Martin", "Jackson", "Lee", "Perez", "Thompson", "White",
            "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young"
    };

    private static void generateEmployees() throws IOException {
        int totalRows = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "employees.csv"))) {
            writer.println("id,version,name,email,departmentId");

            for (int i = 1; i <= NUM_EMPLOYEES; i++) {
                String firstName = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];
                String fullName = firstName + " " + lastName;
                String email = String.format("%s.%s%d@company.com",
                        firstName.toLowerCase(),
                        lastName.toLowerCase(),
                        i);
                String deptId = String.format("D%03d", 1 + RANDOM.nextInt(NUM_DEPARTMENTS));

                writer.println(String.format("E%04d,1,%s,%s,%s",
                        i, fullName, email, deptId));

                totalRows++;
            }
        }

        System.out.printf("[OK] employees.csv       (%d dòng)%n", totalRows);
    }

    private static void generateLeaveBalances() throws IOException {
        int totalRows = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "leave_balances.csv"))) {
            writer.println("balanceId,employeeId,leaveType,totalLeaveDays,usedLeaveDays,remainingLeaveDays,version");

            for (int i = 1; i <= NUM_EMPLOYEES; i++) {
                String employeeId = String.format("E%04d", i);

                writer.println(String.format("LB_%s_ANNUAL,%s,ANNUAL,12,0,12,0",
                        employeeId, employeeId));

                writer.println(String.format("LB_%s_SICK,%s,SICK,6,0,6,0",
                        employeeId, employeeId));

                totalRows += 2;
            }
        }

        System.out.printf("[OK] leave_balances.csv  (%d dòng)%n", totalRows);
    }

    private static void generateAttendanceRecords() throws IOException {
        int totalRows = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "attendance.csv"))) {
            writer.println("id,version,employeeId,yearMonth,workDays,overtimeHours");

            for (int m = 1; m <= MONTHS_TO_GENERATE; m++) {
                for (int i = 1; i <= NUM_EMPLOYEES; i++) {
                    int workDays = 20 + RANDOM.nextInt(7); // 20 -> 26 ngày
                    double otHours = RANDOM.nextInt(10) + (RANDOM.nextDouble() * 2);

                    String attendanceId = String.format("A_E%04d_%02d_%d", i, m, CURRENT_YEAR);
                    String employeeId = String.format("E%04d", i);
                    String yearMonth = String.format("%d-%02d", CURRENT_YEAR, m);

                    writer.println(String.format("%s,1,%s,%s,%d,%.1f",
                            attendanceId,
                            employeeId,
                            yearMonth,
                            workDays,
                            otHours));

                    totalRows++;
                }
            }
        }

        System.out.printf("[OK] attendance.csv      (%d dòng) -> Đạt mốc yêu cầu!%n", totalRows);
    }

    private static void generateLeaveRequests() throws IOException {
        int totalRows = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "leave_requests.csv"))) {
            writer.println("leaveId,employeeId,leaveType,startDate,endDate,reason,status,approvedBy");

            for (int m = 1; m <= MONTHS_TO_GENERATE; m++) {
                for (int i = 1; i <= NUM_EMPLOYEES; i++) {
                    if (RANDOM.nextInt(100) < 10) {
                        String employeeId = String.format("E%04d", i);
                        String leaveType = RANDOM.nextBoolean() ? "ANNUAL" : "SICK";

                        int startDay = 1 + RANDOM.nextInt(20);
                        int days = 1 + RANDOM.nextInt(3);
                        int endDay = startDay + days - 1;

                        String startDate = String.format("%d-%02d-%02d", CURRENT_YEAR, m, startDay);
                        String endDate = String.format("%d-%02d-%02d", CURRENT_YEAR, m, endDay);
                        String leaveId = String.format("LR_%s_%02d_%d", employeeId, m, CURRENT_YEAR);

                        writer.println(String.format("%s,%s,%s,%s,%s,Leave request,PENDING,",
                                leaveId,
                                employeeId,
                                leaveType,
                                startDate,
                                endDate));

                        totalRows++;
                    }
                }
            }
        }

        System.out.printf("[OK] leave_requests.csv  (%d dòng)%n", totalRows);
    }

    private static void generatePayrollEntries() throws IOException {
        int totalRows = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "payroll_entries.csv"))) {
            writer.println("id,version,employeeId,netSalary,status");

            for (int m = 1; m <= MONTHS_TO_GENERATE; m++) {
                for (int i = 1; i <= NUM_EMPLOYEES; i++) {
                    writer.println(String.format("PR_E%04d_%02d_%d,0,E%04d,0.0,PENDING",
                            i, m, CURRENT_YEAR, i));

                    totalRows++;
                }
            }
        }

        System.out.printf("[OK] payroll_entries.csv (%d dòng)%n", totalRows);
    }

    private static void generatePayrollRuns() throws IOException {
        int totalRows = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_DIR + "payroll_runs.csv"))) {
            writer.println("id,version,yearMonth,mechanism,elapsedMs,successCount,doublePaymentCount,wrongLeaveCount,tps");

            String[] mechanisms = {
                    "NO_LOCK",
                    "SYNCHRONIZED",
                    "OPTIMISTIC_LOCKING",
                    "FILE_LOCK"
            };

            for (int m = 1; m <= MONTHS_TO_GENERATE; m++) {
                String yearMonth = String.format("%d-%02d", CURRENT_YEAR, m);

                for (String mechanism : mechanisms) {
                    String runId = String.format("RUN_%s_%02d_%d", mechanism, m, CURRENT_YEAR);

                    writer.println(String.format("%s,0,%s,%s,0,0,0,0,0.0",
                            runId,
                            yearMonth,
                            mechanism));

                    totalRows++;
                }
            }
        }

        System.out.printf("[OK] payroll_runs.csv    (%d dòng)%n", totalRows);
    }
}