package com.abacus.portafolio.operation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PortfolioOperationDTO {
    private LocalDate day;
    private Transaction seller;
    private Transaction buyer;

    @Data
    public static class Transaction {
        private String asset;
        private BigDecimal amount;
    }
}
