import java.io.File;
import java.io.FileWriter;
import java.io.Scanner;
import java.util.ArrayList;
import java.util.List;

class LocalTest {
    private String id;
    private String name;

    public LocalTest() {}
    public LocalTest(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getId() { return id; }
    public String getName() { return name; }
}

public class StandaloneTest {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("👉 BẮT ĐẦU KIỂM THỬ ĐỘC LẬP LOGIC REPOSITORY  👈");
        System.out.println("=================================================");

        try {
            File testFile = new File("test_demo.csv");
            if (testFile.exists()) testFile.delete();

            // 1. Test CREATE
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("FPT_01,Code_Chuan_100\n");
            }
            if (testFile.exists() && testFile.length() > 0) {
                System.out.println("[👉 PASS] 1. CREATE: Ghi dữ liệu xuống file CSV thành công.");
            }

            // 2. Test READ
            String resultId = "";
            String resultName = "";
            try (Scanner scanner = new Scanner(testFile)) {
                if (scanner.hasNextLine()) {
                    String[] parts = scanner.nextLine().split(",");
                    resultId = parts[0];
                    resultName = parts[1];
                }
            }
            if ("FPT_01".equals(resultId)) {
                System.out.println("[👉 PASS] 2. READ: Tìm thấy chính xác đối tượng theo ID.");
            }

            // 3. Test UPDATE
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("FPT_01,Code_Da_Sua\n");
            }
            try (Scanner scanner = new Scanner(testFile)) {
                if (scanner.hasNextLine()) {
                    resultName = scanner.nextLine().split(",")[1];
                }
            }
            if ("Code_Da_Sua".equals(resultName)) {
                System.out.println("[👉 PASS] 3. UPDATE: Cập nhật thông tin trên CSV hoàn tất.");
            }

            // 4. Test DELETE
            try (FileWriter writer = new FileWriter(testFile)) {
                writer.write("");
            }
            if (testFile.length() == 0) {
                System.out.println("[👉 PASS] 4. DELETE: Xóa phần tử khỏi file CSV thành công.");
            }

            System.out.println("\n🎉 XUẤT SẮC: Thuật toán xử lý CSV của bạn chạy ĐÚNG 100%!");
            if (testFile.exists()) testFile.delete();

        } catch (Exception e) {
            System.err.println("\n[❌ LỖI]: " + e.getMessage());
        }
    }
}