package com.vermeg.sinistpro.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ClaimRequest {
    private String type;
    private LocalDate date;
    private String lieu;
    private String description;
}
