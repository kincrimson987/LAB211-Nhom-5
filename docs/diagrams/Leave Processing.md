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

    class LeaveRequest {
        -leaveType: LeaveType
        -startDate: LocalDate
        -endDate: LocalDate
        -reason: String
        -status: LeaveStatus
        -employeeId: String
        -approvedBy: String
        +LeaveRequest()
        +LeaveRequest(leaveId: String, leaveType: LeaveType, startDate: LocalDate, endDate: LocalDate, reason: String, status: LeaveStatus)
        +LeaveRequest(leaveId: String, employeeId: String, leaveType: LeaveType, startDate: LocalDate, endDate: LocalDate, reason: String)
        +getLeaveId() String
        +getLeaveType() LeaveType
        +getStartDate() LocalDate
        +getEndDate() LocalDate
        +getReason() String
        +getStatus() LeaveStatus
        +setLeaveId(leaveId: String) void
        +setLeaveType(leaveType: LeaveType) void
        +setStartDate(startDate: LocalDate) void
        +setEndDate(endDate: LocalDate) void
        +setReason(reason: String) void
        +setStatus(status: LeaveStatus) void
        +getEmployeeId() String
        +getApprovedBy() String
        +setEmployeeId(employeeId: String) void
        +setApprovedBy(approvedBy: String) void
        +approve() void
        +reject() void
        +getDays() int
        +getCsvHeader() String
        +toCsvLine() String
        +fromCsvLine(line: String) void
        +toString() String
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

    class LeaveStatus {
        <<enumeration>>
        PENDING
        APPROVED
        REJECTED
    }

    class LeaveType {
        <<enumeration>>
        ANNUAL
        SICK
        UNPAID
        OTHER
    }

    class LockMechanism {
        <<enumeration>>
        NO_LOCK
        SYNCHRONIZED
        OPTIMISTIC_LOCKING
        FILE_LOCK
    }

    class LeaveRequestRepository {
        +LeaveRequestRepository()
        +LeaveRequestRepository(filePath: String)
        +getHeader() String
        +getId(entity: LeaveRequest) String
        +toLine(entity: LeaveRequest) String
        +parseLine(line: String) LeaveRequest
        +findByEmployee(employeeId: String) List~LeaveRequest~
        +findByStatus(status: LeaveStatus) List~LeaveRequest~
        +findPending() List~LeaveRequest~
        +updateStatus(leaveId: String, newStatus: LeaveStatus, approvedBy: String) boolean
        +approveRequest(leaveId: String, approvedBy: String) boolean
        +rejectRequest(leaveId: String, approvedBy: String) boolean
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

    class LeaveController {
        -LEAVE_SYSTEM_START_DATE: LocalDate
        -ANNUAL_LEAVE_DAYS_PER_YEAR: int
        -SICK_LEAVE_DAYS_PER_YEAR: int
        -leaveRequestRepo: LeaveRequestRepository
        -leaveBalanceRepo: LeaveBalanceRepository
        -employeeRepo: EmployeeRepository
        +LeaveController(leaveRequestRepo: LeaveRequestRepository, leaveBalanceRepo: LeaveBalanceRepository, employeeRepo: EmployeeRepository)
        +submit(employeeId: String, leaveType: LeaveType, startDate: LocalDate, endDate: LocalDate, reason: String) LeaveRequest
        +approve(leaveId: String, approvedBy: String, lockMechanism: LockMechanism) boolean
        +reject(leaveId: String, approvedBy: String) boolean
        +getAllRequests() List~LeaveRequest~
        +getRequestsByEmployee(employeeId: String) List~LeaveRequest~
        +getPendingRequests() List~LeaveRequest~
        +getBalancesByEmployee(employeeId: String) List~LeaveBalance~
        +previewChargeableDays(employeeId: String, startDate: LocalDate, endDate: LocalDate) int
        +previewApprovalChargeableDays(leaveId: String) int
        -ensureDefaultBalances(employeeId: String) void
        -calculateChargeableDays(employeeId: String, startDate: LocalDate, endDate: LocalDate, excludeLeaveId: String, approvedOnly: boolean) int
    }

    class InsufficientLeaveBalanceException {
        +InsufficientLeaveBalanceException(message: String)
    }

    class InvalidLeaveStateException {
        +InvalidLeaveStateException(message: String)
    }

    class EmployeeRepository {
        +findById(id: String) Employee
    }

    class Employee

    BaseEntity <|-- LeaveRequest
    BaseEntity <|-- LeaveBalance

    CsvRepository~T~ <|-- LeaveRequestRepository
    CsvRepository~T~ <|-- LeaveBalanceRepository

    LeaveRequest --> LeaveType : leaveType
    LeaveRequest --> LeaveStatus : status
    LeaveBalance --> LeaveType : leaveType

    LeaveRequestRepository --> LeaveRequest : manages
    LeaveRequestRepository ..> LeaveStatus : filters/updates

    LeaveBalanceRepository --> LeaveBalance : manages
    LeaveBalanceRepository ..> LeaveType : filters/deducts

    LeaveController --> LeaveRequestRepository : uses
    LeaveController --> LeaveBalanceRepository : uses
    LeaveController --> EmployeeRepository : validates employee
    LeaveController --> LeaveRequest : creates/approves/rejects
    LeaveController --> LeaveBalance : checks/deducts
    LeaveController ..> LeaveType : uses
    LeaveController ..> LeaveStatus : checks
    LeaveController ..> LockMechanism : selects locking
    LeaveController ..> InsufficientLeaveBalanceException : throws
    LeaveController ..> InvalidLeaveStateException : throws

    EmployeeRepository --> Employee : finds
```