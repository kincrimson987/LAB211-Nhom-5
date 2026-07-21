import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DepartmentController {

    private String departmentSequenceFile = "data/department_sequence.txt";
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this(departmentRepository, null);
    }

    public DepartmentController(DepartmentRepository departmentRepository,
                                EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    public Department addDepartment(String name, String managerId) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Department name cannot be empty.");
        if (departmentRepository.findByName(name) != null)
            throw new IllegalArgumentException("Department name already exists: " + name);

        String normalizedManagerId = normalizeManagerId(managerId);
        validateManagerExists(normalizedManagerId);
        Department dept = new Department(generateId(), 1L, name.trim(), normalizedManagerId);
        departmentRepository.save(dept);
        return dept;
    }

    public Department getDepartmentById(String id) {
        String normalizedId = normalizeDepartmentId(id);
        Department dept = departmentRepository.findById(normalizedId);
        if (dept == null)
            throw new IllegalArgumentException("Department not found: " + normalizedId);
        return dept;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<Department> searchByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty())
            return departmentRepository.findAll();
        String q = keyword.trim().toLowerCase();
        return departmentRepository.findAll().stream()
                .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(q))
                .collect(java.util.stream.Collectors.toList());
    }

    public Department updateDepartment(String id, String name, String managerId) {
        Department dept = getDepartmentById(id);
        if (name != null && !name.trim().isEmpty()) {
            Department duplicate = departmentRepository.findByName(name);
            if (duplicate != null && !duplicate.getId().equals(dept.getId())) {
                throw new IllegalArgumentException("Department name already exists: " + name.trim());
            }
            dept.setName(name.trim());
        }
        if (managerId != null) {
            String normalizedManagerId = normalizeManagerId(managerId);
            validateManagerExists(normalizedManagerId);
            dept.setManagerId(normalizedManagerId);
        }
        dept.setVersion(dept.getVersion() + 1);
        departmentRepository.update(dept);
        return dept;
    }

    public void deleteDepartment(String id) {
        Department department = getDepartmentById(id);
        if (employeeRepository != null
                && !employeeRepository.findByDepartment(department.getId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot delete department " + department.getId() + " because it still has employees.");
        }
        departmentRepository.delete(department.getId());
    }

    public void saveCsv() { /* data tự động lưu qua repository */ }
    public void loadCsv() { departmentRepository.findAll(); }

    private String generateId() {
        int currentMax = departmentRepository.findAll().stream()
                .map(Department::getId)
                .filter(id -> id != null && id.matches("D\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(1)))
                .max()
                .orElse(0);
        int storedLast = readLastDepartmentNumber();
        int next = Math.max(currentMax, storedLast) + 1;
        saveLastDepartmentNumber(next);
        return String.format("D%03d", next);
    }

    private String normalizeDepartmentId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Department ID cannot be empty.");
        }
        return id.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String normalizeManagerId(String managerId) {
        return managerId == null
                ? ""
                : managerId.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validateManagerExists(String managerId) {
        if (managerId.isEmpty() || employeeRepository == null) {
            return;
        }
        if (employeeRepository.findById(managerId) == null) {
            throw new IllegalArgumentException("Manager employee does not exist: " + managerId);
        }
    }

    private int readLastDepartmentNumber() {
        try {
            Path path = Path.of(departmentSequenceFile);
            if (!Files.exists(path)) return 0;
            String value = Files.readString(path).trim();
            return value.isEmpty() ? 0 : Integer.parseInt(value);
        } catch (IOException | NumberFormatException ex) {
            return 0;
        }
    }

    private void saveLastDepartmentNumber(int number) {
        try {
            Path path = Path.of(departmentSequenceFile);
            Path parent = path.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, String.valueOf(number));
        } catch (IOException ex) {
            throw new RuntimeException("Cannot save department ID sequence.", ex);
        }
    }
}
