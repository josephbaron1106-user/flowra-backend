package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.InventoryDTO;
import com.flowra.flowra_backend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryDTO.Response>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryDTO.Response>> getLowStockInventory() {
        return ResponseEntity.ok(inventoryService.getLowStockInventory());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDTO.Response> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryDTO.Response> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryDTO.UpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(productId, request));
    }
}
