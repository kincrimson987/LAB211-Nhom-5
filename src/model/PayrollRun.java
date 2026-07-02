import java.util.Locale;

public class PayrollRun extends BaseEntity {
    private String yearMonth;
    private String mechanism;
    private long elapsedMs;
    private int successCount;
    private int doublePaymentCount;
    private int wrongLeaveCount;
    private double tps;

    public PayrollRun() {
    }

    public PayrollRun(String runId, long version, String yearMonth, String mechanism,
                      long elapsedMs, int successCount, int doublePaymentCount,
                      int wrongLeaveCount, double tps) {
        super(runId, version);
        this.yearMonth = yearMonth;
        this.mechanism = mechanism;
        this.elapsedMs = elapsedMs;
        this.successCount = successCount;
        this.doublePaymentCount = doublePaymentCount;
        this.wrongLeaveCount = wrongLeaveCount;
        this.tps = tps;
    }

    public String getRunId() {
        return getId();
    }

    public void setRunId(String runId) {
        setId(runId);
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public String getMechanism() {
        return mechanism;
    }

    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getDoublePaymentCount() {
        return doublePaymentCount;
    }

    public void setDoublePaymentCount(int doublePaymentCount) {
        this.doublePaymentCount = doublePaymentCount;
    }

    public int getWrongLeaveCount() {
        return wrongLeaveCount;
    }

    public void setWrongLeaveCount(int wrongLeaveCount) {
        this.wrongLeaveCount = wrongLeaveCount;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    @Override
    public String toCsvLine() {
        return String.format(Locale.US, "%s,%d,%s,%s,%d,%d,%d,%d,%.2f",
                getId(), getVersion(), yearMonth, mechanism, elapsedMs,
                successCount, doublePaymentCount, wrongLeaveCount, tps);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");

        if (parts.length != 9) {
            throw new IllegalArgumentException("Invalid payroll run CSV line: " + line);
        }

        setId(parts[0].trim());
        setVersion(Long.parseLong(parts[1].trim()));
        this.yearMonth = parts[2].trim();
        this.mechanism = parts[3].trim();
        this.elapsedMs = Long.parseLong(parts[4].trim());
        this.successCount = Integer.parseInt(parts[5].trim());
        this.doublePaymentCount = Integer.parseInt(parts[6].trim());
        this.wrongLeaveCount = Integer.parseInt(parts[7].trim());
        this.tps = Double.parseDouble(parts[8].trim());

        if (getId() == null || getId().isBlank()
                || yearMonth == null || yearMonth.isBlank()
                || mechanism == null || mechanism.isBlank()) {
            throw new IllegalArgumentException("Invalid payroll run CSV line: " + line);
        }
    }

    @Override
    public String toString() {
        return "PayrollRun{" +
                "runId='" + getId() + '\'' +
                ", version=" + getVersion() +
                ", yearMonth='" + yearMonth + '\'' +
                ", mechanism='" + mechanism + '\'' +
                ", elapsedMs=" + elapsedMs +
                ", successCount=" + successCount +
                ", doublePaymentCount=" + doublePaymentCount +
                ", wrongLeaveCount=" + wrongLeaveCount +
                ", tps=" + tps +
                '}';
    }
}
