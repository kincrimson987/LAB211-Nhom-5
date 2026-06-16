import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Salary Calculator JUnit Test Suite")
public class TestSalaryCalculatorJUnit {

    private static final double EPS = 0.01;

    @Test
    @DisplayName("Full-time employee: full attendance, overtime, bonus and tax")
    public void testFullTimeFullAttendanceWithOvertimeBonusAndTax() {
        PayrollRule rule = new PayrollRule();
        SalaryCalculator calculator = new SalaryCalculator();

        Employee employee = new FullTimeEmployee(
                "E001",
                1,
                "Alice",
                "alice@company.com",
                "D001",
                12_000_000
        );

        AttendanceRecord attendance = new AttendanceRecord(
                "A001",
                1,
                "E001",
                26,
                10.0
        );

        double actual = calculator.calculate(employee, attendance, rule);

        double base = 12_000_000.0 * 26 / 26;
        double overtime = (12_000_000.0 / 26 / 8) * 10 * 1.5;
        double bonus = 500_000;
        double gross = base + overtime + bonus;
        double tax = gross * 0.10;
        double expected = Math.round((gross - tax) * 100.0) / 100.0;

        printSalaryResult(
                "Full-time salary with overtime, bonus and tax",
                "E001",
                "Full-time",
                26,
                10.0,
                base,
                overtime,
                bonus,
                gross,
                tax,
                expected,
                actual
        );

        assertEquals(expected, actual, EPS,
                "Full-time salary should include base salary, overtime, attendance bonus and tax deduction.");
    }

    @Test
    @DisplayName("Part-time employee: absent 3 days, no bonus and no tax")
    public void testPartTimeAbsentThreeDaysNoBonusNoTax() {
        PayrollRule rule = new PayrollRule();
        SalaryCalculator calculator = new SalaryCalculator();

        Employee employee = new PartTimeEmployee(
                "E002",
                1,
                "Bob",
                "bob@company.com",
                "D001",
                8_000_000
        );

        AttendanceRecord attendance = new AttendanceRecord(
                "A002",
                1,
                "E002",
                23,
                0.0
        );

        double actual = calculator.calculate(employee, attendance, rule);

        double base = 8_000_000.0 * 23 / 26;
        double overtime = 0.0;
        double bonus = 0.0;
        double gross = base;
        double tax = 0.0;
        double expected = Math.round(base * 100.0) / 100.0;

        printSalaryResult(
                "Part-time salary with absent days",
                "E002",
                "Part-time",
                23,
                0.0,
                base,
                overtime,
                bonus,
                gross,
                tax,
                expected,
                actual
        );

        assertEquals(expected, actual, EPS,
                "Part-time salary should be calculated by actual working days without bonus or tax.");
    }

    @Test
    @DisplayName("Full-time employee: high salary with tax")
    public void testFullTimeHighSalaryWithTax() {
        PayrollRule rule = new PayrollRule();
        SalaryCalculator calculator = new SalaryCalculator();

        Employee employee = new FullTimeEmployee(
                "E003",
                1,
                "Carol",
                "carol@company.com",
                "D002",
                20_000_000
        );

        AttendanceRecord attendance = new AttendanceRecord(
                "A003",
                1,
                "E003",
                26,
                0.0
        );

        double actual = calculator.calculate(employee, attendance, rule);

        double base = 20_000_000.0;
        double overtime = 0.0;
        double bonus = 500_000;
        double gross = base + bonus;
        double tax = gross * 0.10;
        double expected = Math.round((gross - tax) * 100.0) / 100.0;

        printSalaryResult(
                "Full-time high salary with tax",
                "E003",
                "Full-time",
                26,
                0.0,
                base,
                overtime,
                bonus,
                gross,
                tax,
                expected,
                actual
        );

        assertEquals(expected, actual, EPS,
                "Full-time high salary should be taxed when gross salary exceeds the tax threshold.");
    }

    @Test
    @DisplayName("Part-time employee: low salary without tax")
    public void testPartTimeLowSalaryNoTax() {
        PayrollRule rule = new PayrollRule();
        SalaryCalculator calculator = new SalaryCalculator();

        Employee employee = new PartTimeEmployee(
                "E004",
                1,
                "Dan",
                "dan@company.com",
                "D003",
                10_000_000
        );

        AttendanceRecord attendance = new AttendanceRecord(
                "A004",
                1,
                "E004",
                26,
                0.0
        );

        double actual = calculator.calculate(employee, attendance, rule);

        double base = 10_000_000.0;
        double overtime = 0.0;
        double bonus = 0.0;
        double gross = base;
        double tax = 0.0;
        double expected = 10_000_000.0;

        printSalaryResult(
                "Part-time low salary without tax",
                "E004",
                "Part-time",
                26,
                0.0,
                base,
                overtime,
                bonus,
                gross,
                tax,
                expected,
                actual
        );

        assertEquals(expected, actual, EPS,
                "Part-time low salary should not be taxed.");
    }

    @Test
    @DisplayName("Full-time employee: salary must not be negative")
    public void testManyAbsentDaysSalaryNotNegative() {
        PayrollRule rule = new PayrollRule();
        SalaryCalculator calculator = new SalaryCalculator();

        Employee employee = new FullTimeEmployee(
                "E005",
                1,
                "Eva",
                "eva@company.com",
                "D004",
                5_000_000
        );

        AttendanceRecord attendance = new AttendanceRecord(
                "A005",
                1,
                "E005",
                0,
                0.0
        );

        double actual = calculator.calculate(employee, attendance, rule);

        double base = 0.0;
        double overtime = 0.0;
        double bonus = 0.0;
        double gross = 0.0;
        double tax = 0.0;
        double expected = 0.0;

        printSalaryResult(
                "Salary must not be negative",
                "E005",
                "Full-time",
                0,
                0.0,
                base,
                overtime,
                bonus,
                gross,
                tax,
                expected,
                actual
        );

        assertEquals(0.0, actual, EPS,
                "Salary should not be negative even when the employee has zero working days.");
    }

    @Test
    @DisplayName("Validation: attendance record must belong to the selected employee")
    public void testAttendanceMustBelongToCorrectEmployee() {
        PayrollRule rule = new PayrollRule();
        SalaryCalculator calculator = new SalaryCalculator();

        Employee employee = new FullTimeEmployee(
                "E001",
                1,
                "Alice",
                "alice@company.com",
                "D001",
                12_000_000
        );

        AttendanceRecord wrongAttendance = new AttendanceRecord(
                "A999",
                1,
                "E999",
                26,
                0.0
        );

        System.out.println("======================================");
        System.out.println("TEST: Invalid Attendance Record");
        System.out.println("Employee ID: E001");
        System.out.println("Attendance employee ID: E999");
        System.out.println("Expected behavior: IllegalArgumentException");
        System.out.println("--------------------------------------");

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(employee, wrongAttendance, rule);
        }, "The system must reject attendance records that do not belong to the selected employee.");

        System.out.println("Exception thrown: true");
        System.out.println("Result: PASSED");
        System.out.println("======================================");
    }

    private void printSalaryResult(
            String testName,
            String employeeId,
            String employeeType,
            int workingDays,
            double overtimeHours,
            double base,
            double overtime,
            double bonus,
            double gross,
            double tax,
            double expected,
            double actual
    ) {
        System.out.println("======================================");
        System.out.println("TEST: " + testName);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Employee type: " + employeeType);
        System.out.println("Working days: " + workingDays);
        System.out.println("Overtime hours: " + overtimeHours);
        System.out.println("--------------------------------------");
        System.out.printf("Base salary: %,.0f VND%n", base);
        System.out.printf("Overtime pay: %,.0f VND%n", overtime);
        System.out.printf("Bonus: %,.0f VND%n", bonus);
        System.out.printf("Gross salary: %,.0f VND%n", gross);
        System.out.printf("Tax deduction: %,.0f VND%n", tax);
        System.out.println("--------------------------------------");
        System.out.printf("Expected net salary: %,.0f VND%n", expected);
        System.out.printf("Actual net salary: %,.0f VND%n", actual);
        System.out.println("Salary calculation correct: " + (Math.abs(expected - actual) <= EPS));
        System.out.println("Result: " + (Math.abs(expected - actual) <= EPS ? "PASSED" : "FAILED"));
        System.out.println("======================================");
    }
}