package com.savantlabs.adapters.model;

import java.time.OffsetDateTime;

public class Author {
    private String name;
    private String email;
    private OffsetDateTime date;

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}