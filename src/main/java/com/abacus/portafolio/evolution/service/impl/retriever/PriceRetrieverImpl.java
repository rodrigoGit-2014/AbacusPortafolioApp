package com.abacus.portafolio.evolution.service.impl.retriever;

import com.abacus.portafolio.etl.entities.Price;
import com.abacus.portafolio.etl.repository.PriceRepository;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.service.IEvolutionRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Order(1)
public class PriceRetrieverImpl implements IEvolutionRetriever {
    private final PriceRepository priceRepository;

    @Override
    public void update(EvolutionRetrieverContext context) {
        List<Price> prices = priceRepository.findByDateBetween(context.getStartDate(), context.getEndDate());
        context.setPrices(prices);

        Map<LocalDate, List<Price>> grouped = prices.stream().collect(Collectors.groupingBy(Price::getDate));
        context.setPricesGroupedByDate(grouped);
        context.setPriceByAsset(prices.stream()
                .collect(Collectors.toMap(
                        Price::getAsset,
                        price -> price
                )));

    }


}
