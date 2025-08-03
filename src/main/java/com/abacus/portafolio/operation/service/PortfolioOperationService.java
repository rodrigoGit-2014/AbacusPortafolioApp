package com.abacus.portafolio.operation.service;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.*;
import com.abacus.portafolio.etl.repository.*;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.service.IEvolutionRetriever;
import com.abacus.portafolio.operation.dto.PortfolioOperationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioOperationService {
    private final AssetRepository assetRepository;
    private final PriceRepository priceRepository;
    private final AssetQuantityRepository assetQuantityRepository;
    private final PortfolioRepository portfolioRepository;
   // private final PortfolioUpdater portfolioUpdater;
    private final AppConfig appConfig;
    private final List<IEvolutionRetriever> retrievers;
    private final AssetWeightRepository assetWeightRepository;

    public void process(Long portfolioId, PortfolioOperationDTO request) {
        List<Asset> assets = assetRepository.findAll();

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        EvolutionRetrieverContext context = calculateTotalPortfolio(portfolio,request.getDay(),assets);

        Asset assetSeller = assetRepository.findByNameIgnoreCase(request.getSeller().getAsset()).orElseThrow(() -> new RuntimeException("Asset not found"));
        Asset assetBuyer = assetRepository.findByNameIgnoreCase(request.getBuyer().getAsset()).orElseThrow(() -> new RuntimeException("Asset not found"));

        Price priceSeller = priceRepository.findByAssetAndDate(assetSeller, request.getDay())
                .orElseThrow(() -> new RuntimeException("Price not found for asset: " + assetSeller.getName()));

        Price priceBuyer = priceRepository.findByAssetAndDate(assetBuyer, request.getDay())
                .orElseThrow(() -> new RuntimeException("Price not found for asset: " + assetBuyer.getName()));

        BigDecimal unitsToSell = request.getSeller().getAmount().divide(priceSeller.getPriceAmount(), appConfig.getScale(), RoundingMode.HALF_UP);
        BigDecimal unitsToBuy = request.getSeller().getAmount().divide(priceBuyer.getPriceAmount(), appConfig.getScale(), RoundingMode.HALF_UP);

        registerOperation(assetSeller, portfolio, request.getDay(), unitsToSell.multiply(BigDecimal.valueOf(-1)));
        registerOperation(assetBuyer, portfolio, request.getDay(), unitsToBuy);

        ddd(portfolio, request.getDay(), context);


    }

    private void ddd(Portfolio portfolio , LocalDate operationDate, EvolutionRetrieverContext context){
        List<AssetWeight> history = assetWeightRepository.findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(portfolio.getId(), operationDate, operationDate);
        for (AssetWeight assetWeight : history) {
            Price price = context.getPriceByAsset().get(assetWeight.getAsset());
            AssetQuantity quantity = context.getQuantitiesByAsset().get(assetWeight.getAsset());
            registerOperation2(operationDate,price,assetWeight,context.getPortfolioValue(), quantity);
        }
    }

    public void registerOperation2( LocalDate operationDate, Price price, AssetWeight current, BigDecimal totalPortfolio, AssetQuantity quantity ) {
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
    private EvolutionRetrieverContext calculateTotalPortfolio(Portfolio portfolio , LocalDate operationDate, List<Asset> assets){
        EvolutionRetrieverContext context = EvolutionRetrieverContext.builder()
                .portfolioId(portfolio.getId())
                .startDate(operationDate)
                .endDate(operationDate)
                .build();

        retrievers.forEach(r -> r.update(context));
        context.setPortfolioValue( getTotalPortfolioValue(assets, context.getQuantitiesByAsset(), context.getPriceByAsset()));
        return context;

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
            //current.setQuantity(current.getQuantity().add(deltaQuantity));
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
}