package com.abacus.portafolio.etl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {
    private int scale;
    private InitialInvestments initialInvestments;

    public BigDecimal findInitialInvestmentAmount(String portfolioName) {
        return initialInvestments.findAmountByPortfolioName(portfolioName);
    }

    @Data
    public static class InitialInvestments {
        private List<InitialInvestment> portfolios;

        public BigDecimal findAmountByPortfolioName(String name) {
            return portfolios.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Portfolio not found: " + name))
                    .getAmount();
        }
    }

    @Data
    public static class InitialInvestment {
        private String name;
        private BigDecimal amount;
    }
}
