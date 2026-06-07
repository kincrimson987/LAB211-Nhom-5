import java.util.Locale;
public class AttendanceRecord extends BaseEntity {
    private String employeeId;
    private String yearMonth;
    private int workDays;
    private double overtimeHours;

    public AttendanceRecord() {
    }

    // Constructor cũ: giữ lại để không làm lỗi SalaryCalculator test
    public AttendanceRecord(String id, long version, String employeeId, int workDays, double overtimeHours) {
        super(id, version);
        this.employeeId = employeeId;
        this.yearMonth = extractYearMonthFromId(id);
        this.workDays = workDays;
        this.overtimeHours = overtimeHours;
    }

    // Constructor mới: có yearMonth rõ ràng
    public AttendanceRecord(String id, long version, String employeeId, String yearMonth,
                            int workDays, double overtimeHours) {
        super(id, version);
        this.employeeId = employeeId;
        this.yearMonth = yearMonth;
        this.workDays = workDays;
        this.overtimeHours = overtimeHours;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public int getWorkDays() {
        return workDays;
    }

    public void setWorkDays(int workDays) {
        this.workDays = workDays;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    @Override
    public String toCsvLine() {
        return String.format(Locale.US, "%s,%d,%s,%s,%d,%.1f",
                getId(), getVersion(), employeeId, yearMonth, workDays, overtimeHours);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");

        // Format mới:
        // id,version,employeeId,yearMonth,workDays,overtimeHours
        if (parts.length >= 6) {
            setId(parts[0].trim());
            setVersion(Long.parseLong(parts[1].trim()));
            this.employeeId = parts[2].trim();
            this.yearMonth = parts[3].trim();
            this.workDays = Integer.parseInt(parts[4].trim());
            this.overtimeHours = Double.parseDouble(parts[5].trim());
        }

        // Format cũ:
        // id,version,employeeId,workDays,overtimeHours
        else if (parts.length >= 5) {
            setId(parts[0].trim());
            setVersion(Long.parseLong(parts[1].trim()));
            this.employeeId = parts[2].trim();
            this.yearMonth = extractYearMonthFromId(getId());
            this.workDays = Integer.parseInt(parts[3].trim());
            this.overtimeHours = Double.parseDouble(parts[4].trim());
        }
    }

    public static String extractYearMonthFromId(String id) {
        // Ví dụ: A_E0001_06_2023 -> 2023-06
        if (id == null) {
            return "";
        }

        String[] parts = id.split("_");

        if (parts.length >= 4) {
            String month = parts[2];
            String year = parts[3];
            return year + "-" + month;
        }

        return "";
    }

    @Override
    public String toString() {
        return "AttendanceRecord{" +
                "id='" + getId() + '\'' +
                ", version=" + getVersion() +
                ", employeeId='" + employeeId + '\'' +
                ", yearMonth='" + yearMonth + '\'' +
                ", workDays=" + workDays +
                ", overtimeHours=" + overtimeHours +
                '}';
    }
}