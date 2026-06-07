```mermaid
classDiagram

class Employee {
    +String id
    +long version
    +String name
    +String email
    +String departmentId
    +Enums.EmploymentType employmentType
    +double baseSalary

    +Employee()
    +Employee(String id, long version, String name, String email, String departmentId)
    +String getId()
    +void setId(String id)
    +long getVersion()
    +void setVersion(long version)
    +String getName()
    +void setName(String name)
    +String getEmail()
    +void setEmail(String email)
    +String getDepartmentId()
    +void setDepartmentId(String departmentId)
    +Enums.EmploymentType getEmploymentType()
    +void setEmploymentType(Enums.EmploymentType employmentType)
    +double getBaseSalary()
    +void setBaseSalary(double baseSalary)
    +String toCsvLine()
    +void fromCsvLine(String line)
}

class Department {
    +String id
    +long version
    +String name
    +String managerId

    +Department()
    +Department(String id, long version, String name, String managerId)
    +String getId()
    +void setId(String id)
    +long getVersion()
    +void setVersion(long version)
    +String getName()
    +void setName(String name)
    +String getManagerId()
    +void setManagerId(String managerId)
    +String toCsvLine()
    +void fromCsvLine(String line)
}

class AttendanceRecord {
    +String id
    +long version
    +String employeeId
    +int workDays
    +double overtimeHours

    +AttendanceRecord()
    +AttendanceRecord(String id, long version, String employeeId, int workDays, double overtimeHours)
    +String getId()
    +void setId(String id)
    +long getVersion()
    +void setVersion(long version)
    +String getEmployeeId()
    +void setEmployeeId(String employeeId)
    +int getWorkDays()
    +void setWorkDays(int workDays)
    +double getOvertimeHours()
    +void setOvertimeHours(double overtimeHours)
    +String toCsvLine()
    +void fromCsvLine(String line)
}

class LeaveBalance {
    +String balanceId
    +String employeeId
    +LeaveType leaveType
    +int totalLeaveDays
    +int usedLeaveDays
    +int remainingLeaveDays
    +int version

    +LeaveBalance()
    +LeaveBalance(int totalLeaveDays, int usedLeaveDays, int remainingLeaveDays)
    +LeaveBalance(String balanceId, String employeeId, LeaveType leaveType, int totalLeaveDays)
    +String getBalanceId()
    +void setBalanceId(String balanceId)
    +String getEmployeeId()
    +void setEmployeeId(String employeeId)
    +LeaveType getLeaveType()
    +void setLeaveType(LeaveType leaveType)
    +int getTotalLeaveDays()
    +void setTotalLeaveDays(int totalLeaveDays)
    +int getUsedLeaveDays()
    +void setUsedLeaveDays(int usedLeaveDays)
    +int getRemainingLeaveDays()
    +void setRemainingLeaveDays(int remainingLeaveDays)
    +int getVersion()
    +void setVersion(int version)
    +void deductLeave(int days)
    +void addLeave(int days)
    +int checkRemaining()
    +String getCsvHeader()
    +String toCsvLine()
    +static LeaveBalance fromCsvLine(String line)
    +String toString()
}

class LeaveRequest {
    +String leaveId
    +String employeeId
    +LeaveType leaveType
    +LocalDate startDate
    +LocalDate endDate
    +String reason
    +LeaveStatus status
    +String approvedBy

    +LeaveRequest()
    +LeaveRequest(String leaveId, LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason, LeaveStatus status)
    +LeaveRequest(String leaveId, String employeeId, LeaveType leaveType, LocalDate startDate, LocalDate endDate, String reason)
    +String getLeaveId()
    +void setLeaveId(String leaveId)
    +String getEmployeeId()
    +void setEmployeeId(String employeeId)
    +LeaveType getLeaveType()
    +void setLeaveType(LeaveType leaveType)
    +LocalDate getStartDate()
    +void setStartDate(LocalDate startDate)
    +LocalDate getEndDate()
    +void setEndDate(LocalDate endDate)
    +String getReason()
    +void setReason(String reason)
    +LeaveStatus getStatus()
    +void setStatus(LeaveStatus status)
    +String getApprovedBy()
    +void setApprovedBy(String approvedBy)
    +void approve()
    +void reject()
    +int getDays()
    +String getCsvHeader()
    +String toCsvLine()
    +static LeaveRequest fromCsvLine(String line)
    +String toString()
}

Employee --> Department
Employee --> AttendanceRecord
Employee --> LeaveBalance
Employee --> LeaveRequest
LeaveRequest --> LeaveStatus
LeaveBalance --> LeaveType
```
