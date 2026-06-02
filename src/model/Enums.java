public class Enums {
    public enum LeaveType {
        ANNUAL, SICK
    }

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum PayrollStatus {
        PENDING, PROCESSED
    }

    public enum LockMechanism {
        NONE, PESSIMISTIC, OPTIMISTIC, QUEUE
    }

    public enum EmploymentType {
        FULLTIME, PARTTIME
    }
}
