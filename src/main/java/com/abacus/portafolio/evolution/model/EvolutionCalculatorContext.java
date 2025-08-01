package com.abacus.portafolio.evolution.model;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.evolution.dto.PortfolioEvolutionDTO;
import com.abacus.portafolio.evolution.dto.WeightByAssetDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class EvolutionCalculatorContext {
    private Map<LocalDate, List<Price>> pricesByDate;
    public List<AssetQuantity> assetQuantities;
    private Map<Asset, BigDecimal> mapPricesByAsset;
    private PortfolioEvolutionDTO portfolioEvolutionDTO;
    private Map<Asset, BigDecimal> assetByAmount;
    private BigDecimal totalAsset;
    private List<WeightByAssetDTO> weightByAsset;

    private LocalDate date;
    private List<Price> prices;
    private List<AssetQuantity> quantities;
}
