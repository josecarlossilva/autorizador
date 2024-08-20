package br.com.autorizador.service;

import br.com.autorizador.model.BenefitCategory;
import br.com.autorizador.model.MerchantMapping;
import br.com.autorizador.model.Transaction;
import br.com.autorizador.repository.BenefitCategoryRepository;
import br.com.autorizador.repository.MerchantMappingRepository;
import br.com.autorizador.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthorizationService {

    @Autowired
    private BenefitCategoryRepository benefitCategoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MerchantMappingRepository merchantMappingRepository;

    @Transactional(timeout = 1) // Timeout de 100ms (1 décimo de segundo)
    public String authorizeTransaction(Transaction transaction) {
        try {
            // Verificar mapeamento do comerciante para corrigir o MCC
            Optional<MerchantMapping> merchantMappingOpt = merchantMappingRepository.findByMerchantName(transaction.getMerchant());
            if (merchantMappingOpt.isPresent()) {
                transaction.setMcc(merchantMappingOpt.get().getCorrectedMcc());
            }

            // Decidir a categoria baseada no MCC
            String category = determineCategoryByMcc(transaction.getMcc());

            // Bloquear a categoria correta e processar a transação
            Optional<BenefitCategory> categoryOpt = benefitCategoryRepository.findByCategoryWithLock(category);
            if (categoryOpt.isPresent()) {
                BenefitCategory benefitCategory = categoryOpt.get();
                if (benefitCategory.getBalance() >= transaction.getAmount()) {
                    return approveTransaction(transaction, benefitCategory);
                } else {
                    return "{\"code\": \"51\"}";  // Saldo insuficiente
                }
            } else {
                return "{\"code\": \"07\"}";  // Categoria não encontrada
            }
        } catch (Exception e) {
            return "{\"code\": \"07\"}";  // Outros problemas
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