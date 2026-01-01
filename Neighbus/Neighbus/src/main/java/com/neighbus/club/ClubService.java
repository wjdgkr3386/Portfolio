package com.neighbus.club;

import com.neighbus.util.PagingDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface ClubService {
	// 동아리 생성
	@Transactional
    boolean createClubAndAddCreator(ClubDTO clubDTO);
    
   // 기존 동아리에 유저를 멤버로 가입시킵니다.    
    boolean joinClub(ClubMemberDTO clubMemberDTO);
    
    // 모든 동아리 목록을 가져옵니다.
    List<ClubDTO> getAllClubs();
    //동아리 상세보기
    ClubDTO getClubById (int id);
    // 동아리 탈퇴
    void deleteClubMember(Long clubId, Long userId);
    
   int isMember(ClubMemberDTO clubMemberDTO);

	PagingDTO<ClubDTO> getClubsWithPaging(ClubDTO clubDTO);

	List<Map<String, Object>> getProvince();

	List<Map<String, Object>> getCity();

	List<ClubDTO> getFilteredClubs(ClubDTO clubDTO);

	List<ClubDTO> getMyClubs(Integer id);

	ClubDetailDTO getClubDetail(int id, com.neighbus.account.AccountDTO accountDTO);

	List<Map<String, Object>> getClubMembers(int clubId);

	@Transactional
	boolean removeClubMember(int clubId, int userId);

	@Transactional
	boolean deleteClubByCreator(int clubId, int creatorId);

	boolean isClubNameDuplicate(String clubName);
}
