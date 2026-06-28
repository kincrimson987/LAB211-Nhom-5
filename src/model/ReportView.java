public class ReportView {
    // Hàm này nhận dữ liệu từ Controller đưa sang để in ra màn hình
    public void renderReport(int totalEmp, int approvedLeaves, String payrollMode) {
        System.out.println("\n=======================================================");
        System.out.println("          PAYROLL SYSTEM - REPORT VIEW (MVC)           ");
        System.out.println("=======================================================");
        System.out.println(" [STATUS] Core System    : ACTIVE (Java 8 Validated)");
        System.out.println(" [CONFIG] Run Mode       : " + payrollMode);
        System.out.println("-------------------------------------------------------");
        System.out.println(" SUMMARY STATISTICS:");
        System.out.println("  - Total Sequential Employees : " + totalEmp);
        System.out.println("  - Approved Leave Days        : " + approvedLeaves + " Days");
        System.out.println("-------------------------------------------------------");
        System.out.println(" [MVC] Data rendered successfully from Controller.");
        System.out.println("=======================================================\n");
    }
}