import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class SimulationTask implements Runnable {

    private final Employee employee;
    private final List<PayrollEntry> payrollEntries;
    private final Map<String, Double> expectedSalaryByEmployee;
    private final CountDownLatch latch;

    public SimulationTask(
            Employee employee,
            List<PayrollEntry> payrollEntries,
            Map<String, Double> expectedSalaryByEmployee,
            CountDownLatch latch
    ) {
        this.employee = employee;
        this.payrollEntries = payrollEntries;
        this.expectedSalaryByEmployee = expectedSalaryByEmployee;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            String employeeId = employee.getId();

            int leaveDays = Math.abs(employeeId.hashCode()) % 4;
            double dailyDeduction = 300000.0;

            double correctLeaveDeduction = leaveDays * dailyDeduction;
            double expectedNetSalary = employee.getBaseSalary() - correctLeaveDeduction;
            expectedNetSalary = employee.roundMoney(expectedNetSalary);

            double actualNetSalary = expectedNetSalary;

            String payrollId = "PR_" + employeeId + "_" + System.nanoTime();

            PayrollEntry entry = new PayrollEntry(
                    payrollId,
                    0,
                    employeeId,
                    actualNetSalary,
                    PayrollStatus.PENDING
            );

            expectedSalaryByEmployee.put(employeeId, expectedNetSalary);
            payrollEntries.add(entry);

        } finally {
            latch.countDown();
        }
    }
}