package com.abacus.portafolio.operation.controller;

import com.abacus.portafolio.evolution.dto.PortfolioEvolutionDTO;
import com.abacus.portafolio.operation.dto.PortfolioOperationDTO;
import com.abacus.portafolio.operation.service.PortfolioOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioOperationController {
    private final PortfolioOperationService portfolioOperationService;

    @PostMapping("/{portfolioId}/operation")
    public ResponseEntity<?> processOperation(@PathVariable Long portfolioId, @RequestBody PortfolioOperationDTO request) {
        log.info("Portfolio operation processing request {}", request);
        PortfolioEvolutionDTO response = portfolioOperationService.process(portfolioId, request);
        return ResponseEntity.ok().body(response);
    }
}
