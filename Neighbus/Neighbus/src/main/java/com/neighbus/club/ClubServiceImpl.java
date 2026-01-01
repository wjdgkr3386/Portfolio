package com.neighbus.club;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neighbus.util.PagingDTO;

// (선택) Lombok을 사용한다면 @Slf4j와 @RequiredArgsConstructor 사용 가능
@Service
public class ClubServiceImpl implements ClubService {

	// (선택) @Slf4j 어노테이션이 없다면 Logger를 직접 선언해야 합니다.
	private static final Logger logger = LoggerFactory.getLogger(ClubServiceImpl.class);

	private final ClubMapper clubMapper; // Mybatis Mapper 주입
	private final com.neighbus.account.AccountMapper accountMapper;
	private final com.neighbus.s3.S3UploadService s3UploadService;

	public ClubServiceImpl(ClubMapper clubMapper, com.neighbus.account.AccountMapper accountMapper, com.neighbus.s3.S3UploadService s3UploadService) {
		this.clubMapper = clubMapper;
		this.accountMapper = accountMapper;
		this.s3UploadService = s3UploadService;
	}

	/**
	 * 동아리 생성 및 생성자 가입 (트랜잭션 처리)
	 */
	@Override
	@Transactional // 이 메서드가 하나의 트랜잭션으로 실행되도록 보장
	public boolean createClubAndAddCreator(ClubDTO clubDTO) {
		try {
			// 1. 동아리 생성 (clubs 테이블 INSERT)
			int clubResult = clubMapper.insertClub(clubDTO);

			// 2. 생성자 멤버 추가 (club_member 테이블 INSERT)
			// (insertClub 쿼리가 <selectKey>로 DTO에 ID를 다시 넣어준다고 가정)
			int memberResult = clubMapper.addCreatorAsMember(clubDTO);

			// 두 작업이 모두 성공했는지 확인 (1줄 이상 INSERT)
			return clubResult > 0 && memberResult > 0;

		} catch (Exception e) {
			logger.error("동아리 생성 또는 생성자 가입 중 오류 발생", e);
			// @Transactional에 의해 자동 롤백됩니다.
			return false;
		}

	}

	@Override
	@Transactional
	public void deleteClubMember(Long clubId, Long userId) {
		// 1. 매퍼에 전달할 Map 생성
		Map<String, Object> params = new HashMap<>();
		params.put("clubId", clubId);
		params.put("userId", userId);

		// 2. 매퍼 호출
		int deletedRows = clubMapper.deleteClubMember(params);

		// 3. (선택) 삭제가 되었는지 확인
		if (deletedRows == 0) {
			// 예: 해당 멤버가 존재하지 않았을 경우
			throw new RuntimeException("해당 클럽 멤버를 찾을 수 없습니다.");
		}

	}

	/**
	 * 동아리 가입 (중복 확인)
	 */
	@Override
	@Transactional
	public boolean joinClub(ClubMemberDTO clubMemberDTO) {
		// 1. 이미 가입했는지 먼저 확인 (Mapper 호출)
		int count = clubMapper.isMember(clubMemberDTO);

		// 2. 이미 가입했다면(count > 0), false 반환
		if (count > 0) {
			logger.warn("User {} is already a member of club {}", clubMemberDTO.getUserId(), clubMemberDTO.getClubId());
			return false;
		}

		// 3. 가입하지 않았을 때만 INSERT 시도
		try {
			int result = clubMapper.insertClubMember(clubMemberDTO);
			return (result > 0); // 1줄 이상 INSERT 성공 시 true 반환
		} catch (Exception e) {
			logger.error("동아리 가입(INSERT) 중 오류 발생", e);
			return false;
		}
	}

	/**
	 * 모든 동아리 조회
	 */
	@Override
	public List<ClubDTO> getAllClubs() {
		return clubMapper.findAllClubs();
	}

	/**
	 * 동아리 상세 조회
	 */
	@Override
	public ClubDTO getClubById(int id) {
		return clubMapper.getClubById(id);
	}

	/**
	 * 가입 여부 확인 (컨트롤러용)
	 */
	@Override
	public int isMember(ClubMemberDTO clubMemberDTO) {
		// (수정) 이전에 'return 0;'으로 되어있던 부분을 수정
		// Mapper를 호출하여 실제 DB를 확인해야 합니다.
		return clubMapper.isMember(clubMemberDTO);
	}

	@Override
	public PagingDTO<ClubDTO> getClubsWithPaging(ClubDTO clubDTO) {
		
		// 1. 전체 게시물 수 조회
		int totalCnt = clubMapper.getClubListCnt(clubDTO);
		clubDTO.setSearchCnt(totalCnt);

		// 2. 페이징 계산 로직
		int page = clubDTO.getSelectPageNo();
		if (page == 0) page = 1;
		
		int rowCnt = 9; 
		clubDTO.setRowCnt(rowCnt);
		
		int pageBlock = 10;

		int pageAllCnt = (int) Math.ceil((double) totalCnt / rowCnt);
		
		int beginRowNo = (page - 1) * rowCnt;
		clubDTO.setBeginRowNo(beginRowNo);
		
		int endPageNo = (int) (Math.ceil((double) page / pageBlock) * pageBlock);
		int beginPageNo = endPageNo - pageBlock + 1;
		if (endPageNo > pageAllCnt) endPageNo = pageAllCnt;

		clubDTO.setBeginPageNo(beginPageNo);
		clubDTO.setEndPageNo(endPageNo);

		// 3. 목록 조회
		List<ClubDTO> clubs = clubMapper.getClubListWithPaging(clubDTO);

		// 4. 페이징 맵 생성 (반드시 <String, Integer> 타입이어야 함)
		Map<String, Integer> pagingMap = new HashMap<>();
		pagingMap.put("selectPageNo", page);
		pagingMap.put("rowCnt", rowCnt);
		pagingMap.put("searchCnt", totalCnt);
		pagingMap.put("beginPageNo", beginPageNo);
		pagingMap.put("endPageNo", endPageNo);
		pagingMap.put("pageAllCnt", pageAllCnt);

		// 5. 생성자를 통해 객체 생성 (타입 추론 해결됨)
		return new PagingDTO<>(clubs, pagingMap);
	}

	@Override
	public List<Map<String, Object>> getProvince() {
		return accountMapper.getProvince();
	}

	@Override
	public List<Map<String, Object>> getCity() {
		return accountMapper.getCity();
	}

	// 동아리 필터
	@Override
	public List<ClubDTO> getFilteredClubs(ClubDTO clubDTO) {
		if (clubDTO.getCity() == 0) {
			return clubMapper.getOderProvince(clubDTO.getProvinceId());
		} else {
			Map<String, Object> params = new HashMap<>();
			params.put("provinceId", clubDTO.getProvinceId());
			params.put("city", clubDTO.getCity());
			return clubMapper.getOderCity(params);
		}
	}

	@Override
	public List<ClubDTO> getMyClubs(Integer userId) {
		return clubMapper.getMyClubs(userId);
	}

	@Override
	public ClubDetailDTO getClubDetail(int id, com.neighbus.account.AccountDTO accountDTO) {
		ClubDTO club = clubMapper.getClubById(id);
		if (club == null) {
			return null;
		}

		ClubDetailDTO clubDetail = new ClubDetailDTO(club, accountDTO);
		if (clubDetail.isLoggedIn()) {
			ClubMemberDTO memberCheck = new ClubMemberDTO();
			memberCheck.setClubId(id);
			memberCheck.setUserId(accountDTO.getId());
			if (clubMapper.isMember(memberCheck) > 0) {
				clubDetail.setMember(true);
			}
		}
		return clubDetail;
	}

	@Override
	public List<Map<String, Object>> getClubMembers(int clubId) {
		return clubMapper.getClubMembers(clubId);
	}

	@Override
	@Transactional
	public boolean removeClubMember(int clubId, int userId) {
		Map<String, Object> params = new HashMap<>();
		params.put("clubId", clubId);
		params.put("userId", userId);
		int affectedRows = clubMapper.removeClubMember(params);
		return affectedRows > 0;
	}

	@Override
	@Transactional
	public boolean deleteClubByCreator(int clubId, int creatorId) {
		// 1. 클럽 정보 조회 (이미지 URL 획득용)
		ClubDTO club = clubMapper.getClubById(clubId);

		Map<String, Object> params = new HashMap<>();
		params.put("clubId", clubId);
		params.put("creatorId", creatorId);
		int affectedRows = clubMapper.deleteClubByCreator(params);

		// 2. DB 삭제 성공 시 S3 이미지 삭제
		if (affectedRows > 0 && club != null && club.getClubImg() != null) {
			try {
				String imgUrl = club.getClubImg();
				// URL에서 Key 추출 로직 (URI 파싱)
				java.net.URI uri = new java.net.URI(imgUrl);
				String path = uri.getPath();
				if (path.startsWith("/")) {
					path = path.substring(1);
				}
				s3UploadService.delete(path);
			} catch (Exception e) {
				logger.error("Failed to delete S3 image for club " + clubId, e);
			}
		}

		return affectedRows > 0;
	}

    @Override
    public boolean isClubNameDuplicate(String clubName) {
        return clubMapper.countByClubName(clubName) > 0;
    }
}
