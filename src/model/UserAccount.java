public class UserAccount extends BaseEntity {
    private String username;
    private String password;
    private String role;
    private boolean active;

    public UserAccount() {
        this.active = true;
    }

    public UserAccount(String id, long version, String username, String password, String role, boolean active) {
        super(id, version);
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
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
                + active;
    }

    @Override
    public void fromCsvLine(String line) {
        String[] parts = line.split(",");

        if (parts.length >= 6) {
            setId(parts[0].trim());
            setVersion(Long.parseLong(parts[1].trim()));
            this.username = parts[2].trim();
            this.password = parts[3].trim();
            this.role = parts[4].trim();
            this.active = Boolean.parseBoolean(parts[5].trim());
        }
    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "id='" + getId() + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}