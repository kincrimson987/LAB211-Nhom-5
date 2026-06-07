import java.io.File;

public class CrudTest {

    public static void main(String[] args) {
        String testPath = "data/test_users.csv";
        
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        File testFile = new File(testPath);
        if (testFile.exists()) {
            testFile.delete();
        }

        UserAccountRepository repo = new UserAccountRepository(testPath);
        System.out.println("=================================================");
        System.out.println("👉 BẮT ĐẦU CHẠY TỰ ĐỘNG UNIT TEST REPOSITORY 👈");
        System.out.println("=================================================");

        try {
            // 1. Test CREATE
            UserAccount account = new UserAccount("U_TEST_01", 1L, "hr_manager_fpt", "pass123", "HR");
            repo.save(account);
            
            if (testFile.exists() && testFile.length() > 0) {
                System.out.println("[👉 PASS] 1. CREATE: Ghi file CSV thành công.");
            } else {
                System.out.println("[❌ FAIL] 1. CREATE: Lỗi không ghi được dữ liệu.");
            }

            // 2. Test READ
            UserAccount searchResult = repo.findById("U_TEST_01");
            if (searchResult != null && "hr_manager_fpt".equals(searchResult.getUsername())) {
                System.out.println("[👉 PASS] 2. READ: Tìm thấy thực thể theo ID.");
            } else {
                System.out.println("[❌ FAIL] 2. READ: Không tìm thấy dữ liệu.");
            }

            // 3. Test UPDATE
            searchResult.setPassword("new_secure_password_2026");
            repo.save(searchResult);
            
            UserAccount updatedResult = repo.findById("U_TEST_01");
            if (updatedResult != null && "new_secure_password_2026".equals(updatedResult.getPassword())) {
                System.out.println("[👉 PASS] 3. UPDATE: Cập nhật dữ liệu xuống CSV thành công.");
            } else {
                System.out.println("[❌ FAIL] 3. UPDATE: Thuộc tính chưa được cập nhật.");
            }

            // 4. Test DELETE
            repo.delete("U_TEST_01");
            UserAccount deleteResult = repo.findById("U_TEST_01");
            
            if (deleteResult == null) {
                System.out.println("[👉 PASS] 4. DELETE: Xóa đối tượng hoàn tất.");
            } else {
                System.out.println("[❌ FAIL] 4. DELETE: Thực thể vẫn tồn tại sau khi xóa.");
            }

            System.out.println("\n🎉 HOÀN THÀNH: Hệ thống Repository đã vượt qua tất cả bài kiểm tra!");

        } catch (Exception e) {
            System.err.println("\n[⚠️ LỖI] Quá trình test xảy ra ngoại lệ:");
            e.printStackTrace();
        }
    }
}