public class LeaveApp {
    public static void main(String[] args) {
        System.out.println("Đang khởi tạo các dữ liệu Repository...");
        
        // Khởi tạo các repositories với đường dẫn tệp tin mặc định trong thư mục data/
        EmployeeRepository employeeRepo = new EmployeeRepository();
        LeaveBalanceRepository leaveBalanceRepo = new LeaveBalanceRepository();
        LeaveRequestRepository leaveRequestRepo = new LeaveRequestRepository();

        // Khởi tạo controller điều phối
        LeaveController leaveController = new LeaveController(leaveRequestRepo, leaveBalanceRepo, employeeRepo);

        // Khởi tạo giao diện Console View
        LeaveView leaveView = new LeaveView(leaveController);
        
        // Bắt đầu vòng lặp giao diện
        leaveView.start();
    }
}
