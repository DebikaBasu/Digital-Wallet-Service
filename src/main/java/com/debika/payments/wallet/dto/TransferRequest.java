package com.debika.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to transfer funds between wallets")
public class TransferRequest {

    @NotNull(message = "Source wallet ID is required")
    @Schema(description = "Source wallet ID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID fromWalletId;

    @NotNull(message = "Destination wallet ID is required")
    @Schema(description = "Destination wallet ID", example = "b1f8e321-7c9b-46e2-8d1a-4f5a6b7c8d9e")
    private UUID toWalletId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Amount to transfer", example = "25.00")
    private BigDecimal amount;
}