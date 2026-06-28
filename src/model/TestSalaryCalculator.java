/**
 * TestSalaryCalculator - test chạy thủ công (không phải JUnit).
 * Đã bỏ SalaryCalculator — gọi employee.calculateSalary() trực tiếp qua đa hình.
 */
public class TestSalaryCalculator {
    private static final double EPS = 0.01;

    public static void assertEqual(double actual, double expected, String message) {
        if (Math.abs(actual - expected) > EPS) {
            System.err.printf("[FAIL] %s: expected=%.2f but was=%.2f%n",
                    message, expected, actual);
            System.exit(1);
        } else {
            System.out.printf("[OK] %s: %.2f%n", message, actual);
        }
    }

    public static void main(String[] args) {
        PayrollRule rule = new PayrollRule();

        // Case 1: FULLTIME đi đủ + 10h OT + có bonus + có thuế
        Employee e1 = new FullTimeEmployee("E001", 1, "Alice", "alice@company.com", "D001", 12000000);

        AttendanceRecord a1 = new AttendanceRecord("A001", 1, "E001", 26, 10.0);

        // Gọi trực tiếp qua đa hình — FullTimeEmployee tự tính
        double result1 = e1.calculateSalary(a1, rule);

        double base1 = 12_000_000.0 * 26 / 26;
        double ot1 = (12_000_000.0 / 26 / 8) * 10 * 1.5;
        double bonus1 = 500_000;
        double gross1 = base1 + ot1 + bonus1;
        double tax1 = gross1 * 0.10;
        double expected1 = Math.round((gross1 - tax1) * 100.0) / 100.0;

        assertEqual(result1, expected1, "FULLTIME full attendance + OT + bonus + tax");

        // Case 2: PARTTIME vắng 3 ngày + không bonus + không thuế
        Employee e2 = new PartTimeEmployee("E002", 1, "Bob", "bob@company.com", "D001", 8000000);

        AttendanceRecord a2 = new AttendanceRecord("A002", 1, "E002", 23, 0.0);

        // Gọi trực tiếp qua đa hình — PartTimeEmployee tự tính
        double result2 = e2.calculateSalary(a2, rule);

        double base2 = 8_000_000.0 * 23 / 26;
        double ot2 = 0;
        double expected2 = Math.round((base2 + ot2) * 100.0) / 100.0;

        assertEqual(result2, expected2, "PARTTIME absent 3 days + no bonus");

        // Case 3: FULLTIME lương cao + đi đủ + có thuế
        Employee e3 = new FullTimeEmployee("E003", 1, "Carol", "carol@company.com", "D002", 20000000);

        AttendanceRecord a3 = new AttendanceRecord("A003", 1, "E003", 26, 0.0);

        double result3 = e3.calculateSalary(a3, rule);

        double gross3 = 20_000_000.0 + 500_000;
        double expected3 = Math.round((gross3 - gross3 * 0.10) * 100.0) / 100.0;

        assertEqual(result3, expected3, "FULLTIME high salary + tax");

        // Case 4: PARTTIME lương thấp + đi đủ + không thuế
        Employee e4 = new PartTimeEmployee("E004", 1, "Dan", "dan@company.com", "D003", 10000000);

        AttendanceRecord a4 = new AttendanceRecord("A004", 1, "E004", 26, 0.0);

        double result4 = e4.calculateSalary(a4, rule);

        double expected4 = 10_000_000.0;

        assertEqual(result4, expected4, "PARTTIME low salary + no tax");

        // Case 5: Vắng nhiều -> lương không âm
        Employee e5 = new FullTimeEmployee("E005", 1, "Eva", "eva@company.com", "D004", 5000000);

        AttendanceRecord a5 = new AttendanceRecord("A005", 1, "E005", 0, 0.0);

        double result5 = e5.calculateSalary(a5, rule);

        assertEqual(result5, 0.0, "Many absent days -> salary not negative");

        System.out.println("All salary tests passed.");
    }
}