package com.debika.payments.wallet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(description = "Transfer result details")
public class TransferResponse {

    @Schema(description = "Transfer out transaction ID")
    private UUID transferOutId;

    @Schema(description = "Transfer in transaction ID")
    private UUID transferInId;

    @Schema(description = "Source wallet ID")
    private UUID fromWalletId;

    @Schema(description = "Destination wallet ID")
    private UUID toWalletId;

    @Schema(description = "Transfer amount")
    private BigDecimal amount;

    @Schema(description = "Timestamp of the transfer")
    private LocalDateTime timestamp;
}