package com.neighbus.chat;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMapper {
    // 채팅방 생성
    void insertRoom(ChatRoomDTO chatRoomDTO);
    // 전체 채팅방 조회
    List<ChatRoomDTO> findAllRooms();
    // 메시지 저장
    void insertMessage(ChatMessageDTO chatMessageDTO);
    // 특정 방의 메시지 조회 (입장 시 이전 대화 불러오기용)
    List<ChatMessageDTO> findMessagesByRoomId(String roomId);
    // 채팅방 아이디로 방 찾기
    ChatRoomDTO findByRoomId(String roomId);
    
    // 가입 모임 채팅방
    List<ChatRoomDTO> findRoomsByUsername(String username);
}
