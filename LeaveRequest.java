public class LeaveRequest extends BaseEntity {
    private String employeeId;
    private Enums.LeaveType type;
    private int days;
    private Enums.LeaveStatus status;

    public LeaveRequest() {}

    public LeaveRequest(String id, long version, String employeeId, Enums.LeaveType type, int days, Enums.LeaveStatus status) {
        super(id, version);
        this.employeeId = employeeId;
        this.type = type;
        this.days = days;
        this.status = status;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Enums.LeaveType getType() { return type; }
    public void setType(Enums.LeaveType type) { this.type = type; }

    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }

    public Enums.LeaveStatus getStatus() { return status; }
    public void setStatus(Enums.LeaveStatus status) { this.status = status; }

    @Override
    public String toCsvLine() {
        return String.format("%s,%d,%s,%s,%d,%s", id, version, employeeId, type, days, status);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 6) {
            this.id = parts[0];
            this.version = Long.parseLong(parts[1]);
            this.employeeId = parts[2];
            this.type = Enums.LeaveType.valueOf(parts[3]);
            this.days = Integer.parseInt(parts[4]);
            this.status = Enums.LeaveStatus.valueOf(parts[5]);
        }
    }
}
