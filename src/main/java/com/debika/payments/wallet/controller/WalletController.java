package com.debika.payments.wallet.controller;

import com.debika.payments.wallet.dto.*;
import com.debika.payments.wallet.model.Wallet;
import com.debika.payments.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@Tag(name = "Wallet Management", description = "APIs for managing user wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(
            summary = "Create a new wallet",
            description = "Creates a wallet for a given user. A user can only have one wallet.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Wallet creation request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateWalletRequest.class)
                    )
            ),
            responses = {

                    @ApiResponse(
                            responseCode = "201",
                            description = "Wallet created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Wallet.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request or wallet already exists",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<Wallet> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {

        Wallet wallet = walletService.createWalletForUser(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(wallet);
    }

    @Operation(
            summary = "Deposit funds",
            description = "Deposits a positive amount into the specified wallet.",
            responses = {

                    @ApiResponse(
                            responseCode = "200",
                            description = "Deposit successful",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Wallet.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid amount or validation failure",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ValidationErrorResponse.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "404",
                            description = "Wallet not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Wallet> deposit(
            @Parameter(description = "Wallet ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody DepositRequest request) {

        Wallet wallet = walletService.deposit(id, request.getAmount());
        return ResponseEntity.ok(wallet);
    }

    @Operation(
            summary = "Withdraw funds",
            description = "Withdraws funds if sufficient balance exists.",
            responses = {

                    @ApiResponse(
                            responseCode = "200",
                            description = "Withdrawal successful",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Wallet.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid amount or insufficient funds",
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
                    )
            }
    )
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Wallet> withdraw(
            @Parameter(description = "Wallet ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody WithdrawRequest request) {

        Wallet wallet = walletService.withdraw(id, request.getAmount());
        return ResponseEntity.ok(wallet);
    }

    @Operation(
            summary = "Get wallet balance",
            description = "Returns the current balance of the specified wallet.",
            responses = {

                    @ApiResponse(
                            responseCode = "200",
                            description = "Balance retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = BalanceResponse.class)
                            )
                    ),

                    @ApiResponse(
                            responseCode = "404",
                            description = "Wallet not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "Wallet ID", required = true)
            @PathVariable UUID id) {

        BalanceResponse balance = walletService.getBalance(id);
        return ResponseEntity.ok(balance);
    }

}