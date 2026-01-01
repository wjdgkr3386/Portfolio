package com.neighbus.club;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.neighbus.main.SearchDTO;

@Mapper
public interface ClubMapper {
	// 클럽 카테고리 목록 조회
	List<Map<String, Object>> getClubCategory();

	// 새로운 동아리를 clubs 테이블에 삽입합니다
	int insertClub(ClubDTO clubDTO);

	// 동아리 생성자(creator)를 club_member 테이블에 멤버로 추가합니다
	int addCreatorAsMember(ClubDTO clubDTO);

	// 동아리 가입
	int insertClubMember(ClubMemberDTO clubMemberDTO);

	// 동아리 탈퇴
	int deleteClubMember(Map<String, Object> params);

	// 모든 동아리 목록을 가져옵니다.
	List<ClubDTO> findAllClubs();

	// 가입 동아리 목록
	List<ClubDTO> getMyClubs(int userId);

	// 상세보기
	ClubDTO getClubById(int id);

	List<Map<String, Object>> getMyClub(Map<String, Object> map);

	// 중복 동아리 검색
	int isMember(ClubMemberDTO dto);

	List<Map<String, Object>> getNewClub(SearchDTO searchDTO);

	List<Map<String, Object>> getPopularClub(SearchDTO searchDTO);

	// 시,도 카테고리 분류
	List<ClubDTO> getOderProvince(@Param("provinceId") int provinceId);

	List<ClubDTO> getOderCity(Map<String, Object> params);

	// 페이징 처리를 위한 메소드
	int searchCnt(ClubDTO clubDTO);

	int getClubListCnt(ClubDTO clubDTO);
	
	List<ClubDTO> getClubListWithPaging(ClubDTO clubDTO);

	int checkJoinClubCount(int id);

	List<Map<String, Object>> getClubMembers(int clubId);

	int removeClubMember(Map<String, Object> params);

	int deleteClubByCreator(Map<String, Object> params);

	int countByClubName(String clubName);
	
	// 내가 만든 동아리의 이미지 리스트 조회
	List<String> selectClubImagesByWriter(int userId);

}
