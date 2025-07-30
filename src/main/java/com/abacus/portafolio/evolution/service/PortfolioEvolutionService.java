package com.abacus.portafolio.evolution.service;

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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioEvolutionService {
    private final PortfolioRepository portfolioRepository;
    private final AssetQuantityRepository assetQuantityRepository;

    private final PriceRepository priceRepository;

    public List<PortfolioEvolutionDTO> calculateEvolution(Long portfolioId, LocalDate startDate, LocalDate endDate) {
        Portfolio portfolioEntity = findPortfolio(portfolioId);
        List<AssetQuantity> assetQuantities = findAssetQuantity(portfolioEntity);
        List<Asset> assets = findAllAsset(assetQuantities);
        List<Price> prices = findAssetByDateBetween(assets, startDate, endDate);
        Map<LocalDate, List<Price>> pricesByDate = prices.stream().collect(Collectors.groupingBy(Price::getDate));

        List<PortfolioEvolutionDTO> result = new ArrayList<>();
        for (LocalDate sortedDate : pricesByDate.keySet().stream().sorted().toList()) {
            List<Price> pricesInDate = pricesByDate.get(sortedDate);
            Map<Asset, BigDecimal> precioMap = pricesInDate.stream().collect(Collectors.toMap(Price::getAsset, Price::getPriceAmount));
            Map<Asset, BigDecimal> valueByAsset = new HashMap<>();
            BigDecimal totalValue = BigDecimal.ZERO;
            for (AssetQuantity c : assetQuantities) {
                BigDecimal precio = precioMap.get(c.getAsset());
                if (precio != null) {
                    BigDecimal valor = c.getAmount().multiply(precio);
                    valueByAsset.put(c.getAsset(), valor);
                    totalValue = totalValue.add(valor);
                }
            }
            BigDecimal finalTotalValue = totalValue;
            List<WeightByAssetDTO> pesos = valueByAsset.entrySet().stream()
                    .map(e -> new WeightByAssetDTO(
                            e.getKey().getName(), // o getId()
                            e.getValue().divide(finalTotalValue, 8, RoundingMode.HALF_UP).doubleValue()
                    ))
                    .toList();
            result.add(new PortfolioEvolutionDTO(
                    sortedDate,
                    totalValue.doubleValue(),
                    pesos
            ));
        }
        return result;

    }

    private Portfolio findPortfolio(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Portafolio no encontrado"));
    }

    private List<AssetQuantity> findAssetQuantity(Portfolio entity) {
        return assetQuantityRepository.findByPortfolio(entity);
    }

    private List<Asset> findAllAsset(List<AssetQuantity> assetQuantities) {
        return assetQuantities.stream()
                .map(AssetQuantity::getAsset)
                .distinct()
                .toList();
    }

    private List<Price> findAssetByDateBetween(List<Asset> assets, LocalDate startDate, LocalDate endDate) {
        return priceRepository.findByAssetInAndDateBetween(assets, startDate, endDate);
    }

}
