package com.neighbus.config;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.neighbus.account.AccountDTO;
import com.neighbus.account.AccountMapper;
import com.neighbus.account.AccountService;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AccountService accountService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 정보 추출
        String provider = "google";
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String image = (String) attributes.get("picture");
        String username = provider + "_" + providerId; 

        // 3. DB 조회 (소셜 ID로 조회)
        AccountDTO accountDTO = accountMapper.getUser(username);

        if (accountDTO == null) {
            // [추가] 이메일로 기존 일반 회원 확인
            AccountDTO existEmailUser = accountMapper.findByEmail(email);

            if (existEmailUser != null) {
                System.out.println("★기존 이메일 계정 발견 -> 계정 통합 진행★");
                existEmailUser.setProvider(provider);
                existEmailUser.setProviderId(providerId);
                
                // 기존 계정에 소셜 연동 정보 업데이트
                accountMapper.updateSocialInfo(existEmailUser);
                accountDTO = existEmailUser;
            } else {
                System.out.println("★신규 소셜 회원 -> 가입 진행★");
                accountDTO = new AccountDTO();
                accountDTO.setUsername(username);
                accountDTO.setPassword("1234"); // 임시 비번
                accountDTO.setName(name);
                accountDTO.setNickname(name);
                accountDTO.setEmail(email);
                accountDTO.setProvider(provider);
                accountDTO.setProviderId(providerId);

                // 필수값 설정
                accountDTO.setPhone("01000000000"); 
                accountDTO.setBirth("000000");
                accountDTO.setSex("N");
                accountDTO.setProvince(1);
                accountDTO.setCity(1);
                accountDTO.setAddress("소셜로그인");
                accountDTO.setImage(image == null ? "/img/profile/default-profile.png" : image);
                accountDTO.setUserUuid(UUID.randomUUID().toString());
                accountDTO.setGrade(1);
                accountDTO.setRole("ROLE_USER");

                accountService.insertSignup(accountDTO);
                // 가입 후 생성된 ID를 포함해 다시 조회
                accountDTO = accountMapper.getUser(username);
            }
        } else {
            // 차단 여부 확인
            if (accountDTO.getIsBlocked() != null && accountDTO.getIsBlocked()) {
                throw new OAuth2AuthenticationException("서비스 이용이 제한된 계정입니다.");
            }
            System.out.println("★기존 소셜 회원 -> 로그인 진행★");
        }

        accountDTO.setAttributes(attributes);
        return accountDTO;
    }
}