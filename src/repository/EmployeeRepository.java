import java.util.List;
import java.util.stream.Collectors;


/**
 * Repository cho employees.csv
 */
public class EmployeeRepository extends CsvRepository<Employee> {

    private static final String DEFAULT_PATH = "data/employees.csv";

    public EmployeeRepository() {
        this(DEFAULT_PATH);
    }

    public EmployeeRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "id,version,name,email,departmentId";
    }

    @Override
    public String getId(Employee entity) {
        return entity.getId();
    }

    @Override
    public String toLine(Employee entity) {
        return entity.toCsvLine();
    }

    @Override
    public Employee parseLine(String line) {
        Employee e = new Employee();
        e.fromCsvLine(line);
        return e;
    }

    public List<Employee> findByDepartment(String departmentId) {
        return findAll().stream()
                .filter(e -> departmentId.equals(e.getDepartmentId()))
                .collect(Collectors.toList());
    }

    public List<Employee> findByType(Enums.EmploymentType type) {
        return findAll().stream()
                .filter(e -> e.getEmploymentType() == type)
                .collect(Collectors.toList());
    }

    public List<Employee> searchByName(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return findAll();
        }
        String q = keyword.toLowerCase();
        return findAll().stream()
                .filter(e -> e.getName() != null && e.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
}
