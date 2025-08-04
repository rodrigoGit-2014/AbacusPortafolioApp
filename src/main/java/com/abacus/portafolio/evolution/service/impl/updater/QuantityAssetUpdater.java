package com.abacus.portafolio.evolution.service.impl.updater;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.repository.AssetQuantityRepository;
import com.abacus.portafolio.evolution.dto.AssetOperationDTO;
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
        updateAssetQuantity(context, context.getAssetSeller());
        updateAssetQuantity(context, context.getAssetBuyer());
    }

    private void updateAssetQuantity(EvolutionUpdaterContext context, Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("AssetQuantity not found for operation.");
        }

        closeCurrentQuantity(context, asset);
        openNewQuantity(context, asset);
    }

    private void closeCurrentQuantity(EvolutionUpdaterContext context, Asset asset) {
        AssetQuantity currentQuantity = context.getQuantitiesByAsset().get(asset);
        AssetQuantity closedQuantity = AssetQuantity.builder()
                .id(currentQuantity.getId())
                .portfolio(currentQuantity.getPortfolio())
                .asset(currentQuantity.getAsset())
                .quantity(currentQuantity.getQuantity())
                .validFrom(currentQuantity.getValidFrom())
                .validTo(context.getOperationDay().minusDays(1))
                .build();

        assetQuantityRepository.save(closedQuantity);
    }

    private static void buildWeightResponse(EvolutionUpdaterContext context, Asset asset, AssetQuantity closedQuantity) {
        context.getResponse().getAssetOperations().add(AssetOperationDTO.builder()
                .assetName(asset.getName())
                .priceAmount(context.getPriceByAsset().get(asset).getPriceAmount())
                .assetAmount(closedQuantity.getQuantity())
                .build());
    }

    private void openNewQuantity(EvolutionUpdaterContext context, Asset asset) {
        AssetQuantity currentQuantity = context.getQuantitiesByAsset().get(asset);
        AssetQuantity newQuantity = AssetQuantity.builder()
                .portfolio(currentQuantity.getPortfolio())
                .asset(currentQuantity.getAsset())
                .quantity(currentQuantity.getQuantity().add(context.getUnitsToBuy()))
                .validFrom(context.getOperationDay())
                .validTo(MAX_VALID_TO)
                .build();

        assetQuantityRepository.save(newQuantity);
        buildWeightResponse(context, asset, newQuantity);
    }
}
