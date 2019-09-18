package entity;

public class DescribedEntity {
    private boolean isPresent;
    private Type type;
    private String data;

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public enum Type {
        METHOD, CLASS, INTERFACE, FIELD
    }
}
