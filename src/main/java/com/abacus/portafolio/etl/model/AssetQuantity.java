package com.abacus.portafolio.etl.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class AssetQuantity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Asset asset;

    @ManyToOne
    private Portfolio portfolio;

    private BigDecimal amount; // c_{i,t}, initially c_{i,0}
}
