import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit test chống double payment — rubric bắt buộc.
 * 2 thread cùng gọi processWithSync() cho E001 → chỉ 1 thread thành công.
 */
public class DoublePaymentTest {

    public static void main(String[] args) throws Exception {
        File testFile = File.createTempFile("double_payment_test_", ".csv");
        testFile.deleteOnExit();

        setupTestData(testFile);

        PayrollEntryRepository repo = new PayrollEntryRepository(testFile.getAbsolutePath());
        PayrollEntry entry = repo.findByEmployeeAndMonth("E001", "2024-01");
        if (entry == null) {
            System.err.println("[FAIL] Không tìm thấy entry test cho E001/2024-01");
            System.exit(1);
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);

        Runnable task = () -> {
            try {
                startLatch.await();
                boolean success = repo.processWithSync(entry.getEntryId(), "HR_TEST");
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

        long processedCount = repo.countProcessedByEmployee("E001");
        PayrollEntry finalEntry = repo.findById(entry.getEntryId());

        System.out.println("========================================");
        System.out.println("  DoublePaymentTest — processWithSync()");
        System.out.println("========================================");
        System.out.printf("Thread thành công: %d / 2%n", successCount.get());
        System.out.printf("Số entry PROCESSED của E001 trong CSV: %d%n", processedCount);
        System.out.printf("Version cuối: %d | Status: %s%n",
                finalEntry.getVersion(), finalEntry.getStatus());

        if (successCount.get() == 1 && processedCount == 1
                && finalEntry.getStatus() == PayrollStatus.PROCESSED) {
            System.out.println("Thread-1 thành công, Thread-2 bị bỏ qua → 0 double payment ✅");
            System.out.println("[PASS] DoublePaymentTest");
        } else {
            System.err.println("[FAIL] Double payment detected — success="
                    + successCount.get() + ", processed=" + processedCount);
            System.exit(1);
        }
    }

    private static void setupTestData(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(PayrollEntry.getFullCsvHeader());
            writer.println("PR_E0001_01_2024,0,E001,0.0,PENDING");
        }
    }
}
