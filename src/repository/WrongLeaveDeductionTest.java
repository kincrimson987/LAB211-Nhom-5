import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test chống Wrong Leave Deduction.
 * 2 thread cùng trừ phép ANNUAL của E001.
 * Nếu dùng deductWithSync(), dữ liệu phải được cập nhật an toàn.
 */
public class WrongLeaveDeductionTest {

    public static void main(String[] args) throws Exception {
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

        System.out.println("========================================");
        System.out.println(" WrongLeaveDeductionTest - deductWithSync()");
        System.out.println("========================================");
        System.out.println("Thread thanh cong: " + successCount.get() + " / 2");
        System.out.println("Remaining leave days: " + finalBalance.getRemainingLeaveDays());
        System.out.println("Used leave days: " + finalBalance.getUsedLeaveDays());
        System.out.println("Version cuoi: " + finalBalance.getVersion());

        int expectedRemaining = 12 - (successCount.get() * 3);
        int expectedUsed = successCount.get() * 3;

        if (finalBalance.getRemainingLeaveDays() == expectedRemaining
                && finalBalance.getUsedLeaveDays() == expectedUsed
                && finalBalance.getRemainingLeaveDays() >= 0) {
            System.out.println("[PASS] No wrong leave deduction.");
        } else {
            System.err.println("[FAIL] Wrong leave deduction detected.");
            System.err.println("Expected remaining: " + expectedRemaining);
            System.err.println("Actual remaining: " + finalBalance.getRemainingLeaveDays());
            System.exit(1);
        }
    }

    private static void setupTestData(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("balanceId,employeeId,leaveType,totalLeaveDays,usedLeaveDays,remainingLeaveDays,version");
            writer.println("LB_E001_ANNUAL,E001,ANNUAL,12,0,12,0");
        }
    }
}