package com.abacus.portafolio.etl.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetInvestment {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Asset asset;

    @ManyToOne
    private Portfolio portfolio;

    private BigDecimal amount; // c_{i,t}, initially c_{i,0}
}
