package com.abacus.portafolio.etl.repository;

import com.abacus.portafolio.etl.model.InitialWeight;
import com.abacus.portafolio.etl.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InitialWeightRepository extends JpaRepository<InitialWeight, Long> {
    List<InitialWeight> findByPortfolio(Portfolio portfolio);
}
