package com.abacus.portafolio.etl.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Price {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    private Asset asset;
    private LocalDate date;
    private BigDecimal priceAmount;


}
