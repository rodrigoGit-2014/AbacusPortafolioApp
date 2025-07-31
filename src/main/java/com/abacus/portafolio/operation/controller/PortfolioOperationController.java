package com.abacus.portafolio.operation.controller;

import com.abacus.portafolio.operation.dto.PortfolioOperationDTO;
import com.abacus.portafolio.operation.service.PortfolioOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioOperationController {
    private final PortfolioOperationService portfolioOperationService;

    @PostMapping("/{portfolioId}/operation")
    public ResponseEntity<?> processOperation(@PathVariable Long portfolioId, @RequestBody PortfolioOperationDTO request) {
        portfolioOperationService.process(portfolioId, request);
        return ResponseEntity.ok().build();
    }
}
