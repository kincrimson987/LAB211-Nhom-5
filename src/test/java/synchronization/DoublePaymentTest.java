package synchronization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Double Payment Prevention Test Suite")
public class DoublePaymentTest {

    static class PayrollProcessor {
        private final Set<String> paidPayrolls = new HashSet<>();

        public boolean processPayroll(String empId, String month) {
            String key = empId + "-" + month;

            if (paidPayrolls.contains(key)) {
                return false; // đã trả lương rồi
            }

            paidPayrolls.add(key);
            return true;
        }

        public int countPayrollEntries(String empId, String month) {
            String key = empId + "-" + month;
            return paidPayrolls.contains(key) ? 1 : 0;
        }
    }

    @Test
    @DisplayName("Prevent double payment for same employee in same month")
    public void testPreventDoublePayment() {
        PayrollProcessor processor = new PayrollProcessor();

        String empId = "E001";
        String month = "2026-06";

        boolean firstPayment = processor.processPayroll(empId, month);
        boolean secondPayment = processor.processPayroll(empId, month);
        int payrollCount = processor.countPayrollEntries(empId, month);

        System.out.println("======================================");
        System.out.println("TEST: Prevent Double Payment");
        System.out.println("Employee ID: " + empId);
        System.out.println("Payroll month: " + month);
        System.out.println("First payment result: " + firstPayment);
        System.out.println("Second payment result: " + secondPayment);
        System.out.println("Payroll entries created: " + payrollCount);
        System.out.println("Expected entries: 1");
        System.out.println("Result: " + (payrollCount == 1 && !secondPayment ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertTrue(firstPayment, "First payment should be successful");
        assertFalse(secondPayment, "Second payment should be rejected");
        assertEquals(1, payrollCount, "Only one payroll entry should be created");
    }

    @Test
    @DisplayName("Allow payment for same employee in different months")
    public void testAllowPaymentForDifferentMonths() {
        PayrollProcessor processor = new PayrollProcessor();

        boolean junePayment = processor.processPayroll("E001", "2026-06");
        boolean julyPayment = processor.processPayroll("E001", "2026-07");

        System.out.println("======================================");
        System.out.println("TEST: Allow Payment In Different Months");
        System.out.println("Employee ID: E001");
        System.out.println("June payment: " + junePayment);
        System.out.println("July payment: " + julyPayment);
        System.out.println("Result: " + (junePayment && julyPayment ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertTrue(junePayment);
        assertTrue(julyPayment);
    }

    @Test
    @DisplayName("Allow payment for different employees in same month")
    public void testAllowPaymentForDifferentEmployees() {
        PayrollProcessor processor = new PayrollProcessor();

        boolean emp1Payment = processor.processPayroll("E001", "2026-06");
        boolean emp2Payment = processor.processPayroll("E002", "2026-06");

        System.out.println("======================================");
        System.out.println("TEST: Allow Payment For Different Employees");
        System.out.println("Employee E001 payment: " + emp1Payment);
        System.out.println("Employee E002 payment: " + emp2Payment);
        System.out.println("Result: " + (emp1Payment && emp2Payment ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertTrue(emp1Payment);
        assertTrue(emp2Payment);
    }
}