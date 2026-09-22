package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.RazorpayDTO;
import com.flowra.flowra_backend.service.RazorpayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment/razorpay")
@RequiredArgsConstructor
public class RazorpayController {

    private final RazorpayService razorpayService;

    @GetMapping("/key")
    public ResponseEntity<RazorpayDTO.KeyResponse> getKey() {
        return ResponseEntity.ok(razorpayService.getKeyDetails());
    }

    @PostMapping("/create-order")
    public ResponseEntity<RazorpayDTO.CreateOrderResponse> createOrder(
            @RequestBody(required = false) RazorpayDTO.CreateOrderRequest request) {
        if (request == null) {
            request = new RazorpayDTO.CreateOrderRequest();
        }
        return ResponseEntity.ok(razorpayService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<RazorpayDTO.VerifyPaymentResponse> verify(
            @Valid @RequestBody RazorpayDTO.VerifyPaymentRequest request) {
        return ResponseEntity.ok(razorpayService.verifyPayment(request));
    }
}
