package com.abacus.portafolio.etl.model;

import lombok.*;
import org.apache.poi.ss.usermodel.Workbook;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtlContext {
    private Workbook workbook;
}
