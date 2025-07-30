package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.Price;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {

    Optional<Price> findByAssetAndDate(Asset asset, LocalDate date);
    Price findFirstByOrderByDateAsc();
    List<Price> findByAssetInAndDateBetween(Collection<Asset> assets, LocalDate dateAfter, LocalDate dateBefore);

}
