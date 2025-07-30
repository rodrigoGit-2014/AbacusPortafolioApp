package com.abacus.portafolio.etl.controller;

import com.abacus.portafolio.etl.service.ExtractFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/etl")
public class ExtractFileController {

    @Autowired
    public ExtractFileService extractFileService;

    @GetMapping("/hello")
    public String hello() {
        return "¡Hola desde el módulo ETL!";
    }

    @PostMapping("/import-excel")
    public ResponseEntity<String> importFile(@RequestParam("file") MultipartFile file) {
        extractFileService.importFromExcel(file);
        return ResponseEntity.ok("Importación completada");
    }
}
