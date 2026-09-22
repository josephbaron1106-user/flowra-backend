package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.dto.PaymentDTO;
import com.flowra.flowra_backend.dto.RazorpayDTO;
import com.flowra.flowra_backend.service.PaymentService;
import com.flowra.flowra_backend.service.RazorpayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayService razorpayService;

    /**
     * Get infused public Razorpay Key ID and company metadata for frontend checkout
     */
    @GetMapping("/key")
    public ResponseEntity<RazorpayDTO.KeyResponse> getRazorpayKey() {
        return ResponseEntity.ok(razorpayService.getKeyDetails());
    }

    /**
     * Create a new Razorpay Order (returns razorpayOrderId, amount in paise, keyId, etc.)
     */
    @PostMapping("/create-order")
    public ResponseEntity<RazorpayDTO.CreateOrderResponse> createRazorpayOrder(
            @RequestBody(required = false) RazorpayDTO.CreateOrderRequest request) {
        if (request == null) {
            request = new RazorpayDTO.CreateOrderRequest();
        }
        return ResponseEntity.ok(razorpayService.createOrder(request));
    }

    /**
     * Verify payment signature and sync with database
     */
    @PostMapping("/verify")
    public ResponseEntity<RazorpayDTO.VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody RazorpayDTO.VerifyPaymentRequest request) {
        return ResponseEntity.ok(razorpayService.verifyPayment(request));
    }

    @PostMapping
    public ResponseEntity<PaymentDTO.Response> recordPayment(@Valid @RequestBody PaymentDTO.Request request) {
        return new ResponseEntity<>(paymentService.recordPayment(request), HttpStatus.CREATED);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentDTO.Response>> getPaymentsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }
}

