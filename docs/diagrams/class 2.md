```mermaid

classDiagram

class BaseEntity {
    -id : String
    -version : long

    +getId() String
    +setId(id : String) void
    +getVersion() long
    +setVersion(version : long) void
    +toCsvLine() String
    +fromCsvLine(line : String) void
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
    +Employee(id : String, version : long, name : String, email : String, departmentId : String)

    +getId() String
    +setId(id : String) void
    +getVersion() long
    +setVersion(version : long) void
    +getName() String
    +setName(name : String) void
    +getEmail() String
    +setEmail(email : String) void
    +getDepartmentId() String
    +setDepartmentId(departmentId : String) void
    +getEmploymentType() Enums.EmploymentType
    +setEmploymentType(employmentType : Enums.EmploymentType) void
    +getBaseSalary() double
    +setBaseSalary(baseSalary : double) void
    +toCsvLine() String
    +fromCsvLine(line : String) void
}

class Department {
    -id : String
    -version : long
    -name : String
    -managerId : String

    +Department()
    +Department(id : String, version : long, name : String, managerId : String)

    +getId() String
    +setId(id : String) void
    +getVersion() long
    +setVersion(version : long) void
    +getName() String
    +setName(name : String) void
    +getManagerId() String
    +setManagerId(managerId : String) void
    +toCsvLine() String
    +fromCsvLine(line : String) void
}

class AttendanceRecord {
    -id : String
    -version : long
    -employeeId : String
    -workDays : int
    -overtimeHours : double

    +AttendanceRecord()
    +AttendanceRecord(id : String, version : long, employeeId : String, workDays : int, overtimeHours : double)

    +getId() String
    +setId(id : String) void
    +getVersion() long
    +setVersion(version : long) void
    +getEmployeeId() String
    +setEmployeeId(employeeId : String) void
    +getWorkDays() int
    +setWorkDays(workDays : int) void
    +getOvertimeHours() double
    +setOvertimeHours(overtimeHours : double) void
    +toCsvLine() String
    +fromCsvLine(line : String) void
}

class LeaveBalance {
    -balanceId : String
    -employeeId : String
    -leaveType : LeaveType
    -totalLeaveDays : int
    -usedLeaveDays : int
    -remainingLeaveDays : int
    -version : int

    +LeaveBalance()
    +LeaveBalance(balanceId : String, employeeId : String, leaveType : LeaveType, totalLeaveDays : int)

    +getBalanceId() String
    +setBalanceId(balanceId : String) void
    +getEmployeeId() String
    +setEmployeeId(employeeId : String) void
    +getLeaveType() LeaveType
    +setLeaveType(leaveType : LeaveType) void
    +getTotalLeaveDays() int
    +setTotalLeaveDays(totalLeaveDays : int) void
    +getUsedLeaveDays() int
    +setUsedLeaveDays(usedLeaveDays : int) void
    +getRemainingLeaveDays() int
    +setRemainingLeaveDays(remainingLeaveDays : int) void
    +getVersion() int
    +setVersion(version : int) void
    +deductLeave(days : int) void
    +addLeave(days : int) void
    +checkRemaining() int
    +toCsvLine() String
    +fromCsvLine(line : String) LeaveBalance
}

class LeaveRequest {
    -leaveId : String
    -employeeId : String
    -leaveType : LeaveType
    -startDate : LocalDate
    -endDate : LocalDate
    -reason : String
    -status : LeaveStatus
    -approvedBy : String

    +LeaveRequest()
    +LeaveRequest(leaveId : String, employeeId : String, leaveType : LeaveType, startDate : LocalDate, endDate : LocalDate, reason : String)

    +getLeaveId() String
    +setLeaveId(leaveId : String) void
    +getEmployeeId() String
    +setEmployeeId(employeeId : String) void
    +getLeaveType() LeaveType
    +setLeaveType(leaveType : LeaveType) void
    +getStartDate() LocalDate
    +setStartDate(startDate : LocalDate) void
    +getEndDate() LocalDate
    +setEndDate(endDate : LocalDate) void
    +getReason() String
    +setReason(reason : String) void
    +getStatus() LeaveStatus
    +setStatus(status : LeaveStatus) void
    +getApprovedBy() String
    +setApprovedBy(approvedBy : String) void
    +approve() void
    +reject() void
    +getDays() int
    +toCsvLine() String
    +fromCsvLine(line : String) LeaveRequest
}

class PayrollEntry {
    -id : String
    -version : long
    -employeeId : String
    -netSalary : double
    -status : PayrollStatus

    +PayrollEntry()
    +PayrollEntry(id : String, employeeId : String)
    +PayrollEntry(id : String, version : long, employeeId : String, netSalary : double, status : PayrollStatus)

    +getId() String
    +setId(id : String) void
    +getVersion() long
    +setVersion(version : long) void
    +getEmployeeId() String
    +setEmployeeId(employeeId : String) void
    +getNetSalary() double
    +setNetSalary(netSalary : double) void
    +getStatus() PayrollStatus
    +setStatus(status : PayrollStatus) void
    +process() void
    +toCsvLine() String
    +fromCsvLine(line : String) void
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
    +PayrollRun(id : String, version : long, yearMonth : String, mechanism : String, elapsedMs : long, successCount : int, doublePaymentCount : int, wrongLeaveCount : int, tps : double)

    +getId() String
    +setId(id : String) void
    +getVersion() long
    +setVersion(version : long) void
    +getYearMonth() String
    +setYearMonth(yearMonth : String) void
    +getMechanism() String
    +setMechanism(mechanism : String) void
    +getElapsedMs() long
    +setElapsedMs(elapsedMs : long) void
    +getSuccessCount() int
    +setSuccessCount(successCount : int) void
    +getDoublePaymentCount() int
    +setDoublePaymentCount(doublePaymentCount : int) void
    +getWrongLeaveCount() int
    +setWrongLeaveCount(wrongLeaveCount : int) void
    +getTps() double
    +setTps(tps : double) void
    +toCsvLine() String
    +fromCsvLine(line : String) void
}

class PayrollRule {
    -standardWorkingDays : int
    -workingHoursPerDay : int
    -overtimeMultiplier : double
    -attendanceBonus : double
    -taxRate : double
    -taxThreshold : double

    +PayrollRule()
    +PayrollRule(standardWorkingDays : int, workingHoursPerDay : int, overtimeMultiplier : double, attendanceBonus : double, taxRate : double, taxThreshold : double)

    +getStandardWorkingDays() int
    +setStandardWorkingDays(standardWorkingDays : int) void
    +getWorkingHoursPerDay() int
    +setWorkingHoursPerDay(workingHoursPerDay : int) void
    +getOvertimeMultiplier() double
    +setOvertimeMultiplier(overtimeMultiplier : double) void
    +getAttendanceBonus() double
    +setAttendanceBonus(attendanceBonus : double) void
    +getTaxRate() double
    +setTaxRate(taxRate : double) void
    +getTaxThreshold() double
    +setTaxThreshold(taxThreshold : double) void
}

Employee --> Department
Employee --> AttendanceRecord
Employee --> LeaveBalance
Employee --> LeaveRequest
PayrollEntry --> PayrollStatus
LeaveRequest --> LeaveStatus
LeaveBalance --> LeaveType
LeaveRequest --> LeaveType

BaseEntity <|-- Employee
BaseEntity <|-- Department
BaseEntity <|-- AttendanceRecord
BaseEntity <|-- PayrollEntry
BaseEntity <|-- PayrollRun
```


