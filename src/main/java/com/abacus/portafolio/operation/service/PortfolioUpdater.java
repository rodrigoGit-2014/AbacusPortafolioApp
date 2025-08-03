package com.abacus.portafolio.operation.service;

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

    public void update(Portfolio portfolio, LocalDate operationDate, List<Asset> assets) {

        EvolutionRetrieverContext context = EvolutionRetrieverContext.builder()
                .portfolioId(portfolio.getId())
                .startDate(operationDate)
                .endDate(operationDate)
                .build();

        retrievers.forEach(r -> r.update(context));
        BigDecimal totalPortfolio = getTotalPortfolioValue(assets, context.getQuantitiesByAsset(),context.getPriceByAsset());

        List<AssetWeight> history = assetWeightRepository.findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(portfolio.getId(), operationDate,operationDate);


    }


    public void registerOperation(Asset asset, Portfolio portfolio, LocalDate operationDate, BigDecimal deltaQuantity) {
        LocalDate updateValidTo = LocalDate.of(9999, 1, 1);
        List<AssetQuantity> history = assetQuantityRepository.findByPortfolioAndAsset(portfolio, asset);
        history.sort(Comparator.comparing(AssetQuantity::getValidFrom));

        Optional<AssetQuantity> recordAtDate = history.stream()
                .filter(r -> !operationDate.isBefore(r.getValidFrom()) && !operationDate.isAfter(r.getValidTo()))
                .findFirst();

        if (recordAtDate.isPresent()) {


            // Caso: operación entre medio → dividir el rango
            AssetQuantity current = recordAtDate.get();

            if (!current.getValidTo().isEqual(updateValidTo)) {
                updateValidTo = current.getValidTo();
            }

            // 1. Cerrar el rango anterior
            current.setValidTo(operationDate.minusDays(1));
            assetQuantityRepository.save(current);

            // 2. Crear nuevo registro modificado
            AssetQuantity updated = new AssetQuantity();

            updated.setPortfolio(portfolio);
            updated.setAsset(asset);
            updated.setQuantity(current.getQuantity().add(deltaQuantity));
            updated.setValidFrom(operationDate);
            updated.setValidTo(updateValidTo);

            assetQuantityRepository.save(updated);


        }
    }

    private BigDecimal getTotalPortfolioValue(List<Asset> assets, Map<Asset, AssetQuantity> quantities, Map<Asset, Price> prices) {
        return assets.stream()
                .map(asset -> calculateAssetValue(quantities.get(asset), prices.get(asset), asset.getName()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAssetValue(AssetQuantity quantity,  Price price, String assetName) {
        if (quantity == null || price == null) {
            throw new IllegalStateException("Missing data for asset: " + assetName);
        }

        return quantity.getQuantity().multiply(price.getPriceAmount());
    }
}
