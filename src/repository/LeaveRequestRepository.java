import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository doc/ghi leave_requests.csv.
 * Dung cho xu ly don nghi phep: tim theo employee, status, approve/reject.
 */
public class LeaveRequestRepository extends CsvRepository<LeaveRequest> {

    /*
     * private static final String DEFAULT_PATH = "data/leave_requests.csv";
     * 
     * public LeaveRequestRepository() {
     * this(DEFAULT_PATH);
     * }
     * 
     * public LeaveRequestRepository(String filePath) {
     * super(filePath);
     * }
     */
    public LeaveRequestRepository() {
        super("data/leave_requests.csv");
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
        String[] p = line.split(",", -1);

        LeaveRequest request = new LeaveRequest();

        if (p.length >= 8) {
            request.setLeaveId(p[0].trim());
            request.setEmployeeId(p[1].trim());
            request.setLeaveType(LeaveType.valueOf(p[2].trim()));

            if (!p[3].trim().isEmpty()) {
                request.setStartDate(LocalDate.parse(p[3].trim()));
            }

            if (!p[4].trim().isEmpty()) {
                request.setEndDate(LocalDate.parse(p[4].trim()));
            }

            request.setReason(p[5].trim().replace(";", ","));
            request.setStatus(LeaveStatus.valueOf(p[6].trim()));

            if (!p[7].trim().isEmpty()) {
                request.setApprovedBy(p[7].trim());
            } else {
                request.setApprovedBy(null);
            }
        }

        return request;
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