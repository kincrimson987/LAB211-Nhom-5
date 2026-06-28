public class ReportView {
    public void renderReport(int totalEmp, int approvedLeaves, String payrollMode) {
        System.out.println("\n=======================================================");
        System.out.println("          PAYROLL SYSTEM - REPORT VIEW (WEEK 5-6)      ");
        System.out.println("=======================================================");
        System.out.println(" [CONFIG] Progress Mode  : Week 5 (File I/O) & Week 6 (MVC)");
        System.out.println("-------------------------------------------------------");
        System.out.println(" SUMMARY STATISTICS:");
        System.out.println("  - Total Employees Processed  : " + totalEmp);
        System.out.println("  - Approved Leave Days        : " + approvedLeaves + " Days");
        System.out.println("-------------------------------------------------------");
        System.out.println("=======================================================\n");
    }
}