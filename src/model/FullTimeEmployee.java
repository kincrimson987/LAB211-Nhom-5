public class FullTimeEmployee extends Employee {

    public FullTimeEmployee() {
        setEmploymentType(EmployeeType.FULLTIME);
    }

    public FullTimeEmployee(String id,
                            long version,
                            String name,
                            String email,
                            String departmentId,
                            double baseSalary) {
        super(id, version, name, email, departmentId, baseSalary);
        setEmploymentType(EmployeeType.FULLTIME);
    }

    @Override
    public double calculateSalary(AttendanceRecord attendance, PayrollRule rule) {
        validateAttendance(attendance);

        if (rule == null) {
            throw new IllegalArgumentException("Payroll rule cannot be null.");
        }

        double base = getBaseSalary()
                * attendance.getWorkDays()
                / rule.getStandardWorkingDays();

        double overtime = (getBaseSalary()
                / rule.getStandardWorkingDays()
                / rule.getWorkingHoursPerDay())
                * attendance.getOvertimeHours()
                * rule.getOvertimeMultiplier();

        double bonus = attendance.getWorkDays() >= rule.getStandardWorkingDays()
                ? rule.getAttendanceBonus()
                : 0.0;

       
        double gross = base + overtime + bonus;

        double tax = gross > rule.getTaxThreshold()
                ? gross * rule.getTaxRate()
                : 0.0;

        double netSalary = gross - tax;

        return roundMoney(Math.max(0.0, netSalary));
    }
}