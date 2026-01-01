package com.neighbus.recruitment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 모임 자동 마감 처리를 위한 API 컨트롤러 (Job Controller 역할).
 * 외부 스케줄러(Cron)가 주기적으로 호출합니다.
 * SecurityConfig에서 '/jobs/**' 경로는 permitAll()로 설정해야 403 에러가 나지 않습니다.
 */
@RestController
@RequestMapping("/jobs") // 경로는 /jobs/close-gatherings
public class RecruitmentJobController {
    
    @Autowired
    private RecruitmentService recruitmentService; 

    /**
     * 마감 처리 로직을 실행하는 엔드포인트
     * @return 처리 결과 메시지
     */
    @GetMapping("/close-gatherings")
    @Transactional
    public String executeAutoClosure() {
        try {
            // Service의 마감 처리 메서드 호출
            int count = recruitmentService.autoCloseExpiredGatherings(); 
            
            // 처리된 건수를 반환
            return "SUCCESS: " + count + " items closed at " + java.time.LocalDateTime.now();
        } catch (Exception e) {
            System.err.println("Auto closure job failed: " + e.getMessage());
            return "FAILURE: " + e.getMessage();
        }
    }
}