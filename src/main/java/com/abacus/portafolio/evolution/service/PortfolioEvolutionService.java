package com.abacus.portafolio.evolution.service;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetInvestment;
import com.abacus.portafolio.etl.entities.Portfolio;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetInvestmentRepository;
import com.abacus.portafolio.etl.repository.PortfolioRepository;
import com.abacus.portafolio.etl.repository.PriceRepository;
import com.abacus.portafolio.evolution.dto.PortfolioEvolutionDTO;
import com.abacus.portafolio.evolution.dto.WeightByAssetDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioEvolutionService {
    private final PortfolioRepository portfolioRepository;
    private final AssetInvestmentRepository assetInvestmentRepository;
    private final AppConfig appConfig;

    private final PriceRepository priceRepository;

    public List<PortfolioEvolutionDTO> calculateEvolution(Long portfolioId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating evolutions for portfolio {}", portfolioId);
        Portfolio portfolioEntity = findPortfolio(portfolioId);
        List<AssetInvestment> assetInvestments = findAssetInvestments(portfolioEntity);
        List<Asset> assets = findAllAssets(assetInvestments);
        List<Price> prices = findAssetByDateBetween(assets, startDate, endDate);
        Map<LocalDate, List<Price>> pricesByDate = groupPricesByDate(prices);
        return buildPortfolioEvolution(pricesByDate, assetInvestments);

    }

    public List<PortfolioEvolutionDTO> buildPortfolioEvolution(Map<LocalDate, List<Price>> pricesByDate, List<AssetInvestment> assetInvestments) {
        List<LocalDate> sortedDates = getSortedDates(pricesByDate);
        return sortedDates.stream()
                .map(currentDate -> buildEvolutionEntryForDate(currentDate, pricesByDate.get(currentDate), assetInvestments))
                .toList();
    }

    private PortfolioEvolutionDTO buildEvolutionEntryForDate(LocalDate date, List<Price> pricesInDate, List<AssetInvestment> assetInvestments) {
        Map<Asset, BigDecimal> priceByAssetMap = mapPricesByAsset(pricesInDate);
        Map<Asset, BigDecimal> totalAmountByAsset = calculateTotalAmountPerAsset(assetInvestments, priceByAssetMap);
        BigDecimal portfolioValue = calculateTotalInvestment(totalAmountByAsset);
        List<WeightByAssetDTO> weights = calculateWeightsByAsset(totalAmountByAsset, portfolioValue);

        return new PortfolioEvolutionDTO(date, portfolioValue, weights);
    }

    private List<WeightByAssetDTO> calculateWeightsByAsset(Map<Asset, BigDecimal> totalAssetAmountMap, BigDecimal totalValue) {
        return totalAssetAmountMap.entrySet().stream()
                .map(e -> new WeightByAssetDTO(
                        e.getKey().getName(),
                        e.getValue().divide(totalValue, appConfig.getScale(), RoundingMode.HALF_UP).doubleValue()
                ))
                .toList();
    }

    private BigDecimal calculateTotalInvestment(Map<Asset, BigDecimal> assetAmounts) {
        return assetAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(appConfig.getScale(), RoundingMode.HALF_UP);
    }

    private Map<Asset, BigDecimal> calculateTotalAmountPerAsset(List<AssetInvestment> assetInvestments, Map<Asset, BigDecimal> assetPriceMap) {
        return assetInvestments.stream()
                .filter(investment -> assetPriceMap.containsKey(investment.getAsset()))
                .collect(Collectors.toMap(
                        AssetInvestment::getAsset,
                        investment -> investment.getAmount().multiply(assetPriceMap.get(investment.getAsset()))
                ));
    }

    private Portfolio findPortfolio(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portafolio no encontrado"));
    }

    private List<AssetInvestment> findAssetInvestments(Portfolio entity) {
        return assetInvestmentRepository.findByPortfolio(entity);
    }

    private List<Asset> findAllAssets(List<AssetInvestment> assetQuantities) {
        return assetQuantities.stream()
                .map(AssetInvestment::getAsset)
                .distinct()
                .toList();
    }

    private List<LocalDate> getSortedDates(Map<LocalDate, ?> pricesByDate) {
        return pricesByDate.keySet().stream()
                .sorted()
                .toList();
    }

    private List<Price> findAssetByDateBetween(List<Asset> assets, LocalDate startDate, LocalDate endDate) {
        return priceRepository.findByAssetInAndDateBetween(assets, startDate, endDate);
    }


    private Map<Asset, BigDecimal> mapPricesByAsset(List<Price> pricesInDate) {
        return pricesInDate.stream()
                .collect(Collectors.toMap(Price::getAsset, Price::getPriceAmount));
    }

    private Map<LocalDate, List<Price>> groupPricesByDate(List<Price> prices) {
        return prices.stream()
                .collect(Collectors.groupingBy(Price::getDate));
    }
}
