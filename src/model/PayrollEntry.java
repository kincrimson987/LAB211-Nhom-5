public class PayrollEntry extends BaseEntity {
    private String employeeId;
    private double netSalary;
    private Enums.PayrollStatus status;

    public PayrollEntry() {}

    public PayrollEntry(String id, long version, String employeeId, double netSalary, Enums.PayrollStatus status) {
        super(id, version);
        this.employeeId = employeeId;
        this.netSalary = netSalary;
        this.status = status;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public double getNetSalary() { return netSalary; }
    public void setNetSalary(double netSalary) { this.netSalary = netSalary; }

    public Enums.PayrollStatus getStatus() { return status; }
    public void setStatus(Enums.PayrollStatus status) { this.status = status; }

    @Override
    public String toCsvLine() {
        return String.format("%s,%d,%s,%.2f,%s", id, version, employeeId, netSalary, status);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 5) {
            this.id = parts[0];
            this.version = Long.parseLong(parts[1]);
            this.employeeId = parts[2];
            this.netSalary = Double.parseDouble(parts[3]);
            this.status = Enums.PayrollStatus.valueOf(parts[4]);
        }
    }
}
