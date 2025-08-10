package com.example.security.security_demo.vector.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SaveBulkRequest {
    @NotEmpty
    private List<@Valid SaveRequest> items;
}