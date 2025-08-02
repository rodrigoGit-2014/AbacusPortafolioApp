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
        List<AssetQuantity> quantities = assetQuantityRepository
                .findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(portfolioId, date, date);
        List<Price> prices = priceRepository.findByDateBetween(date, date);
        Map<Asset,  List<AssetQuantity>> grouped = quantities.stream().collect(Collectors.groupingBy(AssetQuantity::getAsset));
        Map<Asset, List<Price>> priceMap = prices.stream().collect(Collectors.groupingBy(Price::getAsset));
        BigDecimal total = BigDecimal.ZERO;
        for(Asset asset : assets) {
            total = total.add(grouped.get(asset).getLast().getQuantity().multiply(priceMap.get(asset).getFirst().getPriceAmount()));
        }
        return total;
    }
}
