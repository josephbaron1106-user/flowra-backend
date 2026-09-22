package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.PurchaseDTO;
import com.flowra.flowra_backend.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public ResponseEntity<List<PurchaseDTO.Response>> getAllPurchases() {
        return ResponseEntity.ok(purchaseService.getAllPurchases());
    }

    @PostMapping
    public ResponseEntity<PurchaseDTO.Response> recordPurchase(@Valid @RequestBody PurchaseDTO.Request request) {
        return new ResponseEntity<>(purchaseService.recordPurchase(request), HttpStatus.CREATED);
    }
}
