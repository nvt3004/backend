package com.errors;

public class FieldErrorDTO {
    private String field;
    private String error;

    public FieldErrorDTO(String field, String error) {
        this.field = field;
        this.error = error;
    }

    // Getters and setters
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
