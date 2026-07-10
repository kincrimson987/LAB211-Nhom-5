```mermaid
classDiagram
    class ReportController {
        -payrollEntryRepository: PayrollEntryRepository
        -attendanceRepository: AttendanceRepository
        -payrollRunRepository: PayrollRunRepository
        +ReportController(payrollEntryRepository: PayrollEntryRepository, attendanceRepository: AttendanceRepository, payrollRunRepository: PayrollRunRepository, employeeRepository: EmployeeRepository)
        +generatePayrollReport(yearMonth: String) String
        +generateAttendanceReport(yearMonth: String) String
        +generateSimulationComparisonReport() String
        +exportCsv(filePath: String) int
        +importCsv(filePath: String) int
        +getPayrollEntriesByMonth(yearMonth: String) List~PayrollEntry~
        +getAttendanceRecordsByMonth(yearMonth: String) List~AttendanceRecord~
        +getPayrollRuns() List~PayrollRun~
    }

    class ReportView {
        +renderReport(totalEmp: int, approvedLeaves: int, payrollMode: String) void
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

    class AttendanceRecord {
        -employeeId: String
        -yearMonth: String
        -workDays: int
        -overtimeHours: double
        +AttendanceRecord()
        +AttendanceRecord(id: String, version: long, employeeId: String, workDays: int, overtimeHours: double)
        +AttendanceRecord(id: String, version: long, employeeId: String, yearMonth: String, workDays: int, overtimeHours: double)
        +getEmployeeId() String
        +setEmployeeId(employeeId: String) void
        +getYearMonth() String
        +setYearMonth(yearMonth: String) void
        +getWorkDays() int
        +setWorkDays(workDays: int) void
        +getOvertimeHours() double
        +setOvertimeHours(overtimeHours: double) void
        +extractYearMonthFromId(id: String) String
        +toCsvLine() String
        +fromCsvLine(line: String) void
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

    class PayrollEntryRepository {
        +findAll() List~PayrollEntry~
        +writeAllLines(entities: List~PayrollEntry~) void
    }

    class AttendanceRepository {
        +findByMonth(yearMonth: String) List~AttendanceRecord~
    }

    class PayrollRunRepository {
        +findAll() List~PayrollRun~
    }

    class EmployeeRepository

    class PayrollStatus {
        <<enumeration>>
        PENDING
        PROCESSED
    }

    CsvRepository~T~ <|-- PayrollEntryRepository
    CsvRepository~T~ <|-- AttendanceRepository
    CsvRepository~T~ <|-- PayrollRunRepository

    ReportController --> PayrollEntryRepository : payrollEntryRepository
    ReportController --> AttendanceRepository : attendanceRepository
    ReportController --> PayrollRunRepository : payrollRunRepository
    ReportController ..> EmployeeRepository : constructor parameter

    ReportController --> PayrollEntry : payroll report/export/import
    ReportController --> AttendanceRecord : attendance report
    ReportController --> PayrollRun : simulation comparison report

    ReportView ..> ReportController : displays report data

    PayrollEntry --> PayrollStatus : status
```