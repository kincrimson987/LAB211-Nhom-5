import java.util.ArrayList;
import java.util.List;

public class PayrollRuleRepository extends CsvRepository<PayrollRule> {

    /*
     * private static final String DEFAULT_PATH = "data/payroll_rules.csv";
     * 
     * public PayrollRuleRepository() {
     * this(DEFAULT_PATH);
     * }
     */
    public PayrollRuleRepository() {
        super("data/payroll_rules.csv");
    }

    public PayrollRuleRepository(String filePath) {
        super(filePath);
    }

    @Override
    public String getHeader() {
        return "standardWorkingDays,workingHoursPerDay,overtimeMultiplier,attendanceBonus,taxRate,taxThreshold";
    }

    @Override
    public String getId(PayrollRule entity) {
        return "PAYROLL_RULE";
    }

    @Override
    public String toLine(PayrollRule entity) {
        return entity.getStandardWorkingDays() + ","
                + entity.getWorkingHoursPerDay() + ","
                + entity.getOvertimeMultiplier() + ","
                + entity.getAttendanceBonus() + ","
                + entity.getTaxRate() + ","
                + entity.getTaxThreshold();
    }

    @Override
    public PayrollRule parseLine(String line) {
        String[] parts = line.split(",");

        if (parts.length < 6) {
            return new PayrollRule();
        }

        int standardWorkingDays = Integer.parseInt(parts[0].trim());
        int workingHoursPerDay = Integer.parseInt(parts[1].trim());
        double overtimeMultiplier = Double.parseDouble(parts[2].trim());
        double attendanceBonus = Double.parseDouble(parts[3].trim());
        double taxRate = Double.parseDouble(parts[4].trim());
        double taxThreshold = Double.parseDouble(parts[5].trim());

        return new PayrollRule(
                standardWorkingDays,
                workingHoursPerDay,
                overtimeMultiplier,
                attendanceBonus,
                taxRate,
                taxThreshold);
    }

    public PayrollRule getConfig() {
        List<PayrollRule> rules = findAll();

        if (rules.isEmpty()) {
            return new PayrollRule();
        }

        return rules.get(rules.size() - 1);
    }

    public void updateConfig(PayrollRule newRule) {
        List<PayrollRule> rules = new ArrayList<>();
        rules.add(newRule);
        writeAllLines(rules);
    }
}