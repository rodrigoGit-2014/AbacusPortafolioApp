package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByNameIgnoreCase(String name);
}
