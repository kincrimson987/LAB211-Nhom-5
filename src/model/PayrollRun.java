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
        return id;
    }

    public void setRunId(String runId) {
        this.id = runId;
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
        return String.format("%s,%d,%s,%s,%d,%d,%d,%d,%.2f",
                id, version, yearMonth, mechanism, elapsedMs,
                successCount, doublePaymentCount, wrongLeaveCount, tps);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");

        if (parts.length >= 9) {
            this.id = parts[0];
            this.version = Long.parseLong(parts[1]);
            this.yearMonth = parts[2];
            this.mechanism = parts[3];
            this.elapsedMs = Long.parseLong(parts[4]);
            this.successCount = Integer.parseInt(parts[5]);
            this.doublePaymentCount = Integer.parseInt(parts[6]);
            this.wrongLeaveCount = Integer.parseInt(parts[7]);
            this.tps = Double.parseDouble(parts[8]);
        }
    }

    @Override
    public String toString() {
        return "PayrollRun{" +
                "runId='" + id + '\'' +
                ", version=" + version +
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