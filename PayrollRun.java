public class PayrollRun extends BaseEntity {
    private int month;
    private int year;
    private Enums.LockMechanism lockMechanism;
    private int doublePaymentCount;
    private int wrongLeaveCount;
    private long elapsedMs;
    private double tps;

    public PayrollRun() {}

    public PayrollRun(String id, long version, int month, int year, Enums.LockMechanism lockMechanism) {
        super(id, version);
        this.month = month;
        this.year = year;
        this.lockMechanism = lockMechanism;
    }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public Enums.LockMechanism getLockMechanism() { return lockMechanism; }
    public void setLockMechanism(Enums.LockMechanism lockMechanism) { this.lockMechanism = lockMechanism; }

    public int getDoublePaymentCount() { return doublePaymentCount; }
    public void setDoublePaymentCount(int doublePaymentCount) { this.doublePaymentCount = doublePaymentCount; }

    public int getWrongLeaveCount() { return wrongLeaveCount; }
    public void setWrongLeaveCount(int wrongLeaveCount) { this.wrongLeaveCount = wrongLeaveCount; }

    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }

    public double getTps() { return tps; }
    public void setTps(double tps) { this.tps = tps; }

    @Override
    public String toCsvLine() {
        return String.format("%s,%d,%d,%d,%s,%d,%d,%d,%.2f", id, version, month, year, lockMechanism, doublePaymentCount, wrongLeaveCount, elapsedMs, tps);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 9) {
            this.id = parts[0];
            this.version = Long.parseLong(parts[1]);
            this.month = Integer.parseInt(parts[2]);
            this.year = Integer.parseInt(parts[3]);
            this.lockMechanism = Enums.LockMechanism.valueOf(parts[4]);
            this.doublePaymentCount = Integer.parseInt(parts[5]);
            this.wrongLeaveCount = Integer.parseInt(parts[6]);
            this.elapsedMs = Long.parseLong(parts[7]);
            this.tps = Double.parseDouble(parts[8]);
        }
    }
}
