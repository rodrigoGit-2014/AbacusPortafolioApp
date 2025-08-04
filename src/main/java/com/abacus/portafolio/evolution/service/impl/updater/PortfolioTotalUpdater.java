package com.abacus.portafolio.evolution.service.impl.updater;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetRepository;
import com.abacus.portafolio.evolution.model.EvolutionUpdaterContext;
import com.abacus.portafolio.evolution.service.IEvolutionUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Order(1)
public class PortfolioTotalUpdater implements IEvolutionUpdater {
    private final AssetRepository assetRepository;

    @Override
    public void update(EvolutionUpdaterContext context) {
        BigDecimal total = assetRepository.findAll().stream()
                .map(asset -> calculateAssetValue(
                        asset,
                        context.getQuantitiesByAsset().get(asset),
                        context.getPriceByAsset().get(asset)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        context.setPortfolioValue(total);
        context.getResponse().setPortfolioTotal(total);
    }

    private BigDecimal calculateAssetValue(Asset asset, AssetQuantity quantity, Price price) {
        if (quantity == null) {
            throw new IllegalStateException("Missing quantity for asset: " + asset.getName());
        }
        if (price == null) {
            throw new IllegalStateException("Missing price for asset: " + asset.getName());
        }

        return quantity.getQuantity().multiply(price.getPriceAmount());
    }
}
