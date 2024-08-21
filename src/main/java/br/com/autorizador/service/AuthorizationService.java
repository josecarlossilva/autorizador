package br.com.autorizador.service;

import br.com.autorizador.model.BenefitCategory;
import br.com.autorizador.model.MerchantMapping;
import br.com.autorizador.model.Transaction;
import br.com.autorizador.repository.BenefitCategoryRepository;
import br.com.autorizador.repository.MerchantMappingRepository;
import br.com.autorizador.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Service
public class AuthorizationService {

    @Autowired
    private BenefitCategoryRepository benefitCategoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MerchantMappingRepository merchantMappingRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Transactional(timeout = 1) // Timeout de 100ms (1 décimo de segundo)
    public String authorizeTransaction(Transaction transaction) {
        try {
            return transactionTemplate.execute(status -> {
                // Verificar mapeamento do comerciante para corrigir o MCC
                Optional<MerchantMapping> merchantMappingOpt = merchantMappingRepository.findByMerchant(transaction.getMerchant());
                if (merchantMappingOpt.isPresent()) {
                    transaction.setMcc(merchantMappingOpt.get().getCorrectedMcc());
                }

                // Decidir a categoria baseada no MCC
                String category = determineCategoryByMcc(transaction.getMcc());

                return processTransaction(transaction, category);
            });
        } catch (TransactionTimedOutException t) {
            return "{\"code\": \"07\"}";  // Timeout specific error
        } catch (Exception e) {
            return "{\"code\": \"07\"}";  // Outros problemas
        }
    }

    private String processTransaction(Transaction transaction, String category) {
        // Bloquear a categoria correta e processar a transação
        Optional<BenefitCategory> categoryOpt = benefitCategoryRepository.findByCategoryWithLock(category);
        if (categoryOpt.isPresent()) {
            BenefitCategory benefitCategory = categoryOpt.get();
            if (benefitCategory.getBalance() >= transaction.getAmount()) {
                return approveTransaction(transaction, benefitCategory);
            } else {
                return "{\"code\": \"51\"}";
            }
        } else {
            return "{\"code\": \"07\"}";
        }
    }

    private String determineCategoryByMcc(String mcc) {
        switch (mcc) {
            case "5411":
            case "5412":
                return "FOOD";
            case "5811":
            case "5812":
                return "MEAL";
            default:
                return BenefitCategory.CASH_CATEGORY;
        }
    }

    private String approveTransaction(Transaction transaction, BenefitCategory category) {
        category.setBalance(category.getBalance() - transaction.getAmount());
        benefitCategoryRepository.save(category);
        transactionRepository.save(transaction);
        return "{\"code\": \"00\"}";  // Transação aprovada
    }
}