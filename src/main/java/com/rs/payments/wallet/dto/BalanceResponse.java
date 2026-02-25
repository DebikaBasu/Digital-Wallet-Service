package com.rs.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(description = "Wallet balance response")
public class BalanceResponse {

    @Schema(description = "Wallet ID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID walletId;

    @Schema(description = "Current balance", example = "100.50")
    private BigDecimal balance;
}