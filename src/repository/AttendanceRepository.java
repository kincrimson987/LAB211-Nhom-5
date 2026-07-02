import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository đọc/ghi attendance.csv.
 * Dùng để lấy dữ liệu chấm công cho SalaryCalculator.
 */
public class AttendanceRepository extends CsvRepository<AttendanceRecord> {

    /*
     * private static final String DEFAULT_PATH = "data/attendance.csv";
     * 
     * public AttendanceRepository() {
     * this(DEFAULT_PATH);
     * }
     */
    public AttendanceRepository() {
        super("data/attendance.csv");
    }

    public AttendanceRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "id,version,employeeId,yearMonth,workDays,overtimeHours";
    }

    @Override
    public String getId(AttendanceRecord entity) {
        return entity.getId();
    }

    @Override
    public String toLine(AttendanceRecord entity) {
        return entity.toCsvLine();
    }

    @Override
    public AttendanceRecord parseLine(String line) {
        AttendanceRecord record = new AttendanceRecord();
        record.fromCsvLine(line);
        return record;
    }

    public List<AttendanceRecord> findByEmployee(String employeeId) {
        return findAll().stream()
                .filter(record -> employeeId.equals(record.getEmployeeId()))
                .collect(Collectors.toList());
    }

    public List<AttendanceRecord> findByMonth(String yearMonth) {
        return findAll().stream()
                .filter(record -> yearMonth.equals(record.getYearMonth()))
                .collect(Collectors.toList());
    }

    public AttendanceRecord findByEmployeeAndMonth(String employeeId, String yearMonth) {
        return findAll().stream()
                .filter(record -> employeeId.equals(record.getEmployeeId()))
                .filter(record -> yearMonth.equals(record.getYearMonth()))
                .findFirst()
                .orElse(null);
    }
}
