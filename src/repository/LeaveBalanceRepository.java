import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository đọc/ghi leave_balances.csv.
 * Ngăn Wrong Leave Deduction bằng synchronized và optimistic locking.
 */
public class LeaveBalanceRepository extends CsvRepository<LeaveBalance> {

    /*
     * private static final String DEFAULT_PATH = "data/leave_balances.csv";
     * 
     * public LeaveBalanceRepository() {
     * this(DEFAULT_PATH);
     * }
     * 
     * private static final int OPTIMISTIC_MAX_RETRIES = 10;
     * private static final long OPTIMISTIC_BASE_BACKOFF_MS = 50L;
     * 
     * public LeaveBalanceRepository() {
     * this(DEFAULT_PATH);
     * }
     */
    private int OPTIMISTIC_MAX_RETRIES = 10;
    private long OPTIMISTIC_BASE_BACKOFF_MS = 50L;

    public LeaveBalanceRepository() {
        super("data/leave_balances.csv");
    }

    public LeaveBalanceRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "balanceId,employeeId,leaveType,totalLeaveDays,usedLeaveDays,remainingLeaveDays,version";
    }

    @Override
    public String getId(LeaveBalance entity) {
        return entity.getBalanceId();
    }

    @Override
    public String toLine(LeaveBalance entity) {
        return entity.toCsvLine();
    }

    @Override
    public LeaveBalance parseLine(String line) {
        LeaveBalance balance = new LeaveBalance();
        balance.fromCsvLine(line);
        return balance;
    }

    public List<LeaveBalance> findByEmployee(String employeeId) {
        return findAll().stream()
                .filter(balance -> balance.getEmployeeId().equals(employeeId))
                .collect(Collectors.toList());
    }

    public LeaveBalance findByEmployeeAndType(String employeeId, LeaveType leaveType) {
        return findAll().stream()
                .filter(balance -> balance.getEmployeeId().equals(employeeId))
                .filter(balance -> balance.getLeaveType() == leaveType)
                .findFirst()
                .orElse(null);
    }

    /**
     * NO_LOCK: cố ý không khóa để thấy race condition.
     */
    public boolean deductWithNoLock(String employeeId, LeaveType leaveType, int days) {
        LeaveBalance balance = findByEmployeeAndType(employeeId, leaveType);
        if (balance == null)
            return false;
        if (balance.getRemainingLeaveDays() < days)
            return false;

        balance.deductLeave(days);
        update(balance);
        return true;
    }

    /**
     * SYNCHRONIZED: khóa theo từng employee + leaveType.
     */
    public boolean deductWithSync(String employeeId, LeaveType leaveType, int days) {
        synchronized ((employeeId + "_" + leaveType.name()).intern()) {
            if (days <= 0)
                return false;
            LeaveBalance balance = findByEmployeeAndType(employeeId, leaveType);
            if (balance == null)
                return false;
            if (balance.getRemainingLeaveDays() < days)
                return false;

            balance.deductLeave(days);
            update(balance);
            return true;
        }
    }

    /**
     * OPTIMISTIC: dùng version để tránh ghi đè.
     */
    public boolean deductWithOptimistic(String employeeId, LeaveType leaveType, int days) {
        for (int attempt = 0; attempt < OPTIMISTIC_MAX_RETRIES; attempt++) {
            LeaveBalance balance = findByEmployeeAndType(employeeId, leaveType);
            if (balance == null)
                return false;
            if (balance.getRemainingLeaveDays() < days)
                return false;

            long expectedVersion = balance.getVersion();

            try {
                balance.deductLeave(days);
            } catch (IllegalArgumentException e) {
                return false;
            }

            if (updateIfVersionMatch(balance, expectedVersion)) {
                return true;
            }

            sleepWithBackoff(attempt);
        }
        return false;
    }

    /**
     * FILE_LOCK: khóa cả file CSV.
     */
    public boolean deductWithFileLock(String employeeId, LeaveType leaveType, int days) {
        try (RandomAccessFile raf = new RandomAccessFile(getFilePath(), "rw");
                FileChannel channel = raf.getChannel();
                FileLock lock = channel.lock()) {

            List<LeaveBalance> all = readAllLines();
            LeaveBalance balance = findInList(all, employeeId, leaveType);
            if (balance == null)
                return false;
            if (balance.getRemainingLeaveDays() < days)
                return false;

            balance.deductLeave(days);
            writeAllLines(all);
            return true;

        } catch (IOException e) {
            throw new RuntimeException("File lock failed for leave balance: " + employeeId, e);
        }
    }

    /**
     * Chỉ update nếu version trong file vẫn đúng bằng expectedVersion.
     */
    boolean updateIfVersionMatch(LeaveBalance updated, long expectedVersion) {
        List<LeaveBalance> all = readAllLines();
        for (int i = 0; i < all.size(); i++) {
            LeaveBalance current = all.get(i);
            if (!current.getBalanceId().equals(updated.getBalanceId()))
                continue;
            if (current.getVersion() != expectedVersion)
                return false;
            all.set(i, updated);
            writeAllLines(all);
            return true;
        }
        return false;
    }

    /**
     * Tìm balance trong list — instance method thay vì static.
     */
    private LeaveBalance findInList(List<LeaveBalance> balances,
            String employeeId,
            LeaveType leaveType) {
        for (LeaveBalance balance : balances) {
            if (balance.getEmployeeId().equals(employeeId)
                    && balance.getLeaveType() == leaveType) {
                return balance;
            }
        }
        return null;
    }

    private void sleepWithBackoff(int attempt) {
        try {
            Thread.sleep(OPTIMISTIC_BASE_BACKOFF_MS * (1L << attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
   public void deductWithFileLock(String filePath, Runnable deductAction) throws Exception {
        FileLockManager.executeWithLock(filePath, () -> {
            deductAction.run();
            return null;
        });
    }
}
