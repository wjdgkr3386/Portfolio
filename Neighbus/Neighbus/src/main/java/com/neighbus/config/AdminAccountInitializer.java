package com.neighbus.config;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.neighbus.account.AccountDTO;
import com.neighbus.account.AccountMapper;

/**
 * ★ 관리자 계정 자동 생성 클래스 ★
 *
 * 애플리케이션 시작 시 관리자 계정이 없으면 자동으로 생성합니다.
 *
 * 기본 관리자 계정:
 * - username: admin
 * - password: admin123!
 * - grade: 0 (관리자 등급)
 *
 * 보안을 위해 첫 로그인 후 비밀번호를 변경하시기 바랍니다.
 *
 * ★ 비활성화됨 ★
 * DB에 이미 만든 admin 계정을 사용하려면 @Component 주석을 유지하세요.
 * 자동 생성을 원하면 아래 주석을 제거하세요.
 */
// @Component  // ← 이 줄의 주석을 제거하면 자동 생성 활성화
public class AdminAccountInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

	private final AccountMapper accountMapper;
	private final PasswordEncoder passwordEncoder;

	public AdminAccountInitializer(AccountMapper accountMapper, PasswordEncoder passwordEncoder) {
		this.accountMapper = accountMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 관리자 계정이 이미 존재하는지 확인
		AccountDTO existingAdmin = accountMapper.getUser("admin");

		if (existingAdmin == null) {
			log.info("관리자 계정이 존재하지 않습니다. 기본 관리자 계정을 생성합니다...");

			// 관리자 계정 생성
			AccountDTO adminAccount = new AccountDTO();
			adminAccount.setUsername("admin");
			adminAccount.setPassword(passwordEncoder.encode("admin123!"));
			adminAccount.setName("관리자");
			adminAccount.setNickname("Admin");
			adminAccount.setEmail("admin@neighbus.com");
			adminAccount.setPhone("010-0000-0000");
			adminAccount.setGrade(0); // ★ 관리자 등급: 0 ★
			adminAccount.setCity(1); // 기본 지역 ID (필요에 따라 수정)
			adminAccount.setAddress("관리자 주소");
			adminAccount.setBirth("990101");
			adminAccount.setSex("M");
			adminAccount.setUserUuid(UUID.randomUUID().toString());
			adminAccount.setImage("default.jpg");

			try {
				accountMapper.insertSignup(adminAccount);
				log.info("========================================");
				log.info("★ 관리자 계정이 성공적으로 생성되었습니다! ★");
				log.info("Username: admin");
				log.info("Password: admin123!");
				log.info("Grade: 0 (관리자)");
				log.info("보안을 위해 첫 로그인 후 비밀번호를 변경하세요.");
				log.info("========================================");
			} catch (Exception e) {
				log.error("관리자 계정 생성 중 오류가 발생했습니다: {}", e.getMessage(), e);
			}
		} else {
			log.info("관리자 계정이 이미 존재합니다. (Username: admin, Grade: {})", existingAdmin.getGrade());

			// ★ 기존 admin 계정의 grade가 0이 아니면 경고 메시지 출력 ★
			if (existingAdmin.getGrade() != 0) {
				log.warn("경고: 기존 admin 계정의 grade가 {}입니다. 관리자 권한을 받으려면 grade를 0으로 변경하세요.", existingAdmin.getGrade());
				log.warn("SQL: UPDATE users SET grade = 0 WHERE username = 'admin';");
			}
		}
	}
}
