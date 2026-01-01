package com.neighbus.recruitment;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.account.AccountDTO;
import com.neighbus.chat.ChatMapper;
import com.neighbus.chat.ChatRoomDTO;

@RestController
@RequestMapping("/api/mobile/recruitment")
public class RecruitmentMobileRestController {

	@Autowired
    RecruitmentService recruitmentService;
	@Autowired
	ChatMapper chatMapper;

    @PostMapping("/new")
    public ResponseEntity<?> createRecruitment(
		@RequestBody RecruitmentDTO recruitmentDTO, 
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
    	System.out.println("RecruitmentMobileRestController - createRecruitment");
        try {
            // 1. 작성자 설정 (로그인한 사용자 ID)
            recruitmentDTO.setWriter(accountDTO.getId());
            
            // 2. 모임 생성 (DB 저장 및 ID 생성)
            recruitmentService.createRecruitment(recruitmentDTO);
            
            // 3. 생성자를 모임 멤버로 자동 가입 처리
            Map<String, Object> joinParams = new HashMap<>();
            joinParams.put("recruitmentId", recruitmentDTO.getId());
            joinParams.put("userId", accountDTO.getId());
            
            recruitmentService.joinRecruitment(joinParams);

            // 4. 성공 응답 반환 (생성된 모임 ID 포함)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "모임이 생성되었으며 자동으로 가입되었습니다.");
            response.put("recruitmentId", recruitmentDTO.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 에러 발생 시 응답
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "모임 생성 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getRecruitmentDetail(
            @PathVariable("id") int id,
            @AuthenticationPrincipal AccountDTO accountDTO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 모집글 정보 조회
            RecruitmentDTO recruitment = recruitmentService.findById(id);
            if (recruitment == null) {
                result.put("status", 0);
                result.put("message", "존재하지 않는 모집글입니다.");
                return result;
            }
            
            // 현재 참여 인원 수
            int currentUserCount = recruitmentService.countMembers(id);
            
            // 채팅방 존재 여부 확인
            ChatRoomDTO existingRoom = chatMapper.findByRoomId(String.valueOf(id));
            boolean chatRoomExists = (existingRoom != null);
            
            // 현재 사용자의 가입 여부 확인
            boolean isJoined = false;
            if (accountDTO != null) {
                isJoined = recruitmentService.isMember(id, accountDTO.getId());
            }
            
            // 결과 담기
            result.put("status", 1);
            result.put("message", "성공");
            result.put("recruitment", recruitment);
            result.put("currentUserCount", currentUserCount);
            result.put("chatRoomExists", chatRoomExists);
            result.put("isJoined", isJoined);
            
        } catch (Exception e) {
            result.put("status", 0);
            result.put("message", "데이터 조회 중 오류가 발생했습니다.");
        }
        
        return result;
    }
    
    @PostMapping("/join")
    public Map<String, Object> join(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal AccountDTO accountDTO) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 인증 확인
        if (accountDTO == null) {
            result.put("status", 0);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }
        
        try {
            // recruitmentId 검증
            String recruitmentIdStr = payload.get("recruitmentId");
            if (recruitmentIdStr == null) {
                result.put("status", 0);
                result.put("message", "recruitmentId가 필요합니다.");
                return result;
            }
            
            int recruitmentId = Integer.parseInt(recruitmentIdStr);
            int userId = accountDTO.getId();
            
            Map<String, Object> params = new HashMap<>();
            params.put("recruitmentId", recruitmentId);
            params.put("userId", userId);
            
            int joinResult = recruitmentService.joinRecruitment(params);
            
            if (joinResult > 0) {
                result.put("status", 1);
                result.put("message", "모임에 성공적으로 가입했습니다.");
            } else {
                result.put("status", 0);
                result.put("message", "모임 가입에 실패했습니다. 이미 가입했거나, 인원이 가득 찼을 수 있습니다.");
            }
            
        } catch (NumberFormatException e) {
            result.put("status", 0);
            result.put("message", "잘못된 recruitmentId 형식입니다.");
        } catch (Exception e) {
            result.put("status", 0);
            result.put("message", "서버 오류가 발생했습니다.");
        }
        
        return result;
    }
}