import java.util.ArrayList;
import java.util.List;

public class PayrollRunRepository extends CsvRepository<PayrollRun> {

    /*private static final String DEFAULT_PATH = "data/payroll_runs.csv";

    public PayrollRunRepository() {
        this(DEFAULT_PATH);
    }*/
    public PayrollRunRepository() {
        super("data/payroll_runs.csv");
    }


    public PayrollRunRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "id,version,yearMonth,mechanism,elapsedMs,successCount,doublePaymentCount,wrongLeaveCount,tps";
    }

    @Override
    public String getId(PayrollRun entity) {
        return entity.getId();
    }

    @Override
    public String toLine(PayrollRun entity) {
        return entity.toCsvLine();
    }

    @Override
    public PayrollRun parseLine(String line) {
        PayrollRun run = new PayrollRun();
        run.fromCsvLine(line);
        return run;
    }

    public List<PayrollRun> findByMechanism(String mechanism) {
        List<PayrollRun> result = new ArrayList<>();

        for (PayrollRun run : findAll()) {
            if (run.getMechanism() != null
                    && run.getMechanism().equalsIgnoreCase(mechanism)) {
                result.add(run);
            }
        }

        return result;
    }

    public List<PayrollRun> findByYearMonth(String yearMonth) {
        List<PayrollRun> result = new ArrayList<>();

        for (PayrollRun run : findAll()) {
            if (run.getYearMonth() != null
                    && run.getYearMonth().equals(yearMonth)) {
                result.add(run);
            }
        }

        return result;
    }

    public PayrollRun findLatest() {
        List<PayrollRun> all = findAll();

        if (all.isEmpty()) {
            return null;
        }

        return all.get(all.size() - 1);
    }
}