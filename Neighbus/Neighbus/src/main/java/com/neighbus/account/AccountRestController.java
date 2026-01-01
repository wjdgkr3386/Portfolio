package com.neighbus.account;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AccountRestController {


    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final AuthenticationManager authenticationManager;

    public AccountRestController(AccountService accountService,
                                 AuthenticationManager authenticationManager,
                                 AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.authenticationManager = authenticationManager;
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
	
}
