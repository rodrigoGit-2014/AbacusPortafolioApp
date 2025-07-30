package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.model.Asset;
import com.abacus.portafolio.etl.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findByAssetAndDate(Asset asset, LocalDate date);
    Price findFirstByOrderByDateAsc();

}
