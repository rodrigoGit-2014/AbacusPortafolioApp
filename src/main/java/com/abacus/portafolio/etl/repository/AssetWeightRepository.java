package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.AssetWeight;
import com.abacus.portafolio.etl.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetWeightRepository extends JpaRepository<AssetWeight, Long> {
    List<AssetWeight> findByPortfolio(Portfolio portfolio);
}
