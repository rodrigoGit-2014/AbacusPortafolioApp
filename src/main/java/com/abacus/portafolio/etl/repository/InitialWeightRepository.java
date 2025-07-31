package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.entities.CurrentWeight;
import com.abacus.portafolio.etl.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InitialWeightRepository extends JpaRepository<CurrentWeight, Long> {
    List<CurrentWeight> findByPortfolio(Portfolio portfolio);
}
