package com.abacus.portafolio.evolution.util;


import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.evolution.dto.PortfolioEvolutionDTO;
import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.model.EvolutionUpdaterContext;
import org.apache.commons.compress.utils.Lists;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EvolutionContextFactory {

    public EvolutionContextFactory() {
    }

    public static EvolutionRetrieverContext buildRetrieveContext(long portfolioId, LocalDate operationDate) {
        return EvolutionRetrieverContext.builder()
                .portfolioId(portfolioId)
                .startDate(operationDate)
                .endDate(operationDate)
                .build();
    }

    public static EvolutionCalculatorContext buildEvolutionCalculatorContext(LocalDate date, List<Price> dailyPrices, List<AssetQuantity> assetQuantities) {
        return EvolutionCalculatorContext.builder()
                .date(date)
                .prices(dailyPrices)
                .quantities(assetQuantities)
                .build();
    }

    public static PortfolioEvolutionDTO buildPortfolioEvolutionDTO(LocalDate date, EvolutionCalculatorContext context) {
        return new PortfolioEvolutionDTO(
                date,
                context.getTotalAsset(),
                context.getWeightByAsset(), null);
    }

    public static EvolutionUpdaterContext buildUpdaterContext(EvolutionRetrieverContext erc,
                                                               LocalDate operationDay,
                                                               Asset assetSeller,
                                                               Asset assetBuyer,
                                                               BigDecimal unitSeller,
                                                               BigDecimal unitBuyer) {
        return EvolutionUpdaterContext.builder()
                .portfolioId(erc.getPortfolioId())
                .operationDay(operationDay)
                .priceByAsset(erc.getPriceByAsset())
                .quantitiesByAsset(erc.getQuantitiesByAsset())
                .assetSeller(assetSeller)
                .assetBuyer(assetBuyer)
                .unitsToSell(unitSeller)
                .unitsToBuy(unitBuyer)
                .response(new PortfolioEvolutionDTO(operationDay,null,null, Lists.newArrayList()))
                .build();
    }
}

