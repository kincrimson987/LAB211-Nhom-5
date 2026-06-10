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
        Employee employee = new Employee();
        employee.fromCsvLine(line);
        return employee;
    }

    public List<Employee> findByDepartment(String departmentId) {
        return findAll().stream()
                .filter(employee -> departmentId.equals(employee.getDepartmentId()))
                .collect(Collectors.toList());
    }

    public List<Employee> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        String q = keyword.trim().toLowerCase();

        return findAll().stream()
                .filter(employee -> employee.getName() != null
                        && employee.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
}