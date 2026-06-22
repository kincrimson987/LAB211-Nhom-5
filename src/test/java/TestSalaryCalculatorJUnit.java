import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Salary Calculator JUnit Test Suite")
public class TestSalaryCalculatorJUnit {

    private static final double EPS = 0.01;

    @Test
    @DisplayName("Full-time employee salary calculation")
    public void testFullTimeSalary() {
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

        double base = 12_000_000.0;
        double overtime = (12_000_000.0 / 26 / 8) * 10 * 1.5;
        double bonus = 500_000;
        double gross = base + overtime + bonus;
        double tax = gross * 0.10;
        double expected = Math.round((gross - tax) * 100.0) / 100.0;

        assertEquals(expected, actual, EPS);
    }

    @Test
    @DisplayName("Part-time employee salary calculation")
    public void testPartTimeSalary() {
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

        double expected = Math.round((8_000_000.0 * 23 / 26) * 100.0) / 100.0;

        assertEquals(expected, actual, EPS);
    }

    @Test
    @DisplayName("High salary full-time employee with tax")
    public void testHighSalary() {
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

        assertEquals(expected, actual, EPS);
    }

    @Test
    @DisplayName("Salary must not be negative")
    public void testSalaryNotNegative() {
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
                1,
                0.0
        );

        double actual = calculator.calculate(employee, attendance, rule);

        assertEquals(192307.69, actual, EPS);
    }

    @Test
    @DisplayName("Invalid attendance must throw exception")
    public void testInvalidAttendance() {
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

        AttendanceRecord wrong = new AttendanceRecord(
                "A999",
                1,
                "E999",
                26,
                0.0
        );

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculate(employee, wrong, rule);
        });
    }
}