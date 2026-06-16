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
    @DisplayName("Correct leave deduction should not be detected as wrong")
    public void testCorrectLeaveDeduction() {
        LeaveDeductionChecker checker = new LeaveDeductionChecker();

        int oldBalance = 12;
        int leaveDays = 2;
        int actualNewBalance = 10;
        int expectedNewBalance = checker.calculateExpectedBalance(oldBalance, leaveDays);

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

        assertFalse(isWrong, "Correct leave deduction should not be detected as wrong");
        assertEquals(expectedNewBalance, actualNewBalance);
    }

    @Test
    @DisplayName("Wrong leave deduction should be detected")
    public void testWrongLeaveDeduction() {
        LeaveDeductionChecker checker = new LeaveDeductionChecker();

        int oldBalance = 12;
        int leaveDays = 2;
        int actualNewBalance = 9;
        int expectedNewBalance = checker.calculateExpectedBalance(oldBalance, leaveDays);

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

        assertTrue(isWrong, "Wrong leave deduction should be detected");
        assertNotEquals(expectedNewBalance, actualNewBalance);
    }

    @Test
    @DisplayName("Calculate expected leave balance correctly")
    public void testCalculateExpectedLeaveBalance() {
        LeaveDeductionChecker checker = new LeaveDeductionChecker();

        int oldBalance = 15;
        int leaveDays = 5;
        int expectedBalance = checker.calculateExpectedBalance(oldBalance, leaveDays);

        System.out.println("======================================");
        System.out.println("TEST: Calculate Expected Leave Balance");
        System.out.println("Old leave balance: " + oldBalance);
        System.out.println("Leave days requested: " + leaveDays);
        System.out.println("Expected balance: " + expectedBalance);
        System.out.println("Result: " + (expectedBalance == 10 ? "PASSED" : "FAILED"));
        System.out.println("======================================");

        assertEquals(10, expectedBalance);
    }
}