package com.abacus.portafolio.evolution.dto;

import java.time.LocalDate;
import java.util.List;

public record PortfolioEvolutionDTO(LocalDate fecha,
                                    double valorTotal,
                                    List<WeightByAssetDTO> pesos) {
}
