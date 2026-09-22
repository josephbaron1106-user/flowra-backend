package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.AdminDashboardDTO;
import com.flowra.flowra_backend.dto.InventoryDTO;
import com.flowra.flowra_backend.dto.OrderDTO;
import com.flowra.flowra_backend.dto.ProductDTO;
import com.flowra.flowra_backend.service.AdminService;
import com.flowra.flowra_backend.service.InventoryService;
import com.flowra.flowra_backend.service.OrderService;
import com.flowra.flowra_backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO.DashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO.Response>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/orders/current")
    public ResponseEntity<List<OrderDTO.Response>> getCurrentOrders() {
        return ResponseEntity.ok(adminService.getCurrentOrders());
    }

    @GetMapping("/orders/history")
    public ResponseEntity<List<OrderDTO.Response>> getOrderHistory() {
        return ResponseEntity.ok(adminService.getOrderHistory());
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderDTO.Response> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderDTO.StatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO.Response>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDTO.Response> createProduct(@Valid @RequestBody ProductDTO.Request request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDTO.Response> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDTO.Request request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<InventoryDTO.Response>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PutMapping("/inventory/{productId}")
    public ResponseEntity<InventoryDTO.Response> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryDTO.UpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(productId, request));
    }
}
