package io.gigabyte.labs.deprecator;

import java.util.List;

public class Header {
    private String name;
    private List<String> values;
    private String action;

    // Getters, setters, and other necessary methods...


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}