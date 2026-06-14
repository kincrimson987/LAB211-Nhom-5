public class PayrollEntry extends BaseEntity {

    private String employeeId;
    private double netSalary;
    private PayrollStatus status;

    // ==================== CONSTRUCTORS ====================

    public PayrollEntry() {
        super(null, 0);
        this.status = PayrollStatus.PENDING;
    }

    public PayrollEntry(String id, String employeeId) {
        super(id, 0);
        this.employeeId = employeeId;
        this.status = PayrollStatus.PENDING;
    }

    public PayrollEntry(String id, long version, String employeeId, double netSalary, PayrollStatus status) {
        super(id, version);
        this.employeeId = employeeId;
        this.netSalary = netSalary;
        this.status = status != null ? status : PayrollStatus.PENDING;
    }

    // ==================== GETTERS / SETTERS ====================

    public String getEntryId() {
        return this.getId();
    }

    public void setEntryId(String entryId) {
        setId(entryId);
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public double getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(double netSalary) {
        this.netSalary = netSalary;
    }

    public PayrollStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollStatus status) {
        this.status = status;
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Chốt lương — chỉ được gọi đúng 1 lần.
     */
    public void process() {
        if (status == PayrollStatus.PROCESSED) {
            throw new IllegalStateException("Payroll already processed.");
        }
        this.status = PayrollStatus.PROCESSED;
        setVersion(getVersion() + 1);
    }

    // ==================== CSV ====================

    public String getCsvHeader() {
        return "id,version,employeeId,netSalary,status";
    }

    /**
     * Trích xuất yearMonth từ id của chính entry này.
     * Ví dụ: PR_E0001_01_2024 -> 2024-01
     */
    public String extractYearMonth() {
        String id = getId();
        if (id == null) return "";
        String[] parts = id.split("_");
        if (parts.length >= 4) {
            return parts[3] + "-" + parts[2];
        }
        return "";
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId() != null ? getId() : "",
                String.valueOf(getVersion()),
                employeeId != null ? employeeId : "",
                String.valueOf(netSalary),
                status != null ? status.name() : PayrollStatus.PENDING.name());
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid payroll entry CSV line: " + line);
        }
        setId(parts[0].trim());
        setVersion(Long.parseLong(parts[1].trim()));
        this.employeeId = parts[2].trim();
        this.netSalary = Double.parseDouble(parts[3].trim());
        this.status = PayrollStatus.valueOf(parts[4].trim());
    }

    // ==================== toString ====================

    @Override
    public String toString() {
        return "PayrollEntry{" +
                "id='" + getId() + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", netSalary=" + netSalary +
                ", status=" + status +
                ", version=" + getVersion() +
                '}';
    }
}