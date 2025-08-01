package com.abacus.portafolio.evolution.service.impl.calculator;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import com.abacus.portafolio.evolution.service.IEvolutionCalculatorStep;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
@Order(2)
@RequiredArgsConstructor
public class PortfolioValueStep implements IEvolutionCalculatorStep {
    private final AppConfig config;

    @Override
    public void apply(EvolutionCalculatorContext context) {
        Map<Asset, BigDecimal> investmentByAsset = context.getAssetInvestmentMap();
        BigDecimal totalPortfolioValue = calculateTotalInvestment(investmentByAsset);
        context.setTotalAsset(totalPortfolioValue);
    }

    private BigDecimal calculateTotalInvestment(Map<Asset, BigDecimal> investmentByAsset) {
        return investmentByAsset.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(config.getScale(), RoundingMode.HALF_UP);
    }
}
