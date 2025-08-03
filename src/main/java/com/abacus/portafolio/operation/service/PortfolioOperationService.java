package com.abacus.portafolio.operation.service;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetRepository;
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

    private final AppConfig appConfig;
    private final List<IEvolutionRetriever> retrievers;
    private final List<IEvolutionUpdater> updaters;


    public void process(Long portfolioId, PortfolioOperationDTO request) {

        EvolutionRetrieverContext context = loadContext(portfolioId, request.getDay());

        Asset assetSeller = findAsset(request.getSeller());
        Asset assetBuyer = findAsset(request.getBuyer());

        Price priceSeller = context.getPriceByAsset().get(assetSeller);
        Price priceBuyer = context.getPriceByAsset().get(assetBuyer);

        BigDecimal unitsToSell = calculateUnitEquivalence(request.getSeller().getAmount(), priceSeller);
        BigDecimal unitsToBuy = calculateUnitEquivalence(request.getBuyer().getAmount(), priceBuyer);

        updatePortfolio(context, request.getDay(), assetSeller, assetBuyer, unitsToSell, unitsToBuy);

    }

    private BigDecimal calculateUnitEquivalence(BigDecimal amount, Price price) {
        return amount.divide(price.getPriceAmount(), appConfig.getScale(), RoundingMode.HALF_UP);
    }

    private Asset findAsset(PortfolioOperationDTO.Transaction request) {
        return assetRepository.findByNameIgnoreCase(request.getAsset()).orElseThrow(() -> new RuntimeException("Asset not found"));
    }

    private void updatePortfolio(EvolutionRetrieverContext erc,
                                 LocalDate operationDay,
                                 Asset assetSeller, Asset assetBuyer,
                                 BigDecimal unitSeller, BigDecimal unitBuyer) {
        EvolutionUpdaterContext context = buildUpdaterContext(erc, operationDay, assetSeller, assetBuyer, unitSeller, unitBuyer);
        updaters.forEach(u -> u.update(context));

    }

    private EvolutionRetrieverContext loadContext(long portfolioId, LocalDate operationDate) {
        return applyRetrievers(buildRetrieveContext(portfolioId, operationDate));
    }

    private static EvolutionRetrieverContext buildRetrieveContext(long portfolioId, LocalDate operationDate) {
        return EvolutionRetrieverContext.builder().portfolioId(portfolioId).startDate(operationDate).endDate(operationDate).build();
    }

    private EvolutionRetrieverContext applyRetrievers(EvolutionRetrieverContext context) {
        retrievers.forEach(r -> r.update(context));
        return context;
    }

    private static EvolutionUpdaterContext buildUpdaterContext(EvolutionRetrieverContext erc,
                                                               LocalDate operationDay,
                                                               Asset assetSeller,
                                                               Asset assetBuyer,
                                                               BigDecimal unitSeller,
                                                               BigDecimal unitBuyer) {
        return EvolutionUpdaterContext.builder()
                .portfolioId(erc.getPortfolioId())
                .operationDay(operationDay)
                .priceByAsset(erc.getPriceByAsset())
                .quantitiesByAsset(erc.getQuantitiesByAsset())
                .assetSeller(assetSeller)
                .assetBuyer(assetBuyer)
                .unitsToSell(unitSeller)
                .unitsToBuy(unitBuyer)
                .build();
    }

}