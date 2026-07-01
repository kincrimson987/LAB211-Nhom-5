import java.time.LocalDate;
import java.util.List;

public class AttendanceController {

    private final AttendanceRepository attendanceRepo;
    private final EmployeeRepository employeeRepo;

    public AttendanceController(AttendanceRepository attendanceRepo,
                                EmployeeRepository employeeRepo) {
        this.attendanceRepo = attendanceRepo;
        this.employeeRepo = employeeRepo;
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

    public AttendanceRecord submitAdjustmentRequest(String employeeId, String yearMonth,
                                                    String reason, int newWorkDays,
                                                    double newOvertime) {
        validateEmployee(employeeId);
        AttendanceRecord record = attendanceRepo.findByEmployeeAndMonth(employeeId, yearMonth);
        if (record == null) {
            throw new IllegalArgumentException("Khong co du lieu attendance cho thang " + yearMonth + ".");
        }
        if (newWorkDays >= 0) {
            record.setWorkDays(newWorkDays);
        }
        if (newOvertime >= 0) {
            record.setOvertimeHours(newOvertime);
        }
        record.setVersion(record.getVersion() + 1);
        saveOrUpdate(record);
        System.out.println("[Adjustment] " + employeeId + " / " + yearMonth + " - " + reason);
        return record;
    }

    public List<AttendanceRecord> getPendingAdjustments() {
        return attendanceRepo.findAll();
    }

    public void approveAdjustment(String recordId, String approverId) {
        AttendanceRecord record = attendanceRepo.findById(recordId);
        if (record == null) {
            throw new IllegalArgumentException("Attendance record not found: " + recordId);
        }
        attendanceRepo.update(record);
        System.out.println("[Approved] " + recordId + " by " + approverId);
    }

    public void rejectAdjustment(String recordId, String approverId, String reason) {
        AttendanceRecord record = attendanceRepo.findById(recordId);
        if (record == null) {
            throw new IllegalArgumentException("Attendance record not found: " + recordId);
        }
        System.out.println("[Rejected] " + recordId + " by " + approverId + " - " + reason);
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
}
