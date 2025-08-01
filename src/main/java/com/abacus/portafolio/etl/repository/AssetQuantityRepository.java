package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetQuantityRepository extends JpaRepository<AssetQuantity, Long> {
    List<AssetQuantity> findByPortfolio(Portfolio portfolio);
    Optional<AssetQuantity> findByPortfolioAndAsset(Portfolio portfolio, Asset asset);

}
