package com.neighbus.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.JwtTokenProvider;

@RestController
@RequestMapping("/api/mobile/account")
public class AccountMobileRestController {


    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    public AccountMobileRestController(
		AccountService accountService,
        AuthenticationManager authenticationManager,
        AccountMapper accountMapper,
        JwtTokenProvider jwtTokenProvider
    ) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

	@PostMapping(value="/insertSignup")
	public Map<String, Object> insertSignup(
			@RequestBody AccountDTO accountDTO
			){
		System.out.println("AccountRestController - insertSignup");
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			int status = accountService.insertSignup(accountDTO);
			map.put("status", status);
		}catch(Exception e) {
			System.out.println(e);
			map.put("status", -1);
		}
		return map;
	}
	

    @PostMapping("/findAccount")
    public Map<String, Object> findAccount(
    	@RequestBody Map<String, String> request
    ) {
		System.out.println("AccountRestController - findAccount");
        String email = request.get("email");

        String username = accountService.findUsernameByEmail(email);

        Map<String, Object> result = new HashMap<>();
        if (username == null) {
            result.put("status", 0);
        } else {
            result.put("status", 1);
            result.put("username", username);
        }
        return result;
    }
    
	@PostMapping("/findAccountByEmail")
	public Map<String,Object> findAccountByEmail(
		@ModelAttribute AccountFindDTO accountFindDTO
	) {
		System.out.println("AccountRestController - findAccountByEmail");
		return accountService.findAccountByEmail(accountFindDTO);
	}
	
	@PostMapping("/sendTempPassword")
	public Map<String,Object> sendTempPassword(
		@RequestBody Map<String, String> request
	) {
		System.out.println("AccountRestController - sendTempPassword");
	    Map<String, Object> response = new HashMap<>();
	    try {
	        accountService.sendTempPassword(request.get("email"));
	        response.put("success", true);
	        response.put("message", "임시 비밀번호가 발송되었습니다.");
	    } catch (Exception e) {
	        response.put("success", false);
	        response.put("message", "처리 중 오류가 발생했습니다.");
	    }
	    return response;
	}

	@PostMapping("/findAccountByPhone")
	public Map<String,Object> findAccountByPhone(
		@ModelAttribute AccountFindDTO accountFindDTO
	) {
		System.out.println("AccountRestController - findAccountByPhone");
		return accountService.findAccountByPhone(accountFindDTO);
	}
	
	@PostMapping("/updatePasswordByPhone")
	public Map<String,Object> updatePasswordByPhone(
		@RequestBody Map<String, String> request
	) {
	    System.out.println("AccountRestController - updatePasswordByPhone");
	    String phone = request.get("phone");
	    Map<String, Object> response = new HashMap<>();
	    try {
	        accountService.updatePasswordByPhone(phone);
	        response.put("success", true);
	        response.put("message", "임시 비밀번호가 발송되었습니다.");
	    } catch (Exception e) {
	        response.put("success", false);
	        response.put("message", "처리 중 오류가 발생했습니다.");
	    }
	    return response;
	}
	
	@PostMapping("/mobileLogin")
	public ResponseEntity<Map<String, Object>> mobileLogin(@RequestBody Map<String, String> loginData) {
		System.out.println("AccountMobileRestController - mobileLogin");
	    Map<String, Object> response = new HashMap<>();

	    try {
	        String username = loginData.get("username");
	        String password = loginData.get("password");

	        // 인증 시도
	        Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(username, password)
	        );
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        
	        // 토큰 생성
	        String token = jwtTokenProvider.createToken(authentication);
	        // 사용자 정보 가져오기 (Principal에서 정보 추출)
	        Object principal = authentication.getPrincipal();
	        
	        // 앱으로 보낼 응답 구성 (앱의 기대형식에 맞춤)
	        response.put("status", 1);          // 성공 시 1
	        response.put("message", "로그인 성공");
	        response.put("token", token);       // 토큰도 같이 줌 (나중에 API 호출시 필요)
	        response.put("user", principal);    // 유저 객체 (id, name 등 포함)

	        return ResponseEntity.ok(response);

	    } catch (AuthenticationException e) {
	    	System.out.println(e);
	        // 실패 시 응답 구성
	        response.put("status", 0);          // 실패 시 0
	        response.put("message", "아이디 또는 비밀번호를 확인해주세요.");
	        
	        // 200 OK로 보내되 status로 구분하거나, 401로 보내도 됨
	        return ResponseEntity.ok(response); 
	    }
	}
	
	@GetMapping("/getRegions")
	public ResponseEntity<Map<String, Object>> getRegions(
	) {
		System.out.println("AccountMobileRestController - getRegions");
	    Map<String, Object> response = new HashMap<>();
	    try {
	        List<Map<String, Object>> provinceList = accountService.getProvince();
	        List<Map<String, Object>> regionList = accountService.getCity();
	        
	        response.put("status", 1);
	        response.put("provinceList", provinceList);
	        response.put("regionList", regionList);
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        response.put("status", 0);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
}
