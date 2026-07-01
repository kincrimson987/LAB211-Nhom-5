import java.util.Locale;

public class AttendanceAdjustmentRequest extends BaseEntity {
    private String employeeId;
    private String yearMonth;
    private int originalWorkDays;
    private int requestedWorkDays;
    private double originalOvertimeHours;
    private double requestedOvertimeHours;
    private String reason;
    private AttendanceAdjustmentStatus status;
    private String reviewedBy;
    private String reviewNote;

    public AttendanceAdjustmentRequest() {
        this.status = AttendanceAdjustmentStatus.PENDING;
    }

    public AttendanceAdjustmentRequest(String id, long version, String employeeId, String yearMonth,
                                      int originalWorkDays, int requestedWorkDays,
                                      double originalOvertimeHours, double requestedOvertimeHours,
                                      String reason, AttendanceAdjustmentStatus status,
                                      String reviewedBy, String reviewNote) {
        super(id, version);
        this.employeeId = employeeId;
        this.yearMonth = yearMonth;
        this.originalWorkDays = originalWorkDays;
        this.requestedWorkDays = requestedWorkDays;
        this.originalOvertimeHours = originalOvertimeHours;
        this.requestedOvertimeHours = requestedOvertimeHours;
        this.reason = reason;
        this.status = status != null ? status : AttendanceAdjustmentStatus.PENDING;
        this.reviewedBy = reviewedBy;
        this.reviewNote = reviewNote;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public int getOriginalWorkDays() {
        return originalWorkDays;
    }

    public void setOriginalWorkDays(int originalWorkDays) {
        this.originalWorkDays = originalWorkDays;
    }

    public int getRequestedWorkDays() {
        return requestedWorkDays;
    }

    public void setRequestedWorkDays(int requestedWorkDays) {
        this.requestedWorkDays = requestedWorkDays;
    }

    public double getOriginalOvertimeHours() {
        return originalOvertimeHours;
    }

    public void setOriginalOvertimeHours(double originalOvertimeHours) {
        this.originalOvertimeHours = originalOvertimeHours;
    }

    public double getRequestedOvertimeHours() {
        return requestedOvertimeHours;
    }

    public void setRequestedOvertimeHours(double requestedOvertimeHours) {
        this.requestedOvertimeHours = requestedOvertimeHours;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public AttendanceAdjustmentStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceAdjustmentStatus status) {
        this.status = status;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    @Override
    public String toCsvLine() {
        return String.format(Locale.US, "%s,%d,%s,%s,%d,%d,%.1f,%.1f,%s,%s,%s,%s",
                getId(),
                getVersion(),
                employeeId != null ? employeeId : "",
                yearMonth != null ? yearMonth : "",
                originalWorkDays,
                requestedWorkDays,
                originalOvertimeHours,
                requestedOvertimeHours,
                reason != null ? reason.replace(",", ";") : "",
                status != null ? status.name() : AttendanceAdjustmentStatus.PENDING.name(),
                reviewedBy != null ? reviewedBy : "",
                reviewNote != null ? reviewNote.replace(",", ";") : "");
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 12) {
            setId(parts[0].trim());
            setVersion(Long.parseLong(parts[1].trim()));
            employeeId = parts[2].trim();
            yearMonth = parts[3].trim();
            originalWorkDays = Integer.parseInt(parts[4].trim());
            requestedWorkDays = Integer.parseInt(parts[5].trim());
            originalOvertimeHours = Double.parseDouble(parts[6].trim());
            requestedOvertimeHours = Double.parseDouble(parts[7].trim());
            reason = parts[8].trim().replace(";", ",");
            status = AttendanceAdjustmentStatus.valueOf(parts[9].trim());
            reviewedBy = parts[10].trim().isEmpty() ? null : parts[10].trim();
            reviewNote = parts[11].trim().isEmpty() ? null : parts[11].trim().replace(";", ",");
        }
    }
}
