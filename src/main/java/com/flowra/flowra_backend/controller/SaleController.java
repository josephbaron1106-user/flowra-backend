package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.SaleDTO;
import com.flowra.flowra_backend.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public ResponseEntity<List<SaleDTO.Response>> getAllSales(
            @RequestParam(required = false) String type) {
        if (type != null && !type.trim().isEmpty()) {
            return ResponseEntity.ok(saleService.getSalesByType(type.trim().toUpperCase()));
        }
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @PostMapping
    public ResponseEntity<SaleDTO.Response> recordSale(@Valid @RequestBody SaleDTO.Request request) {
        return new ResponseEntity<>(saleService.recordSale(request), HttpStatus.CREATED);
    }
}
