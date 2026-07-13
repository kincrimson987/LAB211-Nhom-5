import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test tính lương gọi trực tiếp qua đa hình (Polymorphism).
 * Đã bỏ SalaryCalculator — logic tính lương nằm trong FullTimeEmployee / PartTimeEmployee.
 */
@DisplayName("Salary Calculator JUnit Test Suite")
public class TestSalaryCalculatorJUnit {

    private static final double EPS = 0.01;

    @Test
    @DisplayName("Full-time employee salary calculation")
    public void testFullTimeSalary() {
        PayrollRule rule = new PayrollRule();

        // Dùng đa hình: FullTimeEmployee tự tính lương theo công thức riêng
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

        double actual = employee.calculateSalary(attendance, rule);

        double base = 12_000_000.0;
        double overtime = (12_000_000.0 / 26 / 8) * 10 * 1.5;
        double bonus = 500_000;
        double gross = base + overtime + bonus;
        double tax = gross * 0.10;
        double expected = Math.floor((gross - tax) / 1_000.0) * 1_000.0;

        assertEquals(expected, actual, EPS);
    }

    @Test
    @DisplayName("Part-time employee salary calculation")
    public void testPartTimeSalary() {
        PayrollRule rule = new PayrollRule();

        // Dùng đa hình: PartTimeEmployee tự tính lương theo công thức riêng (không có thuế, không bonus)
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

        double actual = employee.calculateSalary(attendance, rule);

        double expected = Math.floor((8_000_000.0 * 23 / 26) / 1_000.0) * 1_000.0;

        assertEquals(expected, actual, EPS);
    }

    @Test
    @DisplayName("High salary full-time employee with tax")
    public void testHighSalary() {
        PayrollRule rule = new PayrollRule();

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

        double actual = employee.calculateSalary(attendance, rule);

        double gross = 20_000_000.0 + 500_000;
        double expected = Math.floor((gross - gross * 0.10) / 1_000.0) * 1_000.0;

        assertEquals(expected, actual, EPS);
    }

    @Test
    @DisplayName("Salary must not be negative")
    public void testSalaryNotNegative() {
        PayrollRule rule = new PayrollRule();

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

        double actual = employee.calculateSalary(attendance, rule);

        assertEquals(192307.69, actual, EPS);
    }

    @Test
    @DisplayName("Invalid attendance must throw exception")
    public void testInvalidAttendance() {
        PayrollRule rule = new PayrollRule();

        Employee employee = new FullTimeEmployee(
                "E001",
                1,
                "Alice",
                "alice@company.com",
                "D001",
                12_000_000
        );

        // Attendance có employeeId khác => validateAttendance() sẽ throw exception
        AttendanceRecord wrong = new AttendanceRecord(
                "A999",
                1,
                "E999",
                26,
                0.0
        );

        assertThrows(IllegalArgumentException.class, () -> {
            employee.calculateSalary(wrong, rule);
        });
    }
}
