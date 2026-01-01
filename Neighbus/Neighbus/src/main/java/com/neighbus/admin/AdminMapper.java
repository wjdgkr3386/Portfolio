package com.neighbus.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {
    // 전체 회원 목록 조회
    List<Map<String, Object>> selectAllUsers();

    // 회원 삭제
    int deleteUser(@Param("userId") int userId);

    // 대시보드 통계 조회
    Map<String, Object> selectDashboardStats();

    // 월별 가입자 수 조회
    List<Map<String, Object>> selectMonthlySignups();

    // 동아리별 회원 수 조회 (상위 5개)
    List<Map<String, Object>> selectTopClubsByMembers();

    // 카테고리별 모임 수 조회
    List<Map<String, Object>> selectGatheringsByCategory();

    // 게시글 목록 조회 (댓글 수 포함)
    List<Map<String, Object>> selectPostsWithCommentCount();

    // 동아리 목록 조회 (회원 수 포함)
    List<Map<String, Object>> selectAllClubsWithMemberCount();

    // 동아리 삭제
    int deleteClub(@Param("clubId") int clubId);

    // 갤러리 목록 조회
    List<Map<String, Object>> selectAllGalleries();

    // 갤러리 댓글 삭제
    int deleteGalleryComments(@Param("galleryId") int galleryId);

    // 갤러리 이미지 삭제
    int deleteGalleryImages(@Param("galleryId") int galleryId);

    // 갤러리 삭제
    int deleteGallery(@Param("galleryId") int galleryId);
    
    // [신고 관리]
    // 1. 신고 접수
    void insertReport(ReportDTO reportDTO);
    
    // 2. 모든 신고 목록 조회
    List<ReportDTO> selectAllReports();

    // 3. 전체 신고 수 조회
    int countTotalReports();

    // 4. 상태별 신고 수 조회
    int countReportsByStatus(@Param("status") String status);

    // 5. 신고 상태 변경
    void updateReportStatus(@Param("id") int id, @Param("status") String status);

    // 6. 신고 삭제
    void deleteReport(@Param("id") int id);

    // 클럽 대시보드 통계 조회
    Map<String, Object> selectClubDashboardStats(@Param("clubId") int clubId);

    // User Management Pagination
    int countTodaySignups();
    int countAdminUsers();
    int countTotalUsers(Map<String, Object> params);
    List<Map<String, Object>> selectUsersPaginated(Map<String, Object> params);

    // Club Management Pagination
    int sumTotalClubMembers();
    int countTotalClubs(Map<String, Object> params);
    List<Map<String, Object>> selectClubsPaginated(Map<String, Object> params);

    // Post Management Pagination
    int countTotalPosts(Map<String, Object> params);
    List<Map<String, Object>> selectPostsPaginated(Map<String, Object> params);
    Map<String, Object> selectPostStats();

    // Gallery Management Pagination
    int countTotalGalleries(Map<String, Object> params);
    List<Map<String, Object>> selectGalleriesPaginated(Map<String, Object> params);
    Map<String, Object> selectGalleryStats();

    // Gathering Management Pagination (★추가된 부분)
    List<Map<String, Object>> selectGatheringsPaginated(Map<String, Object> params);
    int countTotalGatherings(Map<String, Object> params);
    int deleteGathering(@Param("id") int id);
    Map<String, Object> selectGatheringDashboardStats();

    //사용자 정지
    void blockUser(Map<String, Object> map);
    //사용자 정지 해제
    void unblockUser();

    // 신고 타입별 작성자 ID 조회
    Integer getPostWriterId(@Param("postId") Long postId);
    Integer getGalleryWriterId(@Param("galleryId") Long galleryId);
    Integer getGatheringWriterId(@Param("gatheringId") Long gatheringId);
    Integer getCommentWriterId(@Param("commentId") Long commentId);
    Integer getClubOwnerId(@Param("clubId") Long clubId);
}