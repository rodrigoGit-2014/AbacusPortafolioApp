package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AssetQuantityRepository extends JpaRepository<AssetQuantity, Long> {
    List<AssetQuantity> findByPortfolio(Portfolio portfolio);
    List<AssetQuantity> findByPortfolioAndAsset(Portfolio portfolio, Asset asset);
    List<AssetQuantity> findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(Long portfolioId, LocalDate date1, LocalDate date2);

}
