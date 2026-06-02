public class LeaveBalance extends BaseEntity {
    private int annualRemaining;
    private int sickRemaining;

    public LeaveBalance() {
    }

    public LeaveBalance(String id, long version, int annualRemaining, int sickRemaining) {
        super(id, version);
        this.annualRemaining = annualRemaining;
        this.sickRemaining = sickRemaining;
    }

    public int getAnnualRemaining() {
        return annualRemaining;
    }

    public void setAnnualRemaining(int annualRemaining) {
        this.annualRemaining = annualRemaining;
    }

    public int getSickRemaining() {
        return sickRemaining;
    }

    public void setSickRemaining(int sickRemaining) {
        this.sickRemaining = sickRemaining;
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%d,%d,%d", id, version, annualRemaining, sickRemaining);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 4) {
            this.id = parts[0];
            this.version = Long.parseLong(parts[1]);
            this.annualRemaining = Integer.parseInt(parts[2]);
            this.sickRemaining = Integer.parseInt(parts[3]);
        }
    }
}
