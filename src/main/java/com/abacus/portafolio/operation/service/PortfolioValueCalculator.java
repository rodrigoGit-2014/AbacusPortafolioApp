package com.abacus.portafolio.operation.service;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetQuantityRepository;
import com.abacus.portafolio.etl.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PortfolioValueCalculator {

    private final AssetQuantityRepository assetQuantityRepository;
    private final PriceRepository priceRepository;

    public BigDecimal calculate(Long portfolioId, LocalDate date, List<Asset> assets) {
        Map<Asset, AssetQuantity> quantitiesByAsset = loadLatestQuantitiesByAsset(portfolioId, date);
        Map<Asset, Price> pricesByAsset = loadPricesByAsset(date);

        return assets.stream()
                .map(asset -> calculateAssetValue(asset, quantitiesByAsset, pricesByAsset))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Asset, AssetQuantity> loadLatestQuantitiesByAsset(Long portfolioId, LocalDate date) {
        return assetQuantityRepository
                .findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(portfolioId, date, date)
                .stream()
                .collect(Collectors.toMap(
                        AssetQuantity::getAsset,
                        aq -> aq, // assuming one record per asset is valid on that date
                        (existing, replacement) -> replacement // if duplicated, keep latest
                ));
    }

    private Map<Asset, Price> loadPricesByAsset(LocalDate date) {
        return priceRepository.findByDateBetween(date, date).stream()
                .collect(Collectors.toMap(
                        Price::getAsset,
                        price -> price // assuming only one price per asset per day
                ));
    }

    private BigDecimal calculateAssetValue(Asset asset, Map<Asset, AssetQuantity> quantities, Map<Asset, Price> prices) {
        AssetQuantity quantity = quantities.get(asset);
        Price price = prices.get(asset);

        if (quantity == null || price == null) {
            throw new IllegalStateException("Missing data for asset: " + asset.getName());
        }

        return quantity.getQuantity().multiply(price.getPriceAmount());
    }
}
