package com.neighbus.recruitment;

import com.neighbus.chat.ChatMapper;
import com.neighbus.chat.ChatRoomDTO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RecruitmentService 인터페이스를 구현합니다.
@Service
public class RecruitmentServiceImpl implements RecruitmentService { 

    private final RecruitmentMapper recruitmentMapper;
    private final ChatMapper chatMapper;

    @Autowired
    public RecruitmentServiceImpl(RecruitmentMapper recruitmentMapper, ChatMapper chatMapper) {
        this.recruitmentMapper = recruitmentMapper;
        this.chatMapper = chatMapper;
    }

 
    /**
     * 모임 생성
     */
    @Override
    @Transactional
    public int createRecruitment(RecruitmentDTO dto) {
        // 1. 모임글 생성 (DB 저장)
        recruitmentMapper.createRecruitment(dto);
        int recruitmentId = dto.getId();

        // 2. 채팅방 자동 생성 로직
        if (recruitmentId > 0) {
            ChatRoomDTO chatRoom = new ChatRoomDTO();
            
            // 방 ID는 모임 ID와 동일하게 설정
            chatRoom.setRoomId(String.valueOf(recruitmentId));
            
            // 방 이름은 모임 제목으로 설정
            chatRoom.setRoomName(dto.getTitle());
            
            // ★ [핵심] 연결 고리 ID 설정 (이게 있어야 삭제 시 같이 지워짐)
            chatRoom.setLinkedRecruitmentId(recruitmentId);
            
            // 친구 관련 컬럼은 null (모집글 채팅이니까)
            chatRoom.setUser1Id(null);
            chatRoom.setUser2Id(null);

            // DB에 저장
            chatMapper.insertRoom(chatRoom);
            System.out.println(">>> [서비스] 모임 및 채팅방 생성 완료: " + recruitmentId);
        }
        return recruitmentId;
    }

    /**
     * 모임 삭제
     */
    @Override
    @Transactional
    public int deleteRecruitment(int recruitmentId) {
        return recruitmentMapper.deleteRecruitment(recruitmentId);
    }

    /**
     * 모임 가입 (비즈니스 로직 포함)
     */
    @Override
    @Transactional
    public int joinRecruitment(Map<String, Object> params) {
        int recruitmentId = (int) params.get("recruitmentId");
        int userId = (int) params.get("userId");

        // 1. 해당 모임 정보 조회
        RecruitmentDTO recruitment = recruitmentMapper.findById(recruitmentId);
        if (recruitment == null) {
            System.out.println("가입 실패: 존재하지 않는 모임입니다.");
            return 0; // 존재하지 않는 모임
        }

        // 2. 이미 가입했는지 확인
        if (isMember(recruitmentId, userId)) {
            System.out.println("가입 실패: 이미 가입한 모임입니다.");
            return 0; // 이미 가입함
        }

        // 3. 최대 인원(maxUser)을 초과하는지 확인
        int currentUserCount = recruitmentMapper.countMembersByRecruitmentId(recruitmentId);
        if (currentUserCount >= recruitment.getMaxUser()) {
            System.out.println("가입 실패: 모임 인원이 모두 찼습니다.");
            return 0; // 인원 초과
        }
        
        // 4. 가입 처리
        return recruitmentMapper.joinRecruitment(params);
    }

    /**
     * 모임 탈퇴
     */
    @Override
    @Transactional
    public int withdrawalRecruitment(Map<String, Object> params) {
        // TODO: 방장(writer)은 탈퇴할 수 없도록 막는 로직 추가 필요
        return recruitmentMapper.withdrawalRecruitment(params);
    }

    /**
     * 모임 전체 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecruitmentDTO> findAllRecruitments() {
        return recruitmentMapper.findAll();
    }
    
    // 가입 클럽 모임 리스트
    @Override
    public List<RecruitmentDTO> getRecruitmentsByMyClubs(int userId) {
        return recruitmentMapper.findRecruitmentsByMyClubs(userId);
    }
    
    /**
     * 특정 동아리(clubId)의 특정 날짜(date) 모집글 목록 조회
     */
    @Override
    public List<RecruitmentDTO> getRecruitmentsByClubAndDate(int clubId, String date) {
        return recruitmentMapper.findRecruitmentsByClubAndDate(clubId, date);
    }

	/**
     * 모임 상세 조회
     */
    @Override
    @Transactional(readOnly = true)
    public RecruitmentDTO findById(int id) {
        return recruitmentMapper.findById(id);
    }

    /**
     * 현재 가입자 수 조회
     */
    @Override
    @Transactional(readOnly = true)
    public int countMembers(int recruitmentId) {
        return recruitmentMapper.countMembersByRecruitmentId(recruitmentId);
    }

    /**
     * 가입 여부 확인
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isMember(int recruitmentId, int userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("recruitmentId", recruitmentId);
        params.put("userId", userId);
        return recruitmentMapper.isMember(params) > 0;
    }
    
    @Override
    public List<RecruitmentDTO> getRecruitmentsByUserId(int userId) {
        return recruitmentMapper.findRecruitmentsByUserId(userId);
    }
    
    @Override
    public int countByRecruitment() {
        return recruitmentMapper.countByRecruitment();
    }
    
    @Override
    public List<Integer> getMemberIdsByRecruitmentId(int recruitmentId) {
        // [수정] Mapper 메서드 이름과 동일하게 find... 로 호출
        return recruitmentMapper.findMemberIdsByRecruitmentId(recruitmentId);
    }
    
    @Override
    public Map<String, Object> getGatheringsPaginated(int page, int size, String keyword, String status, String sortOrder, String sortField) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", (page - 1) * size);
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("sortOrder", sortOrder);

        // sortField 파라미터 추가: id, clubId, meetingDate
        if (sortField != null && (sortField.equals("clubId") || sortField.equals("meetingDate"))) {
            params.put("sortField", sortField);
        } else {
            params.put("sortField", "id"); // Default sort field
        }

        List<Map<String, Object>> gatherings = recruitmentMapper.selectGatheringsPaginated(params);
        int totalElements = recruitmentMapper.countTotalGatherings(params);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // 상태별 통계 계산
        int activeGatherings = recruitmentMapper.countGatheringsByStatus("OPEN");
        int endedGatherings = recruitmentMapper.countGatheringsByStatus("CLOSED");

        Map<String, Object> response = new HashMap<>();
        response.put("content", gatherings);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);
        response.put("number", page);
        response.put("size", size);
        response.put("activeGatherings", activeGatherings);
        response.put("endedGatherings", endedGatherings);

        return response;
    }

    // [★추가된 기능] 모임 자동 마감 처리 구현
    @Override
    @Transactional
    public int updateExpiredRecruitments() {
        // [수정] 메서드 이름은 인터페이스(autoCloseExpiredGatherings 등)와 맞춰야 하는데, 
        // 일단 Mapper 호출 이름은 확실히 updateExpiredRecruitments() 입니다.
        return recruitmentMapper.updateExpiredRecruitments();
    }
    
    // ※ 주의: RecruitmentService 인터페이스에 정의된 이름이 autoCloseExpiredGatherings()라면 
    // 아래 메서드 이름을 그대로 쓰셔야 합니다.
    @Override
    public int autoCloseExpiredGatherings() {
         return recruitmentMapper.updateExpiredRecruitments();
    }

    @Override
    @Transactional
    public int backfillChatRooms() {
        List<RecruitmentDTO> allRecruitments = recruitmentMapper.findAll();
        int createdCount = 0;
        for (RecruitmentDTO recruitment : allRecruitments) {
            ChatRoomDTO existingChatRoom = chatMapper.findByRoomId(String.valueOf(recruitment.getId()));
            if (existingChatRoom == null) {
                ChatRoomDTO chatRoom = new ChatRoomDTO();
                chatRoom.setRoomId(String.valueOf(recruitment.getId()));
                chatRoom.setRoomName(recruitment.getTitle());
                chatMapper.insertRoom(chatRoom);
                createdCount++;
            }
        }
        return createdCount;
    }
    
    @Override
    public List<String> getMeetingDatesByClubId(int clubId) {
        return recruitmentMapper.findMeetingDatesByClubId(clubId);
    }
}