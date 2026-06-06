

public class PayrollEntry {

    // ── Từ diagram ──────────────────────────────
    private PayrollStatus status;
    private int version;

    // ── Thêm cho dự án ──────────────────────────
    private String entryId; // khóa chính
    private String employeeId; // nhân viên nào
    private String yearMonth; // tháng nào "2024-01"
    private double baseSalaryPaid; // lương cơ bản đã tính
    private double overtimePay; // tiền OT
    private double bonus; // thưởng chuyên cần
    private double deduction; // khấu trừ vắng
    private double tax; // thuế TNCN
    private double netSalary; // lương thực nhận
    private String processedBy; // hrId đã xử lý

    // ==================== CONSTRUCTORS ====================

    /** Từ diagram — giữ nguyên, chỉ sửa version = 0 */
    public PayrollEntry() {
        this.status = PayrollStatus.PENDING;
        this.version = 0; // sửa từ 1 → 0 cho đồng nhất với DataGenerator
    }

    /** Thêm — constructor tạo entry mới chờ xử lý */
    public PayrollEntry(String entryId, String employeeId, String yearMonth) {
        this();
        this.entryId = entryId;
        this.employeeId = employeeId;
        this.yearMonth = yearMonth;
    }

    /** Thêm — constructor đầy đủ sau khi tính lương */
    public PayrollEntry(String entryId, String employeeId, String yearMonth,
            double baseSalaryPaid, double overtimePay, double bonus,
            double deduction, double tax, double netSalary) {
        this(entryId, employeeId, yearMonth);
        this.baseSalaryPaid = baseSalaryPaid;
        this.overtimePay = overtimePay;
        this.bonus = bonus;
        this.deduction = deduction;
        this.tax = tax;
        this.netSalary = netSalary;
    }

    // ==================== GETTERS — từ diagram ====================

    public PayrollStatus getStatus() {
        return status;
    }

    public int getVersion() {
        return version;
    }

    // ==================== SETTERS — thêm cho dự án ====================

    public void setStatus(PayrollStatus status) {
        this.status = status;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // ==================== GETTERS/SETTERS — thêm cho dự án ====================

    public String getEntryId() {
        return entryId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public double getBaseSalaryPaid() {
        return baseSalaryPaid;
    }

    public double getOvertimePay() {
        return overtimePay;
    }

    public double getBonus() {
        return bonus;
    }

    public double getDeduction() {
        return deduction;
    }

    public double getTax() {
        return tax;
    }

    public double getNetSalary() {
        return netSalary;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public void setBaseSalaryPaid(double baseSalaryPaid) {
        this.baseSalaryPaid = baseSalaryPaid;
    }

    public void setOvertimePay(double overtimePay) {
        this.overtimePay = overtimePay;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public void setDeduction(double deduction) {
        this.deduction = deduction;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setNetSalary(double netSalary) {
        this.netSalary = netSalary;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    // ==================== BUSINESS METHODS — từ diagram ====================

    /**
     * Từ diagram — giữ nguyên logic bạn.
     * Chốt lương — chỉ được gọi đúng 1 lần.
     */
    public void process() {
        if (status == PayrollStatus.PROCESSED) {
            throw new IllegalStateException("Payroll already processed.");
        }
        this.status = PayrollStatus.PROCESSED;
        this.version++;
    }


    // ==================== CSV — thêm cho dự án ====================

    public String getCsvHeader() {
        return "entryId,employeeId,yearMonth,baseSalaryPaid,overtimePay," +
                "bonus,deduction,tax,netSalary,status,processedBy,version";
    }

    public String toCsvLine() {
        return String.join(",",
                entryId,
                employeeId,
                yearMonth,
                String.valueOf(baseSalaryPaid),
                String.valueOf(overtimePay),
                String.valueOf(bonus),
                String.valueOf(deduction),
                String.valueOf(tax),
                String.valueOf(netSalary),
                status.name(),
                processedBy != null ? processedBy : "",
                String.valueOf(version));
    }

    public static PayrollEntry fromCsvLine(String line) {
        String[] p = line.split(",");
        PayrollEntry pe = new PayrollEntry();
        pe.entryId = p[0].trim();
        pe.employeeId = p[1].trim();
        pe.yearMonth = p[2].trim();
        pe.baseSalaryPaid = Double.parseDouble(p[3].trim());
        pe.overtimePay = Double.parseDouble(p[4].trim());
        pe.bonus = Double.parseDouble(p[5].trim());
        pe.deduction = Double.parseDouble(p[6].trim());
        pe.tax = Double.parseDouble(p[7].trim());
        pe.netSalary = Double.parseDouble(p[8].trim());
        pe.status = PayrollStatus.valueOf(p[9].trim());
        pe.processedBy = p[10].trim().isEmpty() ? null : p[10].trim();
        pe.version = Integer.parseInt(p[11].trim());
        return pe;
    }

    // ==================== toString — từ diagram ====================

    /** Từ diagram — giữ nguyên code bạn, thêm netSalary */
    @Override
    public String toString() {
        return "PayrollEntry{" +
                "entryId='" + entryId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", yearMonth='" + yearMonth + '\'' +
                ", netSalary=" + netSalary +
                ", status=" + status +
                ", version=" + version +
                '}';
    }
}