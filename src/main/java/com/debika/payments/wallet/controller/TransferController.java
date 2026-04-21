package com.debika.payments.wallet.controller;

import com.debika.payments.wallet.dto.ErrorResponse;
import com.debika.payments.wallet.dto.TransferRequest;
import com.debika.payments.wallet.dto.TransferResponse;
import com.debika.payments.wallet.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
@Tag(name = "Transfer Management", description = "APIs for peer-to-peer fund transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(
            summary = "Transfer funds between wallets",
            description = "Atomically transfers funds from one wallet to another wallet.",
            responses = {

                    @ApiResponse(
                            responseCode = "200",
                            description = "Transfer successful",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TransferResponse.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request, insufficient funds, or validation failure",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "404",
                            description = "Wallet not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "409",
                            description = "Duplicate resource",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request) {

        TransferResponse response = transferService.transfer(
                request.getFromWalletId(),
                request.getToWalletId(),
                request.getAmount()
        );

        return ResponseEntity.ok(response);
    }
}