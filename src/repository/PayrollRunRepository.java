import model.PayrollRun;
import java.util.ArrayList;
import java.util.List;

public class PayrollRunRepository extends CsvRepository<PayrollRun> {

    public PayrollRunRepository(String filePath) {
        super(filePath, PayrollRun.class);
    }

    public List<PayrollRun> findByMechanism(String mechanism) {
        List<PayrollRun> result = new ArrayList<>();
        for (PayrollRun run : findAll()) {
            if (run.getMechanism() != null && run.getMechanism().equalsIgnoreCase(mechanism)) {
                result.add(run);
            }
        }
        return result;
    }

    public List<PayrollRun> findByYearMonth(String yearMonth) {
        List<PayrollRun> result = new ArrayList<>();
        for (PayrollRun run : findAll()) {
            if (run.getYearMonth() != null && run.getYearMonth().equals(yearMonth)) {
                result.add(run);
            }
        }
        return result;
    }

    public PayrollRun findLatest() {
        List<PayrollRun> all = findAll();
        if (all.isEmpty()) return null;
        return all.get(all.size() - 1);
    }
}