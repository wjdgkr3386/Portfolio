package com.neighbus.account;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/account")
public class AccountController {
	@Autowired
	private final AccountService accountService;
	@Autowired
	private final AccountMapper accountMapper;

	public AccountController(AccountService accountService, AccountMapper accountMapper) {
		super();
		this.accountService = accountService;
		this.accountMapper = accountMapper;
	}

	@GetMapping(value={"/",""})
	public String redirectToLogin() {
		System.out.println("AccountController - redirectToLogin");
		return "redirect:/account/login";
	}
	
	@GetMapping(value="/login")
	public String loginForm(
	) {
		System.out.println("AccountController - loginForm");
		// ★ 로그인 페이지 접속 시에만 SecurityContext를 지웁니다 ★
		// 로그인 성공 후 리다이렉트에서는 SecurityContext를 유지해야 합니다
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal == null || "anonymousUser".equals(principal)) {
			SecurityContextHolder.clearContext();
		}
		return "account/login";
	}
	
	@GetMapping(value="/signup")
	public String signupForm(
		Model model
	) {
		System.out.println("AccountController - signupForm");
		SecurityContextHolder.clearContext();
	    
	    //DB에서 대한민국 지역 가져오기
		List<Map<String, Object>> provinceList = accountService.getProvince();
		List<Map<String, Object>> regionList = accountService.getCity();
		model.addAttribute("provinceList", provinceList);
		model.addAttribute("regionList", regionList);
		return "account/signup";
	}

	@GetMapping(value="/find")
	public String signupForm(
	) {
		System.out.println("AccountController - find");
		SecurityContextHolder.clearContext();
		return "account/find";
	}
	
	
	@PostMapping("/updateSocialInfo")
	public String updateSocialInfo(@AuthenticationPrincipal AccountDTO accountDTO,
	                               @RequestParam("phone") String phone,
	                               @RequestParam("birth") String birth,
	                               @RequestParam("sex") String sex) {

	    // 1. DTO 정보 수정
	    accountDTO.setPhone(phone);
	    accountDTO.setBirth(birth);
	    accountDTO.setSex(sex);

	    // 2. DB 정보 수정 (영구 저장)
	    accountMapper.updateSocialInfo(accountDTO);

	    // ★ 3. DB에서 최신 정보 다시 조회 (핵심 해결책) ★
	    // 업데이트된 내용 + 기존 이미지(img) 등 모든 정보를 갱신된 상태로 가져옴
	    // (Mapper에 ID나 Email로 조회하는 메서드를 사용하세요)
	    AccountDTO newAccountDTO = accountMapper.getUser(accountDTO.getUsername()); 

	    // 4. 세션 강제 업데이트
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

	    Authentication newAuth = new UsernamePasswordAuthenticationToken(
	            newAccountDTO,         // ★ DB에서 다시 조회한 완전한 객체 사용
	            auth.getCredentials(),
	            auth.getAuthorities()
	    );

	    SecurityContextHolder.getContext().setAuthentication(newAuth);

	    return "redirect:/";
	}
	
	@PostMapping("/phoneVerification")
	@ResponseBody
	public Map<String, Object> phoneVerification(
		@AuthenticationPrincipal AccountDTO accountDTO
	) {
	    AccountFindDTO findDTO = new AccountFindDTO();
	    findDTO.setPhone(accountDTO.getPhone());
	    findDTO.setUsername(accountDTO.getUsername());
	    return accountService.findAccountByPhone(findDTO);
	}
	
	@PostMapping("/updateGrade")
	public String updateGrade(
		@AuthenticationPrincipal AccountDTO accountDTO
	) {
		accountService.updateGrade(accountDTO);
		return "redirect:/mypage";
	}
}
