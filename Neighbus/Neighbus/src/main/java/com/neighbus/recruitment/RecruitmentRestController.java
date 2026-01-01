package com.neighbus.recruitment;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.account.AccountDTO;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/recruitment")
public class RecruitmentRestController {

    private final RecruitmentService recruitmentService;

    @Autowired
    public RecruitmentRestController(RecruitmentService recruitmentService) {
        this.recruitmentService = recruitmentService;
    }
    
    @GetMapping("/dates")
    public ResponseEntity<List<String>> getMeetingDates(@RequestParam("clubId") int clubId) {
        try {
            List<String> dates = recruitmentService.getMeetingDatesByClubId(clubId);
            return ResponseEntity.ok(dates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody Map<String, String> payload, @AuthenticationPrincipal AccountDTO accountDTO) {
        if (accountDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            String recruitmentIdStr = payload.get("recruitmentId");
            if (recruitmentIdStr == null) {
                return ResponseEntity.badRequest().body("recruitmentId가 필요합니다.");
            }
            int recruitmentId = Integer.parseInt(recruitmentIdStr);
            int userId = accountDTO.getId();

            Map<String, Object> params = new HashMap<>();
            params.put("recruitmentId", recruitmentId);
            params.put("userId", userId);

            int result = recruitmentService.joinRecruitment(params);

            if (result > 0) {
                return ResponseEntity.ok("모임에 성공적으로 가입했습니다.");
            } else {
                return ResponseEntity.badRequest().body("모임 가입에 실패했습니다. 이미 가입했거나, 인원이 가득 찼을 수 있습니다.");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("잘못된 recruitmentId 형식입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/withdrawal")
    public ResponseEntity<String> withdrawal(@RequestBody Map<String, String> payload, @AuthenticationPrincipal AccountDTO accountDTO) {
        if (accountDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            String recruitmentIdStr = payload.get("recruitmentId");
            if (recruitmentIdStr == null) {
                return ResponseEntity.badRequest().body("recruitmentId가 필요합니다.");
            }
            int recruitmentId = Integer.parseInt(recruitmentIdStr);
            int userId = accountDTO.getId();

            Map<String, Object> params = new HashMap<>();
            params.put("recruitmentId", recruitmentId);
            params.put("userId", userId);

            int result = recruitmentService.withdrawalRecruitment(params);

            if (result > 0) {
                return ResponseEntity.ok("모임에서 성공적으로 탈퇴했습니다.");
            } else {
                return ResponseEntity.badRequest().body("모임 탈퇴에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("잘못된 recruitmentId 형식입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/{recruitmentId}")
    public ResponseEntity<String> delete(@PathVariable("recruitmentId") int recruitmentId, @AuthenticationPrincipal AccountDTO accountDTO) {
        if (accountDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            RecruitmentDTO recruitment = recruitmentService.findById(recruitmentId);
            if (recruitment == null) {
                return ResponseEntity.badRequest().body("존재하지 않는 모임입니다.");
            }

            if (recruitment.getWriter() != accountDTO.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("모임을 삭제할 권한이 없습니다.");
            }

            int result = recruitmentService.deleteRecruitment(recruitmentId);

            if (result > 0) {
                return ResponseEntity.ok("모임이 성공적으로 삭제되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("모임 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
