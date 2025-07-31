package com.abacus.portafolio.etl.service.impl;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetInvestment;
import com.abacus.portafolio.etl.entities.Portfolio;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.model.EtlContext;
import com.abacus.portafolio.etl.repository.AssetInvestmentRepository;
import com.abacus.portafolio.etl.repository.InitialWeightRepository;
import com.abacus.portafolio.etl.repository.PortfolioRepository;
import com.abacus.portafolio.etl.repository.PriceRepository;
import com.abacus.portafolio.etl.service.FileExtractionStep;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Order(2)
public class QuantityInitializationStep implements FileExtractionStep {
    private final PriceRepository priceRepository;
    private final PortfolioRepository portfolioRepository;
    private final AppConfig appConfig;
    private final InitialWeightRepository initialWeightRepository;
    private final AssetInvestmentRepository assetInvestmentRepository;

    @Override
    public void execute(EtlContext context) {
        Price firstPrice = priceRepository.findFirstByOrderByDateAsc();
        LocalDate initialDate = firstPrice.getDate();
        portfolioRepository.findAll().forEach(portfolio -> {
            BigDecimal initialValue = appConfig.findInitialInvestmentAmount(portfolio.getName());
            int scale = appConfig.getScale();
            initializeQuantities(portfolio, initialValue.doubleValue(), scale, initialDate);
        });
    }

    @Transactional
    public void initializeQuantities(Portfolio portfolio, double initialValue, int scale, LocalDate initialDate) {
        initialWeightRepository.findByPortfolio(portfolio)
                .forEach(weight -> {
                    Asset asset = weight.getAsset();
                    BigDecimal priceAmount = findPriceAmount(asset, initialDate);
                    BigDecimal quantity = BigDecimal.valueOf(weight.getWeight().doubleValue() * initialValue)
                            .divide(priceAmount, scale, RoundingMode.HALF_UP);
                    assetInvestmentRepository.save(AssetInvestment.builder()
                            .asset(asset)
                            .portfolio(portfolio)
                            .amount(quantity)
                            .build());
                });
    }

    private BigDecimal findPriceAmount(Asset asset, LocalDate initialDate) {
        return priceRepository.findByAssetAndDate(asset, initialDate)
                .map(Price::getPriceAmount)
                .orElseThrow(() -> new RuntimeException("Price not found for " +
                        asset.getName() + " on " + initialDate));
    }
}
