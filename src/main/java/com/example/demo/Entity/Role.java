package com.example.demo.Entity;

public enum Role {

    USER("USER"), ADMIN("ADMIN");

    final String p;

    Role(String user) {
        this.p = user;
    }
}
