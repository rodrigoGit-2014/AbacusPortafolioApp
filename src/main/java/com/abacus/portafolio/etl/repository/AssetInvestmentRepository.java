package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetInvestment;
import com.abacus.portafolio.etl.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetInvestmentRepository extends JpaRepository<AssetInvestment, Long> {
    List<AssetInvestment> findByPortfolio(Portfolio portfolio);
    Optional<AssetInvestment> findByPortfolioAndAsset(Portfolio portfolio, Asset asset);

}
