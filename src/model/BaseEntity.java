public abstract class BaseEntity {
    protected String id;
    protected long version;

    public BaseEntity() {
    }

    public BaseEntity(String id, long version) {
        this.id = id;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public abstract String toCsvLine();

    public abstract void fromCsvLine(String line);
}
