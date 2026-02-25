package com.rs.payments.wallet.service;

import com.rs.payments.wallet.dto.TransferResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface TransferService {
    TransferResponse transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount);
}
