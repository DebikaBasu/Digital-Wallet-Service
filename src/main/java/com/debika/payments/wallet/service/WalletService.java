package com.debika.payments.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.debika.payments.wallet.dto.BalanceResponse;
import com.debika.payments.wallet.model.Wallet;

public interface WalletService {
    Wallet createWalletForUser(UUID userId);
    Wallet deposit(UUID walletId, BigDecimal amount);
    Wallet withdraw(UUID walletId, BigDecimal amount);
    BalanceResponse getBalance(UUID walletId);
}