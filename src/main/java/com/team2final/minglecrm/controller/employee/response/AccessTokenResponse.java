package com.team2final.minglecrm.controller.employee.response;

import jakarta.persistence.Access;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
public class AccessTokenResponse {

    private String atk;
    private Date atkExpiration;

    @Builder
    public AccessTokenResponse(String atk, Date atkExpiration) {
        this.atk = atk;
        this.atkExpiration = atkExpiration;
    }
}
