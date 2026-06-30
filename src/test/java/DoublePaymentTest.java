/*import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Double Payment Prevention Test Suite")
public class DoublePaymentTest {

    @Test
    @DisplayName("Prevent double payment with two threads")
    public void testPreventDoublePaymentWithTwoThreads() throws Exception {
        File testFile = File.createTempFile("double_payment_test_", ".csv");
        testFile.deleteOnExit();

        setupSinglePayrollEntry(testFile);

        PayrollEntryRepository repo = new PayrollEntryRepository(testFile.getAbsolutePath());

        PayrollEntry entry = repo.findByEmployeeAndMonth("E001", "2024-01");
        assertNotNull(entry, "Test payroll entry for E001/2024-01 must exist");

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

        boolean isDoublePayment = successCount.get() > 1
                || processedCount > 1
                || finalEntry.getStatus() != PayrollStatus.PROCESSED;

        System.out.println("======================================");
        System.out.println("TEST: Prevent Double Payment With Two Threads");
        System.out.println("Employee ID: E001");
        System.out.println("Payroll month: 2024-01");
        System.out.println("Threads started: 2");
        System.out.println("Successful threads: " + successCount.get() + " / 2");
        System.out.println("Processed entries in CSV: " + processedCount);
        System.out.println("Expected processed entries: 1");
        System.out.println("Final version: " + finalEntry.getVersion());
        System.out.println("Final status: " + finalEntry.getStatus());
        System.out.println("Double payment detected: " + isDoublePayment);
        System.out.println("Result: " + (!isDoublePayment ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertEquals(1, successCount.get(), "Only one thread should process payroll successfully");
        assertEquals(1, processedCount, "Only one payroll entry should be processed");
        assertEquals(PayrollStatus.PROCESSED, finalEntry.getStatus());
        assertFalse(isDoublePayment, "Double payment should not occur");
    }

    @Test
    @DisplayName("Same employee can be paid in different months")
    public void testAllowPaymentForDifferentMonths() throws Exception {
        File testFile = File.createTempFile("different_month_payment_test_", ".csv");
        testFile.deleteOnExit();

        setupTwoMonthsPayrollEntries(testFile);

        PayrollEntryRepository repo = new PayrollEntryRepository(testFile.getAbsolutePath());

        PayrollEntry januaryEntry = repo.findByEmployeeAndMonth("E001", "2024-01");
        PayrollEntry februaryEntry = repo.findByEmployeeAndMonth("E001", "2024-02");

        assertNotNull(januaryEntry);
        assertNotNull(februaryEntry);

        boolean januarySuccess = repo.processWithSync(januaryEntry.getEntryId(), "HR_TEST");
        boolean februarySuccess = repo.processWithSync(februaryEntry.getEntryId(), "HR_TEST");

        PayrollEntry finalJanuaryEntry = repo.findById(januaryEntry.getEntryId());
        PayrollEntry finalFebruaryEntry = repo.findById(februaryEntry.getEntryId());

        boolean isWronglyRejected = !januarySuccess || !februarySuccess;

        System.out.println("======================================");
        System.out.println("TEST: Allow Payment For Different Months");
        System.out.println("Employee ID: E001");
        System.out.println("January payment success: " + januarySuccess);
        System.out.println("February payment success: " + februarySuccess);
        System.out.println("January final status: " + finalJanuaryEntry.getStatus());
        System.out.println("February final status: " + finalFebruaryEntry.getStatus());
        System.out.println("Wrongly rejected payment: " + isWronglyRejected);
        System.out.println("Result: " + (!isWronglyRejected ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertTrue(januarySuccess, "January payroll should be processed");
        assertTrue(februarySuccess, "February payroll should be processed");
        assertEquals(PayrollStatus.PROCESSED, finalJanuaryEntry.getStatus());
        assertEquals(PayrollStatus.PROCESSED, finalFebruaryEntry.getStatus());
        assertFalse(isWronglyRejected);
    }

    @Test
    @DisplayName("Different employees can be paid in same month")
    public void testAllowPaymentForDifferentEmployees() throws Exception {
        File testFile = File.createTempFile("different_employee_payment_test_", ".csv");
        testFile.deleteOnExit();

        setupTwoEmployeesPayrollEntries(testFile);

        PayrollEntryRepository repo = new PayrollEntryRepository(testFile.getAbsolutePath());

        PayrollEntry employee1Entry = repo.findByEmployeeAndMonth("E001", "2024-01");
        PayrollEntry employee2Entry = repo.findByEmployeeAndMonth("E002", "2024-01");

        assertNotNull(employee1Entry);
        assertNotNull(employee2Entry);

        boolean employee1Success = repo.processWithSync(employee1Entry.getEntryId(), "HR_TEST");
        boolean employee2Success = repo.processWithSync(employee2Entry.getEntryId(), "HR_TEST");

        PayrollEntry finalEmployee1Entry = repo.findById(employee1Entry.getEntryId());
        PayrollEntry finalEmployee2Entry = repo.findById(employee2Entry.getEntryId());

        boolean isWronglyRejected = !employee1Success || !employee2Success;

        System.out.println("======================================");
        System.out.println("TEST: Allow Payment For Different Employees");
        System.out.println("Payroll month: 2024-01");
        System.out.println("Employee E001 payment success: " + employee1Success);
        System.out.println("Employee E002 payment success: " + employee2Success);
        System.out.println("Employee E001 final status: " + finalEmployee1Entry.getStatus());
        System.out.println("Employee E002 final status: " + finalEmployee2Entry.getStatus());
        System.out.println("Wrongly rejected payment: " + isWronglyRejected);
        System.out.println("Result: " + (!isWronglyRejected ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertTrue(employee1Success, "Employee E001 payroll should be processed");
        assertTrue(employee2Success, "Employee E002 payroll should be processed");
        assertEquals(PayrollStatus.PROCESSED, finalEmployee1Entry.getStatus());
        assertEquals(PayrollStatus.PROCESSED, finalEmployee2Entry.getStatus());
        assertFalse(isWronglyRejected);
    }

    private static void setupSinglePayrollEntry(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(new PayrollEntry().getCsvHeader());
            writer.println("PR_E0001_01_2024,0,E001,0.0,PENDING");
        }
    }

    private static void setupTwoMonthsPayrollEntries(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(new PayrollEntry().getCsvHeader());
            writer.println("PR_E0001_01_2024,0,E001,0.0,PENDING");
            writer.println("PR_E0001_02_2024,0,E001,0.0,PENDING");
        }
    }

    private static void setupTwoEmployeesPayrollEntries(File file) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(new PayrollEntry().getCsvHeader());
            writer.println("PR_E0001_01_2024,0,E001,0.0,PENDING");
            writer.println("PR_E0002_01_2024,0,E002,0.0,PENDING");
        }
    }
}*/