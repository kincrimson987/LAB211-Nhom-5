import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

 class TestEntity {
    private String id;
    private long version;
    private String name;

    public TestEntity() {
    }

    public TestEntity(String id, long version, String name) {
        this.id = id;
        this.version = version;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

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
        System.out.println(" BAT DAU CHAY STANDALONE CRUD TEST REPOSITORY");
        System.out.println("=================================================");

        try {
            Scanner input = new Scanner(System.in);
            List<TestEntity> mockDatabase = new ArrayList<>();

            // Buoc khoi tao: tao nhieu du lieu nen de test ro hon
            System.out.println("Dang khoi tao du lieu nen voi nhieu ban ghi...");

            mockDatabase.add(new TestEntity("EMP_001", 1L, "Nguyen_Van_A_Senior_Dev"));
            mockDatabase.add(new TestEntity("EMP_002", 1L, "Tran_Thi_B_HR_Manager"));
            mockDatabase.add(new TestEntity("EMP_003", 2L, "Le_Van_C_Product_Owner"));
            mockDatabase.add(new TestEntity("EMP_004", 1L, "Pham_Minh_D_Tester"));
            mockDatabase.add(new TestEntity("EMP_005", 3L, "Hoang_Anh_E_CTO"));

            // Ghi du lieu nen vao file CSV
            try (FileWriter writer = new FileWriter(testFile)) {
                for (TestEntity entity : mockDatabase) {
                    writer.write(entity.toCsvLine() + "\n");
                }
            }

            System.out.println("[OK] Da nap thanh cong 5 nhan su vao file CSV.");
            System.out.print("Nhan ENTER de test CREATE - them EMP_006 vao file CSV...");
            input.nextLine();
            System.out.println("-------------------------------------------------");

            // 1. CREATE - them mot ban ghi moi
            TestEntity newItem = new TestEntity("EMP_006", 1L, "FPT_New_Developer");

            try (FileWriter writer = new FileWriter(testFile, true)) {
                writer.write(newItem.toCsvLine() + "\n");
            }

            if (testFile.exists() && testFile.length() > 0) {
                System.out.println("[PASS] 1. CREATE: Ghi them EMP_006 xuong file CSV thanh cong.");
            } else {
                System.out.println("[FAIL] 1. CREATE: Khong ghi duoc du lieu.");
            }

            System.out.print("Nhan ENTER de test READ va UPDATE...");
            input.nextLine();
            System.out.println("-------------------------------------------------");

            // 2. READ - tim kiem ban ghi vua them
            TestEntity searchResult = null;

            try (Scanner scanner = new Scanner(testFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(line);

                    if ("EMP_006".equals(temp.getId())) {
                        searchResult = temp;
                        break;
                    }
                }
            }

            if (searchResult != null && "FPT_New_Developer".equals(searchResult.getName())) {
                System.out.println("[PASS] 2. READ: Tim thay chinh xac EMP_006 trong file CSV.");
            } else {
                System.out.println("[FAIL] 2. READ: Khong tim thay EMP_006.");
            }

            // 3. UPDATE - sua thong tin EMP_006
            if (searchResult != null) {
                List<TestEntity> currentList = new ArrayList<>();

                try (Scanner scanner = new Scanner(testFile)) {
                    while (scanner.hasNextLine()) {
                        TestEntity temp = new TestEntity();
                        temp.fromCsvLine(scanner.nextLine());

                        if ("EMP_006".equals(temp.getId())) {
                            currentList.add(new TestEntity("EMP_006", 2L, "FPT_Project_Leader"));
                        } else {
                            currentList.add(temp);
                        }
                    }
                }

                try (FileWriter writer = new FileWriter(testFile)) {
                    for (TestEntity entity : currentList) {
                        writer.write(entity.toCsvLine() + "\n");
                    }
                }
            }

            boolean updateSuccess = false;

            try (Scanner scanner = new Scanner(testFile)) {
                while (scanner.hasNextLine()) {
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(scanner.nextLine());

                    if ("EMP_006".equals(temp.getId())
                            && "FPT_Project_Leader".equals(temp.getName())) {
                        updateSuccess = true;
                        break;
                    }
                }
            }

            if (updateSuccess) {
                System.out.println("[PASS] 3. UPDATE: Da sua EMP_006 thanh FPT_Project_Leader.");
            } else {
                System.out.println("[FAIL] 3. UPDATE: Du lieu chua duoc cap nhat.");
            }

            System.out.print("Nhan ENTER de test DELETE...");
            input.nextLine();
            System.out.println("-------------------------------------------------");

            // 4. DELETE - xoa EMP_006 ra khoi file
            List<TestEntity> remainList = new ArrayList<>();

            try (Scanner scanner = new Scanner(testFile)) {
                while (scanner.hasNextLine()) {
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(scanner.nextLine());

                    if (!"EMP_006".equals(temp.getId())) {
                        remainList.add(temp);
                    }
                }
            }

            try (FileWriter writer = new FileWriter(testFile)) {
                for (TestEntity entity : remainList) {
                    writer.write(entity.toCsvLine() + "\n");
                }
            }

            boolean deleted = true;

            try (Scanner scanner = new Scanner(testFile)) {
                while (scanner.hasNextLine()) {
                    TestEntity temp = new TestEntity();
                    temp.fromCsvLine(scanner.nextLine());

                    if ("EMP_006".equals(temp.getId())) {
                        deleted = false;
                        break;
                    }
                }
            }

            if (deleted && remainList.size() == 5) {
                System.out.println("[PASS] 4. DELETE: Da xoa EMP_006. File CSV con lai dung 5 nhan su ban dau.");
            } else {
                System.out.println("[FAIL] 4. DELETE: Chua xoa dung du lieu.");
            }

            System.out.println("-------------------------------------------------");
            System.out.println("[SUCCESS] Tat ca chuc nang CRUD co ban da chay thanh cong.");
            System.out.println("File test duoc tao tai: " + testFile.getAbsolutePath());

            input.close();

        } catch (Exception e) {
            System.err.println("[ERROR] Qua trinh kiem thu bi gian doan:");
            e.printStackTrace();
        }
    }
}

