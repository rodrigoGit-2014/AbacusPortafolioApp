package com.abacus.portafolio.evolution.service.impl.calculator;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.evolution.dto.WeightByAssetDTO;
import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import com.abacus.portafolio.evolution.service.IEvolutionCalculatorStep;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Order(3)
public class WeightsCalculationStep implements IEvolutionCalculatorStep {

    private final AppConfig config;

    @Override
    public void apply(EvolutionCalculatorContext context) {

        BigDecimal totalInvestment = context.getTotalAsset();
        Map<Asset, BigDecimal> investmentByAsset = context.getAssetInvestmentMap();

        if (isZero(totalInvestment)) {
            context.setWeightByAsset(List.of());
            return;
        }

        List<WeightByAssetDTO> assetWeights = calculateWeights(investmentByAsset, totalInvestment);
        context.setWeightByAsset(assetWeights);
    }

    private boolean isZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    private List<WeightByAssetDTO> calculateWeights(Map<Asset, BigDecimal> investments, BigDecimal total) {
        return investments.entrySet().stream()
                .map(entry -> {
                    String assetName = entry.getKey().getName();
                    BigDecimal investment = entry.getValue();
                    double weight = investment.divide(total, config.getScale(), RoundingMode.HALF_UP).doubleValue();
                    return new WeightByAssetDTO(assetName, weight);
                })
                .toList();
    }
}
