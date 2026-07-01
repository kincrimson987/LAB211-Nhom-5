import java.time.LocalDate;
import java.util.List;

public class AttendanceController {

    private final AttendanceRepository attendanceRepo;
    private final EmployeeRepository employeeRepo;
    private final AttendanceAdjustmentRepository adjustmentRepo;

    public AttendanceController(AttendanceRepository attendanceRepo,
                                EmployeeRepository employeeRepo) {
        this(attendanceRepo, employeeRepo, new AttendanceAdjustmentRepository());
    }

    public AttendanceController(AttendanceRepository attendanceRepo,
                                EmployeeRepository employeeRepo,
                                AttendanceAdjustmentRepository adjustmentRepo) {
        this.attendanceRepo = attendanceRepo;
        this.employeeRepo = employeeRepo;
        this.adjustmentRepo = adjustmentRepo;
    }

    public AttendanceRecord checkIn(String employeeId) {
        validateEmployee(employeeId);
        String yearMonth = getCurrentYearMonth();
        AttendanceRecord record = attendanceRepo.findByEmployeeAndMonth(employeeId, yearMonth);
        if (record == null) {
            throw new IllegalArgumentException("Khong co du lieu attendance cho thang " + yearMonth + ".");
        }
        record.setWorkDays(record.getWorkDays() + 1);
        record.setVersion(record.getVersion() + 1);
        saveOrUpdate(record);
        return record;
    }

    public AttendanceRecord checkOut(String employeeId, double overtimeHours) {
        validateEmployee(employeeId);
        if (overtimeHours < 0) {
            throw new IllegalArgumentException("Overtime hours cannot be negative.");
        }
        String yearMonth = getCurrentYearMonth();
        AttendanceRecord record = getOrCreate(employeeId, yearMonth);
        if (overtimeHours > 0) {
            record.setOvertimeHours(record.getOvertimeHours() + overtimeHours);
            record.setVersion(record.getVersion() + 1);
            saveOrUpdate(record);
        }
        return record;
    }

    public AttendanceRecord getRecord(String employeeId, String yearMonth) {
        return attendanceRepo.findByEmployeeAndMonth(employeeId, yearMonth);
    }

    public List<AttendanceRecord> getSummaryByMonth(String yearMonth) {
        return attendanceRepo.findByMonth(yearMonth);
    }

    public List<AttendanceRecord> getRecordsByEmployee(String employeeId) {
        validateEmployee(employeeId);
        return attendanceRepo.findByEmployee(employeeId);
    }

    public AttendanceAdjustmentRequest submitAdjustmentRequest(String employeeId, String yearMonth,
                                                               String reason, int newWorkDays,
                                                               double newOvertime) {
        validateEmployee(employeeId);
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
        validateEmployee(employeeId);
        return adjustmentRepo.findByEmployee(employeeId);
    }

    public void approveAdjustment(String requestId, String approverId) {
        AttendanceAdjustmentRequest request = adjustmentRepo.findById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Adjustment request not found: " + requestId);
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

    private String getCurrentYearMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }

    private String buildAdjustmentRequestId(String employeeId, String yearMonth) {
        int next = adjustmentRepo.findAll().size() + 1;
        return "AR_" + employeeId + "_" + yearMonth.replace("-", "_") + "_" + String.format("%03d", next);
    }
}
