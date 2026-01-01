package com.neighbus.config;

import com.neighbus.account.AccountDTO;
import com.neighbus.account.AccountMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AccountMapper accountMapper;

    public CustomAuthenticationFailureHandler(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String username = request.getParameter("username");
        String errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";

        // 계정이 정지(Locked) 상태인 경우
        if (exception instanceof LockedException) {
            AccountDTO user = accountMapper.getUser(username);
            if (user != null && user.getBlocked_until() != null) {
                // 날짜 포맷팅 (예: 2025-12-31 23:59)
                String date = user.getBlocked_until().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
                errorMessage = "해당 계정은 이용이 제한되었습니다.\n제한 기한: " + date + "까지";
            }
        }

        String encodedMessage = URLEncoder.encode(errorMessage, "UTF-8");
        setDefaultFailureUrl("/account/login?error=true&message=" + encodedMessage);
        
        super.onAuthenticationFailure(request, response, exception);
    }
}