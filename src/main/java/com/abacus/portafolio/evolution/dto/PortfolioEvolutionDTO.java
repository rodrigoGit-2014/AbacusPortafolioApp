package com.abacus.portafolio.evolution.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PortfolioEvolutionDTO(LocalDate day,
                                    BigDecimal portfolioTotal,
                                    List<WeightByAssetDTO> weights) {
}
