package com.sanket.midas.service;

import com.sanket.midas.entity.Account;
import com.sanket.midas.entity.BankTransaction;
import com.sanket.midas.exception.AccountNotFoundException;
import com.sanket.midas.exception.InsufficientBalanceException;
import com.sanket.midas.exception.SelfTransferException;
import com.sanket.midas.kafka.BankingEvent;
import com.sanket.midas.repository.AccountRepository;
import com.sanket.midas.repository.BankTransactionRepository;
import com.sanket.midas.transaction.TransactionStatus;
import com.sanket.midas.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final OutboxService outboxService;

    public AccountService(
            AccountRepository accountRepository,
            BankTransactionRepository bankTransactionRepository,
            OutboxService outboxService) {

        this.accountRepository = accountRepository;
        this.bankTransactionRepository = bankTransactionRepository;
        this.outboxService = outboxService;
    }

    public Account createAccount(
            String name,
            String email,
            BigDecimal balance) {

        Account account =
                new Account(
                        name,
                        email,
                        balance
                );

        return accountRepository.save(account);
    }

    public Account getAccount(Long id) {

        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account with ID "
                                        + id
                                        + " not found"
                        )
                );
    }

    public List<Account> getAllAccounts() {

        return accountRepository.findAll();
    }

    @Transactional
    public Account deposit(
            Long id,
            BigDecimal amount) {

        Account account =
                accountRepository.findById(id)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account with ID "
                                                + id
                                                + " not found"
                                )
                        );

        account.setBalance(
                account.getBalance().add(amount)
        );

        Account updatedAccount =
                accountRepository.save(account);

        LocalDateTime transactionTime =
                LocalDateTime.now();

        BankTransaction transaction =
                new BankTransaction(
                        TransactionType.DEPOSIT,
                        amount,
                        TransactionStatus.COMPLETED,
                        account.getId(),
                        null,
                        transactionTime,
                        "Cash deposit"
                );

        bankTransactionRepository.save(transaction);

        BankingEvent event =
                new BankingEvent(
                        transaction.getId(),
                        "DEPOSIT",
                        account.getId(),
                        null,
                        amount,
                        "COMPLETED",
                        transactionTime,
                        "Cash deposit"
                );

        outboxService.saveEvent(event);

        return updatedAccount;
    }

    @Transactional
    public Account withdraw(
            Long id,
            BigDecimal amount) {

        Account account =
                accountRepository.findById(id)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account with ID "
                                                + id
                                                + " not found"
                                )
                        );

        if (account.getBalance().compareTo(amount) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance. Current balance: "
                            + account.getBalance()
                            + ", requested withdrawal: "
                            + amount
            );
        }

        account.setBalance(
                account.getBalance().subtract(amount)
        );

        Account updatedAccount =
                accountRepository.save(account);

        LocalDateTime transactionTime =
                LocalDateTime.now();

        BankTransaction transaction =
                new BankTransaction(
                        TransactionType.WITHDRAWAL,
                        amount,
                        TransactionStatus.COMPLETED,
                        account.getId(),
                        null,
                        transactionTime,
                        "Cash withdrawal"
                );

        bankTransactionRepository.save(transaction);

        BankingEvent event =
                new BankingEvent(
                        transaction.getId(),
                        "WITHDRAWAL",
                        account.getId(),
                        null,
                        amount,
                        "COMPLETED",
                        transactionTime,
                        "Cash withdrawal"
                );

        outboxService.saveEvent(event);

        return updatedAccount;
    }

    @Transactional
    public BankTransaction transfer(
            Long senderAccountId,
            Long recipientAccountId,
            BigDecimal amount) {

        if (senderAccountId.equals(recipientAccountId)) {

            throw new SelfTransferException(
                    "Sender and recipient accounts must be different"
            );
        }

        Account sender =
                accountRepository.findById(senderAccountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Sender account with ID "
                                                + senderAccountId
                                                + " not found"
                                )
                        );

        Account recipient =
                accountRepository.findById(recipientAccountId)
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Recipient account with ID "
                                                + recipientAccountId
                                                + " not found"
                                )
                        );

        if (sender.getBalance().compareTo(amount) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance. Sender balance: "
                            + sender.getBalance()
                            + ", requested transfer: "
                            + amount
            );
        }

        sender.setBalance(
                sender.getBalance().subtract(amount)
        );

        recipient.setBalance(
                recipient.getBalance().add(amount)
        );

        accountRepository.save(sender);
        accountRepository.save(recipient);

        LocalDateTime transactionTime =
                LocalDateTime.now();

        BankTransaction transaction =
                new BankTransaction(
                        TransactionType.TRANSFER,
                        amount,
                        TransactionStatus.COMPLETED,
                        sender.getId(),
                        recipient.getId(),
                        transactionTime,
                        "Transfer between accounts"
                );

        bankTransactionRepository.save(transaction);

        BankingEvent event =
                new BankingEvent(
                        transaction.getId(),
                        "TRANSFER",
                        sender.getId(),
                        recipient.getId(),
                        amount,
                        "COMPLETED",
                        transactionTime,
                        "Transfer between accounts"
                );

        outboxService.saveEvent(event);

        return transaction;
    }

    public List<BankTransaction> getTransactionHistory(
            Long accountId) {

        getAccount(accountId);

        return bankTransactionRepository
                .findByAccountIdOrRecipientAccountIdOrderByCreatedAtDesc(
                        accountId,
                        accountId
                );
    }
}