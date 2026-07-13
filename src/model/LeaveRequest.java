import java.time.LocalDate;

public class LeaveRequest extends BaseEntity {

    // ── Từ diagram ──────────────────────────────
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;

    // ── Thêm cho dự án ──────────────────────────
    private String employeeId;
    private String approvedBy;
    private int paidLeaveDays;
    private int unpaidLeaveDays;

    // ==================== CONSTRUCTORS ====================

    public LeaveRequest() {
        super(null, 0);
        this.status = LeaveStatus.PENDING;
    }

    public LeaveRequest(String leaveId,
            LeaveType leaveType,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            LeaveStatus status) {
        super(leaveId, 0);
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
    }

    public LeaveRequest(String leaveId, String employeeId,
            LeaveType leaveType,
            LocalDate startDate, LocalDate endDate,
            String reason) {
        super(leaveId, 0);
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = LeaveStatus.PENDING;
    }

    // ==================== GETTERS — từ diagram ====================

    public String getLeaveId() {
        return getId();
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getReason() {
        return reason;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    // ==================== SETTERS — từ diagram ====================

    public void setLeaveId(String leaveId) {
        setId(leaveId);
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    // ==================== GETTERS/SETTERS — thêm cho dự án ====================

    public String getEmployeeId() {
        return employeeId;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public int getPaidLeaveDays() { return paidLeaveDays; }
    public void setPaidLeaveDays(int paidLeaveDays) { this.paidLeaveDays = paidLeaveDays; }
    public int getUnpaidLeaveDays() { return unpaidLeaveDays; }
    public void setUnpaidLeaveDays(int unpaidLeaveDays) { this.unpaidLeaveDays = unpaidLeaveDays; }

    // ==================== BUSINESS METHODS — từ diagram ====================

    public void approve() {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be approved. Current: " + this.status);
        }
        this.status = LeaveStatus.APPROVED;
    }

    public void reject() {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be rejected. Current: " + this.status);
        }
        this.status = LeaveStatus.REJECTED;
    }

    public int getDays() {
        if (startDate == null || endDate == null) return 0;
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    // ==================== CSV ====================

    public String getCsvHeader() {
        return "leaveId,employeeId,leaveType,startDate,endDate,reason,status,approvedBy,paidLeaveDays,unpaidLeaveDays";
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId() != null ? getId() : "",
                employeeId != null ? employeeId : "",
                leaveType != null ? leaveType.name() : "",
                startDate != null ? startDate.toString() : "",
                endDate != null ? endDate.toString() : "",
                reason != null ? reason.replace(",", ";") : "",
                status != null ? status.name() : "",
                approvedBy != null ? approvedBy : "",
                String.valueOf(paidLeaveDays),
                String.valueOf(unpaidLeaveDays));
    }

    @Override
    public void fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        if (p.length >= 8) {
            setId(p[0].trim());
            this.employeeId = p[1].trim();
            this.leaveType = LeaveType.valueOf(p[2].trim());
            this.startDate = p[3].trim().isEmpty() ? null : LocalDate.parse(p[3].trim());
            this.endDate = p[4].trim().isEmpty() ? null : LocalDate.parse(p[4].trim());
            this.reason = p[5].trim().replace(";", ",");
            this.status = LeaveStatus.valueOf(p[6].trim());
            this.approvedBy = p[7].trim().isEmpty() ? null : p[7].trim();
            this.paidLeaveDays = p.length >= 9 && !p[8].trim().isEmpty() ? Integer.parseInt(p[8].trim()) : 0;
            this.unpaidLeaveDays = p.length >= 10 && !p[9].trim().isEmpty() ? Integer.parseInt(p[9].trim()) : 0;
        }
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "leaveId='" + getId() + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", leaveType=" + leaveType +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", approvedBy='" + approvedBy + '\'' +
                '}';
    }
}
