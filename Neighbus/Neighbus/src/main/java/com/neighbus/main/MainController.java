package com.neighbus.main;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.neighbus.Util;
import com.neighbus.account.AccountDTO;
import com.neighbus.account.AccountMapper;
import com.neighbus.club.ClubMapper;
import com.neighbus.recruitment.RecruitmentMapper;
import com.neighbus.recruitment.RecruitmentService;

@Controller
public class MainController {

	@Autowired
	AccountMapper accountMapper;
	@Autowired
	ClubMapper clubMapper;
	@Autowired
	RecruitmentService recruitmentService;
	@Autowired
	RecruitmentMapper recruitmentMapper;
	
	
	@GetMapping(value="/")
	public String mainForm(
		Model model,
		@AuthenticationPrincipal AccountDTO accountDTO,
		SearchDTO searchDTO
	) {
		System.out.println("MainController - mainForm");
		//대시보드
		model.addAttribute("recruitmentCount", recruitmentMapper.countByRecruitment());
		model.addAttribute("userCount", accountMapper.countUsers());
		model.addAttribute("historyCount", accountMapper.countHistory());
		
		//DB에서 대한민국 지역 가져오기
		List<Map<String, Object>> provinceList = accountMapper.getProvince();
		List<Map<String, Object>> regionList = accountMapper.getCity();
		List<Map<String, Object>> categoryList = clubMapper.getClubCategory();
		model.addAttribute("provinceList", provinceList);
		model.addAttribute("regionList", regionList);
		model.addAttribute("categoryList", categoryList);
	    model.addAttribute("searchDTO", searchDTO);

		model.addAttribute("newClubList", clubMapper.getNewClub(searchDTO));
		model.addAttribute("popularClubList", clubMapper.getPopularClub(searchDTO));
		
		// 통계
		model.addAttribute("activeRecruitments", recruitmentService.findAllRecruitments().size());
		model.addAttribute("totalUsers", accountMapper.countUsers());
		model.addAttribute("totalViews", accountMapper.countViews());

		return "main/main";
	}
	
	// ----------------------------------------------------------------------
	
	/**
	 * 개인정보처리방침 페이지 매핑 추가
	 * 요청 URL: /privacy
	 * 템플릿 경로: src/main/resources/templates/main/privacy
	 */
	@GetMapping("/privacy")
	public String privacyPolicy() {
		System.out.println("MainController - privacyPolicy");
		return "main/privacy";
	}
	@GetMapping("/terms")
	public String termsOfService() {
	    System.out.println("MainController - termsOfService");
	    return "main/terms";
	}
}