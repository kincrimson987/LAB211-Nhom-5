public class PayrollController {
    private ReportView view;

    public PayrollController(ReportView view) {
        this.view = view;
    }

    // 1. Xử lý luồng Payroll Single-Thread tuần tự
    public int processSingleThreadPayroll(String[] employees) {
        int count = 0;
        for (String emp : employees) {
            System.out.println("[Single-Thread] Processing salary sequentially for: " + emp);
            count++;
        }
        return count;
    }

    // 2. Xử lý luồng Leave Approval Flow
    public int processLeaveApproval(int currentBalance, int requestDays) {
        if (currentBalance >= requestDays) {
            System.out.println("[Leave Flow] Leave request APPROVED.");
            return requestDays;
        }
        System.out.println("[Leave Flow] Leave request REJECTED.");
        return 0;
    }

    // 3. Hàm kích hoạt điều phối toàn bộ Flow và đẩy dữ liệu sang View
    public void runAndDemoFlow() {
        String[] empList = {"E001", "E002", "E003"};
        
        // Chạy các chức năng hệ thống yêu cầu
        int totalProcessed = processSingleThreadPayroll(empList);
        int approvedDays = processLeaveApproval(12, 3);

        // Đẩy kết quả sang cho View hiển thị theo đúng mô hình MVC
        view.renderReport(totalProcessed, approvedDays, "Single-Thread tuần tự (Week 6-7)");
    }

    // Hàm main phục vụ chạy demo trực tiếp cho thầy xem
    public static void main(String[] args) {
        ReportView view = new ReportView();
        PayrollController controller = new PayrollController(view);
        controller.runAndDemoFlow();
    }
}