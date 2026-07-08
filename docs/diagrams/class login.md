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

    class UserAccount {
        -username: String
        -password: String
        -role: String
        -employeeId: String
        -active: boolean
        +UserAccount()
        +UserAccount(id: String, version: long, username: String, password: String, role: String, active: boolean)
        +UserAccount(id: String, version: long, username: String, password: String, role: String, active: boolean, employeeId: String)
        +getUsername() String
        +setUsername(username: String) void
        +getPassword() String
        +setPassword(password: String) void
        +getRole() String
        +setRole(role: String) void
        +getEmployeeId() String
        +setEmployeeId(employeeId: String) void
        +isActive() boolean
        +setActive(active: boolean) void
        +checkPassword(inputPassword: String) boolean
        +toCsvLine() String
        +fromCsvLine(line: String) void
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

    class UserAccountRepository {
        +UserAccountRepository()
        +UserAccountRepository(filePath: String)
        +getHeader() String
        +getId(entity: UserAccount) String
        +toLine(entity: UserAccount) String
        +parseLine(line: String) UserAccount
        +findByUsername(username: String) UserAccount
        +findByRole(role: String) List~UserAccount~
        +findByEmployeeId(employeeId: String) UserAccount
        +authenticate(username: String, password: String) boolean
    }

    class AuthController {
        <<abstract>>
        -account: UserAccount
        +AuthController(account: UserAccount)
        +getUsername() String
        +getRole() String
        +getEmployeeId() String
        +getAccount() UserAccount
        +canAccess(feature: String) boolean*
        +requireAccess(feature: String) void
        +toString() String
        +login(userRepository: UserAccountRepository, username: String, password: String) AuthController
    }

    class AdminAuthController {
        +AdminAuthController(account: UserAccount)
        +canAccess(feature: String) boolean
    }

    class HrAuthController {
        -ALLOWED: Set~String~
        +HrAuthController(account: UserAccount)
        +canAccess(feature: String) boolean
    }

    class EmployeeAuthController {
        +EmployeeAuthController(account: UserAccount)
        +canAccess(feature: String) boolean
    }

    class Main {
        +main(args: String[]) void
    }

    class LeaveApp {
        +main(args: String[]) void
    }

    BaseEntity <|-- UserAccount
    CsvRepository~T~ <|-- UserAccountRepository
    AuthController <|-- AdminAuthController
    AuthController <|-- HrAuthController
    AuthController <|-- EmployeeAuthController

    AuthController --> UserAccount : account
    UserAccountRepository --> UserAccount : manages
    AuthController ..> UserAccountRepository : login()
    Main ..> UserAccountRepository : creates
```