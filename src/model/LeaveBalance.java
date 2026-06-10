public class LeaveBalance extends BaseEntity {

    // ── Từ diagram ──────────────────────────────
    private int totalLeaveDays;
    private int usedLeaveDays;
    private int remainingLeaveDays;

    // ── Thêm cho dự án ──────────────────────────
    private String employeeId; // biết balance này của nhân viên nào
    private LeaveType leaveType; // ANNUAL hay SICK

    // ==================== CONSTRUCTORS ====================

    /** Từ diagram */
    public LeaveBalance() {
        super(null, 0);
    }

    /** Từ diagram — giữ nguyên, chỉ sửa version = 0 */
    public LeaveBalance(int totalLeaveDays,
            int usedLeaveDays,
            int remainingLeaveDays) {
        super(null, 0);
        this.totalLeaveDays = totalLeaveDays;
        this.usedLeaveDays = usedLeaveDays;
        this.remainingLeaveDays = remainingLeaveDays;
    }

    /** Thêm — constructor đầy đủ cho dự án */
    public LeaveBalance(String balanceId, String employeeId,
            LeaveType leaveType, int totalLeaveDays) {
        super(balanceId, 0);
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.totalLeaveDays = totalLeaveDays;
        this.usedLeaveDays = 0;
        this.remainingLeaveDays = totalLeaveDays;
    }

    // ==================== GETTERS — từ diagram ====================

    public int getTotalLeaveDays() {
        return totalLeaveDays;
    }

    public int getUsedLeaveDays() {
        return usedLeaveDays;
    }

    public int getRemainingLeaveDays() {
        return remainingLeaveDays;
    }

    // ==================== SETTERS — từ diagram ====================

    public void setTotalLeaveDays(int totalLeaveDays) {
        this.totalLeaveDays = totalLeaveDays;
    }

    public void setUsedLeaveDays(int usedLeaveDays) {
        this.usedLeaveDays = usedLeaveDays;
    }

    public void setRemainingLeaveDays(int remainingLeaveDays) {
        this.remainingLeaveDays = remainingLeaveDays;
    }

    // ==================== GETTERS/SETTERS — thêm cho dự án ====================

    public String getBalanceId() {
        return getId();
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setBalanceId(String balanceId) {
        setId(balanceId);
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    // ==================== BUSINESS METHODS — từ diagram ====================

    /** Từ diagram — giữ nguyên code bạn */
    public void deductLeave(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0");
        }
        if (days > this.remainingLeaveDays) {
            throw new IllegalArgumentException("Not enough leave days");
        }
        this.usedLeaveDays += days;
        this.remainingLeaveDays -= days;
        setVersion(getVersion() + 1);
    }

    /** Từ diagram — giữ nguyên code bạn */
    public void addLeave(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0");
        }
        this.totalLeaveDays += days;
        this.remainingLeaveDays += days;
        setVersion(getVersion() + 1);
    }

    /** Từ diagram — giữ nguyên code bạn */
    public int checkRemaining() {
        return this.remainingLeaveDays;
    }

    // ==================== CSV — thêm cho dự án ====================

    public String getCsvHeader() {
        return "balanceId,employeeId,leaveType,totalLeaveDays,usedLeaveDays,remainingLeaveDays,version";
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                getId() != null ? getId() : "",
                employeeId,
                leaveType != null ? leaveType.name() : "",
                String.valueOf(totalLeaveDays),
                String.valueOf(usedLeaveDays),
                String.valueOf(remainingLeaveDays),
                String.valueOf(getVersion()));
    }

    @Override
    public void fromCsvLine(String line) {
        String[] p = line.split(",");
        if (p.length >= 7) {
            setId(p[0].trim());
            this.employeeId = p[1].trim();
            this.leaveType = LeaveType.valueOf(p[2].trim());
            this.totalLeaveDays = Integer.parseInt(p[3].trim());
            this.usedLeaveDays = Integer.parseInt(p[4].trim());
            this.remainingLeaveDays = Integer.parseInt(p[5].trim());
            setVersion(Long.parseLong(p[6].trim()));
        }
    }

    public static LeaveBalance fromCsvLineStatic(String line) {
        LeaveBalance lb = new LeaveBalance();
        lb.fromCsvLine(line);
        return lb;
    }

    // ==================== toString — từ diagram ====================

    @Override
    public String toString() {
        return "LeaveBalance{" +
                "balanceId='" + getId() + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", leaveType=" + leaveType +
                ", totalLeaveDays=" + totalLeaveDays +
                ", usedLeaveDays=" + usedLeaveDays +
                ", remainingLeaveDays=" + remainingLeaveDays +
                ", version=" + getVersion() +
                '}';
    }
}