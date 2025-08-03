package com.abacus.portafolio.evolution.service.impl.updater;

import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.repository.AssetQuantityRepository;
import com.abacus.portafolio.evolution.model.EvolutionUpdaterContext;
import com.abacus.portafolio.evolution.service.IEvolutionUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Order(0)
public class QuantityAssetUpdater implements IEvolutionUpdater {
    private final AssetQuantityRepository assetQuantityRepository;
    private static final LocalDate MAX_VALID_TO = LocalDate.of(9999, 1, 1);

    @Override
    public void update(EvolutionUpdaterContext context) {
        updateAssetQuantity(
                context.getQuantitiesByAsset().get(context.getAssetSeller()),
                context.getOperationDay(),
                context.getUnitsToSell().negate()
        );

        updateAssetQuantity(
                context.getQuantitiesByAsset().get(context.getAssetBuyer()),
                context.getOperationDay(),
                context.getUnitsToBuy()
        );
    }

    private void updateAssetQuantity(AssetQuantity currentQuantity, LocalDate operationDate, BigDecimal deltaQuantity) {
        if (currentQuantity == null) {
            throw new IllegalArgumentException("AssetQuantity not found for operation.");
        }

        closeCurrentQuantity(currentQuantity, operationDate);
        openNewQuantity(currentQuantity, operationDate, deltaQuantity);
    }

    private void closeCurrentQuantity(AssetQuantity currentQuantity, LocalDate operationDate) {
        AssetQuantity closedQuantity = AssetQuantity.builder()
                .id(currentQuantity.getId())
                .portfolio(currentQuantity.getPortfolio())
                .asset(currentQuantity.getAsset())
                .quantity(currentQuantity.getQuantity())
                .validFrom(currentQuantity.getValidFrom())
                .validTo(operationDate.minusDays(1))
                .build();

        assetQuantityRepository.save(closedQuantity);
    }

    private void openNewQuantity(AssetQuantity baseQuantity, LocalDate operationDate, BigDecimal deltaQuantity) {
        AssetQuantity newQuantity = AssetQuantity.builder()
                .portfolio(baseQuantity.getPortfolio())
                .asset(baseQuantity.getAsset())
                .quantity(baseQuantity.getQuantity().add(deltaQuantity))
                .validFrom(operationDate)
                .validTo(MAX_VALID_TO)
                .build();

        assetQuantityRepository.save(newQuantity);
    }
}
