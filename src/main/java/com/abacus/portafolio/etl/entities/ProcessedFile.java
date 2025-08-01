package com.abacus.portafolio.etl.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedFile {
    @Id
    private String checksum;
    private LocalDateTime processedAt;
}
