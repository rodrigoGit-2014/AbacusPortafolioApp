package com.abacus.portafolio.evolution.model;

import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Price;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class EvolutionUpdaterContext {
    private long portfolioId;
    private LocalDate operationDate;
    private Map<Asset, Price> priceByAsset;
    private Map<Asset, AssetQuantity> quantitiesByAsset;
    private BigDecimal portfolioValue;
    private List<Asset> assets;
    private Asset assetSeller;
    private Asset assetBuyer;
    private BigDecimal unitsToSell;
    private BigDecimal unitsToBuy;
}
