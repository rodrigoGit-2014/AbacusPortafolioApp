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

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioOperationService {
    private final AssetRepository assetRepository;
    private final PriceRepository priceRepository;
    private final AssetQuantityRepository assetQuantityRepository;
    private final PortfolioRepository portfolioRepository;
    private final AppConfig appConfig;

    public void process(Long portfolioId, PortfolioOperationDTO request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        Asset assetSeller = assetRepository.findByNameIgnoreCase(request.getSeller().getAsset()).orElseThrow(() -> new RuntimeException("Asset not found"));
        Asset assetBuyer = assetRepository.findByNameIgnoreCase(request.getBuyer().getAsset()).orElseThrow(() -> new RuntimeException("Asset not found"));

        Price priceSeller = priceRepository.findByAssetAndDate(assetSeller, request.getDay())
                .orElseThrow(() -> new RuntimeException("Price not found for asset: " + assetSeller.getName()));

        Price priceBuyer = priceRepository.findByAssetAndDate(assetBuyer, request.getDay())
                .orElseThrow(() -> new RuntimeException("PPrice not found for asset: " + assetBuyer.getName()));

        BigDecimal unitsToSell = priceSeller.getPriceAmount().divide(request.getSeller().getAmount(), appConfig.getScale(), RoundingMode.HALF_UP);
        BigDecimal unitsToBuy = priceBuyer.getPriceAmount().divide(request.getBuyer().getAmount(), appConfig.getScale(), RoundingMode.HALF_UP);

        AssetQuantity assetQuantitySeller = assetQuantityRepository.findByPortfolioAndAsset(portfolio, assetSeller)
                .orElseThrow(() -> new RuntimeException("Asset Investment not found"));

        AssetQuantity assetQuantityBuyer = assetQuantityRepository.findByPortfolioAndAsset(portfolio, assetBuyer)
                .orElseThrow(() -> new RuntimeException("Asset Investment not found"));

        assetQuantitySeller.setQuantity(assetQuantitySeller.getQuantity().subtract(unitsToSell));
        assetQuantityBuyer.setQuantity(assetQuantityBuyer.getQuantity().subtract(unitsToBuy));

        log.info("The new unit investment for Seller is", assetQuantitySeller.getQuantity());
        log.info("The new unit investment for Buyer is", assetQuantityBuyer.getQuantity());

        assetQuantityRepository.save(assetQuantitySeller);
        assetQuantityRepository.save(assetQuantityBuyer);
    }
}