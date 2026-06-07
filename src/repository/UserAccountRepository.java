import model.UserAccount;
import java.util.ArrayList;
import java.util.List;

public class UserAccountRepository extends CsvRepository<UserAccount> {

    public UserAccountRepository(String filePath) {
        super(filePath, UserAccount.class);
    }

    public UserAccount findByUsername(String username) {
        for (UserAccount acc : findAll()) {
            if (acc.getUsername() != null && acc.getUsername().equals(username)) {
                return acc;
            }
        }
        return null;
    }

    public List<UserAccount> findByRole(String role) {
        List<UserAccount> result = new ArrayList<>();
        for (UserAccount acc : findAll()) {
            if (acc.getRole() != null && acc.getRole().equalsIgnoreCase(role)) {
                result.add(acc);
            }
        }
        return result;
    }
}