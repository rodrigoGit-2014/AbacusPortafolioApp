package com.abacus.portafolio.operation.service;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetQuantityRepository;
import com.abacus.portafolio.etl.repository.AssetRepository;
import com.abacus.portafolio.etl.repository.AssetWeightRepository;
import com.abacus.portafolio.etl.repository.PriceRepository;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.model.EvolutionUpdaterContext;
import com.abacus.portafolio.evolution.service.IEvolutionRetriever;
import com.abacus.portafolio.evolution.service.IEvolutionUpdater;
import com.abacus.portafolio.operation.dto.PortfolioOperationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioOperationService {
    private final AssetRepository assetRepository;
    private final PriceRepository priceRepository;
    private final AppConfig appConfig;
    private final List<IEvolutionRetriever> retrievers;
    private final List<IEvolutionUpdater> updaters;



    public void process(Long portfolioId, PortfolioOperationDTO request) {
        
        EvolutionRetrieverContext context = loadContext(portfolioId, request.getDay());

        Asset assetSeller = findAsset(request.getSeller());
        Asset assetBuyer = findAsset(request.getBuyer());

        Price priceSeller = findPrice(request, assetSeller);
        Price priceBuyer = findPrice(request, assetBuyer);

        BigDecimal unitsToSell = calculateOperationAssetAmount(request, priceSeller);
        BigDecimal unitsToBuy = calculateOperationAssetAmount(request, priceBuyer);

        updatePortfolio(context, assetSeller, assetBuyer, unitsToSell, unitsToBuy);

    }

    private BigDecimal calculateOperationAssetAmount(PortfolioOperationDTO request, Price priceSeller) {
        return request.getSeller().getAmount().divide(priceSeller.getPriceAmount(), appConfig.getScale(), RoundingMode.HALF_UP);
    }

    private Price findPrice(PortfolioOperationDTO request, Asset assetSeller) {
        return priceRepository.findByAssetAndDate(assetSeller, request.getDay())
                .orElseThrow(() -> new RuntimeException("Price not found for asset: " + assetSeller.getName()));
    }

    private Asset findAsset(PortfolioOperationDTO.Transaction request) {
        return assetRepository.findByNameIgnoreCase(request.getAsset()).orElseThrow(() -> new RuntimeException("Asset not found"));
    }

    private void updatePortfolio(EvolutionRetrieverContext erc, Asset assetSeller, Asset assetBuyer,
                                 BigDecimal unitSeller, BigDecimal unitBuyer) {
        EvolutionUpdaterContext context= EvolutionUpdaterContext.builder().build();
        updaters.forEach(u -> u.update(context));

    }

    private EvolutionRetrieverContext loadContext(long portfolioId, LocalDate operationDate) {
        EvolutionRetrieverContext context = EvolutionRetrieverContext.builder()
                .portfolioId(portfolioId)
                .startDate(operationDate)
                .endDate(operationDate)
                .build();

        retrievers.forEach(r -> r.update(context));
        return context;

    }

}