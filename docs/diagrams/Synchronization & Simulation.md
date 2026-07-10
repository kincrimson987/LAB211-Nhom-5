```mermaid
classDiagram
    class SimulationController {
        -payrollEntryRepository: PayrollEntryRepository
        -leaveBalanceRepository: LeaveBalanceRepository
        -payrollRunRepository: PayrollRunRepository
        -attendanceRepository: AttendanceRepository
        -employeeRepository: EmployeeRepository
        -payrollRuleRepository: PayrollRuleRepository
        -currentSyncMode: String
        +SimulationController(payrollEntryRepository: PayrollEntryRepository, leaveBalanceRepository: LeaveBalanceRepository, payrollRunRepository: PayrollRunRepository, attendanceRepository: AttendanceRepository, employeeRepository: EmployeeRepository, payrollRuleRepository: PayrollRuleRepository)
        +setSyncMode(mode: String) void
        +getCurrentSyncMode() String
        +runSimulation(yearMonth: String, threadCount: int) PayrollRun
        +getLatestRun() PayrollRun
        +detectDoublePayment() List~String~
        +detectWrongLeaveDeduction() List~String~
        -prepareEntries(employees: List~Employee~, yearMonth: String, rule: PayrollRule) List~PayrollEntry~
        -processEntry(entryId: String, processedBy: String) boolean
    }

    class PayrollRun {
        -yearMonth: String
        -mechanism: String
        -elapsedMs: long
        -successCount: int
        -doublePaymentCount: int
        -wrongLeaveCount: int
        -tps: double
        +PayrollRun()
        +PayrollRun(runId: String, version: long, yearMonth: String, mechanism: String, elapsedMs: long, successCount: int, doublePaymentCount: int, wrongLeaveCount: int, tps: double)
        +getRunId() String
        +setRunId(runId: String) void
        +getYearMonth() String
        +setYearMonth(yearMonth: String) void
        +getMechanism() String
        +setMechanism(mechanism: String) void
        +getElapsedMs() long
        +setElapsedMs(elapsedMs: long) void
        +getSuccessCount() int
        +setSuccessCount(successCount: int) void
        +getDoublePaymentCount() int
        +setDoublePaymentCount(doublePaymentCount: int) void
        +getWrongLeaveCount() int
        +setWrongLeaveCount(wrongLeaveCount: int) void
        +getTps() double
        +setTps(tps: double) void
        +toCsvLine() String
        +fromCsvLine(line: String) void
        +toString() String
    }

    class PayrollRunRepository {
        +PayrollRunRepository()
        +PayrollRunRepository(filePath: String)
        +getHeader() String
        +getId(entity: PayrollRun) String
        +toLine(entity: PayrollRun) String
        +parseLine(line: String) PayrollRun
        +findByMechanism(mechanism: String) List~PayrollRun~
        +findByYearMonth(yearMonth: String) List~PayrollRun~
        +findLatest() PayrollRun
    }

    class PayrollEntry {
        -employeeId: String
        -netSalary: double
        -status: PayrollStatus
        +PayrollEntry()
        +PayrollEntry(id: String, employeeId: String)
        +PayrollEntry(id: String, version: long, employeeId: String, netSalary: double, status: PayrollStatus)
        +getEntryId() String
        +setEntryId(entryId: String) void
        +getEmployeeId() String
        +setEmployeeId(employeeId: String) void
        +getNetSalary() double
        +setNetSalary(netSalary: double) void
        +getStatus() PayrollStatus
        +setStatus(status: PayrollStatus) void
        +process() void
        +getCsvHeader() String
        +extractYearMonth() String
        +toCsvLine() String
        +fromCsvLine(line: String) void
        +toString() String
    }

    class PayrollEntryRepository {
        -EMPLOYEES_PATH: String
        -OPTIMISTIC_MAX_RETRIES: int
        -OPTIMISTIC_BASE_BACKOFF_MS: long
        +PayrollEntryRepository()
        +PayrollEntryRepository(filePath: String)
        +getHeader() String
        +getId(entity: PayrollEntry) String
        +toLine(entity: PayrollEntry) String
        +parseLine(line: String) PayrollEntry
        +findByEmployeeAndMonth(empId: String, yearMonth: String) PayrollEntry
        +findByStatus(status: PayrollStatus) List~PayrollEntry~
        +findByDeptAndMonth(deptId: String, yearMonth: String) List~PayrollEntry~
        +countProcessedByEmployee(employeeId: String) long
        +processWithNoLock(entryId: String, processedBy: String) boolean
        +processWithFileLock(entryId: String, processedBy: String) boolean
        +processWithSync(entryId: String, processedBy: String) boolean
        +processWithOptimistic(entryId: String, processedBy: String) boolean
        +updateIfVersionMatch(updated: PayrollEntry, expectedVersion: long) boolean
        -findInList(entries: List~PayrollEntry~, entryId: String) PayrollEntry
        -markProcessed(entry: PayrollEntry) void
        -sleepWithBackoff(attempt: int) void
        -loadEmployeeIdsByDepartment(deptId: String) Set~String~
    }

    class LeaveBalance {
        -totalLeaveDays: int
        -usedLeaveDays: int
        -remainingLeaveDays: int
        -employeeId: String
        -leaveType: LeaveType
        +LeaveBalance()
        +LeaveBalance(totalLeaveDays: int, usedLeaveDays: int, remainingLeaveDays: int)
        +LeaveBalance(balanceId: String, employeeId: String, leaveType: LeaveType, totalLeaveDays: int)
        +getTotalLeaveDays() int
        +getUsedLeaveDays() int
        +getRemainingLeaveDays() int
        +setTotalLeaveDays(totalLeaveDays: int) void
        +setUsedLeaveDays(usedLeaveDays: int) void
        +setRemainingLeaveDays(remainingLeaveDays: int) void
        +getBalanceId() String
        +getEmployeeId() String
        +getLeaveType() LeaveType
        +setBalanceId(balanceId: String) void
        +setEmployeeId(employeeId: String) void
        +setLeaveType(leaveType: LeaveType) void
        +deductLeave(days: int) void
        +addLeave(days: int) void
        +checkRemaining() int
        +getCsvHeader() String
        +toCsvLine() String
        +fromCsvLine(line: String) void
        +toString() String
    }

    class LeaveBalanceRepository {
        -OPTIMISTIC_MAX_RETRIES: int
        -OPTIMISTIC_BASE_BACKOFF_MS: long
        +LeaveBalanceRepository()
        +LeaveBalanceRepository(filePath: String)
        +getHeader() String
        +getId(entity: LeaveBalance) String
        +toLine(entity: LeaveBalance) String
        +parseLine(line: String) LeaveBalance
        +findByEmployee(employeeId: String) List~LeaveBalance~
        +findByEmployeeAndType(employeeId: String, leaveType: LeaveType) LeaveBalance
        +deductWithNoLock(employeeId: String, leaveType: LeaveType, days: int) boolean
        +deductWithSync(employeeId: String, leaveType: LeaveType, days: int) boolean
        +deductWithOptimistic(employeeId: String, leaveType: LeaveType, days: int) boolean
        +deductWithFileLock(employeeId: String, leaveType: LeaveType, days: int) boolean
        +updateIfVersionMatch(updated: LeaveBalance, expectedVersion: long) boolean
        -findInList(balances: List~LeaveBalance~, employeeId: String, leaveType: LeaveType) LeaveBalance
        -sleepWithBackoff(attempt: int) void
    }

    class PayrollRule {
        -standardWorkingDays: int
        -workingHoursPerDay: int
        -overtimeMultiplier: double
        -attendanceBonus: double
        -taxRate: double
        -taxThreshold: double
        +PayrollRule()
        +PayrollRule(standardWorkingDays: int, workingHoursPerDay: int, overtimeMultiplier: double, attendanceBonus: double, taxRate: double, taxThreshold: double)
        +getStandardWorkingDays() int
        +setStandardWorkingDays(standardWorkingDays: int) void
        +getWorkingHoursPerDay() int
        +setWorkingHoursPerDay(workingHoursPerDay: int) void
        +getOvertimeMultiplier() double
        +setOvertimeMultiplier(overtimeMultiplier: double) void
        +getAttendanceBonus() double
        +setAttendanceBonus(attendanceBonus: double) void
        +getTaxRate() double
        +setTaxRate(taxRate: double) void
        +getTaxThreshold() double
        +setTaxThreshold(taxThreshold: double) void
        +toString() String
    }

    class PayrollRuleRepository {
        +getConfig() PayrollRule
    }

    class AttendanceRepository {
        +findByEmployeeAndMonth(employeeId: String, yearMonth: String) AttendanceRecord
    }

    class EmployeeRepository {
        +findAll() List~Employee~
    }

    class Employee {
        <<abstract>>
        +getId() String
        +getName() String
        +calculateSalary(attendance: AttendanceRecord, rule: PayrollRule) double
    }

    class AttendanceRecord
    class PayrollStatus {
        <<enumeration>>
        PENDING
        PROCESSED
    }

    class LeaveType {
        <<enumeration>>
        ANNUAL
        SICK
        UNPAID
        OTHER
    }

    SimulationController --> PayrollEntryRepository : payrollEntryRepository
    SimulationController --> LeaveBalanceRepository : leaveBalanceRepository
    SimulationController --> PayrollRunRepository : payrollRunRepository
    SimulationController --> AttendanceRepository : attendanceRepository
    SimulationController --> EmployeeRepository : employeeRepository
    SimulationController --> PayrollRuleRepository : payrollRuleRepository
    SimulationController --> PayrollRun : creates
    SimulationController --> PayrollEntry : prepares/processes
    SimulationController --> LeaveBalance : checks
    SimulationController --> Employee : uses
    SimulationController --> PayrollRule : uses
    SimulationController --> AttendanceRecord : uses

    PayrollRunRepository --> PayrollRun : manages
    PayrollEntryRepository --> PayrollEntry : manages
    LeaveBalanceRepository --> LeaveBalance : manages

    PayrollEntry --> PayrollStatus : status
    LeaveBalance --> LeaveType : leaveType
    Employee ..> AttendanceRecord : calculateSalary()
    Employee ..> PayrollRule : calculateSalary()
```