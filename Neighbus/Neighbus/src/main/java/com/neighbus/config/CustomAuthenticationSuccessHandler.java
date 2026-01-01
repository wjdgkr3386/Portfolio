package com.neighbus.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.neighbus.account.AccountDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ★ 로그인 성공 후 처리 핸들러 ★
 *
 * 사용자의 권한(grade)에 따라 다른 페이지로 리다이렉트합니다:
 * - grade 0 (관리자): /admin 페이지로 이동
 * - 그 외 (일반 사용자): / 메인 페이지로 이동
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.info("========================================");
        log.info("★★★ CustomAuthenticationSuccessHandler 호출됨! ★★★");
        log.info("========================================");

        // 로그인한 사용자 정보 가져오기
        Object principal = authentication.getPrincipal();

        log.info("Principal 타입: {}", principal.getClass().getName());

        String redirectUrl = "/"; // 기본 리다이렉트 URL

        if (principal instanceof AccountDTO) {
            AccountDTO account = (AccountDTO) principal;
            int grade = account.getGrade();
            String username = account.getUsername();

            log.info("========================================");
            log.info("로그인 성공!");
            log.info("Username: {}", username);
            log.info("Grade: {}", grade);
            log.info("========================================");

            // ★ grade가 0이면 관리자 -> /admin으로 이동 ★
            if (grade == 0) {
                redirectUrl = "/admin";
                log.info(">>> 관리자 로그인 감지! /admin 페이지로 리다이렉트합니다.");
            } else {
                redirectUrl = "/";
                log.info(">>> 일반 사용자 로그인. 메인 페이지로 리다이렉트합니다.");
            }
        } else {
            log.warn("Principal이 AccountDTO 타입이 아닙니다! 타입: {}", principal.getClass().getName());
        }

        log.info("최종 리다이렉트 URL: {}", redirectUrl);
        log.info("========================================");

        // 리다이렉트 실행
        response.sendRedirect(redirectUrl);
    }
}
