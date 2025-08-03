package com.abacus.portafolio.operation.service;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.*;
import com.abacus.portafolio.etl.repository.AssetWeightRepository;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.service.IEvolutionRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioUpdater {
    private final AssetWeightRepository assetWeightRepository;
    private final List<IEvolutionRetriever> retrievers;
    private final AppConfig appConfig;

    public void update(Portfolio portfolio, LocalDate operationDate, List<Asset> assets) {

        EvolutionRetrieverContext context = EvolutionRetrieverContext.builder()
                .portfolioId(portfolio.getId())
                .startDate(operationDate)
                .endDate(operationDate)
                .build();

        retrievers.forEach(r -> r.update(context));
        BigDecimal totalPortfolio = getTotalPortfolioValue(assets, context.getQuantitiesByAsset(), context.getPriceByAsset());

        List<AssetWeight> history = assetWeightRepository.findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(portfolio.getId(), operationDate, operationDate);
        for (AssetWeight assetWeight : history) {
            Price price = context.getPriceByAsset().get(assetWeight.getAsset());
            registerOperation(operationDate,price,assetWeight,totalPortfolio);
        }

    }


    public void registerOperation( LocalDate operationDate, Price price, AssetWeight current, BigDecimal totalPortfolio) {
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
        updated.setWeight((current.getWeight().multiply(price.getPriceAmount())).divide(totalPortfolio, appConfig.getScale(), BigDecimal.ROUND_HALF_UP));
        updated.setValidFrom(operationDate);
        updated.setValidTo(updateValidTo);

        assetWeightRepository.save(updated);


    }

    private BigDecimal getTotalPortfolioValue(List<Asset> assets, Map<Asset, AssetQuantity> quantities, Map<Asset, Price> prices) {
        return assets.stream()
                .map(asset -> calculateAssetValue(quantities.get(asset), prices.get(asset), asset.getName()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAssetValue(AssetQuantity quantity, Price price, String assetName) {
        if (quantity == null || price == null) {
            throw new IllegalStateException("Missing data for asset: " + assetName);
        }

        return quantity.getQuantity().multiply(price.getPriceAmount());
    }
}
