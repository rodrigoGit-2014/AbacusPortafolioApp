package com.abacus.portafolio.evolution.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AssetOperationDTO {
    private String assetName;
    private BigDecimal priceAmount;
    private BigDecimal assetAmount;
    private BigDecimal weight;
}
