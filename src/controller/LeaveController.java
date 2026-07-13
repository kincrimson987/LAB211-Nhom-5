import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LeaveController {
    private static final LocalDate LEAVE_SYSTEM_START_DATE = LocalDate.of(2026, 7, 1);
    private static final int PAID_LEAVE_DAYS_PER_YEAR = 18;
    private static final LeaveType SHARED_BALANCE_TYPE = LeaveType.PAID_LEAVE;

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
        if (leaveType == LeaveType.UNPAID) {
            throw new IllegalArgumentException("He thong hien tai khong su dung loai nghi UNPAID.");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngay bat dau va ngay ket thuc khong duoc de trong.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngay bat dau phai truoc hoac bang ngay ket thuc.");
        }
        if (startDate.isBefore(LEAVE_SYSTEM_START_DATE)) {
            throw new IllegalArgumentException("Chi duoc xin nghi tu ngay " + LEAVE_SYSTEM_START_DATE + " tro di.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Khong the xin nghi trong qua khu. Ngay bat dau phai tu hom nay tro di.");
        }

        ensureDefaultBalances(employeeId);
        int chargeableDays = calculateChargeableDays(employeeId, startDate, endDate, null, false);

        if (leaveType == LeaveType.ANNUAL || leaveType == LeaveType.SICK) {
            LeaveBalance balance = leaveBalanceRepo.findByEmployeeAndType(employeeId, SHARED_BALANCE_TYPE);
            if (balance == null) {
                throw new IllegalArgumentException("Khong tim thay so du nghi phep cho loai: " + leaveType);
            }
            // The request may exceed the paid balance. The excess is recorded as unpaid
            // when HR approves it instead of rejecting the whole request.
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

        String employeeId = request.getEmployeeId();
        LeaveType leaveType = request.getLeaveType();
        ensureDefaultBalances(employeeId);
        int chargeableDays = calculateChargeableDays(
                employeeId,
                request.getStartDate(),
                request.getEndDate(),
                leaveId,
                true);

        int paidLeaveDays = 0;
        int unpaidLeaveDays = chargeableDays;
        boolean deductSuccess = false;
        if (leaveType == LeaveType.ANNUAL || leaveType == LeaveType.SICK) {
            LeaveBalance balance = leaveBalanceRepo.findByEmployeeAndType(employeeId, SHARED_BALANCE_TYPE);
            paidLeaveDays = Math.min(chargeableDays, balance.getRemainingLeaveDays());
            unpaidLeaveDays = chargeableDays - paidLeaveDays;
            if (paidLeaveDays == 0) {
                deductSuccess = true;
            } else {
            switch (lockMechanism) {
                case NO_LOCK:
                    deductSuccess = leaveBalanceRepo.deductWithNoLock(employeeId, SHARED_BALANCE_TYPE, paidLeaveDays);
                    break;
                case SYNCHRONIZED:
                    deductSuccess = leaveBalanceRepo.deductWithSync(employeeId, SHARED_BALANCE_TYPE, paidLeaveDays);
                    break;
                case OPTIMISTIC_LOCKING:
                    deductSuccess = leaveBalanceRepo.deductWithOptimistic(employeeId, SHARED_BALANCE_TYPE, paidLeaveDays);
                    break;
                case FILE_LOCK:
                    deductSuccess = leaveBalanceRepo.deductWithFileLock(employeeId, SHARED_BALANCE_TYPE, paidLeaveDays);
                    break;
                default:
                    deductSuccess = leaveBalanceRepo.deductWithSync(employeeId, SHARED_BALANCE_TYPE, paidLeaveDays);
            }
            }
        } else {
            deductSuccess = true;
        }

        if (!deductSuccess) {
            throw new InsufficientLeaveBalanceException(
                    "Phe duyet that bai: Nhan vien khong du ngay nghi phep hoac co xung dot dong thoi.");
        }

        request.setPaidLeaveDays(paidLeaveDays);
        request.setUnpaidLeaveDays(unpaidLeaveDays);
        request.approve();
        request.setApprovedBy(approvedBy);
        leaveRequestRepo.update(request);
        return true;
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

    public LeaveRequest getRequestById(String leaveId) {
        return leaveRequestRepo.findById(leaveId);
    }

    public List<LeaveRequest> getRequestsByEmployee(String employeeId) {
        return leaveRequestRepo.findByEmployee(employeeId);
    }

    public List<LeaveRequest> getPendingRequests() {
        return leaveRequestRepo.findPending();
    }

    public List<LeaveBalance> getBalancesByEmployee(String employeeId) {
        ensureDefaultBalances(employeeId);
        return leaveBalanceRepo.findByEmployee(employeeId);
    }

    public int previewChargeableDays(String employeeId, LocalDate startDate, LocalDate endDate) {
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
        if (startDate.isBefore(LEAVE_SYSTEM_START_DATE)) {
            throw new IllegalArgumentException("Chi duoc xin nghi tu ngay " + LEAVE_SYSTEM_START_DATE + " tro di.");
        }
        return calculateChargeableDays(employeeId, startDate, endDate, null, false);
    }

    public int previewApprovalChargeableDays(String leaveId) {
        LeaveRequest request = leaveRequestRepo.findById(leaveId);
        if (request == null) {
            throw new IllegalArgumentException("Khong tim thay yeu cau nghi phep voi ma: " + leaveId);
        }
        return calculateChargeableDays(
                request.getEmployeeId(),
                request.getStartDate(),
                request.getEndDate(),
                leaveId,
                true);
    }

    private void ensureDefaultBalances(String employeeId) {
        if (leaveBalanceRepo.findByEmployeeAndType(employeeId, SHARED_BALANCE_TYPE) == null) {
            leaveBalanceRepo.save(new LeaveBalance(
                    "LB_" + employeeId + "_PAID_LEAVE",
                    employeeId,
                    SHARED_BALANCE_TYPE,
                    PAID_LEAVE_DAYS_PER_YEAR));
        }
    }

    private int calculateChargeableDays(String employeeId,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        String excludeLeaveId,
                                        boolean approvedOnly) {
        Set<LocalDate> requestedDays = new HashSet<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            requestedDays.add(date);
        }

        List<LeaveRequest> existingRequests = leaveRequestRepo.findByEmployee(employeeId);
        for (LeaveRequest existing : existingRequests) {
            if (excludeLeaveId != null && excludeLeaveId.equals(existing.getLeaveId())) {
                continue;
            }
            if (existing.getStatus() == LeaveStatus.REJECTED) {
                continue;
            }
            if (approvedOnly && existing.getStatus() != LeaveStatus.APPROVED) {
                continue;
            }

            LocalDate overlapStart = existing.getStartDate().isAfter(startDate)
                    ? existing.getStartDate()
                    : startDate;
            LocalDate overlapEnd = existing.getEndDate().isBefore(endDate)
                    ? existing.getEndDate()
                    : endDate;

            if (overlapStart.isAfter(overlapEnd)) {
                continue;
            }

            for (LocalDate date = overlapStart; !date.isAfter(overlapEnd); date = date.plusDays(1)) {
                requestedDays.remove(date);
            }
        }

        return requestedDays.size();
    }
}
