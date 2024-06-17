package com.team2final.minglecrm.auth.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team2final.minglecrm.auth.infrastructure.JwtProvider;
import com.team2final.minglecrm.common.exception.ResultResponse;
import com.team2final.minglecrm.employee.dto.request.SignInCheckRequest;
import com.team2final.minglecrm.employee.dto.request.SignInEmailAuthRequest;
import com.team2final.minglecrm.employee.dto.request.SignInRequest;
import com.team2final.minglecrm.employee.dto.response.AccessTokenResponse;
import com.team2final.minglecrm.employee.dto.response.SignInEmailAuthResponse;
import com.team2final.minglecrm.employee.dto.response.SignInValidResponse;
import com.team2final.minglecrm.employee.dto.response.TokenResponse;
import com.team2final.minglecrm.employee.service.EmployeeService;
import com.team2final.minglecrm.service.email.EmailAuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
public class LoginApi {

    private final JwtProvider jwtProvider;
    private final EmployeeService employeeService;
    private final EmailAuthService emailAuthService;

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null; // 또는 적절한 예외 처리
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("rtk")) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private Cookie createRefreshTokenCookie(TokenResponse tokenResponse) {
        Cookie cookie = new Cookie("rtk", tokenResponse.getRtk());
        cookie.setHttpOnly(true);
//            cookie.setSecure(true); // Https 사용 시
        cookie.setPath("/");

        Date now = new Date();
        int age = (int) (tokenResponse.getRtkExpiration().getTime() - now.getTime()) / 1000;
        cookie.setMaxAge(age);

        return cookie;
    }

    private Cookie createDeleteCookie() {
        // 쿠키 삭제
        Cookie cookie = new Cookie("rtk", null); // 쿠키 이름과 값을 설정
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 만료 시간 설정 (0으로 설정하면 쿠키가 삭제됨)
        return cookie;
    }

    @GetMapping("/api/v1/auth/renew")
    public ResultResponse<AccessTokenResponse> reNew(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {

        String rtk = getRefreshTokenFromCookie(request);

        if (rtk == null) {
            return new ResultResponse<>(HttpStatus.UNAUTHORIZED.value(), "fail", null);
        }

        TokenResponse tokenResponse = jwtProvider.renewToken(rtk);
        Cookie cookie = createRefreshTokenCookie(tokenResponse);
        response.addCookie(cookie);
        System.out.println(cookie);

        return new ResultResponse<>(HttpStatus.OK.value(), "success", AccessTokenResponse.builder()
                .atk(tokenResponse.getAtk())
                .atkExpiration(tokenResponse.getAtkExpiration())
                .build());
    }


    @PostMapping("/api/v1/auth/signin/valid")
    public ResponseEntity<SignInValidResponse> signInValid(@RequestBody SignInRequest request) {
        if(employeeService.isValidEmailAndPassword(request)) {
            return ResponseEntity.status(HttpStatus.OK).body(new SignInValidResponse("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new SignInValidResponse("failed", false));
        }
    }

    @PostMapping("/api/v1/auth/signin/email")
    public ResponseEntity<SignInEmailAuthResponse> SignInEmailAuth(@RequestBody SignInEmailAuthRequest request) throws MessagingException {
        try {
            emailAuthService.SendSignInAuthEmail(request.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(new SignInEmailAuthResponse("failed", false));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new SignInEmailAuthResponse("success", true));
    }

    @PostMapping("/api/v1/auth/signin")
    public ResultResponse<TokenResponse> checkAuthCode(@RequestBody SignInCheckRequest request) throws JsonProcessingException {
        Boolean isValidAuthCode = emailAuthService.AuthEmailCheck(request.getAuthCode(), request.getEmail());
        if (!isValidAuthCode) {
            return new ResultResponse<>(HttpStatus.BAD_REQUEST.value(), "fail", null);
        }
        TokenResponse tokenResponse = jwtProvider.createTokensBySignIn(request.getEmail());
        return new ResultResponse<>(HttpStatus.OK.value(), "success", tokenResponse);
    }

    @PostMapping("/api/v1/auth/signintest")
    public ResultResponse<AccessTokenResponse> singInTest(@RequestBody SignInRequest request, HttpServletResponse response) throws JsonProcessingException {
        if(employeeService.isValidEmailAndPassword(request)) {
            TokenResponse tokenResponse = jwtProvider.createTokensBySignIn(request.getEmail());

            Cookie cookie = createRefreshTokenCookie(tokenResponse);
            response.addCookie(cookie);

            return new ResultResponse<>(HttpStatus.OK.value(), "success", AccessTokenResponse.builder()
                    .atk(tokenResponse.getAtk())
                    .atkExpiration(tokenResponse.getAtkExpiration())
                    .build());
        } else {
            return new ResultResponse<>(HttpStatus.BAD_REQUEST.value(), "fail", null);
        }
    }


    @GetMapping("/api/v1/auth/logout")
    public ResultResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {

        String rtk = getRefreshTokenFromCookie(request);

        if (rtk == null) {
            return new ResultResponse<>(HttpStatus.BAD_REQUEST.value(), "fail", null);
        }

        response.addCookie(createDeleteCookie());

        employeeService.logout(rtk);

        return new ResultResponse<>(HttpStatus.OK.value(), "success", null);
    }
}