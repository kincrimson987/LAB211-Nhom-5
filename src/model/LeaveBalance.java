
public class LeaveBalance {

    private int totalLeaveDays;
    private int usedLeaveDays;
    private int remainingLeaveDays;
    private int version;

    public LeaveBalance() {
    }

    public LeaveBalance(int totalLeaveDays,
            int usedLeaveDays,
            int remainingLeaveDays) {

        this.totalLeaveDays = totalLeaveDays;
        this.usedLeaveDays = usedLeaveDays;
        this.remainingLeaveDays = remainingLeaveDays;
        this.version = 1;
    }

    public int getTotalLeaveDays() {
        return this.totalLeaveDays;
    }

    public void setTotalLeaveDays(int totalLeaveDays) {
        this.totalLeaveDays = totalLeaveDays;
    }

    public int getUsedLeaveDays() {
        return this.usedLeaveDays;
    }

    public void setUsedLeaveDays(int usedLeaveDays) {
        this.usedLeaveDays = usedLeaveDays;
    }

    public int getRemainingLeaveDays() {
        return this.remainingLeaveDays;
    }

    public void setRemainingLeaveDays(int remainingLeaveDays) {
        this.remainingLeaveDays = remainingLeaveDays;
    }

    public int getVersion() {
        return this.version;
    }

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

    public void addLeave(int days) {

        if (days <= 0) {
            throw new IllegalArgumentException("Days must be greater than 0");
        }

        this.totalLeaveDays += days;
        this.remainingLeaveDays += days;
        this.version++;
    }

    public int checkRemaining() {
        return this.remainingLeaveDays;
    }

    @Override
    public String toString() {
        return "LeaveBalance{" +
                "totalLeaveDays=" + this.totalLeaveDays +
                ", usedLeaveDays=" + this.usedLeaveDays +
                ", remainingLeaveDays=" + this.remainingLeaveDays +
                ", version=" + this.version +
                '}';
    }
}