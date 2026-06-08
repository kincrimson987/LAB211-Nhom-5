import java.util.ArrayList;
import java.util.List;

public class UserAccountRepository extends CsvRepository<UserAccount> {

    private static final String DEFAULT_PATH = "data/user_accounts.csv";

    public UserAccountRepository() {
        this(DEFAULT_PATH);
    }

    public UserAccountRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "id,version,username,password,role,active";
    }

    @Override
    public String getId(UserAccount entity) {
        return entity.getId();
    }

    @Override
    public String toLine(UserAccount entity) {
        return entity.toCsvLine();
    }

    @Override
    public UserAccount parseLine(String line) {
        UserAccount account = new UserAccount();
        account.fromCsvLine(line);
        return account;
    }

    public UserAccount findByUsername(String username) {
        for (UserAccount account : findAll()) {
            if (account.getUsername() != null
                    && account.getUsername().equals(username)) {
                return account;
            }
        }

        return null;
    }

    public List<UserAccount> findByRole(String role) {
        List<UserAccount> result = new ArrayList<>();

        for (UserAccount account : findAll()) {
            if (account.getRole() != null
                    && account.getRole().equalsIgnoreCase(role)) {
                result.add(account);
            }
        }

        return result;
    }

    public boolean authenticate(String username, String password) {
        UserAccount account = findByUsername(username);

        if (account == null) {
            return false;
        }

        if (!account.isActive()) {
            return false;
        }

        return account.checkPassword(password);
    }
}