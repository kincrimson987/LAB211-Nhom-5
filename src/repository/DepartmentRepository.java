import java.util.List;
import java.util.stream.Collectors;


public class DepartmentRepository extends CsvRepository<Department> {

    private static final String DEFAULT_PATH = "data/departments.csv";

    public DepartmentRepository() {
        this(DEFAULT_PATH);
    }

    public DepartmentRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "id,version,name,managerId";
    }

    @Override
    public String getId(Department entity) {
        return entity.getId();
    }

    @Override
    public String toLine(Department entity) {
        return entity.toCsvLine();
    }

    @Override
    public Department parseLine(String line) {
        Department d = new Department();
        d.fromCsvLine(line);
        return d;
    }

    public Department findByName(String name) {
        if (name == null) return null;
        String q = name.trim().toLowerCase();
        return findAll().stream()
                .filter(d -> d.getName() != null && d.getName().trim().toLowerCase().equals(q))
                .findFirst().orElse(null);
    }
}
