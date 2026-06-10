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
        Employee e1 = new Employee("E001", 1, "Alice", "alice@company.com", "D001");
        e1.setEmploymentType(EmployeeType.FULLTIME);
        e1.setBaseSalary(12000000);

        AttendanceRecord a1 = new AttendanceRecord("A001", 1, "E001", 26, 10.0);

        double result1 = SalaryCalculator.calcNetSalary(e1, a1, rule);

        double base1 = 12_000_000.0 * 26 / 26;
        double ot1 = (12_000_000.0 / 26 / 8) * 10 * 1.5;
        double bonus1 = 500_000;
        double deduction1 = 0;
        double gross1 = base1 + ot1 + bonus1 - deduction1;
        double tax1 = gross1 * 0.10;
        double expected1 = Math.round((gross1 - tax1) * 100.0) / 100.0;

        assertEqual(result1, expected1, "FULLTIME full attendance + OT + bonus + tax");

        // Case 2: PARTTIME vắng 3 ngày + không bonus + không thuế
        Employee e2 = new Employee("E002", 1, "Bob", "bob@company.com", "D001");
        e2.setEmploymentType(EmployeeType.PARTTIME);
        e2.setBaseSalary(8000000);

        AttendanceRecord a2 = new AttendanceRecord("A002", 1, "E002", 23, 0.0);

        double result2 = SalaryCalculator.calcNetSalary(e2, a2, rule);

        double base2 = 8_000_000.0 * 23 / 26;
        double ot2 = 0;
        double bonus2 = 0;
        double deduction2 = (8_000_000.0 / 26) * 3;
        double gross2 = base2 + ot2 + bonus2 - deduction2;
        double tax2 = 0;
        double expected2 = Math.round((gross2 - tax2) * 100.0) / 100.0;

        assertEqual(result2, expected2, "PARTTIME absent 3 days + no bonus");

        // Case 3: FULLTIME lương cao + đi đủ + có thuế
        Employee e3 = new Employee("E003", 1, "Carol", "carol@company.com", "D002");
        e3.setEmploymentType(EmployeeType.FULLTIME);
        e3.setBaseSalary(20000000);

        AttendanceRecord a3 = new AttendanceRecord("A003", 1, "E003", 26, 0.0);

        double result3 = SalaryCalculator.calcNetSalary(e3, a3, rule);

        double gross3 = 20_000_000.0 + 500_000;
        double expected3 = Math.round((gross3 - gross3 * 0.10) * 100.0) / 100.0;

        assertEqual(result3, expected3, "FULLTIME high salary + tax");

        // Case 4: PARTTIME lương thấp + đi đủ + không thuế
        Employee e4 = new Employee("E004", 1, "Dan", "dan@company.com", "D003");
        e4.setEmploymentType(EmployeeType.PARTTIME);
        e4.setBaseSalary(10000000);

        AttendanceRecord a4 = new AttendanceRecord("A004", 1, "E004", 26, 0.0);

        double result4 = SalaryCalculator.calcNetSalary(e4, a4, rule);

        double expected4 = 10_000_000.0 + 500_000;

        assertEqual(result4, expected4, "PARTTIME low salary + no tax");

        // Case 5: Vắng nhiều -> lương không âm
        Employee e5 = new Employee("E005", 1, "Eva", "eva@company.com", "D004");
        e5.setEmploymentType(EmployeeType.FULLTIME);
        e5.setBaseSalary(5000000);

        AttendanceRecord a5 = new AttendanceRecord("A005", 1, "E005", 0, 0.0);

        double result5 = SalaryCalculator.calcNetSalary(e5, a5, rule);

        assertEqual(result5, 0.0, "Many absent days -> salary not negative");

        System.out.println("All salary tests passed.");
    }
}