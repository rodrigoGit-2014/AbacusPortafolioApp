package com.abacus.portafolio.evolution.service;

import com.abacus.portafolio.evolution.dto.PortfolioEvolutionDTO;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioEvolutionService {
    private final List<IEvolutionRetriever> retrievers;
    private final PortfolioEvolutionCalculator calculator;

    public List<PortfolioEvolutionDTO> calculateEvolution(Long portfolioId, LocalDate startDate, LocalDate endDate) {
        EvolutionRetrieverContext context = EvolutionRetrieverContext.builder().portfolioId(portfolioId).startDate(startDate).endDate(endDate).build();
        retrievers.forEach(r -> r.update(context));
        return calculator.calculate(context.getPricesGroupedByDate(), context.getAssetQuantities());
    }
}
