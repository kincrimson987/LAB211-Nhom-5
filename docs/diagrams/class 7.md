```mermaid
classDiagram

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

class Department {
    -id : String
    -version : long
    -name : String
    -managerId : String

    +Department()
    +Department(id : String, version : long, name : String, managerId : String)
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

class AttendanceRecord {
    -id : String
    -version : long
    -employeeId : String
    -workDays : int
    -overtimeHours : double

    +AttendanceRecord()
    +AttendanceRecord(id : String, version : long, employeeId : String, workDays : int, overtimeHours : double)
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
    -leaveType : LeaveType
    -totalLeaveDays : int
    -usedLeaveDays : int
    -remainingLeaveDays : int
    -version : int

    +LeaveBalance()
    +LeaveBalance(totalLeaveDays : int, usedLeaveDays : int, remainingLeaveDays : int)
    +LeaveBalance(balanceId : String, employeeId : String, leaveType : LeaveType, totalLeaveDays : int)
    +getBalanceId() : String
    +setBalanceId(balanceId : String) : void
    +getEmployeeId() : String
    +setEmployeeId(employeeId : String) : void
    +getLeaveType() : LeaveType
    +setLeaveType(leaveType : LeaveType) : void
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
    -leaveType : LeaveType
    -startDate : LocalDate
    -endDate : LocalDate
    -reason : String
    -status : LeaveStatus
    -approvedBy : String

    +LeaveRequest()
    +LeaveRequest(leaveId : String, leaveType : LeaveType, startDate : LocalDate, endDate : LocalDate, reason : String, status : LeaveStatus)
    +LeaveRequest(leaveId : String, employeeId : String, leaveType : LeaveType, startDate : LocalDate, endDate : LocalDate, reason : String)
    +getLeaveId() : String
    +setLeaveId(leaveId : String) : void
    +getEmployeeId() : String
    +setEmployeeId(employeeId : String) : void
    +getLeaveType() : LeaveType
    +setLeaveType(leaveType : LeaveType) : void
    +getStartDate() : LocalDate
    +setStartDate(startDate : LocalDate) : void
    +getEndDate() : LocalDate
    +setEndDate(endDate : LocalDate) : void
    +getReason() : String
    +setReason(reason : String) : void
    +getStatus() : LeaveStatus
    +setStatus(status : LeaveStatus) : void
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

Employee --> Department
Employee --> AttendanceRecord
Employee --> LeaveBalance
Employee --> LeaveRequest
LeaveRequest --> LeaveStatus
LeaveBalance --> LeaveType
```
