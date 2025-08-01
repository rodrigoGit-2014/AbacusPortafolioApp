package com.abacus.portafolio.evolution.service.impl.calculator;

import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import com.abacus.portafolio.evolution.service.IEvolutionCalculatorStep;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(0)
public class PriceMapperStep implements IEvolutionCalculatorStep {
    @Override
    public void apply(EvolutionCalculatorContext context) {
        List<Price> prices = context.getPrices();
        context.setMapPricesByAsset(prices.stream().collect(Collectors.toMap(Price::getAsset, Price::getPriceAmount)));
    }
}
