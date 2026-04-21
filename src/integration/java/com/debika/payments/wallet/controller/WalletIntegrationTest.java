package com.debika.payments.wallet.controller;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.debika.payments.wallet.BaseIntegrationTest;
import com.debika.payments.wallet.dto.CreateWalletRequest;
import com.debika.payments.wallet.dto.DepositRequest;
import com.debika.payments.wallet.dto.TransferRequest;
import com.debika.payments.wallet.dto.WithdrawRequest;
import com.debika.payments.wallet.model.User;
import com.debika.payments.wallet.model.Wallet;
import com.debika.payments.wallet.repository.TransactionRepository;
import com.debika.payments.wallet.repository.UserRepository;
import com.debika.payments.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletIntegrationTest extends BaseIntegrationTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Autowired
    private WalletRepository walletRepository;



    @Test
    void shouldCreateWalletForExistingUser() {
        User user = createUser("walletuser", "wallet@example.com");

        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(user.getId());

        ResponseEntity<Wallet> response = restTemplate.postForEntity(walletsUrl(), request, Wallet.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturnNotFoundForNonExistentUser() {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(UUID.randomUUID());

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.postForEntity(walletsUrl(), request, String.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturn400WhenUserAlreadyHasWallet() {
        User user = createUserWithWallet("dupewalletuser", "dupewallet@example.com", new BigDecimal("0.00"));

        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(user.getId());

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.postForEntity(walletsUrl(), request, String.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldDepositFunds() {
        User user = createUserWithWallet("deposituser", "deposit@example.com", new BigDecimal("100.00"));
        UUID walletId = user.getWallet().getId();

        ResponseEntity<Wallet> response = restTemplate.postForEntity(
                walletUrl(walletId) + "/deposit", new DepositRequest(new BigDecimal("50.00")), Wallet.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldReturn400WhenDepositAmountInvalid() {
        User user = createUserWithWallet("depinvalid", "depinvalid@example.com", new BigDecimal("100.00"));
        UUID walletId = user.getWallet().getId();

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.postForEntity(
                        walletUrl(walletId) + "/deposit", new DepositRequest(new BigDecimal("-10.00")), String.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldWithdrawFunds() {
        User user = createUserWithWallet("withdrawuser", "withdraw@example.com", new BigDecimal("100.00"));
        UUID walletId = user.getWallet().getId();

        ResponseEntity<Wallet> response = restTemplate.postForEntity(
                walletUrl(walletId) + "/withdraw", new WithdrawRequest(new BigDecimal("30.00")), Wallet.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void shouldReturn400WhenInsufficientFundsOnWithdraw() {
        User user = createUserWithWallet("withdrawfail", "withdrawfail@example.com", new BigDecimal("10.00"));
        UUID walletId = user.getWallet().getId();

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.postForEntity(
                        walletUrl(walletId) + "/withdraw", new WithdrawRequest(new BigDecimal("50.00")), String.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Wallet wallet = walletRepository.findById(walletId).orElseThrow();
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void shouldGetBalance() {
        User user = createUserWithWallet("balanceuser", "balance@example.com", new BigDecimal("250.00"));
        UUID walletId = user.getWallet().getId();

        ResponseEntity<String> response = restTemplate.getForEntity(
                walletUrl(walletId) + "/balance", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("250");
    }

    @Test
    void shouldReturn404WhenBalanceWalletNotFound() {
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(
                        walletUrl(UUID.randomUUID()) + "/balance", String.class));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldTransferFundsBetweenWallets() throws Exception {
        User userA = createUserWithWallet("transferA", "transferA@example.com", new BigDecimal("200.00"));
        User userB = createUserWithWallet("transferB", "transferB@example.com", new BigDecimal("100.00"));

        TransferRequest request = new TransferRequest(
                userA.getWallet().getId(), userB.getWallet().getId(), new BigDecimal("50.00"));

        ResponseEntity<String> response = postJson(transfersUrl(), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Wallet walletA = walletRepository.findById(userA.getWallet().getId()).orElseThrow();
        Wallet walletB = walletRepository.findById(userB.getWallet().getId()).orElseThrow();
        assertThat(walletA.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(walletB.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldReturn400WhenInsufficientFundsOnTransfer() throws Exception {
        User userA = createUserWithWallet("poorA", "poorA@example.com", new BigDecimal("10.00"));
        User userB = createUserWithWallet("richB", "richB@example.com", new BigDecimal("500.00"));

        TransferRequest request = new TransferRequest(
                userA.getWallet().getId(), userB.getWallet().getId(), new BigDecimal("100.00"));

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> postJson(transfersUrl(), request));

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Wallet walletA = walletRepository.findById(userA.getWallet().getId()).orElseThrow();
        Wallet walletB = walletRepository.findById(userB.getWallet().getId()).orElseThrow();
        assertThat(walletA.getBalance()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(walletB.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldHandleConcurrentTransfersWithoutLosingFunds() throws Exception {
        User userA = createUserWithWallet("concA", "concA@example.com", new BigDecimal("1000.00"));
        User userB = createUserWithWallet("concB", "concB@example.com", new BigDecimal("1000.00"));
        UUID walletAId = userA.getWallet().getId();
        UUID walletBId = userB.getWallet().getId();

        int threadCount = 10;
        BigDecimal transferAmount = new BigDecimal("10.00");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    postJson(transfersUrl(), new TransferRequest(walletAId, walletBId, transferAmount));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        Wallet walletA = walletRepository.findById(walletAId).orElseThrow();
        Wallet walletB = walletRepository.findById(walletBId).orElseThrow();

        assertThat(walletA.getBalance().add(walletB.getBalance())).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(walletA.getBalance()).isEqualByComparingTo(new BigDecimal("900.00"));
        assertThat(walletB.getBalance()).isEqualByComparingTo(new BigDecimal("1100.00"));
    }

    @Test
    void shouldHandleConcurrentOpposingTransfersAtomically() throws Exception {
        User userA = createUserWithWallet("oppA", "oppA@example.com", new BigDecimal("1000.00"));
        User userB = createUserWithWallet("oppB", "oppB@example.com", new BigDecimal("1000.00"));
        UUID walletAId = userA.getWallet().getId();
        UUID walletBId = userB.getWallet().getId();

        int threadCount = 20;
        BigDecimal transferAmount = new BigDecimal("5.00");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final UUID fromId = (i % 2 == 0) ? walletAId : walletBId;
            final UUID toId = (i % 2 == 0) ? walletBId : walletAId;

            executor.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    postJson(transfersUrl(), new TransferRequest(fromId, toId, transferAmount));
                } catch (Exception e) {
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        Wallet walletA = walletRepository.findById(walletAId).orElseThrow();
        Wallet walletB = walletRepository.findById(walletBId).orElseThrow();

        assertThat(walletA.getBalance().add(walletB.getBalance())).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(walletA.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(walletB.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private User createUserWithWallet(String username, String email, BigDecimal balance) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        Wallet wallet = new Wallet();
        wallet.setBalance(balance);
        wallet.setUser(user);
        user.setWallet(wallet);
        return userRepository.save(user);
    }

    private ResponseEntity<String> postJson(String url, Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
        return restTemplate.postForEntity(url, entity, String.class);
    }

    private String walletsUrl() {
        return "http://localhost:" + port + "/wallets";
    }

    private String walletUrl(UUID id) {
        return "http://localhost:" + port + "/wallets/" + id;
    }

    private String transfersUrl() {
        return "http://localhost:" + port + "/transfers";
    }
}