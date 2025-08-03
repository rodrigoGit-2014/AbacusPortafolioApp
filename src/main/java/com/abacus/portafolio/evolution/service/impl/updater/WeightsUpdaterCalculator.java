package com.abacus.portafolio.evolution.service.impl.updater;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.AssetWeight;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetWeightRepository;
import com.abacus.portafolio.evolution.model.EvolutionUpdaterContext;
import com.abacus.portafolio.evolution.service.IEvolutionUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeightsUpdaterCalculator implements IEvolutionUpdater {
    private final AssetWeightRepository assetWeightRepository;
    private final AppConfig appConfig;

    @Override
    public void update(EvolutionUpdaterContext context) {
        List<AssetWeight> history = assetWeightRepository.findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(context.getPortfolioId(),
                context.getOperationDate(), context.getOperationDate());
        for (AssetWeight assetWeight : history) {
            Price price = context.getPriceByAsset().get(assetWeight.getAsset());
            AssetQuantity quantity = context.getQuantitiesByAsset().get(assetWeight.getAsset());
            registerAssetWeightOperation(context.getOperationDate(), price, assetWeight, context.getPortfolioValue(), quantity);
        }
    }

    public void registerAssetWeightOperation(LocalDate operationDate, Price price, AssetWeight current, BigDecimal totalPortfolio, AssetQuantity quantity) {
        LocalDate updateValidTo = LocalDate.of(9999, 1, 1);
        if (!current.getValidTo().isEqual(updateValidTo)) {
            updateValidTo = current.getValidTo();
        }

        // 1. Cerrar el rango anterior
        current.setValidTo(operationDate.minusDays(1));
        assetWeightRepository.save(current);

        // 2. Crear nuevo registro modificado
        AssetWeight updated = new AssetWeight();

        updated.setPortfolio(current.getPortfolio());
        updated.setAsset(current.getAsset());
        updated.setWeight((quantity.getQuantity().multiply(price.getPriceAmount())).divide(totalPortfolio, appConfig.getScale(), BigDecimal.ROUND_HALF_UP));
        updated.setValidFrom(operationDate);
        updated.setValidTo(updateValidTo);

        assetWeightRepository.save(updated);


    }
}
