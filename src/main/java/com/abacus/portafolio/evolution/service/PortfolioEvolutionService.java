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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioEvolutionService {
    private final List<IEvolutionRetriever> retrievers;
    private final List<IEvolutionCalculatorStep> calculationSteps;

    public List<PortfolioEvolutionDTO> calculateEvolution(Long portfolioId, LocalDate startDate, LocalDate endDate) {
        EvolutionRetrieverContext context = buildEvolutionRetrieverContext(portfolioId, startDate, endDate);
        retrievers.forEach(r -> r.update(context));
        return calculate(context.getPricesGroupedByDate(), context.getAssetQuantities());
    }

    public List<PortfolioEvolutionDTO> calculate(Map<LocalDate, List<Price>> pricesGroupedByDate, List<AssetQuantity> assetQuantities) {
        return pricesGroupedByDate.keySet().stream()
                .sorted()
                .map(date -> calculateDailyEvolution(date, pricesGroupedByDate.get(date), assetQuantities))
                .toList();
    }

    private PortfolioEvolutionDTO calculateDailyEvolution(LocalDate date, List<Price> dailyPrices, List<AssetQuantity> assetQuantities) {
        EvolutionCalculatorContext context = buildEvolutionCalculatorContext(date, dailyPrices, assetQuantities);
        calculationSteps.forEach(step -> step.apply(context));
        return buildPortfolioEvolutionDTO(date, context);
    }

    private static EvolutionRetrieverContext buildEvolutionRetrieverContext(Long portfolioId, LocalDate startDate, LocalDate endDate) {
        return EvolutionRetrieverContext.builder().portfolioId(portfolioId).startDate(startDate).endDate(endDate).build();
    }

    private static PortfolioEvolutionDTO buildPortfolioEvolutionDTO(LocalDate date, EvolutionCalculatorContext context) {
        return new PortfolioEvolutionDTO(
                date,
                context.getTotalAsset(),
                context.getWeightByAsset());
    }

    private static EvolutionCalculatorContext buildEvolutionCalculatorContext(LocalDate date, List<Price> dailyPrices, List<AssetQuantity> assetQuantities) {
        return EvolutionCalculatorContext.builder()
                .date(date)
                .prices(dailyPrices)
                .quantities(assetQuantities)
                .build();
    }
}
