```mermaid
classDiagram
    class BaseEntity {
        <<abstract>>
        -id: String
        -version: long
        +BaseEntity()
        +BaseEntity(id: String, version: long)
        +getId() String
        +setId(id: String) void
        +getVersion() long
        +setVersion(version: long) void
        +toCsvLine() String*
        +fromCsvLine(line: String) void*
        +equals(o: Object) boolean
        +hashCode() int
        +toString() String
    }

    class CsvRepository~T~ {
        <<abstract>>
        -filePath: String
        +CsvRepository(filePath: String)
        +getFilePath() String
        +getHeader() String*
        +getId(entity: T) String*
        +toLine(entity: T) String*
        +parseLine(line: String) T*
        +findAll() List~T~
        +findById(id: String) T
        +save(entity: T) void
        +update(entity: T) void
        +delete(id: String) void
        +readAllLines() List~T~
        +writeAllLines(entities: List~T~) void
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

    class PayrollStatus {
        <<enumeration>>
        PENDING
        PROCESSED
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

    class PayrollRuleRepository {
        +PayrollRuleRepository()
        +PayrollRuleRepository(filePath: String)
        +getHeader() String
        +getId(entity: PayrollRule) String
        +toLine(entity: PayrollRule) String
        +parseLine(line: String) PayrollRule
        +getConfig() PayrollRule
        +updateConfig(newRule: PayrollRule) void
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

    class PayrollController {
        -employeeRepo: EmployeeRepository
        -attendanceRepo: AttendanceRepository
        -ruleRepo: PayrollRuleRepository
        -entryRepo: PayrollEntryRepository
        -runRepo: PayrollRunRepository
        -view: ReportView
        +PayrollController(employeeRepo: EmployeeRepository, attendanceRepo: AttendanceRepository, ruleRepo: PayrollRuleRepository, entryRepo: PayrollEntryRepository, runRepo: PayrollRunRepository)
        +PayrollController(view: ReportView)
        +runPayroll(yearMonth: String) List~PayrollEntry~
        +processMonthlyPayroll(yearMonth: String, userId: String) PayrollRun
        +getAllEntries() List~PayrollEntry~
        +getAllPayrollEntries() List~PayrollEntry~
        +getPayrollByEmployee(employeeId: String) List~PayrollEntry~
        +getEntriesByMonth(yearMonth: String) List~PayrollEntry~
        +getPayrollRule() PayrollRule
        +updatePayrollRule(rule: PayrollRule) void
        +findEmployeeById(employeeId: String) Employee
        +processSingleThreadPayroll(employees: String[]) int
        +processLeaveApproval(currentBalance: int, requestDays: int) int
    }

    class Employee {
        <<abstract>>
        -name: String
        -email: String
        -departmentId: String
        -employmentType: EmployeeType
        -baseSalary: double
        +calculateSalary(attendance: AttendanceRecord, rule: PayrollRule) double*
    }

    class AttendanceRecord {
        -employeeId: String
        -yearMonth: String
        -workDays: int
        -overtimeHours: double
    }

    class EmployeeRepository {
        +findAll() List~Employee~
        +findById(id: String) Employee
    }

    class AttendanceRepository {
        +findByEmployeeAndMonth(employeeId: String, yearMonth: String) AttendanceRecord
    }

    class ReportView {
        +renderReport(count: int, columns: int, title: String) void
    }

    class TestSalaryCalculator {
        -EPS: double
        +assertEqual(actual: double, expected: double, message: String) void
        +main(args: String[]) void
    }
    class FullTimeEmployee {
    +FullTimeEmployee()
    +FullTimeEmployee(id: String, version: long, name: String, email: String, departmentId: String, baseSalary: double)
    +calculateSalary(attendance: AttendanceRecord, rule: PayrollRule) double
}

class PartTimeEmployee {
    +PartTimeEmployee()
    +PartTimeEmployee(id: String, version: long, name: String, email: String, departmentId: String, baseSalary: double)
    +calculateSalary(attendance: AttendanceRecord, rule: PayrollRule) double
}


Employee <|-- FullTimeEmployee
Employee <|-- PartTimeEmployee

    BaseEntity <|-- PayrollEntry
    BaseEntity <|-- PayrollRun

    CsvRepository~T~ <|-- PayrollEntryRepository
    CsvRepository~T~ <|-- PayrollRuleRepository
    CsvRepository~T~ <|-- PayrollRunRepository

    PayrollEntry --> PayrollStatus : status
    PayrollEntryRepository --> PayrollEntry : manages
    PayrollEntryRepository ..> PayrollStatus : checks/updates

    PayrollRuleRepository --> PayrollRule : manages config
    PayrollRunRepository --> PayrollRun : manages

    PayrollController --> EmployeeRepository : uses
    PayrollController --> AttendanceRepository : uses
    PayrollController --> PayrollRuleRepository : uses
    PayrollController --> PayrollEntryRepository : uses
    PayrollController --> PayrollRunRepository : uses
    PayrollController --> PayrollEntry : creates/processes
    PayrollController --> PayrollRun : creates
    PayrollController --> PayrollRule : reads/updates
    PayrollController --> ReportView : optional view

    PayrollController ..> Employee : calculateSalary()
    PayrollController ..> AttendanceRecord : reads attendance
    Employee ..> AttendanceRecord : salary input
    Employee ..> PayrollRule : salary rule

    TestSalaryCalculator ..> PayrollRule : creates
    TestSalaryCalculator ..> Employee : tests polymorphism
    TestSalaryCalculator ..> AttendanceRecord : creates
```