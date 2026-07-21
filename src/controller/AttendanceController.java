import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AttendanceController {

    private static final int STANDARD_WORK_DAYS = 26;

    private final AttendanceRepository attendanceRepo;
    private final EmployeeRepository employeeRepo;
    private final AttendanceAdjustmentRepository adjustmentRepo;
    private final LeaveRequestRepository leaveRequestRepo;

    public AttendanceController(AttendanceRepository attendanceRepo,
                                EmployeeRepository employeeRepo) {
        this(attendanceRepo, employeeRepo, new AttendanceAdjustmentRepository(),
                new LeaveRequestRepository());
    }

    public AttendanceController(AttendanceRepository attendanceRepo,
                                EmployeeRepository employeeRepo,
                                AttendanceAdjustmentRepository adjustmentRepo) {
        this(attendanceRepo, employeeRepo, adjustmentRepo, new LeaveRequestRepository());
    }

    public AttendanceController(AttendanceRepository attendanceRepo,
                                EmployeeRepository employeeRepo,
                                AttendanceAdjustmentRepository adjustmentRepo,
                                LeaveRequestRepository leaveRequestRepo) {
        this.attendanceRepo = attendanceRepo;
        this.employeeRepo = employeeRepo;
        this.adjustmentRepo = adjustmentRepo;
        this.leaveRequestRepo = leaveRequestRepo;
    }

    public AttendanceRecord checkIn(String employeeId) {
        employeeId = normalizeEmployeeId(employeeId);
        validateEmployee(employeeId);
        LocalDate today = LocalDate.now();
        if (isOnApprovedLeave(employeeId, today)) {
            throw new IllegalStateException("Cannot check in on an approved leave date: " + today);
        }
        String yearMonth = getCurrentYearMonth();
        AttendanceRecord record = getOrCreate(employeeId, yearMonth);
        int newWorkDays = record.getWorkDays() + 1;
        int paidLeaveDays = countPaidLeaveDaysInMonth(employeeId, YearMonth.parse(yearMonth));
        if (newWorkDays + paidLeaveDays > STANDARD_WORK_DAYS) {
            throw new IllegalStateException(
                    "Cannot check in: " + paidLeaveDays + " paid leave day(s) + "
                            + record.getWorkDays() + " work day(s) already reach the monthly limit of "
                            + STANDARD_WORK_DAYS + ".");
        }
        validateWorkDays(yearMonth, newWorkDays);
        record.setWorkDays(newWorkDays);
        record.setVersion(record.getVersion() + 1);
        saveOrUpdate(record);
        return record;
    }

    public AttendanceRecord checkOut(String employeeId, double overtimeHours) {
        employeeId = normalizeEmployeeId(employeeId);
        validateEmployee(employeeId);
        if (overtimeHours < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative.");
        }
        String yearMonth = getCurrentYearMonth();
        AttendanceRecord record = attendanceRepo.findByEmployeeAndMonth(employeeId, yearMonth);
        if (record == null || record.getWorkDays() <= 0) {
            throw new IllegalStateException("Cannot check out before checking in for " + yearMonth + ".");
        }
        if (overtimeHours > 0) {
            record.setOvertimeHours(record.getOvertimeHours() + overtimeHours);
            record.setVersion(record.getVersion() + 1);
            saveOrUpdate(record);
        }
        return record;
    }

    public AttendanceRecord getRecord(String employeeId, String yearMonth) {
        validateWorkDays(yearMonth, 0);
        return attendanceRepo.findByEmployeeAndMonth(normalizeEmployeeId(employeeId), yearMonth);
    }

    public List<AttendanceRecord> getSummaryByMonth(String yearMonth) {
        validateWorkDays(yearMonth, 0);
        return attendanceRepo.findByMonth(yearMonth);
    }

    public List<AttendanceRecord> getRecordsByEmployee(String employeeId) {
        employeeId = normalizeEmployeeId(employeeId);
        validateEmployee(employeeId);
        return attendanceRepo.findByEmployee(employeeId);
    }

    public AttendanceAdjustmentRequest submitAdjustmentRequest(String employeeId, String yearMonth,
                                                               String reason, int newWorkDays,
                                                               double newOvertime) {
        employeeId = normalizeEmployeeId(employeeId);
        validateEmployee(employeeId);
        validateWorkDays(yearMonth, newWorkDays);
        validateMonthlyPayableLimit(employeeId, yearMonth, newWorkDays);
        if (newOvertime < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative.");
        }
        AttendanceRecord record = attendanceRepo.findByEmployeeAndMonth(employeeId, yearMonth);
        if (record == null) {
            throw new IllegalArgumentException("Khong co du lieu attendance cho thang " + yearMonth + ".");
        }
        AttendanceAdjustmentRequest request = new AttendanceAdjustmentRequest(
                buildAdjustmentRequestId(employeeId, yearMonth),
                1L,
                employeeId,
                yearMonth,
                record.getWorkDays(),
                newWorkDays,
                record.getOvertimeHours(),
                newOvertime,
                reason,
                AttendanceAdjustmentStatus.PENDING,
                null,
                null);
        adjustmentRepo.save(request);
        System.out.println("[Adjustment] " + employeeId + " / " + yearMonth + " - " + reason);
        return request;
    }

    public List<AttendanceAdjustmentRequest> getPendingAdjustments() {
        return adjustmentRepo.findByStatus(AttendanceAdjustmentStatus.PENDING);
    }

    public List<AttendanceAdjustmentRequest> getAdjustmentRequestsByEmployee(String employeeId) {
        employeeId = normalizeEmployeeId(employeeId);
        validateEmployee(employeeId);
        return adjustmentRepo.findByEmployee(employeeId);
    }

    public void approveAdjustment(String requestId, String approverId) {
        AttendanceAdjustmentRequest request = adjustmentRepo.findById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Adjustment request not found: " + requestId);
        }
        if (request.getStatus() != AttendanceAdjustmentStatus.PENDING) {
            throw new IllegalStateException("Adjustment request has already been reviewed: " + requestId);
        }
        validateWorkDays(request.getYearMonth(), request.getRequestedWorkDays());
        validateMonthlyPayableLimit(
                request.getEmployeeId(), request.getYearMonth(), request.getRequestedWorkDays());
        if (request.getRequestedOvertimeHours() < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative.");
        }
        AttendanceRecord record = attendanceRepo.findByEmployeeAndMonth(request.getEmployeeId(), request.getYearMonth());
        if (record == null) {
            throw new IllegalArgumentException("Attendance record not found for " + request.getEmployeeId() + " / " + request.getYearMonth());
        }
        record.setWorkDays(request.getRequestedWorkDays());
        record.setOvertimeHours(request.getRequestedOvertimeHours());
        record.setVersion(record.getVersion() + 1);
        attendanceRepo.update(record);
        request.setStatus(AttendanceAdjustmentStatus.APPROVED);
        request.setReviewedBy(approverId);
        request.setReviewNote("Approved");
        request.setVersion(request.getVersion() + 1);
        adjustmentRepo.update(request);
        System.out.println("[Approved] " + requestId + " by " + approverId);
    }

    public void rejectAdjustment(String requestId, String approverId, String reason) {
        AttendanceAdjustmentRequest request = adjustmentRepo.findById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Adjustment request not found: " + requestId);
        }
        if (request.getStatus() != AttendanceAdjustmentStatus.PENDING) {
            throw new IllegalStateException("Adjustment request has already been reviewed: " + requestId);
        }
        request.setStatus(AttendanceAdjustmentStatus.REJECTED);
        request.setReviewedBy(approverId);
        request.setReviewNote(reason);
        request.setVersion(request.getVersion() + 1);
        adjustmentRepo.update(request);
        System.out.println("[Rejected] " + requestId + " by " + approverId + " - " + reason);
    }

    private AttendanceRecord getOrCreate(String employeeId, String yearMonth) {
        AttendanceRecord record = attendanceRepo.findByEmployeeAndMonth(employeeId, yearMonth);
        if (record == null) {
            String id = "ATT_" + employeeId + "_" + yearMonth.replace("-", "_");
            record = new AttendanceRecord(id, 1L, employeeId, yearMonth, 0, 0.0);
        }
        return record;
    }

    private void saveOrUpdate(AttendanceRecord record) {
        if (attendanceRepo.findById(record.getId()) == null) {
            attendanceRepo.save(record);
        } else {
            attendanceRepo.update(record);
        }
    }

    private void validateEmployee(String employeeId) {
        if (employeeRepo.findById(employeeId) == null) {
            throw new IllegalArgumentException("Employee not found: " + employeeId);
        }
    }

    private String normalizeEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty.");
        }
        return employeeId.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String getCurrentYearMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }

    private void validateWorkDays(String yearMonth, int workDays) {
        final YearMonth period;
        try {
            period = YearMonth.parse(yearMonth);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Invalid year-month. Expected YYYY-MM: " + yearMonth);
        }

        int maxDays = period.lengthOfMonth();
        if (workDays < 0 || workDays > maxDays) {
            throw new IllegalArgumentException(
                    "Work days for " + yearMonth + " must be between 0 and " + maxDays + ".");
        }
    }

    private boolean isOnApprovedLeave(String employeeId, LocalDate date) {
        for (LeaveRequest request : leaveRequestRepo.findByEmployee(employeeId)) {
            if (request.getStatus() == LeaveStatus.APPROVED
                    && !date.isBefore(request.getStartDate())
                    && !date.isAfter(request.getEndDate())) {
                return true;
            }
        }
        return false;
    }

    private void validateMonthlyPayableLimit(String employeeId, String yearMonth, int workDays) {
        int paidLeaveDays = countPaidLeaveDaysInMonth(employeeId, YearMonth.parse(yearMonth));
        if (workDays + paidLeaveDays > STANDARD_WORK_DAYS) {
            throw new IllegalArgumentException(
                    "Work days (" + workDays + ") + paid leave days (" + paidLeaveDays
                            + ") cannot exceed " + STANDARD_WORK_DAYS + " for " + yearMonth + ".");
        }
    }

    private int countPaidLeaveDaysInMonth(String employeeId, YearMonth target) {
        int total = 0;
        for (LeaveRequest request : leaveRequestRepo.findByEmployee(employeeId)) {
            if (request.getStatus() != LeaveStatus.APPROVED || request.getPaidLeaveDays() <= 0) {
                continue;
            }
            int paidRemaining = request.getPaidLeaveDays();
            for (LocalDate date = request.getStartDate();
                 !date.isAfter(request.getEndDate()) && paidRemaining > 0;
                 date = date.plusDays(1)) {
                if (YearMonth.from(date).equals(target)) total++;
                paidRemaining--;
            }
        }
        return total;
    }

    private String buildAdjustmentRequestId(String employeeId, String yearMonth) {
        String prefix = "AR_" + employeeId + "_" + yearMonth.replace("-", "_") + "_";
        int next = adjustmentRepo.findAll().stream()
                .map(AttendanceAdjustmentRequest::getId)
                .filter(id -> id != null && id.startsWith(prefix))
                .map(id -> id.substring(prefix.length()))
                .filter(suffix -> suffix.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;
        return prefix + String.format("%03d", next);
    }
}
