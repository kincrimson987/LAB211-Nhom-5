package model;

import java.time.LocalDate;

public class LeaveRequest {

    private String leaveId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;

    public LeaveRequest() {
        this.status = LeaveStatus.PENDING;
    }

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

    public String getLeaveId() {
        return this.leaveId;
    }

    public void setLeaveId(String leaveId) {
        this.leaveId = leaveId;
    }

    public LeaveType getLeaveType() {
        return this.leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LeaveStatus getStatus() {
        return this.status;
    }

    public void setStatus(LeaveStatus status) {
        this.status = status;
    }

    public void approve() {
        this.status = LeaveStatus.APPROVED;
    }

    public void reject() {
        this.status = LeaveStatus.REJECTED;
    }

    @Override
    public String toString() {
        return "LeaveRequest{" +
                "leaveId='" + this.leaveId + '\'' +
                ", leaveType=" + this.leaveType +
                ", startDate=" + this.startDate +
                ", endDate=" + this.endDate +
                ", reason='" + this.reason + '\'' +
                ", status=" + this.status +
                '}';
    }
}