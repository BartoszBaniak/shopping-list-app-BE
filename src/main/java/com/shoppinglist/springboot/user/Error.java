package com.shoppinglist.springboot.user;

public class Error {
    private String type;
    private String field;
    private String message;

    public Error(String type, String field, String message) {
        this.type = type;
        this.field = field;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}