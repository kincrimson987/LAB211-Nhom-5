public class PartTimeEmployee extends Employee {

    public PartTimeEmployee() {
        setEmploymentType(EmployeeType.PARTTIME);
    }

    public PartTimeEmployee(String id,
            long version,
            String name,
            String email,
            String departmentId,
            double baseSalary) {

        super(id, version, name, email,
                departmentId, baseSalary);

        setEmploymentType(EmployeeType.PARTTIME);
    }

    @Override
    public double calculateSalary(
            AttendanceRecord attendance,
            PayrollRule rule) {

        double base = getBaseSalary()
                * attendance.getWorkDays()
                / rule.getStandardWorkingDays();

        double overtime = (getBaseSalary()
                / rule.getStandardWorkingDays()
                / rule.getWorkingHoursPerDay())
                * attendance.getOvertimeHours()
                * rule.getOvertimeMultiplier();

        return base + overtime;
    }
}