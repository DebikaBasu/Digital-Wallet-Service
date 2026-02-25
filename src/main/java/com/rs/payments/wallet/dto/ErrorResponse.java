package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(example = "Validation failed")
    private String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}