package com.debika.payments.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.debika.payments.wallet.dto.TransferRequest;
import com.debika.payments.wallet.dto.TransferResponse;
import com.debika.payments.wallet.exception.GlobalExceptionHandler;
import com.debika.payments.wallet.exception.InvalidRequestException;
import com.debika.payments.wallet.exception.ResourceNotFoundException;
import com.debika.payments.wallet.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController transferController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transferController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should transfer funds successfully")
    void shouldTransferFundsSuccessfully() throws Exception {

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequest request =
                new TransferRequest(fromId, toId, new BigDecimal("50.00"));

        TransferResponse response =
                new TransferResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        fromId,
                        toId,
                        new BigDecimal("50.00"),
                        LocalDateTime.now()
                );

        when(transferService.transfer(fromId, toId, new BigDecimal("50.00")))
                .thenReturn(response);

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromWalletId").value(fromId.toString()))
                .andExpect(jsonPath("$.toWalletId").value(toId.toString()))
                .andExpect(jsonPath("$.amount").value(50.00));

        verify(transferService, times(1))
                .transfer(fromId, toId, new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should return 400 when transferring to same wallet")
    void shouldReturn400WhenSameWallet() throws Exception {

        UUID walletId = UUID.randomUUID();

        TransferRequest request =
                new TransferRequest(walletId, walletId, new BigDecimal("10.00"));

        when(transferService.transfer(any(), any(), any()))
                .thenThrow(new InvalidRequestException("Cannot transfer to the same wallet"));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(transferService, times(1))
                .transfer(walletId, walletId, new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Should return 404 when wallet not found")
    void shouldReturn404WhenWalletNotFound() throws Exception {

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        TransferRequest request =
                new TransferRequest(fromId, toId, new BigDecimal("10.00"));

        when(transferService.transfer(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Wallet not found"));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(transferService, times(1))
                .transfer(fromId, toId, new BigDecimal("10.00"));
    }
}