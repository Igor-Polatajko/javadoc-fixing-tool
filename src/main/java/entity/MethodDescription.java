package entity;

import java.util.List;

public class MethodDescription {
    private String returnType;
    private List<String> params;
    private List<String> exceptionsThrown;
    private boolean present;

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public List<String> getExceptionsThrown() {
        return exceptionsThrown;
    }

    public void setExceptionsThrown(List<String> exceptionsThrown) {
        this.exceptionsThrown = exceptionsThrown;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}
