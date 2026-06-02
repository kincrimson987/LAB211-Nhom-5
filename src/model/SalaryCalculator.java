public class SalaryCalculator {
    // default expected work days per month
    public static final int DEFAULT_EXPECTED_WORK_DAYS = 22;
    public static final int HOURS_PER_DAY = 8;

    public static double calculateNetSalary(Employee emp, AttendanceRecord attendance, LeaveBalance balance, int approvedLeaveDays, int expectedWorkDays) {
        if (emp == null || attendance == null) return 0.0;
        double base = emp.getBaseSalary();
        int expDays = expectedWorkDays > 0 ? expectedWorkDays : DEFAULT_EXPECTED_WORK_DAYS;
        double hourlyRate = base / (expDays * HOURS_PER_DAY);

        // Overtime pay at 1.5x
        double otPay = attendance.getOvertimeHours() * hourlyRate * 1.5;

        // Calculate absence days not covered by approved leaves
        int recordedWorkDays = attendance.getWorkDays();
        int absentDays = Math.max(0, expDays - recordedWorkDays - approvedLeaveDays);

        // Deduct per absent day proportional to base
        double absenceDeduction = (base / expDays) * absentDays;

        // Bonus for full attendance
        double bonus = 0.0;
        if (recordedWorkDays + approvedLeaveDays >= expDays) {
            // give 10% bonus
            bonus = 0.10 * base;
        }

        double net = base - absenceDeduction + otPay + bonus;
        // Round to 2 decimals
        return Math.round(net * 100.0) / 100.0;
    }

    public static double calculateNetSalary(Employee emp, AttendanceRecord attendance, LeaveBalance balance, int approvedLeaveDays) {
        return calculateNetSalary(emp, attendance, balance, approvedLeaveDays, DEFAULT_EXPECTED_WORK_DAYS);
    }
}
