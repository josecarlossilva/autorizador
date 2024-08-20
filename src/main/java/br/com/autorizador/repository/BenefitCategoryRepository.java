package br.com.autorizador.repository;

import br.com.autorizador.model.BenefitCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BenefitCategoryRepository extends JpaRepository<BenefitCategory, Long> {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT bc FROM BenefitCategory bc WHERE bc.category = :category")
        Optional<BenefitCategory> findByCategoryWithLock(@Param("category") String category);
}