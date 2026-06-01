```mermaid
classDiagram

class Report {
    <<abstract>>
    - reportId : String
    - generatedDate : LocalDate

    + Report()
    + Report(reportId : String, generatedDate : LocalDate)

    + getReportId() String
    + setReportId(reportId : String) void

    + getGeneratedDate() LocalDate
    + setGeneratedDate(date : LocalDate) void

    + generateReport() void
    + exportReport() void

    + toString() String
}

class PayrollReport {
    + PayrollReport()
    + generatePayrollReport() void
    + exportPayrollReport() void
}

class SimulationReport {
    + SimulationReport()
    + generateSimulationReport() void
    + exportSimulationReport() void
}

class CSVHandler {
    + CSVHandler()

    + loadEmployeeCSV() List~Employee~
    + loadDepartmentCSV() List~Department~
    + loadPayrollCSV() List~Payroll~

    + saveEmployeeCSV(employeeList : List~Employee~) void
    + exportPayrollCSV(payrollList : List~Payroll~) void

    + toString() String
}

class FileManager {
    + FileManager()
    + readFile(path : String) void
    + writeFile(path : String) void
    + appendFile(path : String) void
}

class Employee
class Department
class Payroll

%% Inheritance
Report <|-- PayrollReport
Report <|-- SimulationReport

%% Dependencies
CSVHandler ..> FileManager
CSVHandler ..> Employee
CSVHandler ..> Department
CSVHandler ..> Payroll
```
