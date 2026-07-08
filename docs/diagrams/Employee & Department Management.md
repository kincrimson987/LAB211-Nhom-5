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

    class Employee {
        <<abstract>>
        -name: String
        -email: String
        -departmentId: String
        -employmentType: EmployeeType
        -baseSalary: double
        +Employee()
        +Employee(id: String, version: long, name: String, email: String, departmentId: String)
        +Employee(id: String, version: long, name: String, email: String, departmentId: String, baseSalary: double)
        +getName() String
        +setName(name: String) void
        +getEmail() String
        +setEmail(email: String) void
        +getDepartmentId() String
        +setDepartmentId(departmentId: String) void
        +getEmploymentType() EmployeeType
        +setEmploymentType(employmentType: EmployeeType) void
        +getBaseSalary() double
        +setBaseSalary(baseSalary: double) void
        +validateAttendance(attendance: AttendanceRecord) void
        +roundMoney(value: double) double
        +toCsvLine() String
        +fromCsvLine(line: String) void
        +calculateSalary(attendance: AttendanceRecord, rule: PayrollRule) double*
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

    class EmployeeType {
        <<enumeration>>
        FULLTIME
        PARTTIME
    }

    class Department {
        -name: String
        -managerId: String
        +Department()
        +Department(id: String, version: long, name: String, managerId: String)
        +getName() String
        +setName(name: String) void
        +getManagerId() String
        +setManagerId(managerId: String) void
        +toCsvLine() String
        +fromCsvLine(line: String) void
    }

    class EmployeeNotFoundException {
        +EmployeeNotFoundException(message: String)
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

    class EmployeeRepository {
        +EmployeeRepository()
        +EmployeeRepository(filePath: String)
        +getHeader() String
        +getId(entity: Employee) String
        +toLine(entity: Employee) String
        +parseLine(line: String) Employee
        +findByDepartment(departmentId: String) List~Employee~
        +findByType(type: EmployeeType) List~Employee~
        +searchByName(keyword: String) List~Employee~
    }

    class DepartmentRepository {
        +DepartmentRepository()
        +DepartmentRepository(filePath: String)
        +getHeader() String
        +getId(entity: Department) String
        +toLine(entity: Department) String
        +parseLine(line: String) Department
        +findByName(name: String) Department
    }

    class EmployeeController {
        -EMPLOYEE_ID_SEED: int
        -EMPLOYEE_SEQUENCE_FILE: String
        -employeeRepository: EmployeeRepository
        -departmentRepository: DepartmentRepository
        +EmployeeController(employeeRepository: EmployeeRepository, departmentRepository: DepartmentRepository)
        +addEmployee(name: String, email: String, departmentId: String, type: EmployeeType, baseSalary: double) Employee
        +getAllEmployees() List~Employee~
        +getEmployeeById(id: String) Employee
        +getEmployeesByDepartment(departmentId: String) List~Employee~
        +getEmployeesByType(type: EmployeeType) List~Employee~
        +searchByName(keyword: String) List~Employee~
        +updateEmployee(id: String, name: String, email: String, departmentId: String, baseSalary: double) Employee
        +deleteEmployee(id: String) void
        -generateId() String
        -getNextEmployeeNumber() int
        -extractEmployeeNumber(employeeId: String) int
        -readLastEmployeeNumber() int
        -saveLastEmployeeNumber(number: int) void
        -validateName(name: String) void
        -validateEmail(email: String) void
        -checkEmailUnique(email: String, excludeId: String) void
        -validateDepartmentExists(departmentId: String) void
        -validateBaseSalary(baseSalary: double) void
    }

    class DepartmentController {
        -departmentRepository: DepartmentRepository
        +DepartmentController(departmentRepository: DepartmentRepository)
        +addDepartment(name: String, managerId: String) Department
        +getDepartmentById(id: String) Department
        +getAllDepartments() List~Department~
        +searchByName(keyword: String) List~Department~
        +updateDepartment(id: String, name: String, managerId: String) Department
        +deleteDepartment(id: String) void
        +saveCsv() void
        +loadCsv() void
        -generateId() String
    }

    class AttendanceRecord
    class PayrollRule

    BaseEntity <|-- Employee
    BaseEntity <|-- Department

    Employee <|-- FullTimeEmployee
    Employee <|-- PartTimeEmployee

    CsvRepository~T~ <|-- EmployeeRepository
    CsvRepository~T~ <|-- DepartmentRepository

    Employee --> EmployeeType : employmentType
    Employee --> AttendanceRecord : validateAttendance()
    Employee --> PayrollRule : calculateSalary()

    EmployeeRepository --> Employee : manages
    EmployeeRepository ..> FullTimeEmployee : creates
    EmployeeRepository ..> PartTimeEmployee : creates
    EmployeeRepository ..> EmployeeType : checks

    DepartmentRepository --> Department : manages

    EmployeeController --> EmployeeRepository : uses
    EmployeeController --> DepartmentRepository : validates department
    EmployeeController --> Employee : creates/updates/deletes
    EmployeeController ..> FullTimeEmployee : creates
    EmployeeController ..> PartTimeEmployee : creates
    EmployeeController ..> EmployeeType : uses

    DepartmentController --> DepartmentRepository : uses
    DepartmentController --> Department : creates/updates/deletes
```