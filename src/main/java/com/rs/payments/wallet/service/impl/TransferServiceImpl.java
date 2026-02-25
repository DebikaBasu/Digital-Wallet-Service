package com.rs.payments.wallet.service.impl;

import com.rs.payments.wallet.dto.TransferResponse;
import com.rs.payments.wallet.exception.InsufficientFundsException;
import com.rs.payments.wallet.exception.InvalidRequestException;
import com.rs.payments.wallet.exception.ResourceNotFoundException;
import com.rs.payments.wallet.model.Transaction;
import com.rs.payments.wallet.model.TransactionType;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import com.rs.payments.wallet.service.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransferServiceImpl(WalletRepository walletRepository,
                               TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransferResponse transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Transfer amount must be greater than zero");
        }

        if (fromWalletId.equals(toWalletId)) {
            throw new InvalidRequestException("Cannot transfer to the same wallet");
        }

        // Acquire locks in consistent order to prevent deadlocks
        UUID firstId = fromWalletId.compareTo(toWalletId) < 0 ? fromWalletId : toWalletId;
        UUID secondId = fromWalletId.compareTo(toWalletId) < 0 ? toWalletId : fromWalletId;

        Wallet firstWallet = walletRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + firstId));
        Wallet secondWallet = walletRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + secondId));

        Wallet fromWallet = fromWalletId.equals(firstId) ? firstWallet : secondWallet;
        Wallet toWallet = toWalletId.equals(firstId) ? firstWallet : secondWallet;

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        LocalDateTime now = LocalDateTime.now();

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        Transaction transferOut = new Transaction();
        transferOut.setWallet(fromWallet);
        transferOut.setAmount(amount);
        transferOut.setType(TransactionType.TRANSFER_OUT);
        transferOut.setTimestamp(now);
        transferOut.setDescription("Transfer to wallet " + toWalletId);
        transactionRepository.save(transferOut);

        Transaction transferIn = new Transaction();
        transferIn.setWallet(toWallet);
        transferIn.setAmount(amount);
        transferIn.setType(TransactionType.TRANSFER_IN);
        transferIn.setTimestamp(now);
        transferIn.setDescription("Transfer from wallet " + fromWalletId);
        transactionRepository.save(transferIn);

        return new TransferResponse(
                transferOut.getId(),
                transferIn.getId(),
                fromWalletId,
                toWalletId,
                amount,
                now
        );
    }
}