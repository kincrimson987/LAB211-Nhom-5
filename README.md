# LAB211 - NHÓM 5

## Thành viên 
- Lương Triều Vỹ
- Nguyễn Hoài Nhi
- Nguyễn Trường Phúc
- Đoàn Mạnh Đạt

## Project
# 💼 Employee Payroll Management System
( Hệ thống Mô phỏng Quản lý Bảng lương Nhân viên )
---

## 📋 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cách compile và chạy](#cách-compile-và-chạy)
- [Chạy DataGenerator](#chạy-datagenerator)
- [Chạy Simulator](#chạy-simulator)

---

## 🎯 Giới thiệu

Dự án mô phỏng quy trình xử lý bảng lương hàng tháng của một công ty với nhiều phòng ban. Hệ thống nghiên cứu và giải quyết bài toán **Race Condition** khi nhiều HR Thread xử lý bảng lương đồng thời.

### Research Question

> *"Cơ chế đồng bộ nào đạt đồng thời 0% Double Payment VÀ 0% Wrong Leave Deduction VÀ tổng thời gian xử lý bảng lương 1000 nhân viên ngắn nhất?"*

### 4 cơ chế được so sánh

| Cơ chế | Mô tả | An toàn |
|--------|-------|---------|
| `NO_LOCK` | Không đồng bộ | ❌ |
| `FILE_LOCK` | Khóa toàn bộ file CSV | ✅ |
| `SYNCHRONIZED` | Khóa per-employee | ✅ |
| `OPTIMISTIC` | Version field + retry | ✅ |

---

## 📁 Cấu trúc dự án

```
NHOM_XX_LAB211_Payroll/
├── src/
│   ├── model/               ← Entity, Enum, BaseEntity, SalaryCalculator
│   ├── repository/          ← CsvRepository<T>, các Repo cụ thể
│   ├── controller/          ← PayrollController, LeaveController, SimulatorController
│   └── view/                ← MainView, PayrollView, LeaveView, SimulatorView
├── data/
│   ├── employees.csv         (≥ 1,000 dòng)
│   ├── departments.csv       (≥ 20 dòng)
│   ├── leave_balances.csv    (≥ 2,000 dòng)
│   ├── leave_requests.csv    (≥ 4,000 dòng)
│   ├── attendance.csv        (≥ 12,000 dòng)
│   ├── payroll_entries.csv   (≥ 12,000 dòng)
│   └── payroll_runs.csv      (kết quả simulation)
├── docs/
│   ├── report.docx
│   ├── slide.pptx
│   ├── class_diagram.png
│   └── flowcharts/
├── ai_logs/
│   ├── member1_ai_log.md
│   ├── member2_ai_log.md
│   ├── member3_ai_log.md
│   └── member4_ai_log.md
└── README.md
```

---

## ⚙️ Yêu cầu hệ thống

- **Java JDK** 17 trở lên
- **NetBeans** 17+ hoặc IntelliJ IDEA
- Không cần thư viện ngoài — chỉ dùng Java standard library

Kiểm tra phiên bản Java:
```bash
java -version
javac -version
```

---

## 🚀 Cách compile và chạy

### Bước 1 — Clone hoặc giải nén project

```bash
# Nếu dùng Git
git clone <repository-url>
cd NHOM_XX_LAB211_Payroll

# Nếu dùng file ZIP
# Giải nén → mở terminal tại thư mục gốc
```

### Bước 2 — Tạo thư mục output

```bash
mkdir -p out data
```

### Bước 3 — Compile toàn bộ

```bash
javac -d out src/model/*.java src/repository/*.java src/controller/*.java src/view/*.java
```

### Bước 4 — Chạy chương trình chính

```bash
java -cp out view.MainView
```

### Chạy bằng NetBeans

```
1. File → Open Project → chọn thư mục NHOM_XX_LAB211_Payroll
2. Chuột phải vào project → Properties → Sources → chọn src/
3. Nhấn Run (F6) để chạy
```

---

## 🗃️ Chạy DataGenerator

DataGenerator tạo dữ liệu giả cho 7 file CSV. **Chạy trước khi dùng hệ thống.**

```bash
# Compile
javac -d out src/model/DataGenerator.java

# Chạy
java -cp out model.DataGenerator
```

Kết quả mong đợi:

```
========================================
  DataGenerator — Payroll HR System
========================================
⏳ Sinh departments.csv...   ✅ 20 dòng
⏳ Sinh employees.csv...     ✅ 1000 dòng
⏳ Sinh leave_balances.csv... ✅ 2000 dòng
⏳ Sinh leave_requests.csv... ✅ 4217 dòng
⏳ Sinh attendance.csv...    ✅ 12000 dòng
⏳ Sinh payroll_entries.csv... ✅ 12000 dòng

✅ DataGenerator hoàn thành!
```

---

## 🔬 Chạy Simulator

Simulator chạy 20 HR Thread đồng thời với 4 cơ chế khác nhau và so sánh kết quả.

### Từ giao diện

```
1. Chạy chương trình chính
2. Đăng nhập với tài khoản HR Staff
3. Chọn menu "Simulation"
4. Chọn "Run All Mechanisms"
5. Xem bảng so sánh kết quả
```

### Kết quả mong đợi

```
╔══════════════════════════════════════════════════════════╗
║           SIMULATION COMPARISON REPORT                  ║
╠══════════════╦═══════════╦═══════════╦════════╦═════════╣
║ Cơ chế       ║ Double Pay║ Wrong Leave║ Time(ms)║ TPS    ║
╠══════════════╬═══════════╬═══════════╬════════╬═════════╣
║ NO_LOCK      ║     15    ║      8    ║   800  ║  1250  ║
║ FILE_LOCK    ║      0    ║      0    ║  4500  ║   222  ║
║ SYNCHRONIZED ║      0    ║      0    ║  2200  ║   455  ║
║ OPTIMISTIC   ║      0    ║      0    ║  1100  ║   909  ║
╚══════════════╩═══════════╩═══════════╩════════╩═════════╝
```

---

## 💰 Công thức tính lương

| Thành phần | Công thức |
|------------|-----------|
| Lương cơ bản | `baseSalary × (ngàyLàmViệc / 26)` |
| Overtime | `(baseSalary / 26 / 8) × giờOT × 1.5` |
| Khấu trừ vắng | `(baseSalary / 26) × ngàyVắng` |
| Bonus đi đủ | `500,000đ` nếu vắng = 0 |
| Thuế TNCN | `10%` nếu lương > 11 triệu |
| **Lương thực nhận** | `cơ bản + OT + bonus − khấu trừ − thuế` |

---

## 📝 Lưu ý quan trọng

> ⚠️ **Chạy DataGenerator trước** khi dùng bất kỳ chức năng nào  
> ⚠️ **Không commit file `.class`** — đã được thêm vào `.gitignore`  
> ⚠️ **LeaveBalance và PayrollEntry** có field `version` — không được xóa

---

*"Payroll errors are not just bugs — they are broken promises to real people."*