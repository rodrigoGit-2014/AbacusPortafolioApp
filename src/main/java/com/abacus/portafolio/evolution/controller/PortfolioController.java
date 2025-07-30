package com.abacus.portafolio.evolution.controller;

import com.abacus.portafolio.evolution.dto.PortfolioEvolutionDTO;
import com.abacus.portafolio.evolution.service.PortfolioEvolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/portafolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioEvolutionService portfolioEvolutionService;

    @GetMapping("/{id}/evolution")
    public List<PortfolioEvolutionDTO> getEvolucion(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        return portfolioEvolutionService.calculateEvolution(id, fechaInicio, fechaFin);
    }
}
