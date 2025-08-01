package com.abacus.portafolio.evolution.service;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Portfolio;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetQuantityRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioEvolutionService {
    private final PortfolioRepository portfolioRepository;
    private final AssetQuantityRepository assetQuantityRepository;
    private final AppConfig appConfig;

    private final PriceRepository priceRepository;

    public List<PortfolioEvolutionDTO> calculateEvolution(Long portfolioId, LocalDate startDate, LocalDate endDate) {
        log.info("Calculating evolutions for portfolio {}", portfolioId);
        Portfolio portfolioEntity = findPortfolio(portfolioId);
        List<AssetQuantity> assetQuantities = findAssetQuantities(portfolioEntity);
        List<Price> prices = findPricesByDateBetween(startDate, endDate);
        Map<LocalDate, List<Price>> pricesByDate = groupPricesByDate(prices);
        return buildPortfolioEvolution(pricesByDate, assetQuantities);

    }

    public List<PortfolioEvolutionDTO> buildPortfolioEvolution(Map<LocalDate, List<Price>> pricesByDate, List<AssetQuantity> assetQuantities) {
        List<LocalDate> sortedDates = getSortedDates(pricesByDate);
        return sortedDates.stream()
                .map(currentDate -> buildEvolutionEntryForDate(currentDate, pricesByDate.get(currentDate), assetQuantities))
                .toList();
    }

    private PortfolioEvolutionDTO buildEvolutionEntryForDate(LocalDate date, List<Price> pricesInDate, List<AssetQuantity> assetQuantities) {
        Map<Asset, BigDecimal> assetPrices = mapPricesByAsset(pricesInDate);
        Map<Asset, BigDecimal> totalAmountByAsset = calculateTotalAmountPerAsset(assetQuantities, assetPrices);
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

    private Map<Asset, BigDecimal> calculateTotalAmountPerAsset(List<AssetQuantity> assetQuantities, Map<Asset, BigDecimal> assetPriceMap) {
        return assetQuantities.stream()
                .filter(assetTotal -> assetPriceMap.containsKey(assetTotal.getAsset()))
                .collect(Collectors.toMap(
                        AssetQuantity::getAsset,
                        asset -> asset.getQuantity().multiply(assetPriceMap.get(asset.getAsset()))
                ));
    }

    private Portfolio findPortfolio(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portafolio no encontrado"));
    }

    private List<AssetQuantity> findAssetQuantities(Portfolio entity) {
        return assetQuantityRepository.findByPortfolio(entity);
    }

    private List<Asset> findAllAssets(List<AssetQuantity> assetQuantities) {
        return assetQuantities.stream()
                .map(AssetQuantity::getAsset)
                .distinct()
                .toList();
    }

    private List<LocalDate> getSortedDates(Map<LocalDate, ?> pricesByDate) {
        return pricesByDate.keySet().stream()
                .sorted()
                .toList();
    }

    private List<Price> findPricesByDateBetween(LocalDate startDate, LocalDate endDate) {
        return priceRepository.findByDateBetween(startDate, endDate);
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
