package br.com.autorizador.service;

import br.com.autorizador.model.BenefitCategory;
import br.com.autorizador.model.MerchantMapping;
import br.com.autorizador.model.Transaction;
import br.com.autorizador.repository.BenefitCategoryRepository;
import br.com.autorizador.repository.MerchantMappingRepository;
import br.com.autorizador.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AuthorizationServiceTest {

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private BenefitCategoryRepository benefitCategoryRepository;

    @Mock
    private MerchantMappingRepository merchantMappingRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAuthorizeTransaction_Success() {
        Transaction transaction = new Transaction();
        transaction.setMcc("5411");
        transaction.setAmount(50.0);
        transaction.setMerchant("Supermarket");

        BenefitCategory foodCategory = new BenefitCategory();
        foodCategory.setCategory("FOOD");
        foodCategory.setBalance(100.0);

        when(benefitCategoryRepository.findByCategoryWithLock("FOOD")).thenReturn(Optional.of(foodCategory));

        String result = authorizationService.authorizeTransaction(transaction);

        assertEquals("{\"code\": \"00\"}", result);
    }

    @Test
    public void testAuthorizeTransaction_InsufficientBalance() {
        Transaction transaction = new Transaction();
        transaction.setMcc("5411");
        transaction.setAmount(200.0);
        transaction.setMerchant("Supermarket");

        BenefitCategory foodCategory = new BenefitCategory();
        foodCategory.setCategory("FOOD");
        foodCategory.setBalance(100.0);

        when(benefitCategoryRepository.findByCategoryWithLock("FOOD")).thenReturn(Optional.of(foodCategory));

        String result = authorizationService.authorizeTransaction(transaction);

        assertEquals("{\"code\": \"51\"}", result);
    }

    @Test
    public void testAuthorizeTransaction_UnexpectedError() {
        Transaction transaction = new Transaction();
        transaction.setMcc("5411");
        transaction.setAmount(50.0);
        transaction.setMerchant("Supermarket");

        when(benefitCategoryRepository.findByCategoryWithLock(any())).thenThrow(new RuntimeException());

        String result = authorizationService.authorizeTransaction(transaction);

        assertEquals("{\"code\": \"07\"}", result);
    }

    @Test
    public void testAuthorizeTransaction_CategoryNotFound() {
        Transaction transaction = new Transaction();
        transaction.setMcc("5411");
        transaction.setAmount(50.0);
        transaction.setMerchant("Supermarket");

        when(benefitCategoryRepository.findByCategoryWithLock("FOOD")).thenReturn(Optional.empty());

        String result = authorizationService.authorizeTransaction(transaction);

        assertEquals("{\"code\": \"07\"}", result);
    }

    @Test
    public void testAuthorizeTransaction_CorrectedMcc() {
        Transaction transaction = new Transaction();
        transaction.setMcc("9999");
        transaction.setAmount(50.0);
        transaction.setMerchant("UBER EATS                   SAO PAULO BR");

        MerchantMapping merchantMapping = new MerchantMapping();
        merchantMapping.setMerchantName("UBER EATS                   SAO PAULO BR");
        merchantMapping.setCorrectedMcc("5812");

        BenefitCategory mealCategory = new BenefitCategory();
        mealCategory.setCategory("MEAL");
        mealCategory.setBalance(100.0);

        when(merchantMappingRepository.findByMerchantName("UBER EATS                   SAO PAULO BR")).thenReturn(Optional.of(merchantMapping));
        when(benefitCategoryRepository.findByCategoryWithLock("MEAL")).thenReturn(Optional.of(mealCategory));

        String result = authorizationService.authorizeTransaction(transaction);

        assertEquals("{\"code\": \"00\"}", result);
    }
}