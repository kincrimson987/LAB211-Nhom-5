import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSalaryCalculatorJUnit {

    private static final double EPS = 0.01;

    @Test
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

        assertEquals(expected, actual, EPS);
    }

    @Test
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

        assertEquals(expected, actual, EPS);
    }

    @Test
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

        assertEquals(expected, actual, EPS);
    }

    @Test
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

        assertEquals(10_000_000.0, actual, EPS);
    }

    @Test
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

        assertEquals(0.0, actual, EPS);
    }

    @Test
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
        });
    }
}       