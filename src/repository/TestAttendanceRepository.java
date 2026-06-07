public class TestAttendanceRepository {
    public static void main(String[] args) {
        AttendanceRepository repo = new AttendanceRepository("data/attendance.csv");

        AttendanceRecord record = repo.findByEmployeeAndMonth("E0001", "2023-01");

        if (record == null) {
            System.out.println("[FAIL] Khong tim thay attendance cua E0001 thang 2023-01");
            return;
        }

        System.out.println("[OK] Tim thay attendance:");
        System.out.println(record);

        System.out.println("Tong record cua E0001: " + repo.findByEmployee("E0001").size());
        System.out.println("Tong record thang 2023-01: " + repo.findByMonth("2023-01").size());
    }
}