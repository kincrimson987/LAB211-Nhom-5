import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulatorController {

    private static final int SIMULATION_DEPARTMENT_COUNT = 20;
    private static final int EMPLOYEES_PER_DEPARTMENT = 50;
    private static final int REQUIRED_EMPLOYEE_COUNT =
            SIMULATION_DEPARTMENT_COUNT * EMPLOYEES_PER_DEPARTMENT;
    private static final int THREAD_POOL_SIZE = 10;

    public void runSimulation(List<Employee> allEmployees) {
        List<List<Employee>> simulationDepartments =
                createSimulationDepartments(allEmployees);

        List<PayrollEntry> payrollEntries =
                Collections.synchronizedList(new ArrayList<>());

        Map<String, Double> expectedSalaryByEmployee =
                Collections.synchronizedMap(new HashMap<>());

        ExecutorService executorService =
                Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        CountDownLatch latch =
                new CountDownLatch(REQUIRED_EMPLOYEE_COUNT);

        for (List<Employee> departmentEmployees : simulationDepartments) {
            for (Employee employee : departmentEmployees) {
                SimulationTask task = new SimulationTask(
                        employee,
                        payrollEntries,
                        expectedSalaryByEmployee,
                        latch
                );

                executorService.submit(task);
            }
        }

        try {
            latch.await();

            System.out.println(
                    "Simulation completed: 20 departments x 50 employees = 1000 employees."
            );

            detectDoublePayments(payrollEntries);
            detectWrongLeaveDeductions(payrollEntries, expectedSalaryByEmployee);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Simulation interrupted.");
        } finally {
            executorService.shutdown();
        }
    }

    public List<List<Employee>> createSimulationDepartments(List<Employee> allEmployees) {
        if (allEmployees == null) {
            throw new IllegalArgumentException("Employee list cannot be null.");
        }

        if (allEmployees.size() < REQUIRED_EMPLOYEE_COUNT) {
            throw new IllegalStateException(
                    "Not enough employees for simulation. Required "
                            + REQUIRED_EMPLOYEE_COUNT
                            + ", actual "
                            + allEmployees.size()
            );
        }

        List<List<Employee>> simulationDepartments = new ArrayList<>();

        int employeeIndex = 0;

        for (int departmentIndex = 0;
                departmentIndex < SIMULATION_DEPARTMENT_COUNT;
                departmentIndex++) {

            List<Employee> departmentEmployees = new ArrayList<>();

            for (int i = 0; i < EMPLOYEES_PER_DEPARTMENT; i++) {
                departmentEmployees.add(allEmployees.get(employeeIndex));
                employeeIndex++;
            }

            simulationDepartments.add(departmentEmployees);
        }

        return simulationDepartments;
    }

    public void detectDoublePayments(List<PayrollEntry> payrollEntries) {
        if (payrollEntries == null) {
            throw new IllegalArgumentException("Payroll entry list cannot be null.");
        }

        System.out.println("=== Detect Double Payments ===");

        Map<String, Integer> countByEmployeeId = new HashMap<>();

        for (PayrollEntry entry : payrollEntries) {
            String employeeId = entry.getEmployeeId();

            countByEmployeeId.put(
                    employeeId,
                    countByEmployeeId.getOrDefault(employeeId, 0) + 1
            );
        }

        boolean found = false;

        for (Map.Entry<String, Integer> item : countByEmployeeId.entrySet()) {
            if (item.getValue() > 1) {
                found = true;

                System.out.println(
                        "Double payment detected: employeeId="
                                + item.getKey()
                                + ", count="
                                + item.getValue()
                );
            }
        }

        if (!found) {
            System.out.println("No double payments detected.");
        }
    }

    public void detectWrongLeaveDeductions(
            List<PayrollEntry> payrollEntries,
            Map<String, Double> expectedSalaryByEmployee
    ) {
        if (payrollEntries == null) {
            throw new IllegalArgumentException("Payroll entry list cannot be null.");
        }

        if (expectedSalaryByEmployee == null) {
            throw new IllegalArgumentException("Expected salary map cannot be null.");
        }

        System.out.println("=== Detect Wrong Leave Deductions ===");

        boolean found = false;

        for (PayrollEntry entry : payrollEntries) {
            String employeeId = entry.getEmployeeId();
            Double expectedSalary = expectedSalaryByEmployee.get(employeeId);

            if (expectedSalary == null) {
                continue;
            }

            double actualSalary = entry.getNetSalary();

            if (Double.compare(actualSalary, expectedSalary) != 0) {
                found = true;

                System.out.println(
                        "Wrong leave deduction detected: employeeId="
                                + employeeId
                                + ", expectedSalary="
                                + expectedSalary
                                + ", actualSalary="
                                + actualSalary
                );
            }
        }

        if (!found) {
            System.out.println("No wrong leave deductions detected.");
        }
    }
}