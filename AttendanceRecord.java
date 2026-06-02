public class AttendanceRecord extends BaseEntity {
    private String employeeId;
    private int workDays;
    private double overtimeHours;

    public AttendanceRecord() {}

    public AttendanceRecord(String id, long version, String employeeId, int workDays, double overtimeHours) {
        super(id, version);
        this.employeeId = employeeId;
        this.workDays = workDays;
        this.overtimeHours = overtimeHours;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public int getWorkDays() { return workDays; }
    public void setWorkDays(int workDays) { this.workDays = workDays; }

    public double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }

    @Override
    public String toCsvLine() {
        return String.format("%s,%d,%s,%d,%.1f", id, version, employeeId, workDays, overtimeHours);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 5) {
            this.id = parts[0];
            this.version = Long.parseLong(parts[1]);
            this.employeeId = parts[2];
            this.workDays = Integer.parseInt(parts[3]);
            this.overtimeHours = Double.parseDouble(parts[4]);
        }
    }
}
