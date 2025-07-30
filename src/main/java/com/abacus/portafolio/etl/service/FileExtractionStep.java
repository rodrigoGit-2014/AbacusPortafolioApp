package com.abacus.portafolio.etl.service;

import com.abacus.portafolio.etl.model.EtlContext;

public interface FileExtractionStep {
    void execute(EtlContext context);
}
