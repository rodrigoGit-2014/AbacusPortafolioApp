package com.abacus.portafolio.etl.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "initial-values")
@Data
public class InitialPortfolioValuesConfig {
    private List<PortfolioValue> portfolios;

    public BigDecimal findValue(String name){
        return portfolios.stream()
                         .filter(p -> p.getName().equals(name))
                         .findFirst()
                         .get()
                         .getValue();
    }
    @Data
    public static class PortfolioValue {
        private String name;
        private BigDecimal value;
    }
}
