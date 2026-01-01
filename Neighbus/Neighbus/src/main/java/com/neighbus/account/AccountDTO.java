package com.neighbus.account;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class AccountDTO implements UserDetails, OAuth2User {

    private int id;                     // 유저 고유 id
    private String name;                // 이름
    private String username;            // 로그인 아이디
    private String password;            // 비밀번호
    private int province;               // FK: 도시 ID
    private int city;                   // FK: 지역 ID
    private String address;             // 상세 주소
    private String phone;               // 전화번호
    private String email;               // 이메일
    private String image;               // 프로필 사진 경로
    private int grade;                  // 등급
    private String birth;               // 생년월일 (YYMMDD)
    private String sex;                 // 성별
    private String userUuid;            // UUID
    private String nickname;            // 닉네임
    private LocalDateTime createdAt;    // 생성일
    private String provider;            // google, kakao 등
    private String providerId;          // 소셜 로그인 고유 ID
    private String role;
    private Boolean isBlocked;          // 차단 여부 (1: true, 0: false)
    private LocalDateTime blocked_until;
    
    private Map<String, Object> attributes; // OAuth2 속성

    // OAuth2User 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // UserDetails 구현: 권한 설정
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (this.grade == 0) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }

    // ★ 로그인 차단 핵심 로직 ★
    @Override
    public boolean isAccountNonLocked() {
        // isBlocked가 true(1)이면 false를 반환하여 로그인을 차단함
        return isBlocked == null || !isBlocked;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getProvince() { return province; }
    public void setProvince(int province) { this.province = province; }

    public int getCity() { return city; }
    public void setCity(int city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public String getBirth() { return birth; }
    public void setBirth(String birth) { this.birth = birth; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getUserUuid() { return userUuid; }
    public void setUserUuid(String userUuid) { this.userUuid = userUuid; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getIsBlocked() { return isBlocked; }
    public void setIsBlocked(Boolean isBlocked) { this.isBlocked = isBlocked; }

    public LocalDateTime getBlocked_until() { return blocked_until; }
    public void setBlocked_until(LocalDateTime blocked_until) { this.blocked_until = blocked_until; }

    @Override
    public String toString() {
        return "AccountDTO [id=" + id + ", username=" + username + ", isBlocked=" + isBlocked + "]";
    }
}