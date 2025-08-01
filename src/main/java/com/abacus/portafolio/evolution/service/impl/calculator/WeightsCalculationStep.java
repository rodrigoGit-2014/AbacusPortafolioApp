package com.abacus.portafolio.evolution.service.impl.calculator;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.evolution.dto.WeightByAssetDTO;
import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.service.IEvolutionCalculatorStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WeightsCalculationStep implements IEvolutionCalculatorStep {

    private final AppConfig config;

    @Override
    public void apply(EvolutionCalculatorContext context) {

        BigDecimal totalValue = context.getTotalAsset();
        Map<Asset, BigDecimal> assetTotals = context.getAssetByAmount();

        if (totalValue.compareTo(BigDecimal.ZERO) == 0) context.setWeightByAsset(List.of());

        List<WeightByAssetDTO> ss = assetTotals.entrySet().stream()
                .map(entry -> new WeightByAssetDTO(
                        entry.getKey().getName(),
                        entry.getValue()
                                .divide(totalValue, config.getScale(), RoundingMode.HALF_UP)
                                .doubleValue()
                ))
                .toList();

        context.setWeightByAsset(ss);
    }
}
