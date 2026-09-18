package com.sanket.midas.controller;

import com.sanket.midas.entity.Account;
import com.sanket.midas.entity.BankTransaction;
import com.sanket.midas.exception.AccountNotFoundException;
import com.sanket.midas.exception.GlobalExceptionHandler;
import com.sanket.midas.exception.InsufficientBalanceException;
import com.sanket.midas.exception.SelfTransferException;
import com.sanket.midas.service.AccountService;
import com.sanket.midas.transaction.TransactionStatus;
import com.sanket.midas.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest {

    private MockMvc mockMvc;

    private AccountService accountService;

    @BeforeEach
    void setUp() {

        accountService = mock(AccountService.class);

        AccountController accountController =
                new AccountController(accountService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(accountController)
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .setValidator(validator)
                        .build();
    }

    @Test
    void shouldCreateAccount() throws Exception {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        when(accountService.createAccount(
                "Sanket",
                "sanket@example.com",
                new BigDecimal("1000.00")
        )).thenReturn(account);

        mockMvc.perform(
                post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{"
                                        + "\"name\":\"Sanket\","
                                        + "\"email\":\"sanket@example.com\","
                                        + "\"balance\":1000.00"
                                        + "}"
                        )
        )
        .andExpect(status().isCreated())
        .andExpect(
                jsonPath("$.name")
                        .value("Sanket")
        )
        .andExpect(
                jsonPath("$.email")
                        .value("sanket@example.com")
        )
        .andExpect(
                jsonPath("$.balance")
                        .value(1000.00)
        );
    }

    @Test
    void shouldGetAccount() throws Exception {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        when(accountService.getAccount(1L))
                .thenReturn(account);

        mockMvc.perform(
                get("/api/accounts/1")
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.name")
                        .value("Sanket")
        )
        .andExpect(
                jsonPath("$.email")
                        .value("sanket@example.com")
        )
        .andExpect(
                jsonPath("$.balance")
                        .value(1000.00)
        );
    }

    @Test
    void shouldReturnAllAccounts() throws Exception {

        Account account1 =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1000.00")
                );

        Account account2 =
                new Account(
                        "Rahul",
                        "rahul@example.com",
                        new BigDecimal("500.00")
                );

        when(accountService.getAllAccounts())
                .thenReturn(List.of(account1, account2));

        mockMvc.perform(
                get("/api/accounts")
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.length()")
                        .value(2)
        )
        .andExpect(
                jsonPath("$[0].name")
                        .value("Sanket")
        )
        .andExpect(
                jsonPath("$[1].name")
                        .value("Rahul")
        );
    }

    @Test
    void shouldDepositMoney() throws Exception {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("1500.00")
                );

        when(accountService.deposit(
                1L,
                new BigDecimal("500.00")
        )).thenReturn(account);

        mockMvc.perform(
                post("/api/accounts/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{"
                                        + "\"amount\":500.00"
                                        + "}"
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.balance")
                        .value(1500.00)
        );
    }

    @Test
    void shouldWithdrawMoney() throws Exception {

        Account account =
                new Account(
                        "Sanket",
                        "sanket@example.com",
                        new BigDecimal("700.00")
                );

        when(accountService.withdraw(
                1L,
                new BigDecimal("300.00")
        )).thenReturn(account);

        mockMvc.perform(
                post("/api/accounts/1/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{"
                                        + "\"amount\":300.00"
                                        + "}"
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.balance")
                        .value(700.00)
        );
    }

    @Test
    void shouldTransferMoney() throws Exception {

        BankTransaction transaction =
                new BankTransaction(
                        TransactionType.TRANSFER,
                        new BigDecimal("400.00"),
                        TransactionStatus.COMPLETED,
                        1L,
                        2L,
                        LocalDateTime.now(),
                        "Transfer between accounts"
                );

        when(accountService.transfer(
                1L,
                2L,
                new BigDecimal("400.00")
        )).thenReturn(transaction);

        mockMvc.perform(
                post("/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{"
                                        + "\"senderAccountId\":1,"
                                        + "\"recipientAccountId\":2,"
                                        + "\"amount\":400.00"
                                        + "}"
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.type")
                        .value("TRANSFER")
        )
        .andExpect(
                jsonPath("$.amount")
                        .value(400.00)
        )
        .andExpect(
                jsonPath("$.status")
                        .value("COMPLETED")
        )
        .andExpect(
                jsonPath("$.accountId")
                        .value(1)
        )
        .andExpect(
                jsonPath("$.recipientAccountId")
                        .value(2)
        );
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist()
            throws Exception {

        when(accountService.getAccount(99L))
                .thenThrow(
                        new AccountNotFoundException(
                                "Account with ID 99 not found"
                        )
                );

        mockMvc.perform(
                get("/api/accounts/99")
        )
        .andExpect(status().isNotFound())
        .andExpect(
                jsonPath("$.message")
                        .value("Account with ID 99 not found")
        );
    }

    @Test
    void shouldReturnBadRequestForInsufficientBalance()
            throws Exception {

        when(accountService.withdraw(
                1L,
                new BigDecimal("5000.00")
        )).thenThrow(
                new InsufficientBalanceException(
                        "Insufficient balance"
                )
        );

        mockMvc.perform(
                post("/api/accounts/1/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{"
                                        + "\"amount\":5000.00"
                                        + "}"
                        )
        )
        .andExpect(status().isBadRequest())
        .andExpect(
                jsonPath("$.message")
                        .value("Insufficient balance")
        );
    }

    @Test
    void shouldReturnBadRequestForSelfTransfer()
            throws Exception {

        when(accountService.transfer(
                1L,
                1L,
                new BigDecimal("100.00")
        )).thenThrow(
                new SelfTransferException(
                        "Sender and recipient accounts must be different"
                )
        );

        mockMvc.perform(
                post("/api/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{"
                                        + "\"senderAccountId\":1,"
                                        + "\"recipientAccountId\":1,"
                                        + "\"amount\":100.00"
                                        + "}"
                        )
        )
        .andExpect(status().isBadRequest())
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Sender and recipient accounts must be different"
                        )
        );
    }

    @Test
    void shouldRejectInvalidCreateAccountRequest()
            throws Exception {

        mockMvc.perform(
                post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{"
                                        + "\"name\":\"\","
                                        + "\"email\":\"not-an-email\","
                                        + "\"balance\":-100.00"
                                        + "}"
                        )
        )
        .andExpect(status().isBadRequest())
        .andExpect(
                jsonPath("$.message")
                        .value("Validation failed")
        );
    }
}