package com.neighbus.alarm;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.neighbus.account.AccountDTO;

@Controller
public class NotificationController {

    private final NotificationService notificationService; 


   public NotificationController(NotificationService notificationService) {
		super();
		this.notificationService = notificationService;
	}


   // AJAX 요청을 받아서 JSON 리스트 반환
    @GetMapping("/api/notifications")
    @ResponseBody 
    public List<NotificationDTO> getMyNotifications(@AuthenticationPrincipal AccountDTO accountDTO) {
        if (accountDTO == null) return null;
        return notificationService.getMyNotifications(accountDTO.getId());
    }
    
    @DeleteMapping("/api/notifications/{id}")
    @ResponseBody
    public String deleteNotification(@PathVariable("id") int id) {
        notificationService.deleteNotification(id);
        return "deleted";
    }
    
    // ★ 전체 알림 삭제 추가
    @DeleteMapping("/api/notifications/deleteAll")
    @ResponseBody
    public String deleteAllNotifications(@AuthenticationPrincipal AccountDTO accountDTO) {
        if (accountDTO == null) return "fail";
        notificationService.deleteAllNotifications(accountDTO.getId());
        return "deleted all";
    }

    // 특정 사용자의 읽지 않은 알림 개수 반환 (AJAX)
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public int countUnreadNotifications(@AuthenticationPrincipal AccountDTO accountDTO) {
        if (accountDTO == null) return 0;
        return notificationService.countUnread(accountDTO.getId());
    }
}
