import java.util.List;

public class PayrollRuleRepository extends CsvRepository<PayrollRule> {

    public PayrollRuleRepository(String filePath) {
        super(filePath, PayrollRule.class);
    }

    public PayrollRule getConfig() {
        List<PayrollRule> rules = findAll();
        if (rules.isEmpty()) {
            return new PayrollRule("R001", 1L, 1.5, 0.1, 500000.0);
        }
        return rules.get(rules.size() - 1);
    }

    public void updateConfig(PayrollRule newRule) {
        List<PayrollRule> rules = findAll();
        rules.clear();
        rules.add(newRule);
        saveAll(rules);
    }
}