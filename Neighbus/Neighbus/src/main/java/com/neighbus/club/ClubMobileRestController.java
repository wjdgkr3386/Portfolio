package com.neighbus.club;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neighbus.Util;
import com.neighbus.account.AccountDTO;
import com.neighbus.recruitment.RecruitmentDTO;
import com.neighbus.recruitment.RecruitmentMapper;
import com.neighbus.s3.S3UploadService;
import com.neighbus.util.PagingDTO;

@RestController
@RequestMapping("/api/mobile/club")
public class ClubMobileRestController {
    @Autowired
    ClubService clubService;
    @Autowired
    ClubMapper clubMapper;
    @Autowired
    RecruitmentMapper recruitmentMapper;
    @Autowired
    S3UploadService s3UploadService;

	private static final Logger logger = LoggerFactory.getLogger(ClubController.class);
	
    @GetMapping("/checkName")
    public ResponseEntity<Map<String, Boolean>> checkClubName(@RequestParam("clubName") String clubName) {
        boolean isDuplicate = clubService.isClubNameDuplicate(clubName);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/getClubs")
    public ResponseEntity<Map<String, Object>> getClubs(
    	ClubDTO clubDTO,
    	@AuthenticationPrincipal AccountDTO accountDTO
    ){
    	System.out.println("ClubMobileRestController - getClubs");
    	
    	Map<String, Object> response = new HashMap<>();
    	PagingDTO<ClubDTO> clubs = clubService.getClubsWithPaging(clubDTO);
    	response.put("clubs", clubs.getList());
    	response.put("paging", clubs.getPagingMap());
    	response.put("categoryList", clubMapper.getClubCategory());
    	return ResponseEntity.ok(response);
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<?> getClubDetail(
            @PathVariable("id") int id,
            @AuthenticationPrincipal AccountDTO accountDTO
    ) {
    	System.out.println(id);
        ClubDetailDTO clubDetail = clubService.getClubDetail(id, accountDTO);

        if (clubDetail == null || clubDetail.getClub() == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                        Map.of(
                            "success", false,
                            "message", "클럽을 찾을 수 없습니다."
                        )
                    );
        }

        // ✅ 동아리 ID로 모임 조회
        List<RecruitmentDTO> recruitments = recruitmentMapper.findRecruitmentsByClubId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("club", clubDetail.getClub());
        response.put("isLoggedIn", clubDetail.isLoggedIn());
        response.put("isMember", clubDetail.isMember());
        response.put("recruitments", recruitments);
        return ResponseEntity.ok(response);
    }

    // 동아리 가입
    @PostMapping("/join/{id}")
    public ResponseEntity<Map<String, Object>> joinClub(
            @PathVariable("id") int clubId,
            @AuthenticationPrincipal AccountDTO accountDTO
    ) {
        Map<String, Object> response = new HashMap<>();

        // 로그인 체크
        if (accountDTO == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        ClubMemberDTO clubMemberDTO = new ClubMemberDTO();
        clubMemberDTO.setClubId(clubId);
        clubMemberDTO.setUserId(accountDTO.getId());

        boolean success = clubService.joinClub(clubMemberDTO);

        if (success) {
            response.put("success", true);
            response.put("message", "동아리 가입이 완료되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "이미 가입한 동아리입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }
    
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createClub(
            @ModelAttribute ClubDTO club, 
            @AuthenticationPrincipal AccountDTO accountDTO
    ) {
        Map<String, Object> response = new HashMap<>();

        // 1. 인증 체크 (토큰이 유효하지 않은 경우)
        if (accountDTO == null) {
            logger.error("동아리 생성 실패: 로그인 정보가 없습니다.");
            response.put("status", "fail");
            response.put("message", "로그인이 필요한 서비스입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // 2. 작성자 ID 설정
        club.setWriteId(accountDTO.getId());
        MultipartFile file = club.getClubImage();

        // 3. 이미지 업로드 로직
        String key = Util.s3Key();
        try {
            if (file != null && !file.isEmpty()) {
                // S3 업로드 수행
                String imageUrl = s3UploadService.upload(key, file);
                if (imageUrl != null) {
                    club.setClubImageName(imageUrl);
                }
            } else {
                logger.warn("업로드된 이미지가 없습니다.");
                // 이미지가 필수라면 여기서 실패 응답을 보낼 수 있습니다.
            }

            // 4. DB 저장
            clubService.createClubAndAddCreator(club);
            
            response.put("status", 1); // 성공 코드
            response.put("message", "동아리가 성공적으로 생성되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("동아리 생성 중 오류 발생: ", e);
            // 업로드 실패 시 S3에 올라간 파일이 있다면 삭제
            s3UploadService.delete(key);
            
            response.put("status", -1); // 에러 코드
            response.put("message", "동아리 생성 중 서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
