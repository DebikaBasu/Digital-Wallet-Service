package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Validation error response")
public class ValidationErrorResponse {

    @Schema(example = "Validation failed")
    private String error;

    @Schema(description = "Field level validation errors")
    private Map<String, String> fields;

    public ValidationErrorResponse(String error, Map<String, String> fields) {
        this.error = error;
        this.fields = fields;
    }

    public String getError() {
        return error;
    }

    public Map<String, String> getFields() {
        return fields;
    }
}