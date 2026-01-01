package com.neighbus.chat;

import java.time.LocalDateTime;

public class ChatRoomDTO {
	
    private String roomId;
    private String roomName;
    private LocalDateTime createdAt;

    // ★ 추가된 필드 (DB 컬럼과 매핑)
    private Integer linkedRecruitmentId; // 모집글 ID (친구 채팅이면 null)
    private Integer user1Id;             // 친구 1 ID (모집 채팅이면 null)
    private Integer user2Id;             // 친구 2 ID (모집 채팅이면 null)

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Integer getLinkedRecruitmentId() {
        return linkedRecruitmentId;
    }
    public void setLinkedRecruitmentId(Integer linkedRecruitmentId) {
        this.linkedRecruitmentId = linkedRecruitmentId;
    }
    public Integer getUser1Id() {
        return user1Id;
    }
    public void setUser1Id(Integer user1Id) {
        this.user1Id = user1Id;
    }
    public Integer getUser2Id() {
        return user2Id;
    }
    public void setUser2Id(Integer user2Id) {
        this.user2Id = user2Id;
    }
}