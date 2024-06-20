package com.team2final.minglecrm.voucher.domain.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team2final.minglecrm.voucher.domain.QVoucherHistory;
import com.team2final.minglecrm.voucher.dto.request.VoucherSearchCondition;
import com.team2final.minglecrm.voucher.dto.response.QVoucherHistoryResponse;
import com.team2final.minglecrm.voucher.dto.response.VoucherHistoryResponse;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class VoucherSearchRepository {

    private final JPAQueryFactory queryFactory;

    public VoucherSearchRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<VoucherHistoryResponse> search(VoucherSearchCondition condition) {
        QVoucherHistory voucherHistory = QVoucherHistory.voucherHistory;

        return queryFactory
                .select(new QVoucherHistoryResponse(
                        voucherHistory.id,
                        voucherHistory.voucher.id,
                        voucherHistory.status,
                        voucherHistory.requestDate,
                        voucherHistory.voucher.createdReason,
                        voucherHistory.confirmDate,
                        voucherHistory.conversionDate,
                        voucherHistory.employeeStaff.id,
                        voucherHistory.employeeStaff.name,
                        voucherHistory.employeeManager.id,
                        voucherHistory.employeeManager.name,
                        voucherHistory.customer.id,
                        voucherHistory.customer.name,
                        voucherHistory.customer.email,
                        voucherHistory.voucher.amount,
                        voucherHistory.rejectedReason,
                        voucherHistory.voucherCode
                ))
                .from(voucherHistory)
                .where(
                        requestDateEq(condition.getRequestDate()),
                        createdReasonContains(condition.getCreatedReason()),
                        confirmDateEq(condition.getConfirmDate()),
                        conversionDateEq(condition.getConversionDate()),
                        creatorNameContains(condition.getCreatorName()),
                        confirmerIdEq(condition.getConfirmerId()),
                        confirmerNameContains(condition.getConfirmerName()),
                        customerNameContains(condition.getCustomerName()),
                        customerEmailContains(condition.getCustomerEmail()),
                        amountEq(condition.getAmount()),
                        rejectedReasonContains(condition.getRejectedReason()),
                        voucherCodeContains(condition.getVoucherCode()),
                        dateBetween(condition.getStartDate(), condition.getEndDate())
                )
                .fetch();
    }

    private BooleanExpression requestDateEq(LocalDateTime requestDate) {
        return requestDate != null ? QVoucherHistory.voucherHistory.requestDate.eq(requestDate) : null;
    }

    private BooleanExpression createdReasonContains(String createdReason) {
        return createdReason != null ? QVoucherHistory.voucherHistory.voucher.createdReason.containsIgnoreCase(createdReason) : null;
    }

    private BooleanExpression confirmDateEq(LocalDateTime confirmDate) {
        return confirmDate != null ? QVoucherHistory.voucherHistory.confirmDate.eq(confirmDate) : null;
    }

    private BooleanExpression conversionDateEq(LocalDateTime conversionDate) {
        return conversionDate != null ? QVoucherHistory.voucherHistory.conversionDate.eq(conversionDate) : null;
    }

    private BooleanExpression creatorNameContains(String creatorName) {
        return creatorName != null ? QVoucherHistory.voucherHistory.employeeStaff.name.containsIgnoreCase(creatorName) : null;
    }

    private BooleanExpression confirmerIdEq(Long confirmerId) {
        return confirmerId != null ? QVoucherHistory.voucherHistory.employeeManager.id.eq(confirmerId) : null;
    }

    private BooleanExpression confirmerNameContains(String confirmerName) {
        return confirmerName != null ? QVoucherHistory.voucherHistory.employeeManager.name.containsIgnoreCase(confirmerName) : null;
    }

    private BooleanExpression customerNameContains(String customerName) {
        return customerName != null ? QVoucherHistory.voucherHistory.customer.name.containsIgnoreCase(customerName) : null;
    }

    private BooleanExpression customerEmailContains(String customerEmail) {
        return customerEmail != null ? QVoucherHistory.voucherHistory.customer.email.containsIgnoreCase(customerEmail) : null;
    }

    private BooleanExpression amountEq(Long amount) {
        return amount != null ? QVoucherHistory.voucherHistory.voucher.amount.eq(amount) : null;
    }

    private BooleanExpression rejectedReasonContains(String rejectedReason) {
        return rejectedReason != null ? QVoucherHistory.voucherHistory.rejectedReason.containsIgnoreCase(rejectedReason) : null;
    }

    private BooleanExpression voucherCodeContains(String voucherCode) {
        return voucherCode != null ? QVoucherHistory.voucherHistory.voucherCode.containsIgnoreCase(voucherCode) : null;
    }

    private BooleanExpression dateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return QVoucherHistory.voucherHistory.confirmDate.between(startDate, endDate);
        } else if (startDate != null) {
            return QVoucherHistory.voucherHistory.confirmDate.goe(startDate);
        } else if (endDate != null) {
            return QVoucherHistory.voucherHistory.confirmDate.loe(endDate);
        } else {
            return null;
        }
    }
}