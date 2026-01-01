package com.neighbus.recruitment;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecruitmentMapper {
	
	// 전체 모임 조회
	List<RecruitmentDTO> findAll();

	// 모임 생성
    int createRecruitment(RecruitmentDTO dto);

    // 모임 삭제
    int deleteRecruitment(int recruitmentId);

    // 모임 가입
    int joinRecruitment(Map<String, Object> params);

    // 모임 탈퇴
    int withdrawalRecruitment(Map<String, Object> params);
    
    // 모임 상세보기
    RecruitmentDTO findById(int id);
    
    // 가입 여부 확인
    int isMember(Map<String, Object> params);
    
    // 현재 가입자 수 확인
    int countMembersByRecruitmentId(int recruitmentId);
    
    // 현재 가입 클럽 모임 리스트
    List<RecruitmentDTO> findRecruitmentsByMyClubs(int userId);
    
    // 내가 가입한 모임 리스트
    List<RecruitmentDTO> findRecruitmentsByUserId(int userId);
    
    // 현재 날짜 모임 리스트
    List<RecruitmentDTO> findRecruitmentsByClubAndDate(
                @Param("clubId") int clubId, 
                @Param("date") String date
            );
    
    // 모집중인 갯수
    int countByRecruitment();
    
    // 모임 목록 조회 (페이징, 관리자용)
    List<Map<String, Object>> selectGatheringsPaginated(Map<String, Object> params);

    // 모임 총 개수 조회 (페이징, 관리자용)
    int countTotalGatherings(Map<String, Object> params);
    
    // [수정] XML의 ID(findMemberIdsByRecruitmentId)와 이름을 똑같이 맞췄습니다.
    List<Integer> findMemberIdsByRecruitmentId(int recruitmentId);

    // [★추가된 기능] 마감 시간이 지난 모임을 자동으로 처리합니다.
    int updateExpiredRecruitments();

    // 상태별 모임 개수 조회
    int countGatheringsByStatus(@Param("status") String status);
    
    //동아리에 속한 모임 조회
    List<RecruitmentDTO> findRecruitmentsByClubId(int clubId);

    // 특정 동아리의 모임 날짜 목록 조회
    List<String> findMeetingDatesByClubId(int clubId);
}