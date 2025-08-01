package com.abacus.portafolio.evolution.service.impl.retriever;

import com.abacus.portafolio.etl.entities.Portfolio;
import com.abacus.portafolio.etl.repository.PortfolioRepository;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;
import com.abacus.portafolio.evolution.service.IEvolutionRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioRetrieverImpl implements IEvolutionRetriever {
    private final PortfolioRepository portfolioRepository;

    @Override
    public void update(EvolutionRetrieverContext context) {
        Portfolio portfolio = portfolioRepository.findById(context.getPortfolioId())
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        context.setPortfolio(portfolio);
    }
}
