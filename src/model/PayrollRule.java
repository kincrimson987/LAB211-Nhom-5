public class PayrollRule {
    private int standardWorkingDays;
    private int workingHoursPerDay;
    private double overtimeMultiplier;
    private double attendanceBonus;
    private double taxRate;
    private double taxThreshold;

    public PayrollRule() {
        this.standardWorkingDays = 26;
        this.workingHoursPerDay = 8;
        this.overtimeMultiplier = 1.5;
        this.attendanceBonus = 500000;
        this.taxRate = 0.10;
        this.taxThreshold = 11000000;
    }

    public PayrollRule(int standardWorkingDays, int workingHoursPerDay,
                       double overtimeMultiplier, double attendanceBonus,
                       double taxRate, double taxThreshold) {
        this.standardWorkingDays = standardWorkingDays;
        this.workingHoursPerDay = workingHoursPerDay;
        this.overtimeMultiplier = overtimeMultiplier;
        this.attendanceBonus = attendanceBonus;
        this.taxRate = taxRate;
        this.taxThreshold = taxThreshold;
    }

    public int getStandardWorkingDays() {
        return standardWorkingDays;
    }

    public void setStandardWorkingDays(int standardWorkingDays) {
        this.standardWorkingDays = standardWorkingDays;
    }

    public int getWorkingHoursPerDay() {
        return workingHoursPerDay;
    }

    public void setWorkingHoursPerDay(int workingHoursPerDay) {
        this.workingHoursPerDay = workingHoursPerDay;
    }

    public double getOvertimeMultiplier() {
        return overtimeMultiplier;
    }

    public void setOvertimeMultiplier(double overtimeMultiplier) {
        this.overtimeMultiplier = overtimeMultiplier;
    }

    public double getAttendanceBonus() {
        return attendanceBonus;
    }

    public void setAttendanceBonus(double attendanceBonus) {
        this.attendanceBonus = attendanceBonus;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getTaxThreshold() {
        return taxThreshold;
    }

    public void setTaxThreshold(double taxThreshold) {
        this.taxThreshold = taxThreshold;
    }

    @Override
    public String toString() {
        return "PayrollRule{" +
                "standardWorkingDays=" + standardWorkingDays +
                ", workingHoursPerDay=" + workingHoursPerDay +
                ", overtimeMultiplier=" + overtimeMultiplier +
                ", attendanceBonus=" + attendanceBonus +
                ", taxRate=" + taxRate +
                ", taxThreshold=" + taxThreshold +
                '}';
    }
}