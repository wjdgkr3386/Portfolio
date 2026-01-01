package com.neighbus.mypage;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.account.AccountDTO;
import com.neighbus.account.AccountService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/api/mypage")
@RestController
public class MypageRestController {

	@Autowired
	private AccountService accountService;
	
	@PostMapping("/updatePasswordProc")
	public int updatePasswordApi(
		@AuthenticationPrincipal AccountDTO accountDTO,
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestParam("currentPassword") String currentPassword,
		@RequestParam("password") String password
	){
		System.out.println(accountDTO);
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("id", accountDTO.getId());
		map.put("password", password);
		map.put("currentPassword", currentPassword);
		map.put("myPassword", accountDTO.getPassword());
		int status = accountService.updatePwd(map);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (status==1 && authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
		return status;
	}
}
