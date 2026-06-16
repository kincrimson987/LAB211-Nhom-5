package synchronization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Wrong Leave Deduction Detection Test Suite")
public class WrongLeaveDeductionTest {

    static class LeaveDeductionChecker {

        public int calculateExpectedBalance(int oldBalance, int leaveDays) {
            return oldBalance - leaveDays;
        }

        public boolean detectWrongDeduction(int oldBalance, int leaveDays, int actualNewBalance) {
            int expectedNewBalance = calculateExpectedBalance(oldBalance, leaveDays);
            return actualNewBalance != expectedNewBalance;
        }
    }

    @Test
    @DisplayName("Detect wrong leave deduction")
    public void testWrongLeaveDeductionShouldBeDetected() {
        LeaveDeductionChecker checker = new LeaveDeductionChecker();

        int oldBalance = 12;
        int leaveDays = 2;
        int expectedNewBalance = checker.calculateExpectedBalance(oldBalance, leaveDays);

        // Giả lập hệ thống trừ sai: đúng ra còn 10 nhưng thực tế còn 9
        int actualNewBalance = 9;

        boolean isWrong = checker.detectWrongDeduction(oldBalance, leaveDays, actualNewBalance);

        System.out.println("======================================");
        System.out.println("TEST: Detect Wrong Leave Deduction");
        System.out.println("Old leave balance: " + oldBalance);
        System.out.println("Leave days requested: " + leaveDays);
        System.out.println("Expected new balance: " + expectedNewBalance);
        System.out.println("Actual new balance: " + actualNewBalance);
        System.out.println("Wrong deduction detected: " + isWrong);
        System.out.println("Result: " + (isWrong ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertEquals(10, expectedNewBalance);
        assertTrue(isWrong, "Wrong leave deduction should be detected");
        assertNotEquals(expectedNewBalance, actualNewBalance);
    }

    @Test
    @DisplayName("Correct leave deduction should not be detected as wrong")
    public void testCorrectLeaveDeductionShouldNotBeDetected() {
        LeaveDeductionChecker checker = new LeaveDeductionChecker();

        int oldBalance = 12;
        int leaveDays = 2;
        int expectedNewBalance = checker.calculateExpectedBalance(oldBalance, leaveDays);

        // Giả lập hệ thống trừ đúng: 12 - 2 = 10
        int actualNewBalance = 10;

        boolean isWrong = checker.detectWrongDeduction(oldBalance, leaveDays, actualNewBalance);

        System.out.println("======================================");
        System.out.println("TEST: Correct Leave Deduction");
        System.out.println("Old leave balance: " + oldBalance);
        System.out.println("Leave days requested: " + leaveDays);
        System.out.println("Expected new balance: " + expectedNewBalance);
        System.out.println("Actual new balance: " + actualNewBalance);
        System.out.println("Wrong deduction detected: " + isWrong);
        System.out.println("Result: " + (!isWrong ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertEquals(10, expectedNewBalance);
        assertFalse(isWrong, "Correct leave deduction should not be detected as wrong");
        assertEquals(expectedNewBalance, actualNewBalance);
    }

    @Test
    @DisplayName("Calculate expected leave balance")
    public void testCalculateExpectedLeaveBalance() {
        LeaveDeductionChecker checker = new LeaveDeductionChecker();

        int oldBalance = 15;
        int leaveDays = 5;
        int expectedNewBalance = checker.calculateExpectedBalance(oldBalance, leaveDays);

        // Giả lập actual đúng bằng expected
        int actualNewBalance = 10;

        boolean isWrong = checker.detectWrongDeduction(oldBalance, leaveDays, actualNewBalance);

        System.out.println("======================================");
        System.out.println("TEST: Calculate Expected Leave Balance");
        System.out.println("Old leave balance: " + oldBalance);
        System.out.println("Leave days requested: " + leaveDays);
        System.out.println("Expected new balance: " + expectedNewBalance);
        System.out.println("Actual new balance: " + actualNewBalance);
        System.out.println("Wrong deduction detected: " + isWrong);
        System.out.println("Result: " + (!isWrong && expectedNewBalance == 10 ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertEquals(10, expectedNewBalance);
        assertFalse(isWrong, "Expected balance is correct, so wrong deduction should not be detected");
        assertEquals(expectedNewBalance, actualNewBalance);
    }
}