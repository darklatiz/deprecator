package io.gigabyte.labs.deprecator;

import java.util.List;

public class Endpoint {
    private String path;
    private boolean deprecated;
    private String condition;
    private String conditionValue;
    private List<Header> headers;
    private List<Header> bypass;

    // Getters, setters, and other necessary methods...

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public List<Header> getBypass() {
        return bypass;
    }

    public void setBypass(List<Header> bypass) {
        this.bypass = bypass;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }
}
