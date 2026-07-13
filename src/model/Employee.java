public abstract class Employee extends BaseEntity {
    private String name;
    private String email;
    private String departmentId;
    private EmployeeType employmentType = EmployeeType.FULLTIME;
    private double baseSalary = 0.0;

    public Employee() {
    }

    public Employee(String id, long version, String name, String email, String departmentId) {
        super(id, version);
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
    }

    public Employee(String id,
            long version,
            String name,
            String email,
            String departmentId,
            double baseSalary) {
        super(id, version);
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
        this.baseSalary = baseSalary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public EmployeeType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmployeeType employmentType) {
        this.employmentType = employmentType;
    }

    public double getBaseSalary() {
        if (baseSalary <= 0.0) {
            return employmentType == EmployeeType.FULLTIME ? 12000000.0 : 8000000.0;
        }
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public void validateAttendance(
            AttendanceRecord attendance) {

        if (attendance == null) {
            throw new IllegalArgumentException(
                    "Attendance record cannot be null.");
        }

        if (attendance.getEmployeeId() == null
                || !attendance.getEmployeeId().equals(getId())) {

            throw new IllegalArgumentException(
                    "Attendance record does not belong to employee "
                            + getId());
        }
    }

    public double roundMoney(double value) {
        return Math.floor(value / 1_000.0) * 1_000.0;
    }

    @Override
    public String toCsvLine() {
        return String.format(java.util.Locale.US, "%s,%d,%s,%s,%s,%s,%.2f",
                getId(),
                getVersion(),
                name,
                email,
                departmentId,
                employmentType != null ? employmentType.name() : EmployeeType.FULLTIME.name(),
                baseSalary);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");

        if (parts.length >= 7) {
            setId(parts[0].trim());
            setVersion(Long.parseLong(parts[1].trim()));
            this.name = parts[2].trim();
            this.email = parts[3].trim();
            this.departmentId = parts[4].trim();
            this.employmentType = EmployeeType.valueOf(parts[5].trim().toUpperCase());
            this.baseSalary = Double.parseDouble(parts[6].trim());
        } else if (parts.length >= 5) {
            setId(parts[0].trim());
            setVersion(Long.parseLong(parts[1].trim()));
            this.name = parts[2].trim();
            this.email = parts[3].trim();
            this.departmentId = parts[4].trim();
            this.employmentType = EmployeeType.FULLTIME;
            this.baseSalary = getBaseSalary();
        }
    }

    public abstract double calculateSalary(
            AttendanceRecord attendance,
            PayrollRule rule);
}
