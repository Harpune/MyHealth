package de.dbis.myhealth.models;

public class Result<T> {
    private T value;

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void resetValue() {
        this.value = null;
    }
}
