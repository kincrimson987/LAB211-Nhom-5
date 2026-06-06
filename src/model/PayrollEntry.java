public class PayrollEntry {

    private PayrollStatus status;
    private int version;

    public PayrollEntry() {
        this.status = PayrollStatus.PENDING;
        this.version = 1;
    }

    public PayrollStatus getStatus() {
        return this.status;
    }

    public int getVersion() {
        return this.version;
    }

    public void process() {

        if (status == PayrollStatus.PROCESSED) {
            throw new IllegalStateException(
                    "Payroll already processed.");
        }

        this.status = PayrollStatus.PROCESSED;
        this.version++;
    }

    @Override
    public String toString() {
        return "PayrollEntry{" +
                "status=" + this.status +
                ", version=" + this.version +
                '}';
    }
}