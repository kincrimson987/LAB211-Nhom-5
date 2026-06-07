import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository doc/ghi leave_requests.csv.
 * Dung cho xu ly don nghi phep: tim theo employee, status, approve/reject.
 */
public class LeaveRequestRepository extends CsvRepository<LeaveRequest> {

    private static final String DEFAULT_PATH = "data/leave_requests.csv";

    public LeaveRequestRepository() {
        this(DEFAULT_PATH);
    }

    public LeaveRequestRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "leaveId,employeeId,leaveType,startDate,endDate,reason,status,approvedBy";
    }

    @Override
    public String getId(LeaveRequest entity) {
        return entity.getLeaveId();
    }

    @Override
    public String toLine(LeaveRequest entity) {
        return entity.toCsvLine();
    }

    @Override
    public LeaveRequest parseLine(String line) {
        return LeaveRequest.fromCsvLine(line);
    }

    public List<LeaveRequest> findByEmployee(String employeeId) {
        return findAll().stream()
                .filter(request -> request.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    public List<LeaveRequest> findByStatus(LeaveStatus status) {
        return findAll().stream()
                .filter(request -> request.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<LeaveRequest> findPending() {
        return findByStatus(LeaveStatus.PENDING);
    }

    public boolean updateStatus(String leaveId, LeaveStatus newStatus, String approvedBy) {
        LeaveRequest request = findById(leaveId);

        if (request == null) {
            return false;
        }

        request.setStatus(newStatus);
        request.setApprovedBy(approvedBy);
        update(request);
        return true;
    }

    public boolean approveRequest(String leaveId, String approvedBy) {
        LeaveRequest request = findById(leaveId);

        if (request == null) {
            return false;
        }

        request.approve();
        request.setApprovedBy(approvedBy);
        update(request);
        return true;
    }

    public boolean rejectRequest(String leaveId, String approvedBy) {
        LeaveRequest request = findById(leaveId);

        if (request == null) {
            return false;
        }

        request.reject();
        request.setApprovedBy(approvedBy);
        update(request);
        return true;
    }
}