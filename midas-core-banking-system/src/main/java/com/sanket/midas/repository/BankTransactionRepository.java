package com.sanket.midas.repository;

import com.sanket.midas.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findByAccountIdOrRecipientAccountIdOrderByCreatedAtDesc(
            Long accountId,
            Long recipientAccountId
    );
}