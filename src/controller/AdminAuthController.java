
public class AdminAuthController extends AuthController {

    public AdminAuthController(UserAccount account) {
        super(account);
    }

    @Override
    public boolean canAccess(String feature) {
        return true;
    }
}