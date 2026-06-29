import java.util.List;


public class PayrollView {

    private final PayrollController payrollController;

    public PayrollView(PayrollController payrollController) {
        this.payrollController = payrollController;
    }


    public void displayAllEntries() {
        List<PayrollEntry> entries = payrollController.getAllEntries();
        System.out.println();
        System.out.println("--- BẢNG LƯƠNG TOÀN BỘ ---");
        printPayrollTable(entries);
    }

    public void displayByMonth(String yearMonth) {
        List<PayrollEntry> entries = payrollController.getEntriesByMonth(yearMonth);
        System.out.println();
        System.out.println("--- BẢNG LƯƠNG THÁNG: " + yearMonth + " ---");
        printPayrollTable(entries);
    }


    private void printPayrollTable(List<PayrollEntry> entries) {
        if (entries.isEmpty()) {
            System.out.println("Không có dữ liệu lương.");
            return;
        }

        // Header
        String line = "+-------------------------------+-------------+------------------+--------------+";
        System.out.println(line);
        System.out.printf("| %-29s | %-11s | %-16s | %-12s |%n",
                "Mã phiếu lương", "Mã NV", "Lương thực nhận", "Trạng thái");
        System.out.println(line);

        // Rows
        for (PayrollEntry entry : entries) {
            System.out.printf("| %-29s | %-11s | %16s | %-12s |%n",
                    truncate(entry.getEntryId(), 29),
                    entry.getEmployeeId(),
                    formatMoney(entry.getNetSalary()),
                    entry.getStatus());
        }

        // Footer
        System.out.println(line);
        System.out.printf("Tổng số: %d phiếu lương%n", entries.size());
    }

    public void displayDetailByMonth(String yearMonth) {
        List<PayrollEntry> entries = payrollController.getEntriesByMonth(yearMonth);
        System.out.println();
        System.out.println("--- BẢNG LƯƠNG CHI TIẾT THÁNG: " + yearMonth + " ---");

        if (entries.isEmpty()) {
            System.out.println("Không có dữ liệu lương.");
            return;
        }

        String line = "+-------------------------------+-------------+----------------------+------------------+--------------+";
        System.out.println(line);
        System.out.printf("| %-29s | %-11s | %-20s | %-16s | %-12s |%n",
                "Mã phiếu lương", "Mã NV", "Tên nhân viên", "Lương thực nhận", "Trạng thái");
        System.out.println(line);

        double totalSalary = 0;

        for (PayrollEntry entry : entries) {
            // Lấy tên nhân viên từ controller (View không truy cập trực tiếp Repository)
            String empName = "N/A";
            Employee emp = payrollController.findEmployeeById(entry.getEmployeeId());
            if (emp != null) {
                empName = emp.getName();
            }

            System.out.printf("| %-29s | %-11s | %-20s | %16s | %-12s |%n",
                    truncate(entry.getEntryId(), 29),
                    entry.getEmployeeId(),
                    truncate(empName, 20),
                    formatMoney(entry.getNetSalary()),
                    entry.getStatus());

            totalSalary += entry.getNetSalary();
        }

        System.out.println(line);
        System.out.printf("Tổng số: %d phiếu | Tổng lương: %s VND%n",
                entries.size(), formatMoney(totalSalary));
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f", amount);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
