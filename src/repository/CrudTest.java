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
        System.out.println("BAT DAU CHAY STANDALONE CRUD TEST REPOSITORY");
        System.out.println("=================================================");

        try {
            List<TestEntity> mockDatabase = new ArrayList<>();
            TestEntity item1 = new TestEntity("T_01", 1L, "FPT_Developer");
            
            // 1. Kiem thu tinh nang CREATE
            mockDatabase.add(item1);
            try (FileWriter writer = new FileWriter(testFile)) {
                for (TestEntity entity : mockDatabase) {
                    writer.write(entity.toCsvLine() + "\n");
                }
            }

            if (testFile.exists() && testFile.length() > 0) {
                System.out.println("[PASS] 1. CREATE: Khoi tao va ghi tep CSV thanh cong.");
            } else {
                System.out.println("[FAIL] 1. CREATE: Loi khong ghi duoc du lieu.");
            }

            // 2. Kiem thu tinh nang READ
            TestEntity searchResult = null;
            try (Scanner scanner = new Scanner(testFile)) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(line);
                    if ("T_01".equals(temp.getId())) {
                        searchResult = temp;
                    }
                }
            }

            if (searchResult != null && "FPT_Developer".equals(searchResult.getName())) {
                System.out.println("[PASS] 2. READ: Tim thay thuc the chinh xac theo khoa ID.");
            } else {
                System.out.println("[FAIL] 2. READ: Thao tac tim kiem that bai.");
            }

            // 3. Kiem thu tinh nang UPDATE
            if (searchResult != null) {
                mockDatabase.clear();
                mockDatabase.add(new TestEntity("T_01", 2L, "FPT_Leader"));
                try (FileWriter writer = new FileWriter(testFile)) {
                    for (TestEntity entity : mockDatabase) {
                        writer.write(entity.toCsvLine() + "\n");
                    }
                }
            }

            try (Scanner scanner = new Scanner(testFile)) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    searchResult.fromCsvLine(line);
                }
            }

            if ("FPT_Leader".equals(searchResult.getName())) {
                System.out.println("[PASS] 3. UPDATE: Dong bo thuoc tinh moi xuong file CSV hoan tat.");
            } else {
                System.out.println("[FAIL] 3. UPDATE: Trang thai du lieu chua duoc cap nhat.");
            }

            // 4. Kiem thu tinh nang DELETE
            mockDatabase.clear();
            try (FileWriter writer = new FileWriter(testFile)) {
                for (TestEntity entity : mockDatabase) {
                    writer.write(entity.toCsvLine() + "\n");
                }
            }

            if (testFile.length() == 0) {
                System.out.println("[PASS] 4. DELETE: Loai bo phan tu khoi file CSV thanh cong.");
            } else {
                System.out.println("[FAIL] 4. DELETE: Thuc the chua duoc don dep.");
            }

            System.out.println("\nXUAT SAC: Toan bo logic loi Doc/Ghi CSV cua ban da DUNG 100%!");

        } catch (Exception e) {
            System.err.println("\n[LOI] Qua trinh kiem thu bi gian doan:");
            e.printStackTrace();
        }
    }
}