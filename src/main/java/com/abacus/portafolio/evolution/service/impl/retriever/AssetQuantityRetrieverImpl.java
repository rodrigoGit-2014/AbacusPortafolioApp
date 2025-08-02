package com.abacus.portafolio.evolution.service.impl.retriever;

import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.repository.AssetQuantityRepository;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.service.IEvolutionRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Order(2)
public class AssetQuantityRetrieverImpl implements IEvolutionRetriever {
    private final AssetQuantityRepository assetQuantityRepository;

    @Override
    public void update(EvolutionRetrieverContext context) {
        List<AssetQuantity> quantities = assetQuantityRepository
                .findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        context.getPortfolio().getId(),
                        context.getCurrentDate(),
                        context.getCurrentDate());
        context.setAssetQuantities(quantities);
    }
}
