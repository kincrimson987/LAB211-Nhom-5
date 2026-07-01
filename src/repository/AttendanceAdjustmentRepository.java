import java.util.List;
import java.util.stream.Collectors;

public class AttendanceAdjustmentRepository extends CsvRepository<AttendanceAdjustmentRequest> {

    public AttendanceAdjustmentRepository() {
        super("data/attendance_adjustments.csv");
    }

    public AttendanceAdjustmentRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "id,version,employeeId,yearMonth,originalWorkDays,requestedWorkDays,originalOvertimeHours,requestedOvertimeHours,reason,status,reviewedBy,reviewNote";
    }

    @Override
    public String getId(AttendanceAdjustmentRequest entity) {
        return entity.getId();
    }

    @Override
    public String toLine(AttendanceAdjustmentRequest entity) {
        return entity.toCsvLine();
    }

    @Override
    public AttendanceAdjustmentRequest parseLine(String line) {
        AttendanceAdjustmentRequest request = new AttendanceAdjustmentRequest();
        request.fromCsvLine(line);
        return request;
    }

    public List<AttendanceAdjustmentRequest> findByEmployee(String employeeId) {
        return findAll().stream()
                .filter(request -> employeeId.equals(request.getEmployeeId()))
                .collect(Collectors.toList());
    }

    public List<AttendanceAdjustmentRequest> findByStatus(AttendanceAdjustmentStatus status) {
        return findAll().stream()
                .filter(request -> request.getStatus() == status)
                .collect(Collectors.toList());
    }
}
