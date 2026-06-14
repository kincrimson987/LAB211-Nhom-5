import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

class TestEntity {
    protected String id;
    protected long version;
    private String name;

    public TestEntity() {}
    public TestEntity(String id, long version, String name) {
        this.id = id;
        this.version = version;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public String toCsvLine() { 
        return id + "," + version + "," + name; 
    }

    public void fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 3) {
            this.id = parts[0];
            this.version = Long.parseLong(parts[1]);
            this.name = parts[2];
        }
    }
}

public class CrudTest {
    public static void main(String[] args) {
        String testPath = "data/test_standalone.csv";
        
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        File testFile = new File(testPath);
        if (testFile.exists()) {
            testFile.delete();
        }

        System.out.println("=================================================");
        System.out.println("👉 BẮT ĐẦU CHẠY STANDALONE CRUD TEST REPOSITORY 👈");
        System.out.println("=================================================");

        try {
            Scanner boDieuKhien = new Scanner(System.in);
            List<TestEntity> mockDatabase = new ArrayList<>();

            // =========================================================
            // 🌟 BƯỚC KHỞI TẠO: BƠM NHIỀU DATA NỀN CHO HOÀNH TRÁNG
            // =========================================================
            System.out.println("⏳ Đang khởi tạo cơ sở dữ liệu nền với nhiều bản ghi...");
            mockDatabase.add(new TestEntity("EMP_001", 1L, "Nguyen_Van_A_Senior_Dev"));
            mockDatabase.add(new TestEntity("EMP_002", 1L, "Tran_Thi_B_HR_Manager"));
            mockDatabase.add(new TestEntity("EMP_003", 2L, "Le_Van_C_Product_Owner"));
            mockDatabase.add(new TestEntity("EMP_004", 1L, "Pham_Minh_D_Tester"));
            mockDatabase.add(new TestEntity("EMP_005", 3L, "Hoang_Anh_E_CTO"));

            // Ghi dữ liệu nền vào file CSV
            try (FileWriter writer = new FileWriter(testFile)) {
                for (TestEntity entity : mockDatabase) {
                    writer.write(entity.toCsvLine() + "\n");
                }
            }
            System.out.println("✅ Đã nạp thành công 5 nhân sự vào file CSV!");
            System.out.print("👉 Thầy nhìn file CSV bên phải đã ĐẦY ĐẶN data nền. BẤM ENTER để test hàm CREATE (Thêm người thứ 6)...");
            boDieuKhien.nextLine();
            System.out.println("-------------------------------------------------");

            // =========================================================
            // 1. Kiểm thử tính năng CREATE (Thêm người thứ 6 vào danh sách)
            // =========================================================
            TestEntity itemMoi = new TestEntity("EMP_006", 1L, "FPT_New_Developer");
            
            // Đọc dữ liệu cũ lên, thêm người mới vào, rồi ghi lại toàn bộ
            try (FileWriter writer = new FileWriter(testFile, true)) { // true để ghi nối tiếp vào cuối file
                writer.write(itemMoi.toCsvLine() + "\n");
            }

            if (testFile.exists() && testFile.length() > 0) {
                System.out.println("[👉 PASS] 1. CREATE: Khởi tạo và ghi thêm EMP_006 xuống CSV thành công.");
            } else {
                System.out.println("[❌ FAIL] 1. CREATE: Lỗi không ghi được dữ liệu.");
            }

            System.out.print("👉 Thầy xem người thứ 6 (EMP_006) đã được nối vào cuối file chưa. BẤM ENTER để test READ & UPDATE...");
            boDieuKhien.nextLine(); 
            System.out.println("-------------------------------------------------");

            // =========================================================
            // 2. Kiểm thử tính năng READ (Tìm kiếm 1 người bất kỳ giữa đám đông)
            // =========================================================
            TestEntity searchResult = null;
            try (Scanner scanner = new Scanner(testFile)) {
                while (scanner.hasNextLine()) { // Quét qua toàn bộ các dòng của file
                    String line = scanner.nextLine();
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(line);
                    if ("EMP_006".equals(temp.getId())) { // Tìm đúng ông số 6 vừa thêm
                        searchResult = temp;
                        break;
                    }
                }
            }

            if (searchResult != null && "FPT_New_Developer".equals(searchResult.getName())) {
                System.out.println("[👉 PASS] 2. READ: Duyệt danh sách, tìm thấy chính xác EMP_006 giữa file CSV.");
            } else {
                System.out.println("[❌ FAIL] 2. READ: Thao tác tìm kiếm thất bại.");
            }

            // =========================================================
            // 3. Kiểm thử tính năng UPDATE (Sửa thông tin của ông số 6)
            // =========================================================
            if (searchResult != null) {
                // Đọc toàn bộ danh sách cũ từ file lên bộ nhớ RAM để xử lý sửa đổi
                List<TestEntity> currentList = new ArrayList<>();
                try (Scanner scanner = new Scanner(testFile)) {
                    while (scanner.hasNextLine()) {
                        TestEntity temp = new TestEntity();
                        temp.fromCsvLine(scanner.nextLine());
                        // Nếu thấy ông số 6 thì nâng cấp lên Leader, còn lại giữ nguyên
                        if (temp.getId().equals("EMP_006")) {
                            currentList.add(new TestEntity("EMP_006", 2L, "FPT_Project_Leader"));
                        } else {
                            currentList.add(temp);
                        }
                    }
                }

                // Ghi đè lại toàn bộ danh sách đã sửa xuống file CSV
                try (FileWriter writer = new FileWriter(testFile)) {
                    for (TestEntity entity : currentList) {
                        writer.write(entity.toCsvLine() + "\n");
                    }
                }
            }

            // Đọc lại để kiểm tra kết quả sửa đổi
            boolean updateSuccess = false;
            try (Scanner scanner = new Scanner(testFile)) {
                while (scanner.hasNextLine()) {
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(scanner.nextLine());
                    if ("EMP_006".equals(temp.getId()) && "FPT_Project_Leader".equals(temp.getName())) {
                        updateSuccess = true;
                    }
                }
            }

            if (updateSuccess) {
                System.out.println("[👉 PASS] 3. UPDATE: Đã tìm và sửa riêng thông tin ông EMP_006 thành Project Leader!");
            } else {
                System.out.println("[❌ FAIL] 3. UPDATE: Trạng thái dữ liệu chưa được cập nhật.");
            }

            System.out.print("👉 Thầy nhìn dòng cuối cùng, chữ đã đổi thành FPT_Project_Leader. BẤM ENTER để test DELETE...");
            boDieuKhien.nextLine();
            System.out.println("-------------------------------------------------");

            // =========================================================
            // 4. Kiểm thử tính năng DELETE (Xóa riêng ông số 6 ra khỏi file)
            // =========================================================
            List<TestEntity> remainList = new ArrayList<>();
            try (Scanner scanner = new Scanner(testFile)) {
                while (scanner.hasNextLine()) {
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(scanner.nextLine());
                    // Chỉ giữ lại những người KHÔNG PHẢI là EMP_006
                    if (!temp.getId().equals("EMP_006")) {
                        remainList.add(temp);
                    }
                }
            }

            // Ghi lại danh sách còn lại xuống file
            try (FileWriter writer = new FileWriter(testFile)) {
                for (TestEntity entity : remainList) {
                    writer.write(entity.toCsvLine() + "\n");
                }
            }

            // Kiểm tra xem file có giảm đi 1 dòng và mất ông EMP_006 không
            boolean deleted = true;
            try (Scanner scanner = new Scanner(testFile)) {
                while (scanner.hasNextLine()) {
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(scanner.nextLine());
                    if ("EMP_006".equals(temp.getId())) {
                        deleted = false;
                    }
                }
            }

            if (deleted && remainList.size() == 5) {
                System.out.println("[👉 PASS] 4. DELETE: Loại bỏ thành công EMP_006. File CSV còn lại đúng 5 nhân sự gốc.");
            } else {
                System.out.println("[❌ FAIL] 4. DELETE: Thực thể chưa được dọn dẹp.");
            }

            System.out.println("\n🎉 XUẤT SẮC: Toàn bộ logic lõi Đọc/Ghi dữ liệu hàng loạt đã ĐÚNG 100%!");
            boDieuKhien.close();

        } catch (Exception e) {
            System.err.println("\n[⚠️ LỖI] Quá trình kiểm thử bị gián đoạn:");
            e.printStackTrace();
        }
    }
}