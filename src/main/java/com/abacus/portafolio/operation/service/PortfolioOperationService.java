package com.abacus.portafolio.operation.service;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Portfolio;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetQuantityRepository;
import com.abacus.portafolio.etl.repository.AssetRepository;
import com.abacus.portafolio.etl.repository.PortfolioRepository;
import com.abacus.portafolio.etl.repository.PriceRepository;
import com.abacus.portafolio.operation.dto.PortfolioOperationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioOperationService {
    private final AssetRepository assetRepository;
    private final PriceRepository priceRepository;
    private final AssetQuantityRepository assetQuantityRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioValueCalculator portfolioValueCalculator;
    private final AppConfig appConfig;

    public void process(Long portfolioId, PortfolioOperationDTO request) {
        List<Asset> assets = assetRepository.findAll();

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
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

        BigDecimal totalPortfolio =  portfolioValueCalculator.calculate(portfolioId, request.getDay(), assets);


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
}