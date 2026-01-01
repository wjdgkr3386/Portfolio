package com.neighbus.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController {

    private final AdminService adminService;

    @Autowired
    public ReportApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 신고 접수 API
     * @param reportDTO 프론트엔드에서 전송된 신고 데이터
     * @return 처리 결과
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitReport(@RequestBody ReportDTO reportDTO) {
        try {
            // reporterId는 SecurityContext에서 가져오는 것이 더 안전하지만,
            // 현재 프론트엔드 구현에 따라 DTO에 포함된 값을 사용합니다.
            // 추후 개선: @AuthenticationPrincipal 등을 사용하여 서버에서 직접 ID 설정
            if (reportDTO.getReporterId() == 0 && reportDTO.getType() != null) {
                // 비로그인 사용자 신고 (reporterId를 0 또는 특정 값으로 통일)
                // DB의 reporter_id 컬럼이 users.id를 참조하므로,
                // 실제로는 익명 신고를 위한 별도의 'guest' 유저(예: id=0)가 users 테이블에 있어야 함.
                // '테이블 생성.txt'에 관리자 계정이 id=0으로 있어 충돌 가능성이 있으므로 주의 필요.
                // 여기서는 프론트에서 '0'으로 보내준 값을 그대로 사용.
            }

            adminService.createReport(reportDTO);
            
            // 성공 응답 반환
            return ResponseEntity.ok(Map.of("status", 1, "message", "신고가 성공적으로 접수되었습니다."));

        } catch (Exception e) {
            // 예외 처리
            // 로그를 남기는 것이 좋습니다: e.g., log.error("Error submitting report", e);
            return ResponseEntity.internalServerError().body(Map.of("status", 0, "message", "서버 오류로 인해 신고 접수에 실패했습니다."));
        }
    }
}
