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

        assertEquals(expected, actual, EPS,
                "Full-time salary should include base salary, overtime, attendance bonus and tax deduction.");

        System.out.println("[PASS] Full-time salary with overtime, bonus and tax");
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
        double expected = Math.round(base * 100.0) / 100.0;

        assertEquals(expected, actual, EPS,
                "Part-time salary should be calculated by actual working days without bonus or tax.");

        System.out.println("[PASS] Part-time salary with absent days");
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

        double gross = 20_000_000.0 + 500_000;
        double expected = Math.round((gross - gross * 0.10) * 100.0) / 100.0;

        assertEquals(expected, actual, EPS,
                "Full-time high salary should be taxed when gross salary exceeds the tax threshold.");

        System.out.println("[PASS] Full-time high salary with tax");
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

        assertEquals(10_000_000.0, actual, EPS,
                "Part-time low salary should not be taxed.");

        System.out.println("[PASS] Part-time low salary without tax");
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

        assertEquals(0.0, actual, EPS,
                "Salary should not be negative even when the employee has zero working days.");

        System.out.println("[PASS] Salary is not negative");
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

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(employee, wrongAttendance, rule);
        }, "The system must reject attendance records that do not belong to the selected employee.");

        System.out.println("[PASS] Invalid attendance record is rejected");
    }
}