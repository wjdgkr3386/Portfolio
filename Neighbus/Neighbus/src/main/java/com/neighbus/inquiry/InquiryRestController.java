package com.neighbus.inquiry; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.neighbus.Util;
import com.neighbus.account.AccountDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 동일 패키지에 있기 때문에 InquiryDto, InquiryService는 별도 import 없이 사용 가능 (권장)

@RestController 
@RequestMapping("/api/inquiry") 
public class InquiryRestController {

    private final InquiryService inquiryService;
 

    @Autowired 
    public InquiryRestController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerInquiry(@RequestBody InquiryDTO inquiryDto, @AuthenticationPrincipal AccountDTO currentUser) {
        Map<String, Object> response = new HashMap<>();

        if (currentUser == null) {
            response.put("status", 0);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Integer currentUserId = currentUser.getId();

            int result = inquiryService.registerInquiry(inquiryDto, currentUserId);

            if (result == 1) {
                response.put("status", 1);
                response.put("message", "문의가 성공적으로 접수되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "문의 등록에 실패했습니다. (DB 삽입 오류)");
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (Exception e) {
            // SQL 오류, 연결 오류 등 서버 내부 문제 처리
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "서버 내부 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 관리자용 API - 모든 문의 조회
    @GetMapping("/admin/list")
    public ResponseEntity<Map<String, Object>> getAllInquiries() {
        Map<String, Object> response = new HashMap<>();
        try {
            java.util.List<Map<String, Object>> inquiries = inquiryService.getAllInquiries();
            Util.print(inquiries);
            response.put("status", 1);
            response.put("data", inquiries);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "문의 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 관리자용 API - 문의 상세 조회
    @GetMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> getInquiryById(@PathVariable("id") int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> inquiry = inquiryService.getInquiryById(id);
            if (inquiry != null) {
                response.put("status", 1);
                response.put("data", inquiry);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "문의를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "문의 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 관리자용 API - 문의 상태 변경
    @PostMapping("/admin/update-state")
    public ResponseEntity<Map<String, Object>> updateInquiryState(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int id = request.get("id");
            int state = request.get("state");
            int result = inquiryService.updateInquiryState(id, state);

            if (result == 1) {
                response.put("status", 1);
                response.put("message", "문의 상태가 변경되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "상태 변경 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "상태 변경 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 관리자용 API - 문의 삭제
    @PostMapping("/admin/delete")
    public ResponseEntity<Map<String, Object>> deleteInquiry(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int id = request.get("id");
            int result = inquiryService.deleteInquiry(id);

            if (result == 1) {
                response.put("status", 1);
                response.put("message", "문의가 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "문의 삭제 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "문의 삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

 // Principal principal 파라미터 추가 (java.security.Principal import 필요)
    @PostMapping("/admin/add-comment")
    public ResponseEntity<Map<String, Object>> addAdminComment(
            @RequestBody Map<String, Object> payload, 
            @AuthenticationPrincipal AccountDTO User) { // 👈 1. 로그인 정보 파라미터 추가
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 데이터 파싱 (안전하게 변환)
            Object idObj = payload.get("inquiryId");
            Integer inquiryId = null;
            if (idObj instanceof Number) {
                inquiryId = ((Number) idObj).intValue();
            } else if (idObj != null) {
                inquiryId = Integer.parseInt(idObj.toString());
            }

            String content = (String) payload.get("content");

            if (inquiryId == null || content == null || content.trim().isEmpty()) {
                response.put("status", 0);
                response.put("message", "필수 정보가 누락되었습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 👇 2. 로그인한 관리자 ID 가져오는 핵심 로직         
            int adminId = User.getId();           

            // 3. 서비스 호출 (이제 진짜 adminId가 들어갑니다)
            int result = inquiryService.addAnswer(inquiryId, content, adminId);

            if (result == 1) {
                response.put("status", 1);
                response.put("message", "답변이 성공적으로 등록되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "답변 등록에 실패했습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    // 사용자의 '나의 문의' 목록 조회
    @GetMapping("/my-inquiries")
    public ResponseEntity<Map<String, Object>> getMyInquiries(@AuthenticationPrincipal AccountDTO currentUser) {
        Map<String, Object> response = new HashMap<>();

        if (currentUser == null) {
            response.put("status", 0);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        try {
            List<Map<String, Object>> inquiries = inquiryService.getInquiriesByWriterId(currentUser.getId());
            response.put("status", 1);
            response.put("data", inquiries);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "문의 목록을 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}