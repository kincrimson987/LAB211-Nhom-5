import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PayrollAndLeaveTest {

    @Test
    public void testPayrollAndLeaveFlowViaController() {
        ReportView view = new ReportView();
        PayrollController controller = new PayrollController(view);

        String[] emps = {"E001", "E002", "E003"};
        assertEquals(3, controller.processSingleThreadPayroll(emps));

        assertEquals(3, controller.processLeaveApproval(12, 3));
        assertEquals(0, controller.processLeaveApproval(2, 5)); 
    }
}