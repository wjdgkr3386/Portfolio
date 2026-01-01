package com.neighbus.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger; // ★ Logger import 추가
import org.slf4j.LoggerFactory; // ★ Logger import 추가
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neighbus.Util;

@Transactional
@Service
public class AccountServiceimpl implements AccountService, UserDetailsService {
	
	private static final Logger log = LoggerFactory.getLogger(AccountServiceimpl.class); // ★ Logger 객체 생성
	
	private final AccountMapper accountMapper;
	private final PasswordEncoder passwordEncoder;
	private final JavaMailSender mailSender;
	private final SmsService SmsService;
	
	public AccountServiceimpl(AccountMapper accountMapper, JavaMailSender mailSender, SmsService SmsService) {
		this.accountMapper = accountMapper;
		this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		this.mailSender = mailSender;
		this.SmsService = SmsService;
	}
	
	//비밀번호 암호화해서 회원가입
	@Override
	public int insertSignup(AccountDTO accountDTO) {
		System.out.println("AccountServiceimpl - insertSignup");
		
		if(accountMapper.checkUsername(accountDTO)>0) {
			System.out.println(2);
			return -2;
		}else if(accountMapper.checkPhone(accountDTO)>0){
			System.out.println(3);
			return -3;
		}else if(accountMapper.checkEmail(accountDTO)>0) {
			System.out.println(4);
			return -4;
		}else {
			System.out.println(5);
			accountDTO.setUserUuid(UUID.randomUUID().toString());
			accountDTO.setPassword(passwordEncoder.encode(accountDTO.getPassword()));
			// ★★★ 여기에 추가: 등급을 1(기본값)로 설정 ★★★
	        accountDTO.setGrade(1);
	        // 기본 프로필 이미지 설정
	        accountDTO.setImage("/img/profile/default-profile.png");
	        System.out.println(1111111);
			accountMapper.insertSignup(accountDTO);
			return 1;
		}
	}

	@Override
	public List<Map<String, Object>> getProvince() {
		return accountMapper.getProvince();
	}

	@Override
	public List<Map<String, Object>> getCity() {
		return accountMapper.getCity();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AccountDTO accountDTO = accountMapper.getUser(username);
        if (accountDTO == null) {
            throw new UsernameNotFoundException(username);
        }
		
		// ★ 추가된 디버깅 로직: DB에서 로드된 grade 값을 로그로 출력합니다. ★
		log.info("User details loaded. Username: {}, Grade: {}", accountDTO.getUsername(), accountDTO.getGrade());
        
		return accountDTO;
	}
	
	//메일 보내기
	@Override
	public Map<String,Object> findAccountByEmail(AccountFindDTO accountFindDTO) {
		System.out.println("AccountServiceimpl - findAccountByEmail");
		Map<String,Object> map = new HashMap<String, Object>();
		String code="";
		int status = accountMapper.findAccountByEmail(accountFindDTO);
		if(status==1) {
			code = Util.generate6DigitCode();
			Util.sendVerificationCode(accountFindDTO.getEmail(), "Neighbus 계정 찾기", "인증번호 : "+code, mailSender);
			map.put("code", code);
		}
		map.put("status", status);
		return map;
	}

	@Override
	public void sendTempPassword(String email) {
		System.out.println("AccountServiceimpl - sendTempPassword");
		String newPassword = Util.rCode(8);
		updatePassword(passwordEncoder.encode(newPassword), email);
		Util.sendVerificationCode(email, "Neighbus 임시 비밀번호", "새로운 비밀번호: "+newPassword, mailSender);
	}
	
	@Override
	public void updatePassword(String password, String email) {
		System.out.println("AccountServiceimpl - updatePassword");
		accountMapper.updatePassword(password, email);
	}

	@Override
    public String findUsernameByEmail(String email) {
		System.out.println("AccountServiceimpl - findUsernameByEmail");
        return accountMapper.findUsernameByEmail(email);
    }

	@Override
	public Map<String,Object> findAccountByPhone(AccountFindDTO accountFindDTO){
		System.out.println("AccountServiceimpl - findAccountByPhone");
		Map<String,Object> map = new HashMap<String, Object>();
		String code = "";
		System.out.println(accountFindDTO.getUsername());
		System.out.println(accountFindDTO.getPhone());
		//핸드폰 번호가 등록되어있는지 확인
		int status = accountMapper.findAccountByPhone(accountFindDTO);
		if(status==1) {
			code = Util.generate6DigitCode();
			//핸드폰 번호로 랜덤 코드 보내기
			sendTempPasswordByPhone(accountFindDTO.getPhone(), "[Neighbus] 인증번호는 [" + code + "] 입니다.");
			map.put("code", code);
		}
		map.put("status", status);
		return map;
	}

	@Override
	public void sendTempPasswordByPhone(String phone, String sendMessage) {
		System.out.println("AccountServiceimpl - sendTempPassword");
		SmsService.sendVerificationSms(phone.replace("-", "").replace(".", ""), sendMessage);
	}
	
	@Override
	public void updatePasswordByPhone(String phone) {
		//비밀번호 생성 및 암호화해서 저장하기
		String newPassword = Util.rCode(8);
		String email = accountMapper.getEmailByPhone(phone);
		updatePassword(passwordEncoder.encode(newPassword), email);
		
		//이메일로 보내기
		this.sendTempPasswordByPhone(
				phone.replace("-", "").replace(".", ""),
				"[Neighbus] 새로운 비밀번호: "+newPassword + "\n로그인 후 마이페이지에서 비밀번호를 수정해주세요.");
	}
	
	@Override
	public int updatePwd(Map<String, Object> map) {
		String password = (String) map.get("password");
		String myPassword = (String) map.get("myPassword");
		String currentPassword = (String) map.get("currentPassword");
		boolean isPasswordMatch = passwordEncoder.matches(currentPassword, myPassword);
		if(!isPasswordMatch) {
			return 0;
		}
		map.put("password", passwordEncoder.encode((String) map.get("password")));
		accountMapper.updatePwd(map);
		return 1;
	}
	
	public void updateGrade(AccountDTO accountDTO) {
		accountMapper.updateGrade(accountDTO.getId(), 2);
		AccountDTO newAccountDTO = accountMapper.getUser(accountDTO.getUsername());
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    Authentication newAuth = new UsernamePasswordAuthenticationToken(
	            newAccountDTO, auth.getCredentials(), auth.getAuthorities());
	    SecurityContextHolder.getContext().setAuthentication(newAuth);
	}
}