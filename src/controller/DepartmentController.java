import java.util.List;
import java.util.UUID;

public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department addDepartment(String name, String managerId) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Department name cannot be empty.");
        if (departmentRepository.findByName(name) != null)
            throw new IllegalArgumentException("Department name already exists: " + name);

        Department dept = new Department(generateId(), 1L, name.trim(),
                managerId != null ? managerId.trim() : "");
        departmentRepository.save(dept);
        return dept;
    }

    public Department getDepartmentById(String id) {
        Department dept = departmentRepository.findById(id);
        if (dept == null)
            throw new IllegalArgumentException("Department not found: " + id);
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
        if (name != null && !name.trim().isEmpty()) dept.setName(name.trim());
        if (managerId != null) dept.setManagerId(managerId.trim());
        dept.setVersion(dept.getVersion() + 1);
        departmentRepository.update(dept);
        return dept;
    }

    public void deleteDepartment(String id) {
        getDepartmentById(id);
        departmentRepository.delete(id);
    }

    public void saveCsv() { /* data tự động lưu qua repository */ }
    public void loadCsv() { departmentRepository.findAll(); }

    private String generateId() {
        return "DEPT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}