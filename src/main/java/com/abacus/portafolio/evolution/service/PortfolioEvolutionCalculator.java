package com.abacus.portafolio.evolution.service;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.evolution.dto.PortfolioEvolutionDTO;
import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor

public class PortfolioEvolutionCalculator {
    private final AppConfig appConfig;
    private final List<IEvolutionCalculatorStep> steps;

    public List<PortfolioEvolutionDTO> calculate(Map<LocalDate, List<Price>> pricesByDate, List<AssetQuantity> assetQuantities) {
        return pricesByDate.keySet().stream()
                .sorted()
                .map(date -> buildEvolutionEntryV2(date, pricesByDate.get(date), assetQuantities))
                .toList();
    }

    private PortfolioEvolutionDTO buildEvolutionEntryV2(LocalDate date, List<Price> prices, List<AssetQuantity> quantities) {
        EvolutionCalculatorContext context = EvolutionCalculatorContext.builder().date(date).prices(prices).quantities(quantities).build();
        steps.forEach(step -> {
            step.apply(context);
        });
        return new PortfolioEvolutionDTO(date, context.getTotalAsset(), context.getWeightByAsset());
    }


}
