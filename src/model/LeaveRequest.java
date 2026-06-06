
import java.time.LocalDate;

public class LeaveRequest {

    // ── Từ diagram ──────────────────────────────
    private String leaveId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;

    // ── Thêm cho dự án ──────────────────────────
    private String employeeId; // biết đơn này của nhân viên nào
    private String approvedBy; // hrId đã duyệt/từ chối

    // ==================== CONSTRUCTORS ====================

    /** Từ diagram — status mặc định PENDING */
    public LeaveRequest() {
        this.status = LeaveStatus.PENDING;
    }

    /** Từ diagram — giữ nguyên */
    public LeaveRequest(String leaveId,
            LeaveType leaveType,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            LeaveStatus status) {
        this.leaveId = leaveId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
    }

    /** Thêm — constructor đầy đủ cho dự án */
    public LeaveRequest(String leaveId, String employeeId,
            LeaveType leaveType,
            LocalDate startDate, LocalDate endDate,
            String reason) {
        this.leaveId = leaveId;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = LeaveStatus.PENDING;
    }

    // ==================== GETTERS — từ diagram ====================

    public String getLeaveId() {
        return leaveId;
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
        this.leaveId = leaveId;
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

    // ==================== BUSINESS METHODS — từ diagram ====================

    /** Từ diagram — giữ nguyên, thêm ghi approvedBy */
    public void approve() {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be approved. Current: " + this.status);
        }
        this.status = LeaveStatus.APPROVED;
    }

    /** Từ diagram — giữ nguyên, thêm ghi approvedBy */
    public void reject() {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING requests can be rejected. Current: " + this.status);
        }
        this.status = LeaveStatus.REJECTED;
    }

    /** Thêm — tính số ngày nghỉ */
    public int getDays() {
        if (startDate == null || endDate == null)
            return 0;
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    // ==================== CSV — thêm cho dự án ====================

    public String getCsvHeader() {
        return "leaveId,employeeId,leaveType,startDate,endDate,reason,status,approvedBy";
    }

    public String toCsvLine() {
        return String.join(",",
                leaveId,
                employeeId != null ? employeeId : "",
                leaveType != null ? leaveType.name() : "",
                startDate != null ? startDate.toString() : "",
                endDate != null ? endDate.toString() : "",
                reason != null ? reason.replace(",", ";") : "",
                status != null ? status.name() : "",
                approvedBy != null ? approvedBy : "");
    }

    public static LeaveRequest fromCsvLine(String line) {
        String[] p = line.split(",");
        LeaveRequest lr = new LeaveRequest();
        lr.leaveId = p[0].trim();
        lr.employeeId = p[1].trim();
        lr.leaveType = LeaveType.valueOf(p[2].trim());
        lr.startDate = p[3].trim().isEmpty() ? null : LocalDate.parse(p[3].trim());
        lr.endDate = p[4].trim().isEmpty() ? null : LocalDate.parse(p[4].trim());
        lr.reason = p[5].trim().replace(";", ",");
        lr.status = LeaveStatus.valueOf(p[6].trim());
        lr.approvedBy = p[7].trim().isEmpty() ? null : p[7].trim();
        return lr;
    }

    // ==================== toString — từ diagram ====================

    /** Từ diagram — giữ nguyên code bạn */
    @Override
    public String toString() {
        return "LeaveRequest{" +
                "leaveId='" + leaveId + '\'' +
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