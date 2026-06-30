import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AttendanceController {

    private final AttendanceRepository attendanceRepo;
    private final EmployeeRepository   employeeRepo;

    public AttendanceController(AttendanceRepository attendanceRepo,
                                EmployeeRepository employeeRepo) {
        this.attendanceRepo = attendanceRepo;
        this.employeeRepo   = employeeRepo;
    }

    // ── Check In — thêm 1 ngày công ──────────────────────────

    public AttendanceRecord checkIn(String employeeId) {
        validateEmployee(employeeId);
        String yearMonth = getCurrentYearMonth();
        AttendanceRecord record = getOrCreate(employeeId, yearMonth);
        record.setWorkDays(record.getWorkDays() + 1);
        record.setVersion(record.getVersion() + 1);
        saveOrUpdate(record);
        return record;
    }

    // ── Check Out — ghi nhận giờ tăng ca ─────────────────────

    public AttendanceRecord checkOut(String employeeId, double overtimeHours) {
        validateEmployee(employeeId);
        if (overtimeHours < 0)
            throw new IllegalArgumentException("Overtime hours cannot be negative.");
        String yearMonth = getCurrentYearMonth();
        AttendanceRecord record = getOrCreate(employeeId, yearMonth);
        if (overtimeHours > 0) {
            record.setOvertimeHours(record.getOvertimeHours() + overtimeHours);
            record.setVersion(record.getVersion() + 1);
            saveOrUpdate(record);
        }
        return record;
    }

    // ── View Attendance Record ────────────────────────────────

    public AttendanceRecord getRecord(String employeeId, String yearMonth) {
        return attendanceRepo.findByEmployeeAndMonth(employeeId, yearMonth);
    }

    // ── View Attendance Summary ───────────────────────────────

    public List<AttendanceRecord> getSummaryByMonth(String yearMonth) {
        return attendanceRepo.findByMonth(yearMonth);
    }

    // ── Submit Attendance Adjustment Request ──────────────────

    public AttendanceRecord submitAdjustmentRequest(String employeeId, String yearMonth,
                                                     String reason, int newWorkDays,
                                                     double newOvertime) {
        validateEmployee(employeeId);
        AttendanceRecord record = getOrCreate(employeeId, yearMonth);
        if (newWorkDays >= 0) record.setWorkDays(newWorkDays);
        if (newOvertime >= 0) record.setOvertimeHours(newOvertime);
        record.setVersion(record.getVersion() + 1);
        saveOrUpdate(record);
        System.out.println("[Adjustment] " + employeeId + " / " + yearMonth + " — " + reason);
        return record;
    }

    // ── Review Attendance Adjustment Request ──────────────────

    public List<AttendanceRecord> getPendingAdjustments() {
        return attendanceRepo.findAll();
    }

    // ── Approve Attendance Adjustment ─────────────────────────

    public void approveAdjustment(String recordId, String approverId) {
        AttendanceRecord record = attendanceRepo.findById(recordId);
        if (record == null)
            throw new IllegalArgumentException("Attendance record not found: " + recordId);
        attendanceRepo.update(record);
        System.out.println("[Approved] " + recordId + " by " + approverId);
    }

    // ── Reject Attendance Adjustment ──────────────────────────

    public void rejectAdjustment(String recordId, String approverId, String reason) {
        AttendanceRecord record = attendanceRepo.findById(recordId);
        if (record == null)
            throw new IllegalArgumentException("Attendance record not found: " + recordId);
        System.out.println("[Rejected] " + recordId + " by " + approverId + " — " + reason);
    }

    // ── Helpers ───────────────────────────────────────────────

    private AttendanceRecord getOrCreate(String employeeId, String yearMonth) {
        AttendanceRecord record = attendanceRepo.findByEmployeeAndMonth(employeeId, yearMonth);
        if (record == null) {
            String id = "ATT_" + employeeId + "_"
                    + yearMonth.replace("-", "_");
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
        if (employeeRepo.findById(employeeId) == null)
            throw new IllegalArgumentException("Employee not found: " + employeeId);
    }

    private String getCurrentYearMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
    }
}