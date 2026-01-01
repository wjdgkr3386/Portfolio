package com.neighbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.neighbus.config.CustomAuthenticationFailureHandler;
import com.neighbus.config.CustomAuthenticationSuccessHandler;
import com.neighbus.config.CustomOAuth2UserService;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomOAuth2UserService customOAuth2UserService;

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    
    private final JwtTokenProvider jwtTokenProvider;


    public SecurityConfig(CustomAuthenticationSuccessHandler handler,
    		CustomAuthenticationFailureHandler customAuthenticationFailureHandler,
            CustomOAuth2UserService customOAuth2UserService,
            JwtTokenProvider jwtTokenProvider) {
		this.customAuthenticationSuccessHandler = handler;
		this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
		this.customOAuth2UserService = customOAuth2UserService;
		this.jwtTokenProvider = jwtTokenProvider;
		log.info("========================================");
		log.info("SecurityConfig 생성됨!");
		log.info("CustomAuthenticationSuccessHandler 주입됨: {}", customAuthenticationSuccessHandler != null);
		log.info("========================================");
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        log.info("========================================");
        log.info("SecurityFilterChain 설정 시작!");
        log.info("사용할 핸들러: {}", customAuthenticationSuccessHandler.getClass().getName());
        log.info("========================================");

        http
        	.cors(cors -> {})
            .csrf(csrf -> csrf.ignoringRequestMatchers(
            		"/api/mobile/**",
                    "/insertSignup",
                    "/loginProc", 
                    "/logout", 
                    "/gallery/api/**", 
                    "/api/mypage/**",
                    "/findAccountByEmail", 
                    "/sendTempPassword", 
                    "/findAccount",
                    "/sendTempPasswordByPhoneToEmail",
            		"/updatePasswordByPhone",
                    "/club/**",
                    "/freeboard/**",
                    "/mypage/**",
                    "/api/recruitment/**",
                    "/api/inquiry/**",
                    "/api/admin/**",
                    "/filterRegion",
                    "/ws-stomp/**",
                    "/chat/**", 
                    "/clubSelect", 
                    "/findAccountByPhone", 
                    "/chatbot/**",
                    "/api/notifications/**",
                    "/account/phoneVerification",
                    "/api/main/**",
                    "/account/updateGrade"
            ))
            .authorizeHttpRequests(authorize -> authorize
                // ★ 관리자 전용 경로
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .requestMatchers(
                		"/",
                		"/about",
                		"/club",
                		"/account/**",
                		"/api/mobile/club/getClubs",
                		"/api/mobile/account/mobileLogin",
                		"/api/mobile/account/getRegions",
                		"/api/mobile/account/insertSignup",
                		"/findAccount",
                		"/findAccountByEmail",
                		"/findAccountByPhone",
                		"/sendTempPassword",
                		"/sendTempPasswordByPhoneToEmail",
                		"/updatePasswordByPhone",
                		"/insertSignup",
                		"/filterRegion",
                		"/api/notifications/**",
                		"/chatbot/**",
                		"/ws-stomp/**",
                		"/.well-known/**",
                		"/favicon.ico",
                		"/webjars/**",
                		"/css/**",
                		"/css2/**",
                		"/js/**",
                		"/img/**",
                		"/sys_img/**",
                		"/error"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .formLogin(form -> form
                .loginPage("/account/login")
                .loginProcessingUrl("/loginProc")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
            )
            // 2. ★ 구글 로그인(OAuth2) 추가 설정 ★
            .oauth2Login(oauth2 -> oauth2
            	    .loginPage("/account/login")
            	    .successHandler(customAuthenticationSuccessHandler)
            	    .userInfoEndpoint(userInfo -> userInfo
            	        .userService(customOAuth2UserService) // ★ 이 줄을 꼭 추가해야 함!
            	    )
            	)
            .logout(logout -> logout
                .logoutUrl("/logout")                 
                .logoutSuccessUrl("/account/login")   
                .invalidateHttpSession(true)          
                .deleteCookies("JSESSIONID")          
                .permitAll()
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())  // iframe 허용 (같은 origin만)
        	   );

        return http.build();
    }
}