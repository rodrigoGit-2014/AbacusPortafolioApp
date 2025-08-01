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
public class AssetValueCalculatorStep implements IEvolutionCalculatorStep {
    @Override
    public void apply(EvolutionCalculatorContext context) {

        Map<Asset, BigDecimal> pricesByAsset = context.getAssetPriceMap();
        List<AssetQuantity> assetQuantities = context.getQuantities();

        Map<Asset, BigDecimal> investmentByAsset = assetQuantities.stream()
                .filter(q -> pricesByAsset.containsKey(q.getAsset()))
                .collect(Collectors.toMap(
                        AssetQuantity::getAsset,
                        q -> calculateAssetValue(q, pricesByAsset)
                ));

        context.setAssetsValueMap(investmentByAsset);
    }

    private BigDecimal calculateAssetValue(AssetQuantity quantity, Map<Asset, BigDecimal> pricesByAsset) {
        return quantity.getQuantity().multiply(pricesByAsset.get(quantity.getAsset()));
    }
}
