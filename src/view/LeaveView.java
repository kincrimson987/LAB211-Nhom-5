import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class LeaveView {
    private final LeaveController leaveController;
    private final Scanner scanner;

    public LeaveView(LeaveController leaveController) {
        this.leaveController = leaveController;
        this.scanner = new Scanner(System.in);
    }

    public LeaveView(LeaveController leaveController, Scanner scanner) {
        this.leaveController = leaveController;
        this.scanner = scanner;
    }

    /**
     * Start the view loop.
     */
    public void start() {
        boolean exit = false;
        while (!exit) {
            displayMainMenuHeader();
            System.out.println("1. Xem danh sách yêu cầu nghỉ phép");
            System.out.println("2. Xem các yêu cầu đang chờ duyệt (PENDING)");
            System.out.println("3. Xem số dư ngày phép của nhân viên");
            System.out.println("4. Gửi yêu cầu nghỉ phép mới (Submit)");
            System.out.println("5. Phê duyệt yêu cầu nghỉ phép (Approve)");
            System.out.println("6. Từ chối yêu cầu nghỉ phép (Reject)");
            System.out.println("7. Thoát");
            System.out.print("Vui lòng chọn (1-7): ");

            String choice = scanner.nextLine().trim();
            System.out.println();

            switch (choice) {
                case "1":
                    handleViewRequests();
                    break;
                case "2":
                    handleViewPendingRequests();
                    break;
                case "3":
                    handleViewBalances();
                    break;
                case "4":
                    handleSubmitRequest();
                    break;
                case "5":
                    handleApproveRequest();
                    break;
                case "6":
                    handleRejectRequest();
                    break;
                case "7":
                    exit = true;
                    System.out.println("Đang thoát khỏi phân hệ Quản lý Nghỉ phép...");
                    break;
                default:
                    System.out.println("Lựa chọn không hợp lệ. Vui lòng chọn lại từ 1 đến 7.");
            }
            System.out.println();
        }
    }

    private void displayMainMenuHeader() {
        System.out.println("=========================================================================================");
        System.out.println("                     💼 HỆ THỐNG QUẢN LÝ NGHỈ PHÉP (LEAVE MANAGEMENT)                     ");
        System.out.println("=========================================================================================");
    }

    private void handleViewRequests() {
        System.out.println("--- XEM DANH SÁCH YÊU CẦU NGHỈ PHÉP ---");
        System.out.println("1. Xem tất cả (Tối đa 50 yêu cầu mới nhất)");
        System.out.println("2. Lọc theo Mã nhân viên");
        System.out.print("Chọn tùy chọn (1-2): ");
        String opt = scanner.nextLine().trim();

        if (opt.equals("1")) {
            List<LeaveRequest> all = leaveController.getAllRequests();
            // Lấy 50 dòng cuối cùng hoặc đầu tiên. Ở đây lấy tối đa 50 dòng đầu tiên để tránh tràn màn hình.
            int limit = Math.min(all.size(), 50);
            List<LeaveRequest> sublist = all.subList(0, limit);
            System.out.printf("Hiển thị %d/%d yêu cầu:%n", limit, all.size());
            printRequestTable(sublist);
        } else if (opt.equals("2")) {
            System.out.print("Nhập Mã nhân viên (ví dụ: E0013): ");
            String empId = scanner.nextLine().trim().toUpperCase();
            List<LeaveRequest> list = leaveController.getRequestsByEmployee(empId);
            System.out.printf("Yêu cầu nghỉ phép của nhân viên %s (%d yêu cầu):%n", empId, list.size());
            printRequestTable(list);
        } else {
            System.out.println("Lựa chọn không hợp lệ.");
        }
    }

    private void handleViewPendingRequests() {
        System.out.println("--- DANH SÁCH YÊU CẦU CHỜ DUYỆT (PENDING) ---");
        List<LeaveRequest> pending = leaveController.getPendingRequests();
        System.out.printf("Có %d yêu cầu đang chờ xử lý:%n", pending.size());
        printRequestTable(pending);
    }

    private void handleViewBalances() {
        System.out.println("--- XEM SỐ DƯ NGÀY PHÉP ---");
        System.out.print("Nhập Mã nhân viên (ví dụ: E0013): ");
        String empId = scanner.nextLine().trim().toUpperCase();
        List<LeaveBalance> balances = leaveController.getBalancesByEmployee(empId);
        printBalanceTable(balances);
    }

    private void handleSubmitRequest() {
        System.out.println("--- NỘP YÊU CẦU NGHỈ PHÉP MỚI ---");
        System.out.print("Nhập Mã nhân viên (ví dụ: E0013): ");
        String empId = scanner.nextLine().trim().toUpperCase();

        System.out.println("Chọn Loại ngày nghỉ:");
        System.out.println("1. ANNUAL (Nghỉ phép năm)");
        System.out.println("2. SICK (Nghỉ ốm)");
        System.out.println("3. UNPAID (Nghỉ không lương)");
        System.out.println("4. OTHER (Khác)");
        System.out.print("Chọn loại (1-4): ");
        String typeChoice = scanner.nextLine().trim();
        LeaveType type;
        switch (typeChoice) {
            case "1": type = LeaveType.ANNUAL; break;
            case "2": type = LeaveType.SICK; break;
            case "3": type = LeaveType.UNPAID; break;
            case "4": type = LeaveType.OTHER; break;
            default:
                System.out.println("Loại phép không hợp lệ. Chọn mặc định ANNUAL.");
                type = LeaveType.ANNUAL;
        }

        LocalDate startDate = readLocalDate("Nhập Ngày bắt đầu (định dạng YYYY-MM-DD): ");
        if (startDate == null) return;

        LocalDate endDate = readLocalDate("Nhập Ngày kết thúc (định dạng YYYY-MM-DD): ");
        if (endDate == null) return;

        System.out.print("Nhập lý do nghỉ: ");
        String reason = scanner.nextLine().trim();

        try {
            LeaveRequest req = leaveController.submit(empId, type, startDate, endDate, reason);
            System.out.println("\n[THÀNH CÔNG] Đơn nghỉ phép đã được nộp.");
            System.out.println("Chi tiết yêu cầu:");
            System.out.println("- Mã yêu cầu: " + req.getLeaveId());
            System.out.println("- Số ngày nghỉ: " + req.getDays());
            System.out.println("- Trạng thái: " + req.getStatus());
        } catch (Exception e) {
            System.out.println("\n[LỖI] " + e.getMessage());
        }
    }

    private void handleApproveRequest() {
        System.out.println("--- PHÊ DUYỆT YÊU CẦU NGHỈ PHÉP ---");
        List<LeaveRequest> pending = leaveController.getPendingRequests();
        if (pending.isEmpty()) {
            System.out.println("Không có yêu cầu nào đang chờ xử lý.");
            return;
        }
        
        System.out.println("Danh sách yêu cầu PENDING hiện tại:");
        printRequestTable(pending);

        System.out.print("Nhập Mã yêu cầu (Leave ID) muốn phê duyệt: ");
        String leaveId = scanner.nextLine().trim();

        System.out.print("Nhập Mã người duyệt (ví dụ: E0001 / Manager ID): ");
        String approvedBy = scanner.nextLine().trim().toUpperCase();

        System.out.println("Chọn Cơ chế Đồng bộ hóa (Lock Mechanism) để sử dụng:");
        System.out.println("1. SYNCHRONIZED (Khóa đồng bộ theo Nhân viên - Khuyên dùng)");
        System.out.println("2. OPTIMISTIC LOCKING (Khóa lạc quan bằng Version field)");
        System.out.println("3. FILE LOCK (Khóa toàn bộ file CSV)");
        System.out.println("4. NO LOCK (Không sử dụng cơ chế bảo vệ)");
        System.out.print("Chọn cơ chế (1-4): ");
        String lockChoice = scanner.nextLine().trim();
        LockMechanism lock;
        switch (lockChoice) {
            case "1": lock = LockMechanism.SYNCHRONIZED; break;
            case "2": lock = LockMechanism.OPTIMISTIC_LOCKING; break;
            case "3": lock = LockMechanism.FILE_LOCK; break;
            case "4": lock = LockMechanism.NO_LOCK; break;
            default:
                System.out.println("Cơ chế không hợp lệ. Sử dụng mặc định: SYNCHRONIZED.");
                lock = LockMechanism.SYNCHRONIZED;
        }

        try {
            boolean success = leaveController.approve(leaveId, approvedBy, lock);
            if (success) {
                System.out.println("\n[THÀNH CÔNG] Đã phê duyệt yêu cầu nghỉ phép: " + leaveId);
            } else {
                System.out.println("\n[THẤT BẠI] Phê duyệt yêu cầu nghỉ phép không thành công.");
            }
        } catch (Exception e) {
            System.out.println("\n[LỖI] " + e.getMessage());
        }
    }

    private void handleRejectRequest() {
        System.out.println("--- TỪ CHỐI YÊU CẦU NGHỈ PHÉP ---");
        List<LeaveRequest> pending = leaveController.getPendingRequests();
        if (pending.isEmpty()) {
            System.out.println("Không có yêu cầu nào đang chờ xử lý.");
            return;
        }

        System.out.println("Danh sách yêu cầu PENDING hiện tại:");
        printRequestTable(pending);

        System.out.print("Nhập Mã yêu cầu (Leave ID) muốn từ chối: ");
        String leaveId = scanner.nextLine().trim();

        System.out.print("Nhập Mã người duyệt/từ chối: ");
        String approvedBy = scanner.nextLine().trim().toUpperCase();

        try {
            boolean success = leaveController.reject(leaveId, approvedBy);
            if (success) {
                System.out.println("\n[THÀNH CÔNG] Đã từ chối yêu cầu nghỉ phép: " + leaveId);
            } else {
                System.out.println("\n[THẤT BẠI] Không thể từ chối yêu cầu nghỉ phép.");
            }
        } catch (Exception e) {
            System.out.println("\n[LỖI] " + e.getMessage());
        }
    }

    private LocalDate readLocalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Không được để trống ngày.");
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Định dạng ngày không hợp lệ. Vui lòng nhập lại (YYYY-MM-DD).");
            }
        }
    }

    private void printRequestTable(List<LeaveRequest> requests) {
        if (requests.isEmpty()) {
            System.out.println("Không có yêu cầu nghỉ phép nào.");
            return;
        }
        System.out.println("+--------------------------------------+-------------+------------+------------+------------+------+----------------------+------------+------------+");
        System.out.printf("| %-36s | %-11s | %-10s | %-10s | %-10s | %-4s | %-20s | %-10s | %-10s |%n",
                "Mã yêu cầu (Leave ID)", "Mã nhân viên", "Loại phép", "Từ ngày", "Đến ngày", "Ngày", "Lý do", "Trạng thái", "Người duyệt");
        System.out.println("+--------------------------------------+-------------+------------+------------+------------+------+----------------------+------------+------------+");
        for (LeaveRequest req : requests) {
            String reason = req.getReason();
            if (reason.length() > 20) {
                reason = reason.substring(0, 17) + "...";
            }
            System.out.printf("| %-36s | %-11s | %-10s | %-10s | %-10s | %-4d | %-20s | %-10s | %-10s |%n",
                    req.getLeaveId(),
                    req.getEmployeeId(),
                    req.getLeaveType(),
                    req.getStartDate(),
                    req.getEndDate(),
                    req.getDays(),
                    reason,
                    req.getStatus(),
                    req.getApprovedBy() != null ? req.getApprovedBy() : "Chưa duyệt");
        }
        System.out.println("+--------------------------------------+-------------+------------+------------+------------+------+----------------------+------------+------------+");
    }

    private void printBalanceTable(List<LeaveBalance> balances) {
        if (balances.isEmpty()) {
            System.out.println("Không có thông tin số dư nghỉ phép cho nhân viên này.");
            return;
        }
        System.out.println("+----------------------------------+-------------+------------+-----------+-----------+-----------+---------+");
        System.out.printf("| %-32s | %-11s | %-10s | %-9s | %-9s | %-9s | %-7s |%n",
                "Mã Balance ID", "Mã NV", "Loại phép", "Tổng ngày", "Đã dùng", "Còn lại", "Version");
        System.out.println("+----------------------------------+-------------+------------+-----------+-----------+-----------+---------+");
        for (LeaveBalance bal : balances) {
            System.out.printf("| %-32s | %-11s | %-10s | %-9d | %-9d | %-9d | %-7d |%n",
                    bal.getBalanceId(),
                    bal.getEmployeeId(),
                    bal.getLeaveType(),
                    bal.getTotalLeaveDays(),
                    bal.getUsedLeaveDays(),
                    bal.getRemainingLeaveDays(),
                    bal.getVersion());
        }
        System.out.println("+----------------------------------+-------------+------------+-----------+-----------+-----------+---------+");
    }
}
