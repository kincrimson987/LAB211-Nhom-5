public class Employee extends BaseEntity {
    private String name;
    private String email;
    private String departmentId;
    private Enums.EmploymentType employmentType = Enums.EmploymentType.FULLTIME;
    private double baseSalary = 0.0; // monthly base salary

    public Employee() {
    }

    public Employee(String id, long version, String name, String email, String departmentId) {
        super(id, version);
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
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

    public Enums.EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(Enums.EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public double getBaseSalary() {
        if (baseSalary <= 0.0) {
            return employmentType == Enums.EmploymentType.FULLTIME ? 4000.0 : 2000.0;
        }
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%d,%s,%s,%s", id, version, name, email, departmentId);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 5) {
            this.id = parts[0];
            this.version = Long.parseLong(parts[1]);
            this.name = parts[2];
            this.email = parts[3];
            this.departmentId = parts[4];
        }
    }
}
