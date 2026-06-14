public class SalaryCalculator {

    public double calculate(Employee employee,
                            AttendanceRecord attendance,
                            PayrollRule rule) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null.");
        }

        if (attendance == null) {
            throw new IllegalArgumentException("Attendance record cannot be null.");
        }

        if (rule == null) {
            throw new IllegalArgumentException("Payroll rule cannot be null.");
        }

        return employee.calculateSalary(attendance, rule);
    }
}