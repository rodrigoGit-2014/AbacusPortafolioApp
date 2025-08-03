package com.abacus.portafolio.evolution.service.impl.updater;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.repository.AssetQuantityRepository;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.model.EvolutionUpdaterContext;
import com.abacus.portafolio.evolution.service.IEvolutionUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuantityAssetUpdater implements IEvolutionUpdater {
    private final AssetQuantityRepository assetQuantityRepository;

    @Override
    public void update(EvolutionUpdaterContext context) {
        registerAssetQuantityOperation(context.getAssetSeller(), context.getOperationDate(), context.getUnitsToSell().multiply(BigDecimal.valueOf(-1)), context.getQuantitiesByAsset());
        registerAssetQuantityOperation(context.getAssetBuyer(), context.getOperationDate(), context.getUnitsToBuy(), context.getQuantitiesByAsset());


    }

    public void registerAssetQuantityOperation(Asset asset, LocalDate operationDate, BigDecimal deltaQuantity, Map<Asset, AssetQuantity> quantitiesByAsset) {
        LocalDate updateValidTo = LocalDate.of(9999, 1, 1);
        AssetQuantity current = quantitiesByAsset.get(asset);

        if (!current.getValidTo().isEqual(updateValidTo)) {
            updateValidTo = current.getValidTo();
        }

        // 1. Cerrar el rango anterior
        current.setValidTo(operationDate.minusDays(1));
        assetQuantityRepository.save(current);

        // 2. Crear nuevo registro modificado
        AssetQuantity updated = new AssetQuantity();
        updated.setPortfolio(current.getPortfolio());
        updated.setAsset(asset);
        updated.setQuantity(current.getQuantity().add(deltaQuantity));
        updated.setValidFrom(operationDate);
        updated.setValidTo(updateValidTo);

        assetQuantityRepository.save(updated);


    }
}
