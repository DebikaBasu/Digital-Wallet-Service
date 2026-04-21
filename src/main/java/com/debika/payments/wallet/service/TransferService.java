package com.debika.payments.wallet.service;

import com.debika.payments.wallet.dto.TransferResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface TransferService {
    TransferResponse transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount);
}
