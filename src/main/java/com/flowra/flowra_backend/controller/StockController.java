package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.StockDTO;
import com.flowra.flowra_backend.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockDTO.Response>> getAllStock() {
        return ResponseEntity.ok(stockService.getAllStock());
    }

    @GetMapping("/low")
    public ResponseEntity<List<StockDTO.Response>> getLowStock() {
        return ResponseEntity.ok(stockService.getLowStock());
    }

    @GetMapping("/{flowerId}")
    public ResponseEntity<StockDTO.Response> getStockByFlowerId(@PathVariable Long flowerId) {
        return ResponseEntity.ok(stockService.getStockByFlowerId(flowerId));
    }

    @PutMapping("/{flowerId}/threshold")
    public ResponseEntity<StockDTO.Response> updateThreshold(
            @PathVariable Long flowerId,
            @RequestBody StockDTO.UpdateThresholdRequest request) {
        return ResponseEntity.ok(stockService.updateMinThreshold(flowerId, request.getMinThreshold()));
    }
}
