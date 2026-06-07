public class SalaryCalculator {

    public static double calcBaseSalary(Employee employee, AttendanceRecord attendance, PayrollRule rule) {
        if (employee == null || attendance == null || rule == null) {
            return 0.0;
        }

        double baseSalary = employee.getBaseSalary();
        int workingDays = attendance.getWorkDays();

        return baseSalary * workingDays / rule.getStandardWorkingDays();
    }

    public static double calcOvertime(Employee employee, AttendanceRecord attendance, PayrollRule rule) {
        if (employee == null || attendance == null || rule == null) {
            return 0.0;
        }

        double hourlyRate = employee.getBaseSalary()
                / rule.getStandardWorkingDays()
                / rule.getWorkingHoursPerDay();

        return hourlyRate * attendance.getOvertimeHours() * rule.getOvertimeMultiplier();
    }

    public static double calcDeduction(Employee employee, AttendanceRecord attendance, PayrollRule rule) {
        if (employee == null || attendance == null || rule == null) {
            return 0.0;
        }

        int absentDays = rule.getStandardWorkingDays() - attendance.getWorkDays();

        if (absentDays < 0) {
            absentDays = 0;
        }

        double dailySalary = employee.getBaseSalary() / rule.getStandardWorkingDays();

        return dailySalary * absentDays;
    }

   public static double calcBonus(AttendanceRecord attendance, PayrollRule rule) {
    if (attendance == null || rule == null) {
        return 0.0;
    }

    int absentDays = rule.getStandardWorkingDays() - attendance.getWorkDays();

    if (absentDays < 0) {
        absentDays = 0;
    }

    if (absentDays == 0) {
        return rule.getAttendanceBonus();
    }

    return 0.0;
}
    public static double calcGross(Employee employee, AttendanceRecord attendance, PayrollRule rule) {
        double baseSalaryPaid = calcBaseSalary(employee, attendance, rule);
        double overtimePay = calcOvertime(employee, attendance, rule);
        double bonus = calcBonus(attendance, rule);
        double deduction = calcDeduction(employee, attendance, rule);

        return baseSalaryPaid + overtimePay + bonus - deduction;
    }

    public static double calcTax(Employee employee, AttendanceRecord attendance, PayrollRule rule) {
        if (employee == null || attendance == null || rule == null) {
            return 0.0;
        }

        double gross = calcGross(employee, attendance, rule);

        if (gross > rule.getTaxThreshold()) {
            return gross * rule.getTaxRate();
        }

        return 0.0;
    }

    public static double calcNetSalary(Employee employee, AttendanceRecord attendance, PayrollRule rule) {
        double gross = calcGross(employee, attendance, rule);
        double tax = calcTax(employee, attendance, rule);

        double netSalary = gross - tax;

        if (netSalary < 0) {
            netSalary = 0.0;
        }

        return round(netSalary);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}