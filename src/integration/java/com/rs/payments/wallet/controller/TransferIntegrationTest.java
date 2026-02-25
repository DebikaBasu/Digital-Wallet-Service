package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.BaseIntegrationTest;
import com.rs.payments.wallet.dto.TransferRequest;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.model.Wallet;
import com.rs.payments.wallet.repository.TransactionRepository;
import com.rs.payments.wallet.repository.UserRepository;
import com.rs.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferIntegrationTest extends BaseIntegrationTest {

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldRollbackWhenInsufficientFunds() {

        User user1 = userRepository.save(new User(null, "user3", "u3@test.com", null));
        User user2 = userRepository.save(new User(null, "user4", "u4@test.com", null));

        Wallet wallet1 = walletRepository.save(
                new Wallet(null, user1, new BigDecimal("10.00")));
        Wallet wallet2 = walletRepository.save(
                new Wallet(null, user2, new BigDecimal("50.00")));

        TransferRequest request = new TransferRequest(
                wallet1.getId(),
                wallet2.getId(),
                new BigDecimal("100.00")
        );

        String url = "http://localhost:" + port + "/transfers";

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.postForEntity(url, request, String.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Wallet unchangedWallet1 = walletRepository.findById(wallet1.getId()).orElseThrow();
        Wallet unchangedWallet2 = walletRepository.findById(wallet2.getId()).orElseThrow();

        assertThat(unchangedWallet1.getBalance()).isEqualByComparingTo("10.00");
        assertThat(unchangedWallet2.getBalance()).isEqualByComparingTo("50.00");

    }
}