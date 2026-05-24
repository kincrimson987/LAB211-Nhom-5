# UML Class Diagram - Payroll Management System

```mermaid
classDiagram

%% =========================
%% BASE ENTITY
%% =========================
class BaseEntity {
    <<abstract>>
    -id : String
    +getId() String
    +setId(id : String) void
    +toCsvLine() String
    +fromCsvLine(line : String) BaseEntity
}

%% =========================
%% USER ABSTRACT
%% =========================
class User {
    <<abstract>>
    -username : String
    -password : String
    -fullName : String
    -role : UserRole

    +validatePassword(password : String) boolean
}

%% =========================
%% USER CLASSES
%% =========================
class Admin {
    -adminId : String
}

class HRStaff {
    -hrId : String
    -departmentId : String
    -email : String
    -phone : String
}

class Employee {
    -employeeId : String
    -departmentId : String
    -type : EmployeeType
    -baseSalary : double
    -email : String
    -phone : String
    -joinDate : String
}

%% =========================
%% MAIN ENTITIES
%% =========================
class Department {
    -deptId : String
    -name : String
    -managerId : String
    -location : String
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
}

class LeaveRequest {
    -requestId : String
    -employeeId : String
    -leaveType : LeaveType
    -startDate : String
    -endDate : String
    -days : int
    -status : LeaveStatus
}

class AttendanceRecord {
    -attendanceId : String
    -employeeId : String
    -yearMonth : String
    -daysWorked : int
    -hoursOvertime : int
    -daysAbsent : int
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
    -version : int
}

class PayrollRun {
    -runId : String
    -yearMonth : String
    -mechanism : LockMechanism
    -elapsedMs : long
    -successCount : int
    -doublePaymentCount : int
    -wrongLeaveCount : int
    -tps : double
}

%% =========================
%% CONFIG + CALCULATOR
%% =========================
class PayrollConfig {
    -workingDaysPerMonth : int
    -overtimeRate : double
    -bonusAmount : double
    -taxRate : double
}

class SalaryCalculator {
    -config : PayrollConfig

    +calcGrossSalary(emp, att) double
    +calcTax(grossSalary) double
    +calcNetSalary(emp, att) double
}

%% =========================
%% ENUMS
%% =========================
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

%% =========================
%% EXCEPTIONS
%% =========================
class DoublePaymentException
class WrongLeaveDeductionException
class OptimisticLockException
class InsufficientLeaveException
class CsvParseException

%% =========================
%% INHERITANCE
%% =========================
BaseEntity <|-- User
BaseEntity <|-- Department
BaseEntity <|-- LeaveBalance
BaseEntity <|-- LeaveRequest
BaseEntity <|-- AttendanceRecord
BaseEntity <|-- PayrollEntry
BaseEntity <|-- PayrollRun
BaseEntity <|-- PayrollConfig

User <|-- Admin
User <|-- HRStaff
User <|-- Employee

%% =========================
%% RELATIONSHIPS
%% =========================
Employee "*" --> "1" Department
HRStaff "*" --> "1" Department

LeaveBalance "*" --> "1" Employee
LeaveRequest "*" --> "1" Employee
AttendanceRecord "*" --> "1" Employee
PayrollEntry "*" --> "1" Employee

SalaryCalculator --> PayrollConfig

Employee --> EmployeeType
Employee --> UserRole
HRStaff --> UserRole
Admin --> UserRole

LeaveBalance --> LeaveType
LeaveRequest --> LeaveType
LeaveRequest --> LeaveStatus

PayrollEntry --> PayrollStatus
PayrollRun --> LockMechanism
```
