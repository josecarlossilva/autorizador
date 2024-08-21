package br.com.autorizador.repository;

import br.com.autorizador.model.MerchantMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantMappingRepository extends JpaRepository<MerchantMapping, Long> {

    Optional<MerchantMapping> findByMerchant(String merchant);
}