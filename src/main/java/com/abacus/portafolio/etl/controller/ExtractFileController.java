package com.abacus.portafolio.etl.controller;

import com.abacus.portafolio.etl.exceptions.DuplicateFileException;
import com.abacus.portafolio.etl.exceptions.InvalidFileFormatException;
import com.abacus.portafolio.etl.service.ExtractFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        try {
            extractFileService.importFromExcel(file);
            return ResponseEntity.ok("Importación completada");
        } catch (DuplicateFileException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (InvalidFileFormatException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
