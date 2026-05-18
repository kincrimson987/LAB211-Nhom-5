# Báo cáo Thiết Kế Kiến Trúc & Cấu trúc Dữ Liệu
**Project:** Employee Payroll Management Simulation (LAB211)

---

## 1. Class Diagram Tổng Quan (Mô hình MVC - Phần MODEL)
Tất cả các mô hình dữ liệu chính yếu (Entity) đều được kế thừa từ `BaseEntity` để chia sẻ khóa chính dạng chuỗi (`id`) và thuộc tính `version` (kiểu long) dùng làm nền tảng cho cơ chế **Optimistic Locking** ngăn ngừa race condition.

```mermaid
classDiagram
    %% Định nghĩa các class
    class BaseEntity {
        <<infra>>
        +String id (PK)
        +long version (locking)
    }

    class Employee {
        +String name
        +String email
        +String departmentId (FK)
    }

    class Department {
        +String name
        +String managerId (FK)
    }

    class LeaveBalance {
        <<critical>>
        +int annualRemaining
        +int sickRemaining
    }

    class LeaveRequest {
        +String employeeId (FK)
        +LeaveType type
        +int days
        +LeaveStatus status
    }

    class AttendanceRecord {
        +String employeeId (FK)
        +int workDays
        +double overtimeHours
    }

    class PayrollEntry {
        <<critical>>
        +String employeeId (FK)
        +double netSalary
        +PayrollStatus status
    }

    class PayrollRun {
        +int month
        +int year
        +LockMechanism lockMechanism
        +int doublePaymentCount
        +int wrongLeaveCount
        +long elapsedMs
        +double tps
    }

    %% Kế thừa (Inheritance)
    BaseEntity <|-- Employee
    BaseEntity <|-- Department
    BaseEntity <|-- LeaveBalance
    BaseEntity <|-- LeaveRequest
    BaseEntity <|-- AttendanceRecord
    BaseEntity <|-- PayrollEntry
    BaseEntity <|-- PayrollRun
```

---

## 2. Schema Thực tế tại 7 File CSV

Tổng quan dữ liệu được tự động sinh ngẫu nhiên tuân thủ tuyệt đối quy tắc của lược đồ lớp trên. 

| Tên File | Thuộc tính (Cột) | Kiểu Dữ Liệu Tương Ứng | Ghi chú (Khóa Ngoại / Vai trò) |
| --- | --- | --- | --- |
| **`departments.csv`** | `id`<br>`version`<br>`name`<br>`managerId` | String (Ví dụ: `D001`)<br>long (Luôn = 1)<br>String<br>String | <br>Cột locking.<br><br>**FK** trỏ tới `id` trong `employees.csv` |
| **`employees.csv`** | `id`<br>`version`<br>`name`<br>`email`<br>`departmentId` | String (Ví dụ: `E0001`)<br>long<br>String<br>String<br>String | <br><br><br><br>**FK** trỏ tới `id` trong `departments.csv` |
| **`leave_balances.csv`** | `id`<br>`version`<br>`annualRemaining`<br>`sickRemaining` | String (Dùng chung Employee ID)<br>long<br>int<br>int | <br>⚡ Cực kỳ quan trọng để test **Wrong Leave Deduction**.<br>(Trừ phép xong version tăng lên 1) |
| **`leave_requests.csv`** | `id`<br>`version`<br>`employeeId`<br>`type`<br>`days`<br>`status` | String (Ví dụ: LR_...)<br>long<br>String<br>Enum *(ANNUAL/SICK)*<br>int<br>Enum *(PENDING/APPROVED/REJECTED)* | <br><br>**FK** trỏ tới `id` trong Employee.<br><br><br> |
| **`attendance.csv`** | `id`<br>`version`<br>`employeeId`<br>`workDays`<br>`overtimeHours` | String (Ví dụ: A_...)<br>long<br>String<br>int<br>double | <br><br>**FK** trỏ tới `id` trong Employee.<br><br> *Note: Bản ghi được tổng hợp theo tháng.* |
| **`payroll_entries.csv`** | `id`<br>`version`<br>`employeeId`<br>`netSalary`<br>`status` | String (Ví dụ: PR_...)<br>long<br>String<br>double<br>Enum *(PENDING/PROCESSED)* | <br>⚡ Cực kỳ quan trọng để test **Double Payment**.<br>**FK** trỏ tới `id` Employee.<br><br>(Thread xử lý xong sửa PENDING thành PROCESSED) |
| **`payroll_runs.csv`** | `id`<br>`version`<br>`month`<br>`year`<br>`lockMechanism`<br>`doublePaymentCount`<br>`wrongLeaveCount`<br>`elapsedMs`<br>`tps` | String (VD: RUN_NONE_...)<br>long<br>int<br>int<br>Enum *(OPTIMISTIC/QUEUE/...)*<br>int<br>int<br>long<br>double | Lưu trữ output kết quả cho báo cáo phân tích hiệu năng của Simulator. Đặc biệt là thông số count xem thuật toán nào bỏ lọt lỗi. |

---

## 3. Quá Trình Build File Dữ Liệu
Tool đã được cài đặt thuật toán để sinh dữ liệu chéo nhau theo nguyên tắc:
- Tổng số nhân viên (`NUM_EMPLOYEES`): 1,200 người.
- Tổng giả lập: **12 Tháng**.
- Điều này giúp file báo cáo `attendance.csv` và `payroll_entries.csv` đạt ngưỡng **14,400 Records mỗi file**, vượt mức yêu cầu >= 10.000 dòng.
