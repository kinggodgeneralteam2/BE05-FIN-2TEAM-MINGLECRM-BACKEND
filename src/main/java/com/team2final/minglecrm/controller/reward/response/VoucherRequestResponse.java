package com.team2final.minglecrm.controller.reward.response;

import com.team2final.minglecrm.entity.reward.Voucher;
import com.team2final.minglecrm.entity.reward.VoucherHistory;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class VoucherRequestResponse {
    private final Long customerId;
    private final Long employeeId;
    private final LocalDateTime requestDate;
    private final String voucherCode;

    public static VoucherRequestResponse of(VoucherHistory voucherHistory) {
        return new VoucherRequestResponse(
                voucherHistory.getCustomer().getId(),
                voucherHistory.getEmployeeStaff().getId(),
                voucherHistory.getRequestDate(),
                voucherHistory.getVoucher().getVoucherCode()
        );
    }

}