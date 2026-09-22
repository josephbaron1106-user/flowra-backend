package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.FlowerDTO;
import com.flowra.flowra_backend.service.FlowerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flowers")
@RequiredArgsConstructor
public class FlowerController {

    private final FlowerService flowerService;

    @GetMapping
    public ResponseEntity<List<FlowerDTO.Response>> getAllFlowers() {
        return ResponseEntity.ok(flowerService.getAllFlowers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlowerDTO.Response> getFlowerById(@PathVariable Long id) {
        return ResponseEntity.ok(flowerService.getFlowerById(id));
    }

    @PostMapping
    public ResponseEntity<FlowerDTO.Response> createFlower(@Valid @RequestBody FlowerDTO.Request request) {
        return new ResponseEntity<>(flowerService.createFlower(request), HttpStatus.CREATED);
    }
}
