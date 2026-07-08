```mermaid
classDiagram

class CsvRepository~T~ {
    +getFilePath() : String
    +findAll() : List~T~
    +findById(id : String) : T
    +save(entity : T) : void
    +update(entity : T) : void
    +delete(id : String) : void
    +getHeader() : String
    +getId(entity : T) : String
    +toLine(entity : T) : String
    +parseLine(line : String) : T
    +readAllLines() : List~T~
    +writeAllLines(entities : List~T~) : void
}

class PayrollEntryRepository {
    +PayrollEntryRepository()
    +PayrollEntryRepository(String : filePath :)
    +findByEmployeeAndMonth(empId : String, yearMonth : String) : PayrollEntry
    +findByStatus(status : PayrollStatus) : List~PayrollEntry~
    +findByDeptAndMonth(deptId : String, yearMonth : String) : List~PayrollEntry~
    +countProcessedByEmployee(employeeId : String) : long
    +processWithNoLock(entryId : String, processedBy : String) : boolean
    +processWithFileLock(entryId : String, processedBy : String) : boolean
    +processWithSync(entryId : String, processedBy : String) : boolean
    +processWithOptimistic(entryId : String, processedBy : String) : boolean
    ~updateIfVersionMatch(updated : PayrollEntry, expectedVersion : long) : boolean
    -findInList(entries : List~PayrollEntry~, String : entryId :) : PayrollEntry
    -markProcessed(PayrollEntry : entry :, String : processedBy :) : void
    -sleepWithBackoff(int : attempt :) : void
    -loadEmployeeIdsByDepartment(deptId : String) : Set~String~
}

class PayrollEntry {
    -id : String
    -version : long
    -employeeId : String
    -netSalary : double
    -status : PayrollStatus

    +PayrollEntry()
    +PayrollEntry(String : id :, String : employeeId :)
    +PayrollEntry(String : id :, long : version :, String : employeeId :, double : netSalary :, PayrollStatus : status :)
    +getEntryId() : String
    +setEntryId(entryId : String) : void
    +getEmployeeId() : String
    +setEmployeeId(employeeId : String) : void
    +getNetSalary() : double
    +setNetSalary(netSalary : double) : void
    +getStatus() : PayrollStatus
    +setStatus(status : PayrollStatus) : void
    +process() : void
    +getFullCsvHeader() : String
    +getCsvHeader() : String
    +toCsvLine() : String
    +parseCsvLine(String : line :) : PayrollEntry
    +fromCsvLine(line : String) : void
    +extractYearMonthFromId(String : id :) : String
    +toString() : String
}

class PayrollRun {
    -id : String
    -version : long
    -yearMonth : String
    -mechanism : String
    -elapsedMs : long
    -successCount : int
    -doublePaymentCount : int
    -wrongLeaveCount : int
    -tps : double

    +PayrollRun()
    +PayrollRun(String : runId :, long : version :, String : yearMonth :, String : mechanism :, long : elapsedMs :, int : successCount :, int : doublePaymentCount :, int : wrongLeaveCount :, double : tps :)
    +getRunId() : String
    +setRunId(runId : String) : void
    +getYearMonth() : String
    +setYearMonth(yearMonth : String) : void
    +getMechanism() : String
    +setMechanism(mechanism : String) : void
    +getElapsedMs() : long
    +setElapsedMs(elapsedMs : long) : void
    +getSuccessCount() : int
    +setSuccessCount(successCount : int) : void
    +getDoublePaymentCount() : int
    +setDoublePaymentCount(doublePaymentCount : int) : void
    +getWrongLeaveCount() : int
    +setWrongLeaveCount(wrongLeaveCount : int) : void
    +getTps() : double
    +setTps(tps : double) : void
    +toCsvLine() : String
    +fromCsvLine(line : String) : void
    +toString() : String
}

class PayrollRule {
    -standardWorkingDays : int
    -workingHoursPerDay : int
    -overtimeMultiplier : double
    -attendanceBonus : double
    -taxRate : double
    -taxThreshold : double

    +PayrollRule()
    +PayrollRule(int : standardWorkingDays :, int : workingHoursPerDay :, double : overtimeMultiplier :, double : attendanceBonus :, double : taxRate :, double : taxThreshold :)
    +getStandardWorkingDays() : int
    +setStandardWorkingDays(standardWorkingDays : int) : void
    +getWorkingHoursPerDay() : int
    +setWorkingHoursPerDay(workingHoursPerDay : int) : void
    +getOvertimeMultiplier() : double
    +setOvertimeMultiplier(overtimeMultiplier : double) : void
    +getAttendanceBonus() : double
    +setAttendanceBonus(attendanceBonus : double) : void
    +getTaxRate() : double
    +setTaxRate(taxRate : double) : void
    +getTaxThreshold() : double
    +setTaxThreshold(taxThreshold : double) : void
    +toString() : String
}

class SalaryCalculator {
    +calcBaseSalary(Employee : employee :, AttendanceRecord : attendance :, PayrollRule : rule :) : double
    +calcOvertime(Employee : employee :, AttendanceRecord : attendance :, PayrollRule : rule :) : double
    +calcDeduction(Employee : employee :, AttendanceRecord : attendance :, PayrollRule : rule :) : double
    +calcBonus(AttendanceRecord : attendance :, PayrollRule : rule :) : double
    +calcGross(Employee : employee :, AttendanceRecord : attendance :, PayrollRule : rule :) : double
    +calcTax(Employee : employee :, AttendanceRecord : attendance :, PayrollRule : rule :) : double
    +calcNetSalary(Employee : employee :, AttendanceRecord : attendance :, PayrollRule : rule :) : double
}

class Employee {
    -id : String
    -version : long
    -name : String
    -email : String
    -departmentId : String
    -employmentType : Enums.EmploymentType
    -baseSalary : double

    +Employee()
    +Employee(String : id :, long : version :, String : name :, String : email :, String : departmentId :)
    +getId() : String
    +setId(id : String) : void
    +getVersion() : long
    +setVersion(version : long) : void
    +getName() : String
    +setName(name : String) : void
    +getEmail() : String
    +setEmail(email : String) : void
    +getDepartmentId() : String
    +setDepartmentId(departmentId : String) : void
    +getEmploymentType() : Enums.EmploymentType
    +setEmploymentType(employmentType : Enums.EmploymentType) : void
    +getBaseSalary() : double
    +setBaseSalary(baseSalary : double) : void
    +toCsvLine() : String
    +fromCsvLine(line : String) : void
}

class AttendanceRecord {
    -id : String
    -version : long
    -employeeId : String
    -workDays : int
    -overtimeHours : double

    +AttendanceRecord()
    +AttendanceRecord(String : id :, long : version :, String : employeeId :, int : workDays :, double : overtimeHours :)
    +getId() : String
    +setId(id : String) : void
    +getVersion() : long
    +setVersion(version : long) : void
    +getEmployeeId() : String
    +setEmployeeId(employeeId : String) : void
    +getWorkDays() : int
    +setWorkDays(workDays : int) : void
    +getOvertimeHours() : double
    +setOvertimeHours(overtimeHours : double) : void
    +toCsvLine() : String
    +fromCsvLine(line : String) : void
}

class LeaveBalance {
    -balanceId : String
    -employeeId : String
    -leaveType : Enums.LeaveType
    -totalLeaveDays : int
    -usedLeaveDays : int
    -remainingLeaveDays : int
    -version : int

    +LeaveBalance()
    +LeaveBalance(int : totalLeaveDays :, int : usedLeaveDays :, int : remainingLeaveDays :)
    +LeaveBalance(String : balanceId :, String : employeeId :, leaveType : Enums.LeaveType, int : totalLeaveDays :)
    +getBalanceId() : String
    +setBalanceId(balanceId : String) : void
    +getEmployeeId() : String
    +setEmployeeId(employeeId : String) : void
    +getLeaveType() : Enums.LeaveType
    +setLeaveType(leaveType : Enums.LeaveType) : void
    +getTotalLeaveDays() : int
    +setTotalLeaveDays(totalLeaveDays : int) : void
    +getUsedLeaveDays() : int
    +setUsedLeaveDays(usedLeaveDays : int) : void
    +getRemainingLeaveDays() : int
    +setRemainingLeaveDays(remainingLeaveDays : int) : void
    +getVersion() : int
    +setVersion(version : int) : void
    +deductLeave(days : int) : void
    +addLeave(days : int) : void
    +checkRemaining() : int
    +getCsvHeader() : String
    +toCsvLine() : String
    +fromCsvLine(String : line :) : LeaveBalance
    +toString() : String
}

class LeaveRequest {
    -leaveId : String
    -employeeId : String
    -leaveType : Enums.LeaveType
    -startDate : LocalDate
    -endDate : LocalDate
    -reason : String
    -status : Enums.LeaveStatus
    -approvedBy : String

    +LeaveRequest()
    +LeaveRequest(String : leaveId :, leaveType : Enums.LeaveType, LocalDate : startDate :, LocalDate : endDate :, String : reason :, status : Enums.LeaveStatus)
    +LeaveRequest(String : leaveId :, String : employeeId :, leaveType : Enums.LeaveType, LocalDate : startDate :, LocalDate : endDate :, String : reason :)
    +getLeaveId() : String
    +setLeaveId(leaveId : String) : void
    +getEmployeeId() : String
    +setEmployeeId(employeeId : String) : void
    +getLeaveType() : Enums.LeaveType
    +setLeaveType(leaveType : Enums.LeaveType) : void
    +getStartDate() : LocalDate
    +setStartDate(startDate : LocalDate) : void
    +getEndDate() : LocalDate
    +setEndDate(endDate : LocalDate) : void
    +getReason() : String
    +setReason(reason : String) : void
    +getStatus() : Enums.LeaveStatus
    +setStatus(status : Enums.LeaveStatus) : void
    +getApprovedBy() : String
    +setApprovedBy(approvedBy : String) : void
    +approve() : void
    +reject() : void
    +getDays() : int
    +getCsvHeader() : String
    +toCsvLine() : String
    +fromCsvLine(String : line :) : LeaveRequest
    +toString() : String
}

class Department {
    -id : String
    -version : long
    -name : String
    -managerId : String

    +Department()
    +Department(String : id :, long : version :, String : name :, String : managerId :)
    +getId() : String
    +setId(id : String) : void
    +getVersion() : long
    +setVersion(version : long) : void
    +getName() : String
    +setName(name : String) : void
    +getManagerId() : String
    +setManagerId(managerId : String) : void
    +toCsvLine() : String
    +fromCsvLine(line : String) : void
}

CsvRepository <|-- PayrollEntryRepository
PayrollEntryRepository --> PayrollEntry
PayrollEntryRepository --> PayrollRun
PayrollEntryRepository --> LeaveRequest
PayrollEntryRepository --> LeaveBalance
PayrollEntryRepository --> AttendanceRecord
PayrollEntryRepository --> Department
SalaryCalculator --> Employee
SalaryCalculator --> AttendanceRecord
SalaryCalculator --> PayrollRule
PayrollEntry --> PayrollStatus
LeaveRequest --> LeaveStatus
LeaveBalance --> LeaveType
PayrollEntry --> Employee
AttendanceRecord --> Employee
LeaveBalance --> Employee
LeaveRequest --> Employee
Department --> Employee
```
