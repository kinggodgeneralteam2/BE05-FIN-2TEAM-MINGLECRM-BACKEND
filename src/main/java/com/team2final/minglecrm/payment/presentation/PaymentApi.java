package com.team2final.minglecrm.payment.presentation;

import com.team2final.minglecrm.payment.dto.response.PaymentDetailResponse;
import com.team2final.minglecrm.payment.dto.response.PaymentResponse;
import com.team2final.minglecrm.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/{customerId}/payments")
public class PaymentApi {

    private final PaymentService paymentService;

    // 결제 내역 전체 조회
    @GetMapping()
    public ResponseEntity<List<PaymentResponse>> getPaymentList(@PathVariable Long customerId) {
        List<PaymentResponse> paymentDetailResponse = paymentService.findById(customerId);
        return ResponseEntity.ok(paymentDetailResponse);
    }

    // 결제 내역 상세 조회
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponse> getPayment(@PathVariable Long customerId, @PathVariable Long paymentId) {
        PaymentDetailResponse paymentDetailResponse = paymentService.findPaymentById(customerId, paymentId);
        return ResponseEntity.ok(paymentDetailResponse);
    }
}