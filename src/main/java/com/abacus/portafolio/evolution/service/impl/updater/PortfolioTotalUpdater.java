package com.abacus.portafolio.evolution.service.impl.updater;

import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.evolution.model.EvolutionUpdaterContext;
import com.abacus.portafolio.evolution.service.IEvolutionUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PortfolioTotalUpdater implements IEvolutionUpdater {
    @Override
    public void update(EvolutionUpdaterContext context) {
        context.setPortfolioValue(context.getAssets().stream()
                .map(asset -> calculateAssetValue(context.getQuantitiesByAsset().get(asset), context.getPriceByAsset().get(asset), asset.getName()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

    }

    private BigDecimal calculateAssetValue(AssetQuantity quantity, Price price, String assetName) {
        if (quantity == null || price == null) {
            throw new IllegalStateException("Missing data for asset: " + assetName);
        }

        return quantity.getQuantity().multiply(price.getPriceAmount());
    }
}
