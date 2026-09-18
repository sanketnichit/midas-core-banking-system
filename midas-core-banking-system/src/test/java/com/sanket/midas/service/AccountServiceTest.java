package com.sanket.midas.service;

import com.sanket.midas.entity.Account;
import com.sanket.midas.entity.BankTransaction;
import com.sanket.midas.exception.AccountNotFoundException;
import com.sanket.midas.exception.InsufficientBalanceException;
import com.sanket.midas.exception.SelfTransferException;
import com.sanket.midas.repository.AccountRepository;
import com.sanket.midas.repository.BankTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BankTransactionRepository bankTransactionRepository;

    @Mock
    private OutboxService outboxService;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService =
                new AccountService(
                        accountRepository,
                        bankTransactionRepository,
                        outboxService
                );
    }

    @Test
    void shouldCreateAccount() {

        Account account =
                new Account(
                        "Sanket",
                        "sanket2@example.com",
                        new BigDecimal("1000.00")
                );

        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        Account result =
                accountService.createAccount(
                        "Sanket",
                        "sanket2@example.com",
                        new BigDecimal("1000.00")
                );

        assertEquals(
                "Sanket",
                result.getName()
        );

        assertEquals(
                "sanket2@example.com",
                result.getEmail()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                result.getBalance()
        );

        verify(accountRepository)
                .save(any(Account.class));
    }

    @Test
    void shouldDepositMoney() {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        accountService.deposit(
                1L,
                new BigDecimal("500.00")
        );

        assertEquals(
                new BigDecimal("1500.00"),
                account.getBalance()
        );

        verify(accountRepository)
                .save(account);

        verify(bankTransactionRepository)
                .save(any(BankTransaction.class));

        verify(outboxService)
                .saveEvent(any());
    }

    @Test
    void shouldWithdrawMoney() {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        accountService.withdraw(
                1L,
                new BigDecimal("300.00")
        );

        assertEquals(
                new BigDecimal("700.00"),
                account.getBalance()
        );

        verify(accountRepository)
                .save(account);

        verify(bankTransactionRepository)
                .save(any(BankTransaction.class));

        verify(outboxService)
                .saveEvent(any());
    }

    @Test
    void shouldTransferMoneyBetweenAccounts() {

        Account sender =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        Account recipient =
                new Account(
                        "Rahul",
                        "rahul@example.com",
                        new BigDecimal("500.00")
                );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(recipient));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        accountService.transfer(
                1L,
                2L,
                new BigDecimal("400.00")
        );

        assertEquals(
                new BigDecimal("600.00"),
                sender.getBalance()
        );

        assertEquals(
                new BigDecimal("900.00"),
                recipient.getBalance()
        );

        verify(accountRepository, times(2))
                .save(any(Account.class));

        verify(bankTransactionRepository)
                .save(any(BankTransaction.class));

        verify(outboxService)
                .saveEvent(any());
    }

    @Test
    void shouldRejectWithdrawalWithInsufficientBalance() {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("200.00")
                );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.withdraw(
                        1L,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                new BigDecimal("200.00"),
                account.getBalance()
        );

        verify(accountRepository, never())
                .save(any(Account.class));

        verifyNoInteractions(
                bankTransactionRepository,
                outboxService
        );
    }

    @Test
    void shouldRejectTransferWithInsufficientBalance() {

        Account sender =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("200.00")
                );

        Account recipient =
                new Account(
                        "Rahul",
                        "rahul@example.com",
                        new BigDecimal("500.00")
                );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(recipient));

        assertThrows(
                InsufficientBalanceException.class,
                () -> accountService.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                new BigDecimal("200.00"),
                sender.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                recipient.getBalance()
        );

        verify(accountRepository, never())
                .save(any(Account.class));

        verifyNoInteractions(
                bankTransactionRepository,
                outboxService
        );
    }

    @Test
    void shouldRejectSelfTransfer() {

        assertThrows(
                SelfTransferException.class,
                () -> accountService.transfer(
                        1L,
                        1L,
                        new BigDecimal("100.00")
                )
        );

        verifyNoInteractions(
                accountRepository,
                bankTransactionRepository,
                outboxService
        );
    }

    @Test
    void shouldRejectDepositForMissingAccount() {

        when(accountRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.deposit(
                        99L,
                        new BigDecimal("100.00")
                )
        );

        verify(accountRepository)
                .findById(99L);

        verify(accountRepository, never())
                .save(any(Account.class));

        verifyNoInteractions(
                bankTransactionRepository,
                outboxService
        );
    }

    @Test
    void shouldRejectTransferWhenSenderDoesNotExist() {

        when(accountRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.transfer(
                        99L,
                        2L,
                        new BigDecimal("100.00")
                )
        );

        verify(accountRepository)
                .findById(99L);

        verify(accountRepository, never())
                .save(any(Account.class));

        verifyNoInteractions(
                bankTransactionRepository,
                outboxService
        );
    }

    @Test
    void shouldPropagateOptimisticLockingFailureDuringTransfer() {

        Account sender =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        Account recipient =
                new Account(
                        "Rahul",
                        "rahul@example.com",
                        new BigDecimal("500.00")
                );

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(recipient));

        when(accountRepository.save(sender))
                .thenThrow(
                        new ObjectOptimisticLockingFailureException(
                                Account.class,
                                1L
                        )
                );

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> accountService.transfer(
                        1L,
                        2L,
                        new BigDecimal("400.00")
                )
        );

        assertEquals(
                new BigDecimal("600.00"),
                sender.getBalance()
        );

        assertEquals(
                new BigDecimal("900.00"),
                recipient.getBalance()
        );

        verify(accountRepository)
                .save(sender);

        verify(accountRepository, never())
                .save(recipient);

        verifyNoInteractions(
                bankTransactionRepository,
                outboxService
        );
    }
}