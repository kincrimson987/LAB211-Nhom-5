import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository doc/ghi leave_balances.csv.
 * Trong tam: ngan Wrong Leave Deduction bang synchronized va optimistic locking.
 */
public class LeaveBalanceRepository extends CsvRepository<LeaveBalance> {

    private static final String DEFAULT_PATH = "data/leave_balances.csv";
    private static final int OPTIMISTIC_MAX_RETRIES = 10;
    private static final long OPTIMISTIC_BASE_BACKOFF_MS = 50L;

    public LeaveBalanceRepository() {
        this(DEFAULT_PATH);
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
        return LeaveBalance.fromCsvLine(line);
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
     * NO_LOCK: co tinh khong khoa.
     * Dung cho simulation de thay race condition co the xay ra.
     */
    public boolean deductWithNoLock(String employeeId, LeaveType leaveType, int days) {
        LeaveBalance balance = findByEmployeeAndType(employeeId, leaveType);

        if (balance == null) {
            return false;
        }

        if (balance.getRemainingLeaveDays() < days) {
            return false;
        }

        balance.deductLeave(days);
        update(balance);
        return true;
    }

    /**
     * SYNCHRONIZED: khoa theo tung employee + leaveType.
     * Dam bao chi 1 thread duoc tru phep cua cung 1 nhan vien tai 1 thoi diem.
     */
    public boolean deductWithSync(String employeeId, LeaveType leaveType, int days) {
        synchronized ((employeeId + "_" + leaveType.name()).intern()) {
            LeaveBalance balance = findByEmployeeAndType(employeeId, leaveType);

            if (balance == null) {
                return false;
            }

            if (balance.getRemainingLeaveDays() < days) {
                return false;
            }

            balance.deductLeave(days);
            update(balance);
            return true;
        }
    }

    /**
     * OPTIMISTIC: dung version de tranh ghi de.
     * Neu version bi thread khac doi thi retry.
     */
    public boolean deductWithOptimistic(String employeeId, LeaveType leaveType, int days) {
        for (int attempt = 0; attempt < OPTIMISTIC_MAX_RETRIES; attempt++) {
            LeaveBalance balance = findByEmployeeAndType(employeeId, leaveType);

            if (balance == null) {
                return false;
            }

            if (balance.getRemainingLeaveDays() < days) {
                return false;
            }

            int expectedVersion = balance.getVersion();

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
     * FILE_LOCK: khoa ca file CSV.
     * An toan nhung co the cham hon vi moi lan thao tac khoa toan bo file.
     */
    public boolean deductWithFileLock(String employeeId, LeaveType leaveType, int days) {
        try (RandomAccessFile raf = new RandomAccessFile(getFilePath(), "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) {

            List<LeaveBalance> all = readAllLines();
            LeaveBalance balance = findInList(all, employeeId, leaveType);

            if (balance == null) {
                return false;
            }

            if (balance.getRemainingLeaveDays() < days) {
                return false;
            }

            balance.deductLeave(days);
            writeAllLines(all);
            return true;

        } catch (IOException e) {
            throw new RuntimeException("File lock failed for leave balance: " + employeeId, e);
        }
    }

    /**
     * Chi update neu version trong file van dung bang expectedVersion.
     */
    boolean updateIfVersionMatch(LeaveBalance updated, int expectedVersion) {
        List<LeaveBalance> all = readAllLines();

        for (int i = 0; i < all.size(); i++) {
            LeaveBalance current = all.get(i);

            if (!current.getBalanceId().equals(updated.getBalanceId())) {
                continue;
            }

            if (current.getVersion() != expectedVersion) {
                return false;
            }

            all.set(i, updated);
            writeAllLines(all);
            return true;
        }

        return false;
    }

    private static LeaveBalance findInList(List<LeaveBalance> balances,
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

    private static void sleepWithBackoff(int attempt) {
        try {
            Thread.sleep(OPTIMISTIC_BASE_BACKOFF_MS * (1L << attempt));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}