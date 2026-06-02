public class TestSalaryCalculator {
    private static final double EPS = 0.01;

    public static void assertEqual(double a, double b, String msg) {
        if (Math.abs(a - b) > EPS) {
            System.err.printf("[FAIL] %s: expected=%.2f but was=%.2f\n", msg, b, a);
            System.exit(2);
        } else {
            System.out.printf("[OK] %s: %.2f\n", msg, a);
        }
    }

    public static void main(String[] args) {
        // 1) FULLTIME with OT, full attendance
        Employee e1 = new Employee("E0001", 1, "Alice Full", "alice@company.com", "D001");
        e1.setEmploymentType(Enums.EmploymentType.FULLTIME);
        e1.setBaseSalary(4000.0);
        AttendanceRecord a1 = new AttendanceRecord("A1", 1, "E0001", 22, 10.0);
        double net1 = SalaryCalculator.calculateNetSalary(e1, a1, null, 0);
        // expected: base + OT + bonus (10% of base)
        double hourly = 4000.0 / (22 * 8);
        double expected1 = 4000.0 + 10.0 * hourly * 1.5 + 0.10 * 4000.0;
        expected1 = Math.round(expected1 * 100.0) / 100.0;
        assertEqual(net1, expected1, "FULLTIME with OT and full attendance");

        // 2) PARTTIME absent (no approved leaves)
        Employee e2 = new Employee("E0002", 1, "Bob Part", "bob@company.com", "D001");
        e2.setEmploymentType(Enums.EmploymentType.PARTTIME);
        e2.setBaseSalary(2000.0);
        AttendanceRecord a2 = new AttendanceRecord("A2", 1, "E0002", 15, 0.0);
        double net2 = SalaryCalculator.calculateNetSalary(e2, a2, null, 0);
        int absent = 22 - 15 - 0;
        double expected2 = 2000.0 - (2000.0 / 22.0) * absent;
        expected2 = Math.round(expected2 * 100.0) / 100.0;
        assertEqual(net2, expected2, "PARTTIME absent without leave");

        // 3) PARTTIME full days, bonus applies
        Employee e3 = new Employee("E0003", 1, "Carol Part", "carol@company.com", "D002");
        e3.setEmploymentType(Enums.EmploymentType.PARTTIME);
        e3.setBaseSalary(2000.0);
        AttendanceRecord a3 = new AttendanceRecord("A3", 1, "E0003", 22, 0.0);
        double net3 = SalaryCalculator.calculateNetSalary(e3, a3, null, 0);
        double expected3 = 2000.0 + 0.10 * 2000.0;
        expected3 = Math.round(expected3 * 100.0) / 100.0;
        assertEqual(net3, expected3, "PARTTIME full days with bonus");

        // 4) FULLTIME with approved sick leave (no deduction)
        Employee e4 = new Employee("E0004", 1, "Dan Sick", "dan@company.com", "D003");
        e4.setEmploymentType(Enums.EmploymentType.FULLTIME);
        e4.setBaseSalary(4500.0);
        AttendanceRecord a4 = new AttendanceRecord("A4", 1, "E0004", 20, 5.0); // 20 present + 2 approved leave = 22
        double net4 = SalaryCalculator.calculateNetSalary(e4, a4, null, 2);
        double hourly4 = 4500.0 / (22 * 8);
        double expected4 = 4500.0 + 5.0 * hourly4 * 1.5 + 0.10 * 4500.0; // bonus since 20+2>=22
        expected4 = Math.round(expected4 * 100.0) / 100.0;
        assertEqual(net4, expected4, "FULLTIME with approved sick leave and OT");

        // 5) Combination: FULLTIME absent and some OT but not enough days -> no bonus
        Employee e5 = new Employee("E0005", 1, "Eva Mix", "eva@company.com", "D002");
        e5.setEmploymentType(Enums.EmploymentType.FULLTIME);
        e5.setBaseSalary(5000.0);
        AttendanceRecord a5 = new AttendanceRecord("A5", 1, "E0005", 18, 8.0); // 4 absent
        double net5 = SalaryCalculator.calculateNetSalary(e5, a5, null, 0);
        double hourly5 = 5000.0 / (22 * 8);
        int absent5 = 22 - 18;
        double expected5 = 5000.0 - (5000.0 / 22.0) * absent5 + 8.0 * hourly5 * 1.5; // no bonus
        expected5 = Math.round(expected5 * 100.0) / 100.0;
        assertEqual(net5, expected5, "FULLTIME absent with OT no bonus");

        System.out.println("All salary tests passed.");
    }
}
