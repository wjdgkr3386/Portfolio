package com.neighbus.club;



import com.neighbus.alarm.NotificationService;
import com.neighbus.account.AccountDTO; // 본인의 회원 DTO 경로 확인
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice // 모든 컨트롤러에서 이 코드가 실행됨
public class GlobalControllerAdvice {
	
    private final NotificationService notificationService;
	
    public GlobalControllerAdvice(NotificationService notificationService) {
		super();
		this.notificationService = notificationService;
	}



	// "unreadCount"라는 이름으로 모든 뷰(HTML)에 값을 넘겨줍니다.
    @ModelAttribute("unreadCount")
    public int addUnreadCount(@AuthenticationPrincipal AccountDTO accountDTO) {
        // 1. 로그인 안 된 상태면 0 리턴
        if (accountDTO == null) {
            return 0;
        }

        // 2. 로그인 된 상태면 DB에서 안 읽은 알림 개수 조회
        // accountDTO.getId()는 본인의 회원 ID(PK) getter 메서드를 쓰세요.
        return notificationService.countUnread(accountDTO.getId());
    }
}
