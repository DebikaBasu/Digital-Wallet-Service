package com.rs.payments.wallet.exception;

import org.springframework.dao.DuplicateKeyException;

public class DuplicateResourceException extends DuplicateKeyException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
