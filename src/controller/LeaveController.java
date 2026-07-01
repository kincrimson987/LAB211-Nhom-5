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

    public LeaveRequest submit(String employeeId, LeaveType leaveType,
                               LocalDate startDate, LocalDate endDate,
                               String reason) {
        Employee emp = employeeRepo.findById(employeeId);
        if (emp == null) {
            throw new EmployeeNotFoundException("Khong tim thay nhan vien voi ma: " + employeeId);
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngay bat dau va ngay ket thuc khong duoc de trong.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngay bat dau phai truoc hoac bang ngay ket thuc.");
        }

        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;

        if (leaveType == LeaveType.ANNUAL || leaveType == LeaveType.SICK) {
            LeaveBalance balance = leaveBalanceRepo.findByEmployeeAndType(employeeId, leaveType);
            if (balance == null) {
                throw new IllegalArgumentException("Khong tim thay so du nghi phep cho loai: " + leaveType);
            }
            if (balance.getRemainingLeaveDays() < days) {
                throw new InsufficientLeaveBalanceException(
                        String.format("Khong du ngay nghi phep. Can: %d, Con lai: %d",
                                days, balance.getRemainingLeaveDays()));
            }
        }

        String monthStr = String.format("%02d", startDate.getMonthValue());
        String yearStr = String.valueOf(startDate.getYear());
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String leaveId = String.format("LR_%s_%s_%s_%s", employeeId, monthStr, yearStr, uniqueSuffix);

        LeaveRequest request = new LeaveRequest(leaveId, employeeId, leaveType, startDate, endDate, reason);
        leaveRequestRepo.save(request);
        return request;
    }

    public boolean approve(String leaveId, String approvedBy, LockMechanism lockMechanism) {
        LeaveRequest request = leaveRequestRepo.findById(leaveId);
        if (request == null) {
            throw new IllegalArgumentException("Khong tim thay yeu cau nghi phep voi ma: " + leaveId);
        }

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStateException(
                    "Chi co the phe duyet yeu cau trang thai PENDING. Trang thai hien tai: " + request.getStatus());
        }

        int days = request.getDays();
        String employeeId = request.getEmployeeId();
        LeaveType leaveType = request.getLeaveType();

        boolean deductSuccess = false;
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
            deductSuccess = true;
        }

        if (!deductSuccess) {
            throw new InsufficientLeaveBalanceException(
                    "Phe duyet that bai: Nhan vien khong du ngay nghi phep hoac co xung dot dong thoi.");
        }

        return leaveRequestRepo.approveRequest(leaveId, approvedBy);
    }

    public boolean reject(String leaveId, String approvedBy) {
        LeaveRequest request = leaveRequestRepo.findById(leaveId);
        if (request == null) {
            throw new IllegalArgumentException("Khong tim thay yeu cau nghi phep voi ma: " + leaveId);
        }

        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveStateException(
                    "Chi co the tu choi yeu cau trang thai PENDING. Trang thai hien tai: " + request.getStatus());
        }

        return leaveRequestRepo.rejectRequest(leaveId, approvedBy);
    }

    public List<LeaveRequest> getAllRequests() {
        return leaveRequestRepo.findAll();
    }

    public List<LeaveRequest> getRequestsByEmployee(String employeeId) {
        return leaveRequestRepo.findByEmployee(employeeId);
    }

    public List<LeaveRequest> getPendingRequests() {
        return leaveRequestRepo.findPending();
    }

    public List<LeaveBalance> getBalancesByEmployee(String employeeId) {
        return leaveBalanceRepo.findByEmployee(employeeId);
    }
}
