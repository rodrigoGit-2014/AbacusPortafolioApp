package com.abacus.portafolio.evolution.model;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Portfolio;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.evolution.dto.WeightByAssetDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class EvolutionRetrieverContext {
    private Long portfolioId;
    private LocalDate startDate;
    private LocalDate endDate;
   // private LocalDate currentDate;

    private Portfolio portfolio;
    private List<AssetQuantity> assetQuantities;
    private List<Price> prices;
    private Map<LocalDate, List<Price>> pricesGroupedByDate;


    private Map<Asset, BigDecimal> assetPrices;
   // private Map<Asset, BigDecimal> totalAmountsByAsset;
    //private BigDecimal portfolioValue;
    //private List<WeightByAssetDTO> weights;

    private Map<Asset, AssetQuantity> quantitiesByAsset;
    private Map<Asset, Price> priceByAsset;
}
