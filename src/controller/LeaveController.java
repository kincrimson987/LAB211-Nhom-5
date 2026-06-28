import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LeaveController {
    private final LeaveRequestRepository leaveRequestRepo;
    private final LeaveBalanceRepository leaveBalanceRepo;
    private final EmployeeRepository employeeRepo;

    public LeaveController(LeaveRequestRepository leaveRequestRepo, 
                           LeaveBalanceRepository leaveBalanceRepo, 
                           EmployeeRepository employeeRepo) {
        this.leaveRequestRepo = leaveRequestRepo;
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.employeeRepo = employeeRepo;
    }

    /**
     * Submit a new leave request.
     */
    public LeaveRequest submit(String employeeId, LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason) {
        // Validate employee exists
        Employee emp = employeeRepo.findById(employeeId);
        if (emp == null) {
            throw new EmployeeNotFoundException("Không tìm thấy nhân viên với mã: " + employeeId);
        }

        // Validate dates
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        }

        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;

        // Validate leave balance for ANNUAL and SICK types
        if (leaveType == LeaveType.ANNUAL || leaveType == LeaveType.SICK) {
            LeaveBalance balance = leaveBalanceRepo.findByEmployeeAndType(employeeId, leaveType);
            if (balance == null) {
                throw new IllegalArgumentException("Không tìm thấy thông tin số dư nghỉ phép cho loại: " + leaveType);
            }
            if (balance.getRemainingLeaveDays() < days) {
                throw new InsufficientLeaveBalanceException(
                        String.format("Không đủ ngày nghỉ phép. Cần: %d, Còn lại: %d", days, balance.getRemainingLeaveDays()));
            }
        }

        // Generate unique leave ID in standard format (LR_empId_month_year_suffix)
        String monthStr = String.format("%02d", startDate.getMonthValue());
        String yearStr = String.valueOf(startDate.getYear());
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String leaveId = String.format("LR_%s_%s_%s_%s", employeeId, monthStr, yearStr, uniqueSuffix);

        // Create and save request
        LeaveRequest request = new LeaveRequest(leaveId, employeeId, leaveType, startDate, endDate, reason);
        leaveRequestRepo.save(request);
        return request;
    }

    /**
     * Approve a pending leave request with a specific locking mechanism.
     */
    public boolean approve(String leaveId, String approvedBy, LockMechanism lockMechanism) {
        LeaveRequest request = leaveRequestRepo.findById(leaveId);
        if (request == null) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu nghỉ phép với mã: " + leaveId);
        }

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStateException(
                    "Chỉ có thể phê duyệt yêu cầu trạng thái PENDING. Trạng thái hiện tại: " + request.getStatus());
        }

        int days = request.getDays();
        String employeeId = request.getEmployeeId();
        LeaveType leaveType = request.getLeaveType();

        boolean deductSuccess = false;
        
        // Call appropriate locking mechanism in LeaveBalanceRepository
        if (leaveType == LeaveType.ANNUAL || leaveType == LeaveType.SICK) {
            switch (lockMechanism) {
                case NO_LOCK:
                    deductSuccess = leaveBalanceRepo.deductWithNoLock(employeeId, leaveType, days);
                    break;
                case SYNCHRONIZED:
                    deductSuccess = leaveBalanceRepo.deductWithSync(employeeId, leaveType, days);
                    break;
                case OPTIMISTIC_LOCKING:
                    deductSuccess = leaveBalanceRepo.deductWithOptimistic(employeeId, leaveType, days);
                    break;
                case FILE_LOCK:
                    deductSuccess = leaveBalanceRepo.deductWithFileLock(employeeId, leaveType, days);
                    break;
                default:
                    deductSuccess = leaveBalanceRepo.deductWithSync(employeeId, leaveType, days);
            }
        } else {
            // For unpaid or other leave types that don't track a balance limit, they just succeed.
            deductSuccess = true;
        }

        if (!deductSuccess) {
            throw new InsufficientLeaveBalanceException(
                    "Phê duyệt thất bại: Nhân viên không đủ ngày nghỉ phép hoặc có xung đột đồng thời.");
        }

        // Update status of the leave request
        return leaveRequestRepo.approveRequest(leaveId, approvedBy);
    }

    /**
     * Reject a pending leave request.
     */
    public boolean reject(String leaveId, String approvedBy) {
        LeaveRequest request = leaveRequestRepo.findById(leaveId);
        if (request == null) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu nghỉ phép với mã: " + leaveId);
        }

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStateException(
                    "Chỉ có thể từ chối yêu cầu trạng thái PENDING. Trạng thái hiện tại: " + request.getStatus());
        }

        return leaveRequestRepo.rejectRequest(leaveId, approvedBy);
    }

    /**
     * Get all leave requests.
     */
    public List<LeaveRequest> getAllRequests() {
        return leaveRequestRepo.findAll();
    }

    /**
     * Get leave requests for a specific employee.
     */
    public List<LeaveRequest> getRequestsByEmployee(String employeeId) {
        return leaveRequestRepo.findByEmployee(employeeId);
    }

    /**
     * Get pending leave requests.
     */
    public List<LeaveRequest> getPendingRequests() {
        return leaveRequestRepo.findPending();
    }

    /**
     * Get leave balances for an employee.
     */
    public List<LeaveBalance> getBalancesByEmployee(String employeeId) {
        return leaveBalanceRepo.findByEmployee(employeeId);
    }
}
