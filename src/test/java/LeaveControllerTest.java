import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LeaveController Unit Tests")
public class LeaveControllerTest {

    private File tempEmployeesFile;
    private File tempBalancesFile;
    private File tempRequestsFile;

    private EmployeeRepository employeeRepo;
    private LeaveBalanceRepository leaveBalanceRepo;
    private LeaveRequestRepository leaveRequestRepo;
    private LeaveController leaveController;

    @BeforeEach
    public void setUp() throws Exception {
        tempEmployeesFile = File.createTempFile("test_employees_", ".csv");
        tempEmployeesFile.deleteOnExit();
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempEmployeesFile))) {
            writer.println("id,version,name,email,departmentId,employmentType,baseSalary");
            writer.println("E0001,1,John Smith,john@company.com,D001,FULLTIME,4000.0");
        }

        tempBalancesFile = File.createTempFile("test_balances_", ".csv");
        tempBalancesFile.deleteOnExit();
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempBalancesFile))) {
            writer.println("balanceId,employeeId,leaveType,totalLeaveDays,usedLeaveDays,remainingLeaveDays,version");
            writer.println("LB_E0001_ANNUAL,E0001,ANNUAL,12,0,12,0");
            writer.println("LB_E0001_SICK,E0001,SICK,6,0,6,0");
        }

        tempRequestsFile = File.createTempFile("test_requests_", ".csv");
        tempRequestsFile.deleteOnExit();
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempRequestsFile))) {
            writer.println("leaveId,employeeId,leaveType,startDate,endDate,reason,status,approvedBy");
        }

        employeeRepo = new EmployeeRepository(tempEmployeesFile.getAbsolutePath());
        leaveBalanceRepo = new LeaveBalanceRepository(tempBalancesFile.getAbsolutePath());
        leaveRequestRepo = new LeaveRequestRepository(tempRequestsFile.getAbsolutePath());

        leaveController = new LeaveController(leaveRequestRepo, leaveBalanceRepo, employeeRepo);
    }

    @Test
    @DisplayName("Submit leave request successfully")
    public void testSubmitLeaveRequestSuccess() {
        LocalDate start = LocalDate.of(2023, 5, 1);
        LocalDate end = LocalDate.of(2023, 5, 3); // 3 days

        LeaveRequest req = leaveController.submit("E0001", LeaveType.ANNUAL, start, end, "Vacation");
        assertNotNull(req);
        assertEquals("E0001", req.getEmployeeId());
        assertEquals(LeaveType.ANNUAL, req.getLeaveType());
        assertEquals(start, req.getStartDate());
        assertEquals(end, req.getEndDate());
        assertEquals(3, req.getDays());
        assertEquals("Vacation", req.getReason());
        assertEquals(LeaveStatus.PENDING, req.getStatus());

        // Check if saved to repo
        LeaveRequest saved = leaveRequestRepo.findById(req.getLeaveId());
        assertNotNull(saved);
        assertEquals(LeaveStatus.PENDING, saved.getStatus());
    }

    @Test
    @DisplayName("Submit request should fail for non-existing employee")
    public void testSubmitLeaveRequestEmployeeNotFound() {
        LocalDate start = LocalDate.of(2023, 5, 1);
        LocalDate end = LocalDate.of(2023, 5, 3);

        assertThrows(EmployeeNotFoundException.class, () -> {
            leaveController.submit("E9999", LeaveType.ANNUAL, start, end, "Vacation");
        });
    }

    @Test
    @DisplayName("Submit request should fail for invalid date range")
    public void testSubmitLeaveRequestInvalidDates() {
        LocalDate start = LocalDate.of(2023, 5, 5);
        LocalDate end = LocalDate.of(2023, 5, 1); // Start after End

        assertThrows(IllegalArgumentException.class, () -> {
            leaveController.submit("E0001", LeaveType.ANNUAL, start, end, "Vacation");
        });
    }

    @Test
    @DisplayName("Submit request should fail if remaining balance is insufficient")
    public void testSubmitLeaveRequestInsufficientBalance() {
        LocalDate start = LocalDate.of(2023, 5, 1);
        LocalDate end = LocalDate.of(2023, 5, 15); // 15 days, balance is only 12

        assertThrows(InsufficientLeaveBalanceException.class, () -> {
            leaveController.submit("E0001", LeaveType.ANNUAL, start, end, "Vacation");
        });
    }

    @Test
    @DisplayName("Approve request successfully and deduct balance")
    public void testApproveRequestSuccess() {
        LocalDate start = LocalDate.of(2023, 5, 1);
        LocalDate end = LocalDate.of(2023, 5, 4); // 4 days
        LeaveRequest req = leaveController.submit("E0001", LeaveType.ANNUAL, start, end, "Vacation");

        boolean approved = leaveController.approve(req.getLeaveId(), "MGR001", LockMechanism.SYNCHRONIZED);
        assertTrue(approved);

        // Verify status updated in repository
        LeaveRequest updatedReq = leaveRequestRepo.findById(req.getLeaveId());
        assertEquals(LeaveStatus.APPROVED, updatedReq.getStatus());
        assertEquals("MGR001", updatedReq.getApprovedBy());

        // Verify balance deducted
        LeaveBalance balance = leaveBalanceRepo.findByEmployeeAndType("E0001", LeaveType.ANNUAL);
        assertEquals(8, balance.getRemainingLeaveDays());
        assertEquals(4, balance.getUsedLeaveDays());
    }

    @Test
    @DisplayName("Approve request fails if balance is no longer sufficient at approval time")
    public void testApproveRequestFailsDueToBalance() {
        LocalDate start = LocalDate.of(2023, 5, 1);
        LocalDate end = LocalDate.of(2023, 5, 10); // 10 days
        LeaveRequest req1 = leaveController.submit("E0001", LeaveType.ANNUAL, start, end, "Vacation 1");

        LocalDate start2 = LocalDate.of(2023, 5, 15);
        LocalDate end2 = LocalDate.of(2023, 5, 19); // 5 days
        LeaveRequest req2 = leaveController.submit("E0001", LeaveType.ANNUAL, start2, end2, "Vacation 2");

        // Total balance is 12.
        // Approve req1 (10 days) -> leaves 2.
        assertTrue(leaveController.approve(req1.getLeaveId(), "MGR001", LockMechanism.SYNCHRONIZED));

        // Try to approve req2 (5 days) -> should fail because only 2 days remain
        assertThrows(InsufficientLeaveBalanceException.class, () -> {
            leaveController.approve(req2.getLeaveId(), "MGR001", LockMechanism.SYNCHRONIZED);
        });

        // Request 2 status should remain PENDING
        LeaveRequest savedReq2 = leaveRequestRepo.findById(req2.getLeaveId());
        assertEquals(LeaveStatus.PENDING, savedReq2.getStatus());
    }

    @Test
    @DisplayName("Reject request successfully without deducting balance")
    public void testRejectRequestSuccess() {
        LocalDate start = LocalDate.of(2023, 5, 1);
        LocalDate end = LocalDate.of(2023, 5, 4); // 4 days
        LeaveRequest req = leaveController.submit("E0001", LeaveType.ANNUAL, start, end, "Vacation");

        boolean rejected = leaveController.reject(req.getLeaveId(), "MGR001");
        assertTrue(rejected);

        // Verify status in repository
        LeaveRequest updatedReq = leaveRequestRepo.findById(req.getLeaveId());
        assertEquals(LeaveStatus.REJECTED, updatedReq.getStatus());
        assertEquals("MGR001", updatedReq.getApprovedBy());

        // Verify balance remains unchanged (12 remaining, 0 used)
        LeaveBalance balance = leaveBalanceRepo.findByEmployeeAndType("E0001", LeaveType.ANNUAL);
        assertEquals(12, balance.getRemainingLeaveDays());
        assertEquals(0, balance.getUsedLeaveDays());
    }

    @Test
    @DisplayName("Approval or rejection fails on already processed request")
    public void testApproveOrRejectProcessedRequestFails() {
        LocalDate start = LocalDate.of(2023, 5, 1);
        LocalDate end = LocalDate.of(2023, 5, 2); // 2 days
        LeaveRequest req = leaveController.submit("E0001", LeaveType.ANNUAL, start, end, "Vacation");

        // Reject it first
        assertTrue(leaveController.reject(req.getLeaveId(), "MGR001"));

        // Try to approve -> should throw InvalidLeaveStateException
        assertThrows(InvalidLeaveStateException.class, () -> {
            leaveController.approve(req.getLeaveId(), "MGR001", LockMechanism.SYNCHRONIZED);
        });

        // Try to reject again -> should throw InvalidLeaveStateException
        assertThrows(InvalidLeaveStateException.class, () -> {
            leaveController.reject(req.getLeaveId(), "MGR001");
        });
    }
}
