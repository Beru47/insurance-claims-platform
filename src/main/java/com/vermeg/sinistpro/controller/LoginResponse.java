package com.vermeg.sinistpro.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {
    @JsonProperty("token")
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public LoginResponse() {
        // Default constructor required for Jackson deserialization
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}