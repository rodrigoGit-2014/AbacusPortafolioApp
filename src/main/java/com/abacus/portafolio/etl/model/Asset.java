package com.abacus.portafolio.etl.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Asset {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    public Asset(String name) {
        this.name = name;
    }
}
