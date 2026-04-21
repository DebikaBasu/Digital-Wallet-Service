package com.debika.payments.wallet.repository;

import java.util.UUID;
import com.debika.payments.wallet.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}