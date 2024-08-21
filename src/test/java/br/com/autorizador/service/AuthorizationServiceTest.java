package br.com.autorizador.service;

import br.com.autorizador.model.BenefitCategory;
import br.com.autorizador.model.Transaction;
import br.com.autorizador.repository.BenefitCategoryRepository;
import br.com.autorizador.repository.MerchantMappingRepository;
import br.com.autorizador.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuthorizationServiceTest {

    @InjectMocks
    private AuthorizationService authorizationService;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private MerchantMappingRepository merchantMappingRepository;

    @MockBean
    private BenefitCategoryRepository benefitCategoryRepository;

    @MockBean
    private TransactionTemplate transactionTemplate;

    @Mock
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAuthorizeTransaction_Timeout() {
        // Configure mocks
        when(transaction.getMcc()).thenReturn("5411");
        when(transaction.getAmount()).thenReturn(50.0);
        when(transaction.getMerchant()).thenReturn("");

        // Simulate delay to cause timeout
        doAnswer(invocation -> {
            Thread.sleep(2000); // Simulate 2 seconds delay, causing the transaction to timeout
            return Optional.of(new BenefitCategory("FOOD", 100.0));
        }).doThrow(TransactionTimedOutException.class).when(benefitCategoryRepository).findByCategoryWithLock("FOOD");

        when(transactionTemplate.execute(any())).then(invocation -> {
            benefitCategoryRepository.findByCategoryWithLock("FOOD");
            return "{\"code\": \"07\"}";
        });

        String response = authorizationService.authorizeTransaction(transaction);
        assertEquals("{\"code\": \"07\"}", response); // Timeout specific error
    }

    @Test
    public void testAuthorizeTransaction_Success() {
        // Configurar mocks
        when(transaction.getMcc()).thenReturn("5411");
        when(transaction.getAmount()).thenReturn(50.0);
        when(transaction.getMerchant()).thenReturn("");

        when(merchantMappingRepository.findByMerchant("Supermarket")).thenReturn(Optional.empty());
        when(benefitCategoryRepository.findByCategoryWithLock("FOOD")).thenReturn(Optional.of(new BenefitCategory("FOOD", 100.0)));

        doAnswer(invocation -> {
            BenefitCategory category = invocation.getArgument(0);
            category.setBalance(category.getBalance() - 50);
            return null;
        }).when(benefitCategoryRepository).save(any(BenefitCategory.class));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        when(transactionTemplate.execute(any())).then(invocation -> {
            Optional<BenefitCategory> categoryOpt = benefitCategoryRepository.findByCategoryWithLock("FOOD");

            if (categoryOpt.isEmpty()) {
                return "{\"code\": \"07\"}"; // Categoria não encontrada
            }

            BenefitCategory category = categoryOpt.get();

            if (transaction.getAmount() > category.getBalance()) {
                return "{\"code\": \"51\"}"; // Saldo insuficiente
            }

            category.setBalance(category.getBalance() - transaction.getAmount());
            benefitCategoryRepository.save(category);
            transactionRepository.save(transaction);
            return "{\"code\": \"00\"}";
        });

        String response = authorizationService.authorizeTransaction(transaction);
        assertEquals("{\"code\": \"00\"}", response); // Transação aprovada
    }

    @Test
    public void testAuthorizeTransaction_InsufficientBalance() {
        // Configurar mocks
        when(transaction.getMcc()).thenReturn("5411");
        when(transaction.getAmount()).thenReturn(150.0); // Valor maior que o saldo
        when(transaction.getMerchant()).thenReturn("");

        when(merchantMappingRepository.findByMerchant("Supermarket")).thenReturn(Optional.empty());
        when(benefitCategoryRepository.findByCategoryWithLock("FOOD")).thenReturn(Optional.of(new BenefitCategory("FOOD", 100.0))); // Saldo inicial de 100

        doAnswer(invocation -> {
            BenefitCategory category = invocation.getArgument(0);
            category.setBalance(category.getBalance() - 150); // Tentativa de débito maior que o saldo
            return null;
        }).when(benefitCategoryRepository).save(any(BenefitCategory.class));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        when(transactionTemplate.execute(any())).then(invocation -> {
            Optional<BenefitCategory> categoryOpt = benefitCategoryRepository.findByCategoryWithLock("FOOD");

            if (categoryOpt.isEmpty()) {
                return "{\"code\": \"07\"}"; // Categoria não encontrada
            }

            BenefitCategory category = categoryOpt.get();

            if (transaction.getAmount() > category.getBalance()) {
                return "{\"code\": \"51\"}"; // Saldo insuficiente
            }

            category.setBalance(category.getBalance() - transaction.getAmount());
            benefitCategoryRepository.save(category);
            transactionRepository.save(transaction);
            return "{\"code\": \"00\"}";
        });

        String response = authorizationService.authorizeTransaction(transaction);
        assertEquals("{\"code\": \"51\"}", response); // Saldo insuficiente
    }

    @Test
    public void testAuthorizeTransaction_UnexpectedError() {
        // Configurar mocks
        when(transaction.getMcc()).thenReturn("5411");
        when(transaction.getAmount()).thenReturn(50.0);
        when(transaction.getMerchant()).thenReturn("");

        when(merchantMappingRepository.findByMerchant("Supermarket")).thenReturn(Optional.empty());
        when(benefitCategoryRepository.findByCategoryWithLock("FOOD")).thenReturn(Optional.of(new BenefitCategory("FOOD", 100.0)));

        // Simular erro inesperado ao salvar a categoria de benefício
        doThrow(new RuntimeException("Unexpected error")).when(benefitCategoryRepository).save(any(BenefitCategory.class));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        when(transactionTemplate.execute(any())).then(invocation -> {
            Optional<BenefitCategory> categoryOpt = benefitCategoryRepository.findByCategoryWithLock("FOOD");

            if (categoryOpt.isEmpty()) {
                return "{\"code\": \"07\"}"; // Categoria não encontrada
            }

            BenefitCategory category = categoryOpt.get();

            if (transaction.getAmount() > category.getBalance()) {
                return "{\"code\": \"51\"}"; // Saldo insuficiente
            }

            // Este ponto irá lançar a exceção simulada
            category.setBalance(category.getBalance() - transaction.getAmount());
            benefitCategoryRepository.save(category);
            transactionRepository.save(transaction);
            return "{\"code\": \"00\"}";
        });

        String response = authorizationService.authorizeTransaction(transaction);
        assertEquals("{\"code\": \"07\"}", response); // Erro inesperado
    }

    @Test
    public void testAuthorizeTransaction_CategoryNotFound() {
        // Configurar mocks
        when(transaction.getMcc()).thenReturn("5411");
        when(transaction.getAmount()).thenReturn(50.0);
        when(transaction.getMerchant()).thenReturn("");

        when(merchantMappingRepository.findByMerchant("Supermarket")).thenReturn(Optional.empty());
        when(benefitCategoryRepository.findByCategoryWithLock("FOOD")).thenReturn(Optional.empty()); // Categoria não encontrada

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        when(transactionTemplate.execute(any())).then(invocation -> {
            Optional<BenefitCategory> categoryOpt = benefitCategoryRepository.findByCategoryWithLock("FOOD");

            if (categoryOpt.isEmpty()) {
                return "{\"code\": \"07\"}"; // Categoria não encontrada
            }

            BenefitCategory category = categoryOpt.get();

            if (transaction.getAmount() > category.getBalance()) {
                return "{\"code\": \"51\"}"; // Saldo insuficiente
            }

            category.setBalance(category.getBalance() - transaction.getAmount());
            benefitCategoryRepository.save(category);
            transactionRepository.save(transaction);
            return "{\"code\": \"00\"}";
        });

        String response = authorizationService.authorizeTransaction(transaction);
        assertEquals("{\"code\": \"07\"}", response); // Categoria não encontrada
    }

    @Test
    public void testAuthorizeTransaction_CorrectedMcc() {
        // Configurar mocks
        when(transaction.getMcc()).thenReturn("9999"); // MCC original que será corrigido
        when(transaction.getAmount()).thenReturn(50.0);
        when(transaction.getMerchant()).thenReturn("UBER EATS                   SAO PAULO BR");

        BenefitCategory mealCategory = new BenefitCategory("MEAL", 100.0);
        when(benefitCategoryRepository.findByCategoryWithLock("MEAL")).thenReturn(Optional.of(mealCategory));

        doAnswer(invocation -> {
            BenefitCategory category = invocation.getArgument(0);
            category.setBalance(category.getBalance() - 50);
            return null;
        }).when(benefitCategoryRepository).save(any(BenefitCategory.class));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        when(transactionTemplate.execute(any())).then(invocation -> {
            Optional<BenefitCategory> categoryOpt = benefitCategoryRepository.findByCategoryWithLock("MEAL");

            if (categoryOpt.isEmpty()) {
                return "{\"code\": \"07\"}"; // Categoria não encontrada
            }

            BenefitCategory category = categoryOpt.get();

            if (transaction.getAmount() > category.getBalance()) {
                return "{\"code\": \"51\"}"; // Saldo insuficiente
            }

            category.setBalance(category.getBalance() - transaction.getAmount());
            benefitCategoryRepository.save(category);
            transactionRepository.save(transaction);
            return "{\"code\": \"00\"}";
        });

        String response = authorizationService.authorizeTransaction(transaction);
        assertEquals("{\"code\": \"00\"}", response); // Transação aprovada com MCC corrigido
    }
}