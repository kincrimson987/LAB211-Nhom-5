

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
        return getId();
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
        setVersion(getVersion() + 1);
    }


    // ==================== CSV — theo schema chính thức ====================

    public static String getFullCsvHeader() {
        return "id,version,employeeId,netSalary,status";
    }

    public String getCsvHeader() {
        return getFullCsvHeader();
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

    public static PayrollEntry parseCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid payroll entry CSV line: " + line);
        }

        PayrollEntry pe = new PayrollEntry();
        pe.id = parts[0].trim();
        pe.version = Long.parseLong(parts[1].trim());
        pe.employeeId = parts[2].trim();
        pe.netSalary = Double.parseDouble(parts[3].trim());
        pe.status = PayrollStatus.valueOf(parts[4].trim());
        return pe;
    }

    @Override
    public void fromCsvLine(String line) {
        PayrollEntry parsed = parseCsvLine(line);
        setId(parsed.getId());
        setVersion(parsed.getVersion());
        this.employeeId = parsed.employeeId;
        this.netSalary = parsed.netSalary;
        this.status = parsed.status;
    }

    public static String extractYearMonthFromId(String id) {
        if (id == null) {
            return "";
        }
        String[] parts = id.split("_");
        if (parts.length >= 4) {
            return parts[3] + "-" + parts[2];
        }
        return "";
    }

    // ==================== toString — từ diagram ====================

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