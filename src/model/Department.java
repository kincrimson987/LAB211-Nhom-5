public class Department extends BaseEntity {
    private String name;
    private String managerId;

    public Department() {
    }

    public Department(String id, long version, String name, String managerId) {
        super(id, version);
        this.name = name;
        this.managerId = managerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%d,%s,%s", getId(), getVersion(), name, managerId);
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid department CSV line: " + line);
        }
        setId(parts[0].trim());
        setVersion(Long.parseLong(parts[1].trim()));
        this.name = parts[2].trim();
        this.managerId = parts[3].trim();

        if (getId().isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("Department ID and name are required: " + line);
        }
    }
}
