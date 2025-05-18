package com.vermeg.sinistpro.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {
    @JsonProperty("identifier")
    private String identifier; // Can be CIN, email, or telephone
    @JsonProperty("password")
    private String password;

    public LoginRequest() {
        // Default constructor required for Jackson deserialization
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}