package com.abacus.portafolio.evolution.service.impl.updater;

import com.abacus.portafolio.etl.config.AppConfig;
import com.abacus.portafolio.etl.entities.Asset;
import com.abacus.portafolio.etl.entities.AssetQuantity;
import com.abacus.portafolio.etl.entities.AssetWeight;
import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.AssetWeightRepository;
import com.abacus.portafolio.evolution.model.EvolutionUpdaterContext;
import com.abacus.portafolio.evolution.service.IEvolutionUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeightsUpdaterCalculator implements IEvolutionUpdater {
    private final AssetWeightRepository assetWeightRepository;
    private final AppConfig appConfig;
    private static final LocalDate MAX_VALID_TO = LocalDate.of(9999, 1, 1);

    @Override
    public void update(EvolutionUpdaterContext context) {
        List<AssetWeight> currentWeights = findValidWeightsForDate(context);

        for (AssetWeight oldWeight : currentWeights) {
            Asset asset = oldWeight.getAsset();
            Price price = context.getPriceByAsset().get(asset);
            AssetQuantity quantity = context.getQuantitiesByAsset().get(asset);

            validateInputs(asset, price, quantity);

            closeOldWeightRecord(oldWeight, context.getOperationDay());
            createAndSaveNewWeightRecord(oldWeight, price, quantity, context);
        }
    }

    private List<AssetWeight> findValidWeightsForDate(EvolutionUpdaterContext context) {
        return assetWeightRepository.findByPortfolioIdAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                context.getPortfolioId(),
                context.getOperationDay(),
                context.getOperationDay()
        );
    }

    private void validateInputs(Asset asset, Price price, AssetQuantity quantity) {
        if (price == null || quantity == null) {
            throw new IllegalArgumentException("Missing price or quantity for asset: " + asset.getName());
        }
    }

    private void closeOldWeightRecord(AssetWeight oldWeight, LocalDate operationDate) {
        oldWeight.setValidTo(operationDate.minusDays(1));
        assetWeightRepository.save(oldWeight);
    }

    private void createAndSaveNewWeightRecord(AssetWeight oldWeight, Price price, AssetQuantity quantity, EvolutionUpdaterContext context) {
        BigDecimal assetValue = quantity.getQuantity().multiply(price.getPriceAmount());
        BigDecimal weight = assetValue.divide(context.getPortfolioValue(), appConfig.getScale(), BigDecimal.ROUND_HALF_UP);

        AssetWeight newWeight = AssetWeight.builder()
                .portfolio(oldWeight.getPortfolio())
                .asset(oldWeight.getAsset())
                .weight(weight)
                .validFrom(context.getOperationDay())
                .validTo(MAX_VALID_TO)
                .build();

        assetWeightRepository.save(newWeight);
    }
}
