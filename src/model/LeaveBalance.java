
public class LeaveBalance {

    // ── Từ diagram ──────────────────────────────
    private int totalLeaveDays;
    private int usedLeaveDays;
    private int remainingLeaveDays;
    private int version;

    // ── Thêm cho dự án ──────────────────────────
    private String balanceId; // khóa chính — đọc/ghi CSV
    private String employeeId; // biết balance này của nhân viên nào
    private LeaveType leaveType; // ANNUAL hay SICK

    // ==================== CONSTRUCTORS ====================

    /** Từ diagram */
    public LeaveBalance() {
    }

    /** Từ diagram — giữ nguyên, chỉ sửa version = 0 */
    public LeaveBalance(int totalLeaveDays,
            int usedLeaveDays,
            int remainingLeaveDays) {
        this.totalLeaveDays = totalLeaveDays;
        this.usedLeaveDays = usedLeaveDays;
        this.remainingLeaveDays = remainingLeaveDays;
        this.version = 0; // sửa từ 1 → 0 vì DataGenerator sinh ra version=0
    }

    /** Thêm — constructor đầy đủ cho dự án */
    public LeaveBalance(String balanceId, String employeeId,
            LeaveType leaveType, int totalLeaveDays) {
        this.balanceId = balanceId;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.totalLeaveDays = totalLeaveDays;
        this.usedLeaveDays = 0;
        this.remainingLeaveDays = totalLeaveDays;
        this.version = 0;
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

    public int getVersion() {
        return version;
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

    public void setVersion(int version) {
        this.version = version;
    }

    // ==================== GETTERS/SETTERS — thêm cho dự án ====================

    public String getBalanceId() {
        return balanceId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setBalanceId(String balanceId) {
        this.balanceId = balanceId;
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
        this.version++;
    }

    /** Từ diagram — giữ nguyên code bạn */
    public void addLeave(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0");
        }
        this.totalLeaveDays += days;
        this.remainingLeaveDays += days;
        this.version++;
    }

    /** Từ diagram — giữ nguyên code bạn */
    public int checkRemaining() {
        return this.remainingLeaveDays;
    }

    // ==================== CSV — thêm cho dự án ====================

    public String getCsvHeader() {
        return "balanceId,employeeId,leaveType,totalLeaveDays,usedLeaveDays,remainingLeaveDays,version";
    }

    public String toCsvLine() {
        return String.join(",",
                balanceId,
                employeeId,
                leaveType != null ? leaveType.name() : "",
                String.valueOf(totalLeaveDays),
                String.valueOf(usedLeaveDays),
                String.valueOf(remainingLeaveDays),
                String.valueOf(version));
    }

    public static LeaveBalance fromCsvLine(String line) {
        String[] p = line.split(",");
        LeaveBalance lb = new LeaveBalance();
        lb.balanceId = p[0].trim();
        lb.employeeId = p[1].trim();
        lb.leaveType = LeaveType.valueOf(p[2].trim());
        lb.totalLeaveDays = Integer.parseInt(p[3].trim());
        lb.usedLeaveDays = Integer.parseInt(p[4].trim());
        lb.remainingLeaveDays = Integer.parseInt(p[5].trim());
        lb.version = Integer.parseInt(p[6].trim());
        return lb;
    }

    // ==================== toString — từ diagram ====================

    @Override
    public String toString() {
        return "LeaveBalance{" +
                "balanceId='" + balanceId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", leaveType=" + leaveType +
                ", totalLeaveDays=" + totalLeaveDays +
                ", usedLeaveDays=" + usedLeaveDays +
                ", remainingLeaveDays=" + remainingLeaveDays +
                ", version=" + version +
                '}';
    }
}