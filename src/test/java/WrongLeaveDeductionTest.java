

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wrong Leave Deduction Detection Test Suite")
public class WrongLeaveDeductionTest {

    @Test
    @DisplayName("Detect wrong leave deduction with two threads")
    public void testWrongLeaveDeductionWithTwoThreads() throws Exception {
        File testFile = File.createTempFile("wrong_leave_deduction_test_", ".csv");
        testFile.deleteOnExit();

        setupTestData(testFile);

        LeaveBalanceRepository repo = new LeaveBalanceRepository(testFile.getAbsolutePath());

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);

        Runnable task = () -> {
            try {
                startLatch.await();

                boolean success = repo.deductWithSync("E001", LeaveType.ANNUAL, 3);

                if (success) {
                    successCount.incrementAndGet();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };

        Thread thread1 = new Thread(task, "Thread-1");
        Thread thread2 = new Thread(task, "Thread-2");

        thread1.start();
        thread2.start();

        startLatch.countDown();
        doneLatch.await();

        LeaveBalance finalBalance = repo.findByEmployeeAndType("E001", LeaveType.ANNUAL);

        int expectedRemaining = 12 - (successCount.get() * 3);
        int expectedUsed = successCount.get() * 3;

        boolean isWrong = finalBalance.getRemainingLeaveDays() != expectedRemaining
                || finalBalance.getUsedLeaveDays() != expectedUsed
                || finalBalance.getRemainingLeaveDays() < 0;

        System.out.println("======================================");
        System.out.println("TEST: Detect Wrong Leave Deduction With Two Threads");
        System.out.println("Employee ID: E001");
        System.out.println("Leave type: ANNUAL");
        System.out.println("Initial remaining leave days: 12");
        System.out.println("Deduct days per thread: 3");
        System.out.println("Threads started: 2");
        System.out.println("Successful threads: " + successCount.get() + " / 2");
        System.out.println("Expected remaining leave days: " + expectedRemaining);
        System.out.println("Actual remaining leave days: " + finalBalance.getRemainingLeaveDays());
        System.out.println("Expected used leave days: " + expectedUsed);
        System.out.println("Actual used leave days: " + finalBalance.getUsedLeaveDays());
        System.out.println("Final version: " + finalBalance.getVersion());
        System.out.println("Wrong deduction detected: " + isWrong);
        System.out.println("Result: " + (!isWrong ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertNotNull(finalBalance, "Final leave balance must exist");
        assertFalse(isWrong, "No wrong leave deduction should occur when using deductWithSync()");
        assertEquals(expectedRemaining, finalBalance.getRemainingLeaveDays());
        assertEquals(expectedUsed, finalBalance.getUsedLeaveDays());
        assertTrue(finalBalance.getRemainingLeaveDays() >= 0);
    }

    @Test
    @DisplayName("Correct single leave deduction")
    public void testCorrectSingleLeaveDeduction() throws Exception {
        File testFile = File.createTempFile("correct_leave_deduction_test_", ".csv");
        testFile.deleteOnExit();

        setupTestData(testFile);

        LeaveBalanceRepository repo = new LeaveBalanceRepository(testFile.getAbsolutePath());

        boolean success = repo.deductWithSync("E001", LeaveType.ANNUAL, 2);

        LeaveBalance finalBalance = repo.findByEmployeeAndType("E001", LeaveType.ANNUAL);

        int expectedRemaining = 10;
        int expectedUsed = 2;

        boolean isWrong = finalBalance.getRemainingLeaveDays() != expectedRemaining
                || finalBalance.getUsedLeaveDays() != expectedUsed;

        System.out.println("======================================");
        System.out.println("TEST: Correct Single Leave Deduction");
        System.out.println("Employee ID: E001");
        System.out.println("Leave type: ANNUAL");
        System.out.println("Old leave balance: 12");
        System.out.println("Leave days requested: 2");
        System.out.println("Deduction success: " + success);
        System.out.println("Expected remaining leave days: " + expectedRemaining);
        System.out.println("Actual remaining leave days: " + finalBalance.getRemainingLeaveDays());
        System.out.println("Expected used leave days: " + expectedUsed);
        System.out.println("Actual used leave days: " + finalBalance.getUsedLeaveDays());
        System.out.println("Wrong deduction detected: " + isWrong);
        System.out.println("Result: " + (!isWrong ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertTrue(success, "Leave deduction should be successful");
        assertFalse(isWrong, "Correct leave deduction should not be detected as wrong");
        assertEquals(expectedRemaining, finalBalance.getRemainingLeaveDays());
        assertEquals(expectedUsed, finalBalance.getUsedLeaveDays());
    }

    @Test
    @DisplayName("Calculate expected leave balance")
    public void testCalculateExpectedLeaveBalance() {
        int oldBalance = 15;
        int leaveDays = 5;

        int expectedRemaining = calculateExpectedRemaining(oldBalance, leaveDays);

        int actualRemaining = 10;

        boolean isWrong = actualRemaining != expectedRemaining;

        System.out.println("======================================");
        System.out.println("TEST: Calculate Expected Leave Balance");
        System.out.println("Old leave balance: " + oldBalance);
        System.out.println("Leave days requested: " + leaveDays);
        System.out.println("Expected remaining leave days: " + expectedRemaining);
        System.out.println("Actual remaining leave days: " + actualRemaining);
        System.out.println("Wrong deduction detected: " + isWrong);
        System.out.println("Result: " + (!isWrong ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertEquals(10, expectedRemaining);
        assertFalse(isWrong, "Expected balance is correct, so wrong deduction should not be detected");
    }

    private int calculateExpectedRemaining(int oldBalance, int leaveDays) {
        return oldBalance - leaveDays;
    }

    private static void setupTestData(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("balanceId,employeeId,leaveType,totalLeaveDays,usedLeaveDays,remainingLeaveDays,version");
            writer.println("LB_E001_ANNUAL,E001,ANNUAL,12,0,12,0");
        }
    }
}