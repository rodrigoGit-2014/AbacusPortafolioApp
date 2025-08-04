package com.abacus.portafolio.evolution.service;

import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.evolution.dto.PortfolioEvolutionDTO;
import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static com.abacus.portafolio.evolution.util.EvolutionContextFactory.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioEvolutionService {
    private final List<IEvolutionRetriever> retrievers;
    private final List<IEvolutionCalculatorStep> calculationSteps;


    public List<PortfolioEvolutionDTO> calculateEvolution(Long portfolioId, LocalDate startDate, LocalDate endDate) {
        return Stream.iterate(startDate, date -> !date.isAfter(endDate), date -> date.plusDays(1))
                .map(currentDay -> {
                    EvolutionRetrieverContext context = loadContext(portfolioId, currentDay);
                    List<Price> prices = context.getPricesGroupedByDate().get(currentDay);
                    return calculateDailyEvolution(currentDay, prices, context.getAssetQuantities());
                })
                .toList();
    }

    private PortfolioEvolutionDTO calculateDailyEvolution(LocalDate date, List<Price> dailyPrices, List<AssetQuantity> assetQuantities) {
        EvolutionCalculatorContext context = buildEvolutionCalculatorContext(date, dailyPrices, assetQuantities);
        calculationSteps.forEach(step -> step.apply(context));
        return buildPortfolioEvolutionDTO(date, context);
    }

    private EvolutionRetrieverContext loadContext(long portfolioId, LocalDate operationDate) {
        return applyRetrievers(buildRetrieveContext(portfolioId, operationDate));
    }

    private EvolutionRetrieverContext applyRetrievers(EvolutionRetrieverContext context) {
        retrievers.forEach(r -> r.update(context));
        return context;
    }

}
