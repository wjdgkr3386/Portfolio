package com.neighbus.alarm;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NotificationMapper {
    // 알림 저장
    void save(NotificationDTO notificationDTO);

    // 특정 사용자의 읽지 않은 알림 조회
    List<NotificationDTO> findUnreadNotifications(int usersId);
    
    int countUnread(int userId);
   

    // 3. 내 알림 목록 조회
    List<NotificationDTO> selectMyNotifications(int userId);
    // 알림 삭제
    void deleteNotification(int id);
    
    // ★ 전체 알림 삭제 추가
    void deleteAllNotifications(int userId);
}