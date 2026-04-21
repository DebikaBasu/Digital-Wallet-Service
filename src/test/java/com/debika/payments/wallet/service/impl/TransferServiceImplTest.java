package com.debika.payments.wallet.service.impl;

import com.debika.payments.wallet.dto.TransferResponse;
import com.debika.payments.wallet.exception.InsufficientFundsException;
import com.debika.payments.wallet.exception.InvalidRequestException;
import com.debika.payments.wallet.exception.ResourceNotFoundException;
import com.debika.payments.wallet.model.Transaction;
import com.debika.payments.wallet.model.TransactionType;
import com.debika.payments.wallet.model.Wallet;
import com.debika.payments.wallet.repository.TransactionRepository;
import com.debika.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Test
    @DisplayName("Should throw exception when amount is zero or negative")
    void shouldThrowWhenInvalidAmount() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        assertThrows(InvalidRequestException.class,
                () -> transferService.transfer(fromId, toId, BigDecimal.ZERO));

        assertThrows(InvalidRequestException.class,
                () -> transferService.transfer(fromId, toId, new BigDecimal("-10")));

        verify(walletRepository, never()).findByIdWithLock(any());
    }

    @Test
    @DisplayName("Should transfer funds successfully")
    void shouldTransferFunds() {
        // Given
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet fromWallet = new Wallet(fromId, null, new BigDecimal("100.00"));
        Wallet toWallet = new Wallet(toId, null, new BigDecimal("50.00"));

        when(walletRepository.findByIdWithLock(any(UUID.class)))
                .thenAnswer(inv -> {
                    UUID id = inv.getArgument(0);
                    if (id.equals(fromId)) return Optional.of(fromWallet);
                    if (id.equals(toId)) return Optional.of(toWallet);
                    return Optional.empty();
                });
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        TransferResponse response = transferService.transfer(fromId, toId, new BigDecimal("30.00"));

        // Then
        assertEquals(new BigDecimal("70.00"), fromWallet.getBalance());
        assertEquals(new BigDecimal("80.00"), toWallet.getBalance());
        assertEquals(fromId, response.getFromWalletId());
        assertEquals(toId, response.getToWalletId());
        assertEquals(new BigDecimal("30.00"), response.getAmount());

        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        List<Transaction> transactions = transactionCaptor.getAllValues();
        assertEquals(TransactionType.TRANSFER_OUT, transactions.get(0).getType());
        assertEquals(TransactionType.TRANSFER_IN, transactions.get(1).getType());
    }

    @Test
    @DisplayName("Should throw exception when transferring to same wallet")
    void shouldThrowWhenSameWallet() {
        // Given
        UUID walletId = UUID.randomUUID();

        // When & Then
        assertThrows(InvalidRequestException.class,
                () -> transferService.transfer(walletId, walletId, new BigDecimal("10.00")));

        verify(walletRepository, never()).findByIdWithLock(any());
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void shouldThrowWhenInsufficientFunds() {
        // Given
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Wallet fromWallet = new Wallet(fromId, null, new BigDecimal("10.00"));
        Wallet toWallet = new Wallet(toId, null, new BigDecimal("50.00"));

        when(walletRepository.findByIdWithLock(any(UUID.class)))
                .thenAnswer(inv -> {
                    UUID id = inv.getArgument(0);
                    if (id.equals(fromId)) return Optional.of(fromWallet);
                    if (id.equals(toId)) return Optional.of(toWallet);
                    return Optional.empty();
                });

        // When & Then
        assertThrows(InsufficientFundsException.class,
                () -> transferService.transfer(fromId, toId, new BigDecimal("50.00")));

        assertEquals(new BigDecimal("10.00"), fromWallet.getBalance());
        assertEquals(new BigDecimal("50.00"), toWallet.getBalance());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when wallet not found")
    void shouldThrowWhenWalletNotFound() {
        // Given
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        when(walletRepository.findByIdWithLock(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> transferService.transfer(fromId, toId, new BigDecimal("10.00")));
    }
}