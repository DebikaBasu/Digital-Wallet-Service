package com.debika.payments.wallet.service.impl;

import com.debika.payments.wallet.dto.BalanceResponse;
import com.debika.payments.wallet.exception.InsufficientFundsException;
import com.debika.payments.wallet.exception.ResourceNotFoundException;
import com.debika.payments.wallet.exception.WalletAlreadyExistsException;
import com.debika.payments.wallet.model.Transaction;
import com.debika.payments.wallet.model.TransactionType;
import com.debika.payments.wallet.model.User;
import com.debika.payments.wallet.model.Wallet;
import com.debika.payments.wallet.repository.TransactionRepository;
import com.debika.payments.wallet.repository.UserRepository;
import com.debika.payments.wallet.repository.WalletRepository;
import com.debika.payments.wallet.service.WalletService;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(UserRepository userRepository, WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Wallet createWalletForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getWallet() != null) {
            throw new WalletAlreadyExistsException("User already has a wallet");
        }

        Wallet wallet = new Wallet();
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);
        user.setWallet(wallet);

        user = userRepository.save(user);
        return user.getWallet();
    }


    @Override
    @Transactional
    public Wallet deposit(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription("Deposit of " + amount);
        transactionRepository.save(transaction);

        return wallet;
    }

    @Override
    @Transactional
    public Wallet withdraw(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setDescription("Withdrawal of " + amount);
        transactionRepository.save(transaction);

        return wallet;
    }

    @Override
    public BalanceResponse getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        return new BalanceResponse(wallet.getId(), wallet.getBalance());
    }
}