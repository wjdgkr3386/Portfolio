package com.neighbus.recruitment;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.neighbus.account.AccountDTO;
import com.neighbus.chat.ChatMapper;
import com.neighbus.chat.ChatRoomDTO;

@Controller
@RequestMapping("/recruitment")
public class RecruitmentController {

	private final RecruitmentService recruitmentService;
	@Autowired
    private ChatMapper chatMapper;

	@Value("${google.maps.appkey}")
	private String googleMapsApiKey;

	@Autowired
	public RecruitmentController(RecruitmentService recruitmentService) {
		this.recruitmentService = recruitmentService;
	}

	/**
	 * 모임 목록 페이지 (GET /recruitment)
	 * @param model View에 데이터를 전달할 Model 객체
	 * @return 렌더링할 Thymeleaf 템플릿 이름
	 */
	@GetMapping(value = {"/",""})
	public String showRecruitmentList(Model model, @AuthenticationPrincipal AccountDTO accountDTO) {
		// 서비스에서 전체 모임 목록을 조회합니다.
		List<RecruitmentDTO> recruitmentList = recruitmentService.findAllRecruitments();

		// Model에 "recruitments"라는 이름으로 목록을 추가합니다.
		model.addAttribute("recruitments", recruitmentList);

        List<RecruitmentDTO> myClubsRecruitments;

        if (accountDTO != null) {
            // 1. 로그인한 사용자의 ID로 데이터를 조회
            int userId = accountDTO.getId(); 
            myClubsRecruitments = recruitmentService.getRecruitmentsByMyClubs(userId);
        } else {
            // 2. 비로그인 시 빈 목록
            myClubsRecruitments = Collections.emptyList();
        }

        // 3. Model에 조회한 데이터 목록을 추가
        model.addAttribute("myClubsRecruitments", myClubsRecruitments);

		// resources/templates/recruitment/recruitment.html 파일을 렌더링합니다.
		return "recruitment/recruitment";
	}

	/**
	 * 모임 상세 페이지 (GET /recruitment/{id})
	 */
		@GetMapping("/{id}")
		public String showRecruitmentDetail(@PathVariable("id") int id, Model model,
		                                    @AuthenticationPrincipal AccountDTO accountDTO) {
			RecruitmentDTO recruitment = recruitmentService.findById(id);
	        int currentUserCount = recruitmentService.countMembers(id);

			model.addAttribute("googleMapsApiKey", googleMapsApiKey);
	        model.addAttribute("recruitment", recruitment);
	        model.addAttribute("currentUserCount", currentUserCount);

	        // ---------------------------------------------------------
	        // 🚨 2. [추가] 채팅방 존재 여부 확인 로직
	        // 모집글 ID(int)를 String으로 변환하여 조회
	        ChatRoomDTO existingRoom = chatMapper.findByRoomId(String.valueOf(id));

	        // 방이 있으면 true, 없으면 false
	        boolean chatRoomExists = (existingRoom != null);

	        // 모델에 결과를 담아서 HTML로 보냄
	        model.addAttribute("chatRoomExists", chatRoomExists);
	        // ---------------------------------------------------------

	        // ---------------------------------------------------------
	        // 🚨 3. [추가] 현재 사용자의 가입 여부 확인
	        boolean isJoined = false;
	        if (accountDTO != null) {
	            isJoined = recruitmentService.isMember(id, accountDTO.getId());
	        }
	        model.addAttribute("isJoined", isJoined);
	        // ---------------------------------------------------------

	        return "recruitment/recruitment_detail";
		}

		/**
	     * 새 모임 생성 폼 페이지 (GET /recruitment/new)
	     */
	    @GetMapping("/new")
	    public String showCreateForm(@RequestParam("clubId") int clubId, Model model) { // [수정 1] 파라미터 받기
	        RecruitmentDTO dto = new RecruitmentDTO();
	        dto.setClubId(clubId); // [수정 2] DTO에 동아리 ID 미리 세팅
			model.addAttribute("googleMapsApiKey", googleMapsApiKey);
			
	        model.addAttribute("recruitmentDTO", dto); // (변수명 소문자 권장)
	        return "recruitment/recruitment_form";
	    }

	    /**
	     * 새 모임 생성 처리 (POST /recruitment/new)
	     */
	    @PostMapping("/new")
	    public String createRecruitment(@ModelAttribute RecruitmentDTO recruitmentDTO, 
	                                    @AuthenticationPrincipal AccountDTO accountDTO) {
	        
	        // 1. 작성자 설정
	        recruitmentDTO.setWriter(accountDTO.getId());
	        
	        // 2. 모임 생성 (DB에 저장되고, recruitmentDTO에 ID가 생성됨)
	        recruitmentService.createRecruitment(recruitmentDTO);
	        
	        // ---------------------------------------------
	        // [추가된 로직] 3. 생성자(작성자)를 바로 모임 멤버로 가입시키기
	        // ----------------------------------------------
	        Map<String, Object> joinParams = new HashMap<>();
	        joinParams.put("recruitmentId", recruitmentDTO.getId()); // 방금 생성된 모임 ID
	        joinParams.put("userId", accountDTO.getId());            // 작성자 ID
	        
	        // RestController가 하던 일을 여기서 바로 처리 (서비스 호출)
	        recruitmentService.joinRecruitment(joinParams);
	        // ---------------------------------------------------------

	        // 4. 생성 및 가입 후 해당 모임 상세 페이지로 이동
	        return "redirect:/recruitment/" + recruitmentDTO.getId();
	    }
	
	// 가입한 클럽 모임 리스트
	@GetMapping("/recruitments/my-clubs-page")
    public String showMyClubsPage(@AuthenticationPrincipal AccountDTO accountDTO, Model model) {
        
        List<RecruitmentDTO> myClubsRecruitments;

        if (accountDTO != null) {
            // 1. 로그인한 사용자의 ID로 데이터를 조회
            int userId = accountDTO.getId(); 
            myClubsRecruitments = recruitmentService.getRecruitmentsByMyClubs(userId);
        } else {
            // 2. 비로그인 시 빈 목록
            myClubsRecruitments = Collections.emptyList();
        }

        // 3. Model에 조회한 데이터 목록을 추가
        model.addAttribute("recruitmentList", myClubsRecruitments);

        // 4. "recruitments/myClubsPage" 이름의 HTML 템플릿(JSP/Thymeleaf) 파일로 이동
        return "recruitment/myClubsPage";
    }
	
	
	//날짜별 모임
	@GetMapping("/recruitments/api")
	@ResponseBody
	public List<RecruitmentDTO> getRecruitments(
	    @RequestParam("clubId") int clubId,
	    @RequestParam("date") String date // 자바스크립트가 보내준 날짜
	) {
	    return recruitmentService.getRecruitmentsByClubAndDate(clubId, date);
	}
	
	@GetMapping("myRecruitments")
	private String viewmyRecruitments(Model model, @AuthenticationPrincipal AccountDTO accountDTO) {
		
        List<RecruitmentDTO> recruitmentList;
        if (accountDTO != null) {
            int userId = accountDTO.getId();
            recruitmentList = recruitmentService.getRecruitmentsByUserId(userId);
        } else {
            recruitmentList = Collections.emptyList();
        }
        model.addAttribute("recruitmentList", recruitmentList);
		
		return "recruitment/myRecruitments";

	}
    
   
}
 