public class SalaryCalculator {

    public double calculate(
            Employee employee,
            AttendanceRecord attendance,
            PayrollRule rule) {

        if (employee == null
                || attendance == null
                || rule == null) {
            return 0.0;
        }

        return employee.calculateSalary(
                attendance,
                rule);
    }
}