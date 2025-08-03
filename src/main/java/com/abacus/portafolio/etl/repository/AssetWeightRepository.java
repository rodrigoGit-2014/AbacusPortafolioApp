package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.AssetWeight;
import com.abacus.portafolio.etl.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AssetWeightRepository extends JpaRepository<AssetWeight, Long> {
    List<AssetWeight> findByPortfolio(Portfolio portfolio);
    List<AssetWeight> findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(Long portfolioId, LocalDate date1, LocalDate date2);

}
