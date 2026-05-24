```mermaid
classDiagram
    class BaseEntity {
        <<abstract>>
        -id : String
        +BaseEntity()
        +BaseEntity(id : String)
        +getId() String
        +setId(id : String) void
        +toCsvLine() String
        +fromCsvLine(line : String) BaseEntity
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class Admin {
        -adminId : String
        -username : String
        -password : String
        -fullName : String
        -role : UserRole
        +Admin()
        +Admin(adminId : String, username : String, password : String, fullName : String)
        +getAdminId() String
        +setAdminId(adminId : String) void
        +getUsername() String
        +setUsername(username : String) void
        +getPassword() String
        +setPassword(password : String) void
        +getFullName() String
        +setFullName(fullName : String) void
        +getRole() UserRole
        +setRole(role : UserRole) void
        +validatePassword(pwd : String) boolean
        +toCsvLine() String
        +fromCsvLine(line : String) Admin
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class HRStaff {
        -hrId : String
        -username : String
        -password : String
        -fullName : String
        -departmentId : String
        -email : String
        -phone : String
        -role : UserRole
        +HRStaff()
        +HRStaff(hrId : String, username : String, password : String, fullName : String, departmentId : String)
        +getHrId() String
        +setHrId(hrId : String) void
        +getUsername() String
        +setUsername(username : String) void
        +getPassword() String
        +setPassword(password : String) void
        +getFullName() String
        +setFullName(fullName : String) void
        +getDepartmentId() String
        +setDepartmentId(departmentId : String) void
        +getEmail() String
        +setEmail(email : String) void
        +getPhone() String
        +setPhone(phone : String) void
        +getRole() UserRole
        +setRole(role : UserRole) void
        +validatePassword(pwd : String) boolean
        +toCsvLine() String
        +fromCsvLine(line : String) HRStaff
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class Employee {
        -employeeId : String
        -username : String
        -password : String
        -fullName : String
        -departmentId : String
        -type : EmployeeType
        -baseSalary : double
        -email : String
        -phone : String
        -joinDate : String
        -role : UserRole
        +Employee()
        +Employee(employeeId : String, username : String, password : String, fullName : String, departmentId : String, type : EmployeeType, baseSalary : double)
        +getEmployeeId() String
        +setEmployeeId(employeeId : String) void
        +getUsername() String
        +setUsername(username : String) void
        +getPassword() String
        +setPassword(password : String) void
        +getFullName() String
        +setFullName(fullName : String) void
        +getDepartmentId() String
        +setDepartmentId(departmentId : String) void
        +getType() EmployeeType
        +setType(type : EmployeeType) void
        +getBaseSalary() double
        +setBaseSalary(baseSalary : double) void
        +getEmail() String
        +setEmail(email : String) void
        +getPhone() String
        +setPhone(phone : String) void
        +getJoinDate() String
        +setJoinDate(joinDate : String) void
        +getRole() UserRole
        +setRole(role : UserRole) void
        +validatePassword(pwd : String) boolean
        +toCsvLine() String
        +fromCsvLine(line : String) Employee
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class Department {
        -deptId : String
        -name : String
        -managerId : String
        -location : String
        -employeeCount : int
        +Department()
        +Department(deptId : String, name : String, managerId : String)
        +getDeptId() String
        +setDeptId(deptId : String) void
        +getName() String
        +setName(name : String) void
        +getManagerId() String
        +setManagerId(managerId : String) void
        +getLocation() String
        +setLocation(location : String) void
        +getEmployeeCount() int
        +setEmployeeCount(count : int) void
        +toCsvLine() String
        +fromCsvLine(line : String) Department
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class LeaveBalance {
        -balanceId : String
        -employeeId : String
        -leaveType : LeaveType
        -totalDays : int
        -remainingDays : int
        -usedDays : int
        -year : int
        -version : int
        +LeaveBalance()
        +LeaveBalance(balanceId : String, employeeId : String, leaveType : LeaveType, totalDays : int)
        +getBalanceId() String
        +setBalanceId(balanceId : String) void
        +getEmployeeId() String
        +setEmployeeId(employeeId : String) void
        +getLeaveType() LeaveType
        +setLeaveType(leaveType : LeaveType) void
        +getTotalDays() int
        +setTotalDays(totalDays : int) void
        +getRemainingDays() int
        +setRemainingDays(remainingDays : int) void
        +getUsedDays() int
        +setUsedDays(usedDays : int) void
        +getYear() int
        +setYear(year : int) void
        +getVersion() int
        +setVersion(version : int) void
        +toCsvLine() String
        +fromCsvLine(line : String) LeaveBalance
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class LeaveRequest {
        -requestId : String
        -employeeId : String
        -leaveType : LeaveType
        -startDate : String
        -endDate : String
        -days : int
        -reason : String
        -status : LeaveStatus
        -approvedBy : String
        -createdAt : String
        +LeaveRequest()
        +LeaveRequest(requestId : String, employeeId : String, leaveType : LeaveType, startDate : String, days : int)
        +getRequestId() String
        +setRequestId(requestId : String) void
        +getEmployeeId() String
        +setEmployeeId(employeeId : String) void
        +getLeaveType() LeaveType
        +setLeaveType(leaveType : LeaveType) void
        +getStartDate() String
        +setStartDate(startDate : String) void
        +getEndDate() String
        +setEndDate(endDate : String) void
        +getDays() int
        +setDays(days : int) void
        +getReason() String
        +setReason(reason : String) void
        +getStatus() LeaveStatus
        +setStatus(status : LeaveStatus) void
        +getApprovedBy() String
        +setApprovedBy(approvedBy : String) void
        +getCreatedAt() String
        +setCreatedAt(createdAt : String) void
        +toCsvLine() String
        +fromCsvLine(line : String) LeaveRequest
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class AttendanceRecord {
        -attendanceId : String
        -employeeId : String
        -yearMonth : String
        -daysWorked : int
        -hoursOvertime : int
        -daysAbsent : int
        -daysLate : int
        -notes : String
        +AttendanceRecord()
        +AttendanceRecord(attendanceId : String, employeeId : String, yearMonth : String, daysWorked : int, hoursOvertime : int, daysAbsent : int)
        +getAttendanceId() String
        +setAttendanceId(attendanceId : String) void
        +getEmployeeId() String
        +setEmployeeId(employeeId : String) void
        +getYearMonth() String
        +setYearMonth(yearMonth : String) void
        +getDaysWorked() int
        +setDaysWorked(daysWorked : int) void
        +getHoursOvertime() int
        +setHoursOvertime(hoursOvertime : int) void
        +getDaysAbsent() int
        +setDaysAbsent(daysAbsent : int) void
        +getDaysLate() int
        +setDaysLate(daysLate : int) void
        +getNotes() String
        +setNotes(notes : String) void
        +toCsvLine() String
        +fromCsvLine(line : String) AttendanceRecord
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class PayrollEntry {
        -entryId : String
        -employeeId : String
        -yearMonth : String
        -baseSalary : double
        -overtimePay : double
        -bonus : double
        -deduction : double
        -tax : double
        -netSalary : double
        -status : PayrollStatus
        -processedBy : String
        -processedAt : String
        -version : int
        +PayrollEntry()
        +PayrollEntry(entryId : String, employeeId : String, yearMonth : String)
        +getEntryId() String
        +setEntryId(entryId : String) void
        +getEmployeeId() String
        +setEmployeeId(employeeId : String) void
        +getYearMonth() String
        +setYearMonth(yearMonth : String) void
        +getBaseSalary() double
        +setBaseSalary(baseSalary : double) void
        +getOvertimePay() double
        +setOvertimePay(overtimePay : double) void
        +getBonus() double
        +setBonus(bonus : double) void
        +getDeduction() double
        +setDeduction(deduction : double) void
        +getTax() double
        +setTax(tax : double) void
        +getNetSalary() double
        +setNetSalary(netSalary : double) void
        +getStatus() PayrollStatus
        +setStatus(status : PayrollStatus) void
        +getProcessedBy() String
        +setProcessedBy(processedBy : String) void
        +getProcessedAt() String
        +setProcessedAt(processedAt : String) void
        +getVersion() int
        +setVersion(version : int) void
        +toCsvLine() String
        +fromCsvLine(line : String) PayrollEntry
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class PayrollRun {
        -runId : String
        -yearMonth : String
        -mechanism : LockMechanism
        -startTime : String
        -endTime : String
        -elapsedMs : long
        -totalEmployees : int
        -successCount : int
        -failedCount : int
        -doublePaymentCount : int
        -wrongLeaveCount : int
        -tps : double
        +PayrollRun()
        +PayrollRun(runId : String, yearMonth : String, mechanism : LockMechanism)
        +getRunId() String
        +setRunId(runId : String) void
        +getYearMonth() String
        +setYearMonth(yearMonth : String) void
        +getMechanism() LockMechanism
        +setMechanism(mechanism : LockMechanism) void
        +getStartTime() String
        +setStartTime(startTime : String) void
        +getEndTime() String
        +setEndTime(endTime : String) void
        +getElapsedMs() long
        +setElapsedMs(elapsedMs : long) void
        +getTotalEmployees() int
        +setTotalEmployees(totalEmployees : int) void
        +getSuccessCount() int
        +setSuccessCount(successCount : int) void
        +getFailedCount() int
        +setFailedCount(failedCount : int) void
        +getDoublePaymentCount() int
        +setDoublePaymentCount(count : int) void
        +getWrongLeaveCount() int
        +setWrongLeaveCount(count : int) void
        +getTps() double
        +setTps(tps : double) void
        +toCsvLine() String
        +fromCsvLine(line : String) PayrollRun
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class PayrollConfig {
        -configId : String
        -workingDaysPerMonth : int
        -overtimeRate : double
        -bonusAmount : double
        -taxThreshold : double
        -taxRate : double
        -updatedBy : String
        -updatedAt : String
        +PayrollConfig()
        +PayrollConfig(workingDaysPerMonth : int, overtimeRate : double, bonusAmount : double, taxThreshold : double, taxRate : double)
        +getConfigId() String
        +setConfigId(configId : String) void
        +getWorkingDaysPerMonth() int
        +setWorkingDaysPerMonth(days : int) void
        +getOvertimeRate() double
        +setOvertimeRate(rate : double) void
        +getBonusAmount() double
        +setBonusAmount(amount : double) void
        +getTaxThreshold() double
        +setTaxThreshold(threshold : double) void
        +getTaxRate() double
        +setTaxRate(rate : double) void
        +getUpdatedBy() String
        +setUpdatedBy(updatedBy : String) void
        +getUpdatedAt() String
        +setUpdatedAt(updatedAt : String) void
        +toCsvLine() String
        +fromCsvLine(line : String) PayrollConfig
        +getCsvHeader() String
        +toString() String
        +equals(obj : Object) boolean
        +hashCode() int
    }

    class SalaryCalculator {
        -config : PayrollConfig
        +SalaryCalculator()
        +SalaryCalculator(config : PayrollConfig)
        +getConfig() PayrollConfig
        +setConfig(config : PayrollConfig) void
        +calcBaseSalary(emp : Employee, att : AttendanceRecord) double
        +calcOvertime(emp : Employee, att : AttendanceRecord) double
        +calcBonus(att : AttendanceRecord) double
        +calcDeduction(emp : Employee, att : AttendanceRecord) double
        +calcTax(grossSalary : double) double
        +calcNetSalary(emp : Employee, att : AttendanceRecord) double
        +calcGrossSalary(emp : Employee, att : AttendanceRecord) double
    }

    class EmployeeType {
        <<enumeration>>
        FULLTIME
        PARTTIME
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

    class LockMechanism {
        <<enumeration>>
        NO_LOCK
        FILE_LOCK
        SYNCHRONIZED
        OPTIMISTIC
    }

    class UserRole {
        <<enumeration>>
        ADMIN
        HR_STAFF
        EMPLOYEE
    }

    class DoublePaymentException {
        -employeeId : String
        -yearMonth : String
        +DoublePaymentException(employeeId : String, yearMonth : String)
        +getEmployeeId() String
        +getYearMonth() String
        +getMessage() String
    }

    class WrongLeaveDeductionException {
        -employeeId : String
        -expected : int
        -actual : int
        +WrongLeaveDeductionException(employeeId : String, expected : int, actual : int)
        +getEmployeeId() String
        +getExpected() int
        +getActual() int
        +getMessage() String
    }

    class OptimisticLockException {
        -employeeId : String
        -maxRetry : int
        +OptimisticLockException(employeeId : String, maxRetry : int)
        +getEmployeeId() String
        +getMaxRetry() int
        +getMessage() String
    }

    class InsufficientLeaveException {
        -employeeId : String
        -requested : int
        -remaining : int
        +InsufficientLeaveException(employeeId : String, requested : int, remaining : int)
        +getEmployeeId() String
        +getRequested() int
        +getRemaining() int
        +getMessage() String
    }

    class CsvParseException {
        -filename : String
        -lineNumber : int
        -rawLine : String
        +CsvParseException(filename : String, lineNumber : int, rawLine : String)
        +getFilename() String
        +getLineNumber() int
        +getRawLine() String
        +getMessage() String
    }

    class AuthenticationException {
        -username : String
        -role : UserRole
        +AuthenticationException(username : String, role : UserRole)
        +getUsername() String
        +getRole() UserRole
        +getMessage() String
    }

    BaseEntity <|-- Admin
    BaseEntity <|-- HRStaff
    BaseEntity <|-- Employee
    BaseEntity <|-- Department
    BaseEntity <|-- LeaveBalance
    BaseEntity <|-- LeaveRequest
    BaseEntity <|-- AttendanceRecord
    BaseEntity <|-- PayrollEntry
    BaseEntity <|-- PayrollRun
    BaseEntity <|-- PayrollConfig

    Employee "many" --> "1" Department : thuộc về
    HRStaff "many" --> "1" Department : thuộc về
    LeaveBalance "1..*" --> "1" Employee : của
    LeaveRequest "0..*" --> "1" Employee : nộp bởi
    AttendanceRecord "1..*" --> "1" Employee : của
    PayrollEntry "1..*" --> "1" Employee : của

    Employee --> EmployeeType
    Employee --> UserRole
    HRStaff --> UserRole
    Admin --> UserRole
    LeaveBalance --> LeaveType
    LeaveRequest --> LeaveType
    LeaveRequest --> LeaveStatus
    PayrollEntry --> PayrollStatus
    PayrollRun --> LockMechanism
    SalaryCalculator --> PayrollConfig
```
