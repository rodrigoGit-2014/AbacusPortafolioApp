package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByNameIgnoreCase(String name);
}
