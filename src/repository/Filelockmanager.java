import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.Callable;

public class FileLockManager {
    public static <T> T executeWithLock(String filePath, Callable<T> action) throws Exception {
        // Mở file ở chế độ read-write ("rw") để thao tác kênh dữ liệu (Channel)
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
             FileChannel channel = raf.getChannel()) {
            
            // Ép luồng (Thread) phải đợi cho đến khi lấy được khóa độc quyền (Exclusive Lock)
            FileLock lock = channel.lock(); 
            try {
                return action.call(); // Thực thi hàm nghiệp vụ đọc/ghi file an toàn ở đây
            } finally {
                if (lock != null && lock.isValid()) {
                    lock.release(); // Giải phóng khóa sau khi làm xong để luồng khác vào
                }
            }
        }
    }
}