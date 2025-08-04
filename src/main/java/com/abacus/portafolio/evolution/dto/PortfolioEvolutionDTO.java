package com.abacus.portafolio.evolution.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioEvolutionDTO {
    private LocalDate day;
    private BigDecimal portfolioTotal;
    private List<WeightByAssetDTO> weights;
    private List<AssetOperationDTO> assetOperations;
}

