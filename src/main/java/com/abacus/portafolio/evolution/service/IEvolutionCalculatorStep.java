package com.abacus.portafolio.evolution.service;

import com.abacus.portafolio.evolution.model.EvolutionCalculatorContext;
import com.abacus.portafolio.evolution.model.EvolutionRetrieverContext;

public interface IEvolutionCalculatorStep {
    void apply(EvolutionCalculatorContext context);
}
