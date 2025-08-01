package com.abacus.portafolio.evolution.service.impl.calculator;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import com.abacus.portafolio.evolution.service.IEvolutionCalculatorStep;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Order(1)
public class AssetAmountCalculator implements IEvolutionCalculatorStep {
    @Override
    public void apply(EvolutionCalculatorContext context) {
        List<AssetQuantity> quantities = context.getQuantities();
        Map<Asset, BigDecimal> prices = context.getMapPricesByAsset();
        context.setAssetByAmount(quantities.stream()
                .filter(q -> prices.containsKey(q.getAsset()))
                .collect(Collectors.toMap(
                        AssetQuantity::getAsset,
                        q -> q.getQuantity().multiply(prices.get(q.getAsset()))
                )));
    }
}
