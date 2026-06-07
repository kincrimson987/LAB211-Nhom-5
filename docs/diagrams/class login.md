```mermaid
classDiagram

class BaseEntity {
    -String id
    -long version
    +String getId()
    +void setId(String id)
    +long getVersion()
    +void setVersion(long version)
    +String toCsvLine()
    +void fromCsvLine(String line)
}

class Employee {
    -String id
    -long version
    -String name
    -String email
    -String departmentId
    -Enums.EmploymentType employmentType
    -double baseSalary

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
    -String id
    -long version
    -String name
    -String managerId

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
    -String id
    -long version
    -String employeeId
    -int workDays
    -double overtimeHours

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
    -String balanceId
    -String employeeId
    -LeaveType leaveType
    -int totalLeaveDays
    -int usedLeaveDays
    -int remainingLeaveDays
    -int version

    +LeaveBalance()
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
    -String leaveId
    -String employeeId
    -LeaveType leaveType
    -LocalDate startDate
    -LocalDate endDate
    -String reason
    -LeaveStatus status
    -String approvedBy

    +LeaveRequest()
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

class PayrollEntry {
    -String id
    -long version
    -String employeeId
    -double netSalary
    -PayrollStatus status

    +PayrollEntry()
    +PayrollEntry(String id, String employeeId)
    +PayrollEntry(String id, long version, String employeeId, double netSalary, PayrollStatus status)
    +String getEntryId()
    +void setEntryId(String entryId)
    +String getEmployeeId()
    +void setEmployeeId(String employeeId)
    +double getNetSalary()
    +void setNetSalary(double netSalary)
    +PayrollStatus getStatus()
    +void setStatus(PayrollStatus status)
    +void process()
    +static String getFullCsvHeader()
    +String getCsvHeader()
    +String toCsvLine()
    +static PayrollEntry parseCsvLine(String line)
    +void fromCsvLine(String line)
    +static String extractYearMonthFromId(String id)
    +String toString()
}

class PayrollRun {
    -String id
    -long version
    -String yearMonth
    -String mechanism
    -long elapsedMs
    -int successCount
    -int doublePaymentCount
    -int wrongLeaveCount
    -double tps

    +PayrollRun()
    +PayrollRun(String runId, long version, String yearMonth, String mechanism, long elapsedMs, int successCount, int doublePaymentCount, int wrongLeaveCount, double tps)
    +String getRunId()
    +void setRunId(String runId)
    +String getYearMonth()
    +void setYearMonth(String yearMonth)
    +String getMechanism()
    +void setMechanism(String mechanism)
    +long getElapsedMs()
    +void setElapsedMs(long elapsedMs)
    +int getSuccessCount()
    +void setSuccessCount(int successCount)
    +int getDoublePaymentCount()
    +void setDoublePaymentCount(int doublePaymentCount)
    +int getWrongLeaveCount()
    +void setWrongLeaveCount(int wrongLeaveCount)
    +double getTps()
    +void setTps(double tps)
    +String toCsvLine()
    +void fromCsvLine(String line)
    +String toString()
}

class PayrollRule {
    -int standardWorkingDays
    -int workingHoursPerDay
    -double overtimeMultiplier
    -double attendanceBonus
    -double taxRate
    -double taxThreshold

    +PayrollRule()
    +PayrollRule(int standardWorkingDays, int workingHoursPerDay, double overtimeMultiplier, double attendanceBonus, double taxRate, double taxThreshold)
    +int getStandardWorkingDays()
    +void setStandardWorkingDays(int standardWorkingDays)
    +int getWorkingHoursPerDay()
    +void setWorkingHoursPerDay(int workingHoursPerDay)
    +double getOvertimeMultiplier()
    +void setOvertimeMultiplier(double overtimeMultiplier)
    +double getAttendanceBonus()
    +void setAttendanceBonus(double attendanceBonus)
    +double getTaxRate()
    +void setTaxRate(double taxRate)
    +double getTaxThreshold()
    +void setTaxThreshold(double taxThreshold)
    +String toString()
}

class CsvRepository~T~ {
    +List~T~ findAll()
    +T findById(String id)
    +void save(T entity)
    +void update(T entity)
    +void delete(String id)
}

class PayrollEntryRepository {
    +PayrollEntry findByEmployeeAndMonth(String empId, String yearMonth)
    +List~PayrollEntry~ findByStatus(PayrollStatus status)
    +long countProcessedByEmployee(String employeeId)
    +boolean processWithNoLock(String entryId, String processedBy)
    +boolean processWithFileLock(String entryId, String processedBy)
    +boolean processWithSync(String entryId, String processedBy)
    +boolean processWithOptimistic(String entryId, String processedBy)
}

class LeaveType {
    <<enumeration>>
    ANNUAL
    SICK
}

class LeaveStatus {
    <<enumeration>>
    PENDING
    APPROVED
    REJECTED
}

class PayrollStatus {
    <<enumeration>>
    PENDING
    PROCESSED
}

class EmploymentType {
    <<enumeration>>
    FULLTIME
    PARTTIME
}

BaseEntity <|-- Employee
BaseEntity <|-- Department
BaseEntity <|-- AttendanceRecord
BaseEntity <|-- PayrollEntry
BaseEntity <|-- PayrollRun

Employee --> Department
Employee --> AttendanceRecord
Employee --> LeaveBalance
Employee --> LeaveRequest
PayrollEntry --> PayrollStatus
LeaveRequest --> LeaveStatus
LeaveBalance --> LeaveType
LeaveRequest --> LeaveType
```
