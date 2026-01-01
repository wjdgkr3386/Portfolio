package com.neighbus.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neighbus.freeboard.FreeboardMapper;

@Service
public class AdminService {

    private final AdminMapper adminMapper;
    private final FreeboardMapper freeboardMapper;

    @Autowired
    public AdminService(AdminMapper adminMapper, FreeboardMapper freeboardMapper) {
        this.adminMapper = adminMapper;
        this.freeboardMapper = freeboardMapper;
    }

    /**
     * 전체 회원 목록 조회
     */
    public List<Map<String, Object>> getAllUsers() {
        return adminMapper.selectAllUsers();
    }

    /**
     * 회원 삭제
     */
    public int deleteUser(int userId) {
        return adminMapper.deleteUser(userId);
    }

    /**
     * 게시글 삭제 (관리자용 - 권한 체크 없음)
     */
    public void deletePost(int postId) {
        freeboardMapper.deletePost(postId);
    }

    /**
     * 대시보드 통계 조회
     */
    public Map<String, Object> getDashboardStats() {
        return adminMapper.selectDashboardStats();
    }

    /**
     * 월별 가입자 수 조회
     */
    public List<Map<String, Object>> getMonthlySignups() {
        return adminMapper.selectMonthlySignups();
    }

    /**
     * 동아리별 회원 수 조회 (상위 5개)
     */
    public List<Map<String, Object>> getTopClubsByMembers() {
        return adminMapper.selectTopClubsByMembers();
    }

    /**
     * 카테고리별 모임 수 조회
     */
    public List<Map<String, Object>> getGatheringsByCategory() {
        return adminMapper.selectGatheringsByCategory();
    }

    /**
     * 게시글 목록 조회 (댓글 수 포함)
     */
    public List<Map<String, Object>> getPostsWithCommentCount() {
        return adminMapper.selectPostsWithCommentCount();
    }

    /**
     * 동아리 목록 조회 (회원 수 포함)
     */
    public List<Map<String, Object>> getAllClubsWithMemberCount() {
        return adminMapper.selectAllClubsWithMemberCount();
    }

    /**
     * 동아리 삭제
     */
    public int deleteClub(int clubId) {
        return adminMapper.deleteClub(clubId);
    }

    /**
     * 갤러리 목록 조회
     */
    public List<Map<String, Object>> getAllGalleries() {
        return adminMapper.selectAllGalleries();
    }

    /**
     * 갤러리 삭제 (관련 데이터 함께 삭제)
     */
    @Transactional
    public int deleteGallery(int galleryId) {
        // 1. 갤러리 댓글 삭제
        adminMapper.deleteGalleryComments(galleryId);

        // 2. 갤러리 이미지 삭제
        adminMapper.deleteGalleryImages(galleryId);

        // 3. 갤러리 본체 삭제
        return adminMapper.deleteGallery(galleryId);
    }

    // [신고 관리 서비스]
    
    public void createReport(ReportDTO reportDTO) {
        adminMapper.insertReport(reportDTO);
    }

    public List<ReportDTO> getAllReports() {
        return adminMapper.selectAllReports();
    }

    public int getReportTotalCount() {
        return adminMapper.countTotalReports();
    }

    public int getReportStatusCount(String status) {
        return adminMapper.countReportsByStatus(status);
    }

    public void updateReportStatus(int id, String status) {
        adminMapper.updateReportStatus(id, status);
    }

    public void deleteReport(int id) {
        adminMapper.deleteReport(id);
    }

    /**
     * 클럽별 대시보드 통계 조회
     */
    public Map<String, Object> getClubDashboardStats(int clubId) {
        return adminMapper.selectClubDashboardStats(clubId);
    }

    /**
     * 회원 목록 조회 (페이징)
     */
    public Map<String, Object> getUsersPaginated(int page, int size, String role, String sortOrder, String sortField) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", (page - 1) * size);
        params.put("role", role);

        if (sortOrder != null && (sortOrder.equalsIgnoreCase("asc") || sortOrder.equalsIgnoreCase("desc"))) {
            params.put("sortOrder", sortOrder);
        } else {
            params.put("sortOrder", "desc"); // Default sort order
        }

        // sortField 파라미터 추가: id 또는 created_at
        if (sortField != null && sortField.equals("created_at")) {
            params.put("sortField", "created_at");
        } else {
            params.put("sortField", "id"); // Default sort field
        }

        List<Map<String, Object>> users = adminMapper.selectUsersPaginated(params);
        int totalElements = adminMapper.countTotalUsers(params);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Additional stats (not affected by filter)
        int todaySignups = adminMapper.countTodaySignups();
        int adminCount = adminMapper.countAdminUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("content", users);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);
        response.put("number", page);
        response.put("size", size);
        response.put("todaySignups", todaySignups);
        response.put("adminCount", adminCount);

        return response;
    }

    /**
     * 동아리 목록 조회 (페이징)
     */
    public Map<String, Object> getClubsPaginated(int page, int size, String keyword, String sortOrder, String sortField) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", (page - 1) * size);
        params.put("keyword", keyword);
        params.put("sortOrder", sortOrder);

        // sortField 파라미터 추가: id, memberCount, created_at
        if (sortField != null && (sortField.equals("memberCount") || sortField.equals("created_at"))) {
            params.put("sortField", sortField);
        } else {
            params.put("sortField", "id"); // Default sort field
        }

        List<Map<String, Object>> clubs = adminMapper.selectClubsPaginated(params);
        int totalElements = adminMapper.countTotalClubs(params);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Additional stats
        int totalMembers = adminMapper.sumTotalClubMembers();

        Map<String, Object> response = new HashMap<>();
        response.put("content", clubs);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);
        response.put("number", page);
        response.put("size", size);
        response.put("totalMembers", totalMembers);
        // Active clubs stat is not implemented in DB, so we send the filtered total
        response.put("activeClubs", totalElements);

        return response;
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    public Map<String, Object> getPostsPaginated(int page, int size, String keyword, String sortOrder, String sortField) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", (page - 1) * size);
        params.put("keyword", keyword);
        params.put("sortOrder", sortOrder);

        // sortField 파라미터 추가: id, viewCount, commentCount, createdAt
        if (sortField != null && (sortField.equals("viewCount") || sortField.equals("commentCount") || sortField.equals("createdAt"))) {
            params.put("sortField", sortField);
        } else {
            params.put("sortField", "id"); // Default sort field
        }

        List<Map<String, Object>> posts = adminMapper.selectPostsPaginated(params);
        int totalElements = adminMapper.countTotalPosts(params);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        Map<String, Object> response = new HashMap<>();
        response.put("content", posts);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);
        response.put("number", page);
        response.put("size", size);

        return response;
    }

    /**
     * 게시글 통계 조회
     */
    public Map<String, Object> getPostStats() {
        return adminMapper.selectPostStats();
    }

    /**
     * 갤러리 목록 조회 (페이징)
     */
    public Map<String, Object> getGalleriesPaginated(int page, int size, String keyword, String clubName, String sortOrder, String sortField) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", (page - 1) * size);
        params.put("keyword", keyword);
        params.put("clubName", clubName);
        params.put("sortOrder", sortOrder);

        if (sortField != null && (sortField.equals("viewCount") || sortField.equals("commentCount") || sortField.equals("createdAt"))) {
            params.put("sortField", sortField);
        } else {
            params.put("sortField", "id");
        }

        List<Map<String, Object>> galleries = adminMapper.selectGalleriesPaginated(params);
        int totalElements = adminMapper.countTotalGalleries(params);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        Map<String, Object> response = new HashMap<>();
        response.put("content", galleries);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);
        response.put("number", page);
        response.put("size", size);

        return response;
    }

    /**
     * 갤러리 통계 조회
     */
    public Map<String, Object> getGalleryStats() {
        return adminMapper.selectGalleryStats();
    }

    // 유저 정지 기능
    public void blockUser(int id, int banTime) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("banTime", banTime);
        adminMapper.blockUser(map);
    }
    
    // 유저 정지 해제 기능
    public void unblockUser() {
        adminMapper.unblockUser();
    }

    // [★추가된 기능] 모임 목록 조회 (페이징)
    public Map<String, Object> getGatheringsPaginated(int page, int size, String keyword, String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", size);
        params.put("offset", (page - 1) * size);
        params.put("keyword", keyword);
        params.put("status", status); // 'OPEN' or 'CLOSED'

        List<Map<String, Object>> content = adminMapper.selectGatheringsPaginated(params);
        int totalElements = adminMapper.countTotalGatherings(params);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // 통계 데이터 가져오기
        Map<String, Object> stats = adminMapper.selectGatheringDashboardStats();

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);
        response.put("number", page);
        response.put("size", size);
        
        if (stats != null) {
            response.put("activeGatherings", stats.get("activeGatherings"));
            response.put("endedGatherings", stats.get("endedGatherings"));
        }

        return response;
    }

    // [★추가된 기능] 모임 삭제
    public void deleteGathering(int id) {
        adminMapper.deleteGathering(id);
    }
}