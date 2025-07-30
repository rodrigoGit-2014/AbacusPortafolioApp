package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.model.Asset;
import com.abacus.portafolio.etl.model.AssetQuantity;
import com.abacus.portafolio.etl.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetQuantityRepository extends JpaRepository<AssetQuantity, Long> {
    List<AssetQuantity> findByPortfolio(Portfolio portfolio);
    Optional<AssetQuantity> findByPortfolioAfterAndAsset(Portfolio portfolio, Asset asset);

}
