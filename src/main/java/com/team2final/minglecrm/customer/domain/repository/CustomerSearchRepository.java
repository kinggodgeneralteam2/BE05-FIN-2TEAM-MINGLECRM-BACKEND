package com.team2final.minglecrm.customer.domain.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team2final.minglecrm.customer.domain.QCustomer;
import com.team2final.minglecrm.customer.dto.request.CustomerSearchCondition;
import com.team2final.minglecrm.customer.dto.response.CustomerResponse;
import com.team2final.minglecrm.customer.dto.response.QCustomerResponse;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerSearchRepository {

    private final JPAQueryFactory queryFactory;

    public CustomerSearchRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<CustomerResponse> search(CustomerSearchCondition condition, Pageable pageable) {
        QCustomer customer = QCustomer.customer;

        List<CustomerResponse> results = queryFactory
                .select(new QCustomerResponse(
                        customer.id,
                        customer.name,
                        customer.phone,
                        customer.employee.name,
                        customer.grade,
                        customer.gender,
                        customer.birth
                ))
                .from(customer)
                .where(
                        customerNameEq(condition.getCustomerName()),
                        customerGradeEq(condition.getGrade()),
                        genderEq(condition.getGender())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long totalCount = queryFactory
                .select(customer)
                .from(customer)
                .where(
                        customerNameEq(condition.getCustomerName()),
                        customerGradeEq(condition.getGrade()),
                        genderEq(condition.getGender())
                )
                .fetchCount();

        return new PageImpl<>(results, pageable, totalCount);
    }


    private BooleanExpression customerNameEq(String customerName) {
        return customerName != null ? QCustomer.customer.name.contains(customerName) : null;
    }

    private BooleanExpression customerGradeEq(String grade) {
        return grade != null ? QCustomer.customer.grade.eq(grade) : null;
    }

    private BooleanExpression genderEq(String gender) {
        return gender != null ? QCustomer.customer.gender.eq(gender) : null;
    }

}