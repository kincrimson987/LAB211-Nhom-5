public class UserAccount extends BaseEntity {
    private String username;
    private String password;
    private String role;
    private String employeeId;
    private boolean active;

    public UserAccount() {
        this.active = true;
    }

    public UserAccount(String id, long version, String username, String password, String role, boolean active) {
        this(id, version, username, password, role, active, "");
    }

    public UserAccount(String id, long version, String username, String password,
                       String role, boolean active, String employeeId) {
        super(id, version);
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
        this.employeeId = employeeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean checkPassword(String inputPassword) {
        return password != null && password.equals(inputPassword);
    }

    @Override
    public String toCsvLine() {
        return getId() + ","
                + getVersion() + ","
                + username + ","
                + password + ","
                + role + ","
                + active + ","
                + (employeeId != null ? employeeId : "");
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",", -1);

        if (parts.length != 7) {
            throw new IllegalArgumentException("Invalid user account CSV line: " + line);
        }
        setId(parts[0].trim());
        setVersion(Long.parseLong(parts[1].trim()));
        this.username = parts[2].trim();
        this.password = parts[3].trim();
        this.role = parts[4].trim();
        this.active = Boolean.parseBoolean(parts[5].trim());
        this.employeeId = parts[6].trim();
        if (getId().isEmpty() || username.isEmpty() || password.isEmpty() || role.isEmpty()) {
            throw new IllegalArgumentException("Required user account field is empty: " + line);
        }
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id='" + getId() + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", active=" + active +
                '}';
    }
}
