package com.neighbus.admin;

import com.neighbus.inquiry.InquiryService;
import com.neighbus.notice.NoticeDto;
import com.neighbus.notice.NoticeService;
import com.neighbus.freeboard.FreeboardService;
import com.neighbus.recruitment.RecruitmentService;
import com.neighbus.account.AccountDTO;
import com.neighbus.admin.ReportDTO; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // React 개발 서버 포트 허용
public class AdminRestController {

    private final InquiryService inquiryService;
    private final AdminService adminService;
    private final NoticeService noticeService;
    private final FreeboardService freeboardService;
    private final RecruitmentService recruitmentService;
    private final AdminMapper adminMapper;

    @Autowired
    public AdminRestController(InquiryService inquiryService, AdminService adminService, NoticeService noticeService, FreeboardService freeboardService, RecruitmentService recruitmentService, AdminMapper adminMapper) {
        this.inquiryService = inquiryService;
        this.adminService = adminService;
        this.noticeService = noticeService;
        this.freeboardService = freeboardService;
        this.recruitmentService = recruitmentService;
        this.adminMapper = adminMapper;
    }

    // ==========================================
    // 1. 회원(User) 관리 API
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "sortOrder", required = false) String sortOrder,
            @RequestParam(name = "sortField", required = false) String sortField) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> paginatedData = adminService.getUsersPaginated(page, size, role, sortOrder, sortField);
            response.put("status", 1);
            response.put("data", paginatedData); // React UserTable에서 res.data.list로 접근
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "회원 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/users/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int userId = request.get("id");
            int result = adminService.deleteUser(userId);

            if (result == 1) {
                response.put("status", 1);
                response.put("message", "회원이 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "회원 삭제 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "회원 삭제 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // 2. 문의(Inquiry) 관리 API
    // ==========================================

    @GetMapping("/inquiries")
    public List<Map<String, Object>> getInquiryList() {
        return inquiryService.getAllInquiries();
    }
    
    @PostMapping("/process-inquiry")
    public ResponseEntity<Map<String, Object>> processInquiry(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Object idObj = payload.get("inquiryId");
            Integer inquiryId = null;
            if (idObj instanceof Number) {
                inquiryId = ((Number) idObj).intValue();
            } else if (idObj != null) {
                inquiryId = Integer.parseInt(idObj.toString());
            }

            String action = (String) payload.get("action");
            int newStatus = (action.equals("answered")) ? 2 : 3; 

            if (inquiryId == null) {
                response.put("status", 0);
                response.put("message", "문의 ID가 유효하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            int result = inquiryService.updateInquiryState(inquiryId, newStatus); 

            if (result == 1) {
                response.put("status", 1);
                response.put("message", "상태가 성공적으로 업데이트되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "업데이트 실패: 해당 ID의 문의를 찾을 수 없습니다.");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "처리 중 서버 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // 3. 공지사항(Notice) 관리 API
    // ==========================================

    @GetMapping("/notices")
    public ResponseEntity<Map<String, Object>> getNoticeList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> notices = noticeService.getAllNotices();
            response.put("status", 1);
            response.put("data", notices);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "공지사항 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/notices/register")
    public ResponseEntity<Map<String, Object>> registerNotice(@RequestBody NoticeDto noticeDto, @AuthenticationPrincipal AccountDTO currentUser) {
        Map<String, Object> response = new HashMap<>();
        if (currentUser == null) {
            response.put("status", 0);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        try {
            noticeDto.setWriter(currentUser.getId());
            int result = noticeService.registerNotice(noticeDto);
            if (result == 1) {
                response.put("status", 1);
                response.put("message", "공지사항이 등록되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "공지사항 등록 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "공지사항 등록 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/notices/update")
    public ResponseEntity<Map<String, Object>> updateNotice(@RequestBody NoticeDto noticeDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = noticeService.updateNotice(noticeDto);
            if (result == 1) {
                response.put("status", 1);
                response.put("message", "공지사항이 수정되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "공지사항 수정 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "공지사항 수정 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/notices/delete")
    public ResponseEntity<Map<String, Object>> deleteNotice(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int id = request.get("id");
            int result = noticeService.deleteNotice(id);
            if (result == 1) {
                response.put("status", 1);
                response.put("message", "공지사항이 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "공지사항 삭제 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "공지사항 삭제 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // 4. 게시글(Post) 관리 API
    // ==========================================

    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getPostList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(name = "sortField", defaultValue = "id") String sortField) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> paginatedData = adminService.getPostsPaginated(page, size, keyword, sortOrder, sortField);
            response.put("status", 1);
            response.put("data", paginatedData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "게시글 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/posts/delete")
    public ResponseEntity<Map<String, Object>> deletePost(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int postId = request.get("id");
            adminService.deletePost(postId);
            response.put("status", 1);
            response.put("message", "게시글이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "게시글 삭제 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/posts/stats")
    public ResponseEntity<Map<String, Object>> getPostStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = adminService.getPostStats();
            response.put("status", 1);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "게시글 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // 5. 대시보드(Dashboard) API
    // ==========================================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            response.put("status", 1);
            response.put("data", stats); // React Dashboard에서 사용
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/dashboard/monthly-signups")
    public ResponseEntity<Map<String, Object>> getMonthlySignups() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> signups = adminService.getMonthlySignups();
            response.put("status", 1);
            response.put("data", signups);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "월별 가입자 수 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/dashboard/top-clubs")
    public ResponseEntity<Map<String, Object>> getTopClubsByMembers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> clubs = adminService.getTopClubsByMembers();
            response.put("status", 1);
            response.put("data", clubs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "동아리별 회원 수 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/dashboard/gatherings-by-category")
    public ResponseEntity<Map<String, Object>> getGatheringsByCategory() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> gatherings = adminService.getGatheringsByCategory();
            response.put("status", 1);
            response.put("data", gatherings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "카테고리별 모임 수 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // 6. 동아리(Club) 관리 API
    // ==========================================

    @GetMapping("/clubs")
    public ResponseEntity<Map<String, Object>> getClubList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(name = "sortField", required = false) String sortField) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> paginatedData = adminService.getClubsPaginated(page, size, keyword, sortOrder, sortField);
            response.put("status", 1);
            response.put("data", paginatedData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "동아리 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/clubs/delete")
    public ResponseEntity<Map<String, Object>> deleteClub(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int clubId = request.get("id");
            int result = adminService.deleteClub(clubId);
            if (result == 1) {
                response.put("status", 1);
                response.put("message", "동아리가 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "동아리 삭제 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "동아리 삭제 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // 7. 모임(Gathering) 관리 API
    // ==========================================

    @GetMapping("/gatherings")
    public ResponseEntity<Map<String, Object>> getGatheringList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(name = "sortField", required = false) String sortField) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> paginatedData = recruitmentService.getGatheringsPaginated(page, size, keyword, status, sortOrder, sortField);
            response.put("status", 1);
            response.put("data", paginatedData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "모임 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/gatherings/delete")
    public ResponseEntity<Map<String, Object>> deleteGathering(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int recruitmentId = request.get("id");
            int result = recruitmentService.deleteRecruitment(recruitmentId);
            if (result == 1) {
                response.put("status", 1);
                response.put("message", "모임이 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "모임 삭제 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "모임 삭제 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // 8. 갤러리(Gallery) 관리 API
    // ==========================================

    @GetMapping("/galleries")
    public ResponseEntity<Map<String, Object>> getGalleryList(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "clubName", required = false) String clubName,
            @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder,
            @RequestParam(name = "sortField", defaultValue = "id") String sortField) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> paginatedData = adminService.getGalleriesPaginated(page, size, keyword, clubName, sortOrder, sortField);
            response.put("status", 1);
            response.put("data", paginatedData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "갤러리 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/galleries/delete")
    public ResponseEntity<Map<String, Object>> deleteGallery(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int galleryId = request.get("id");
            int result = adminService.deleteGallery(galleryId);
            if (result == 1) {
                response.put("status", 1);
                response.put("message", "갤러리가 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 0);
                response.put("message", "갤러리 삭제 실패");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "갤러리 삭제 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/galleries/stats")
    public ResponseEntity<Map<String, Object>> getGalleryStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = adminService.getGalleryStats();
            response.put("status", 1);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "갤러리 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // 9. 신고(Report) 및 제재 관리 API
    // ==========================================

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getReportList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReportDTO> reports = adminService.getAllReports();
            response.put("status", 1);
            response.put("data", reports); // React ReportTable에 바인딩
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "신고 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/reports/totalCount")
    public ResponseEntity<Map<String, Object>> getReportTotalCount() {
        Map<String, Object> response = new HashMap<>();
        try {
            int count = adminService.getReportTotalCount();
            response.put("data", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reports/pendingCount")
    public ResponseEntity<Map<String, Object>> getReportPendingCount() {
        return getCountByStatus("PENDING");
    }

    @GetMapping("/reports/processingCount")
    public ResponseEntity<Map<String, Object>> getReportProcessingCount() {
        return getCountByStatus("PROCESSING");
    }

    @GetMapping("/reports/completedCount")
    public ResponseEntity<Map<String, Object>> getReportCompletedCount() {
        return getCountByStatus("COMPLETED");
    }

    @GetMapping("/reports/rejectedCount")
    public ResponseEntity<Map<String, Object>> getReportRejectedCount() {
        return getCountByStatus("REJECTED");
    }

    private ResponseEntity<Map<String, Object>> getCountByStatus(String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            int count = adminService.getReportStatusCount(status);
            response.put("data", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/reports/updateStatus")
    public ResponseEntity<Map<String, Object>> updateReportStatus(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int id = Integer.parseInt(request.get("id").toString());
            String status = (String) request.get("status");
            
            adminService.updateReportStatus(id, status);
            
            response.put("status", 1);
            response.put("message", "상태가 성공적으로 변경되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "상태 변경 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/reports/delete")
    public ResponseEntity<Map<String, Object>> deleteReport(@RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int id = request.get("id");
            adminService.deleteReport(id);
            
            response.put("status", 1);
            response.put("message", "신고 내역이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // React 프론트엔드와 연동되는 유저 차단 및 신고 처리 API
    @PostMapping("/reports/block")
    public ResponseEntity<Map<String, Object>> blockUser(@RequestBody Map<String, Object> request){
        System.out.println("=== AdminRestController: blockUser 호출됨 ===");
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 파라미터 안전하게 추출 (숫자형이 Long/Integer로 들어올 수 있음 처리)
            Long targetId = null;
            Object targetIdObj = request.get("targetId");
            if (targetIdObj instanceof Number) {
                targetId = ((Number) targetIdObj).longValue();
            } else if (targetIdObj instanceof String) {
                targetId = Long.parseLong((String) targetIdObj);
            }

            Integer banTime = null;
            Object banTimeObj = request.get("banTime");
            if (banTimeObj instanceof Number) {
                banTime = ((Number) banTimeObj).intValue();
            } else if (banTimeObj instanceof String) {
                banTime = Integer.parseInt((String) banTimeObj);
            }

            String type = (String) request.get("type"); // REPORT_TYPE (POST, COMMENT, USER 등)

            Integer reportId = null;
            Object reportIdObj = request.get("reportId");
            if (reportIdObj instanceof Number) {
                reportId = ((Number) reportIdObj).intValue();
            } else if (reportIdObj instanceof String) {
                reportId = Integer.parseInt((String) reportIdObj);
            }

            // 2. 유효성 검사
            if (targetId == null || banTime == null || type == null || reportId == null) {
                result.put("status", "error");
                result.put("message", "필수 파라미터가 누락되었습니다.");
                return ResponseEntity.badRequest().body(result);
            }

            // 3. 신고 대상 타입(type)에 따라 실제 정지할 userId 조회
            Integer userIdToBlock = null;

            switch (type) {
                case "USER":
                    userIdToBlock = targetId.intValue();
                    break;
                case "POST":
                    userIdToBlock = adminMapper.getPostWriterId(targetId);
                    break;
                case "GALLERY":
                    userIdToBlock = adminMapper.getGalleryWriterId(targetId);
                    break;
                case "GATHERING":
                    userIdToBlock = adminMapper.getGatheringWriterId(targetId);
                    break;
                case "COMMENT":
                    userIdToBlock = adminMapper.getCommentWriterId(targetId);
                    break;
                case "CLUB":
                    userIdToBlock = adminMapper.getClubOwnerId(targetId);
                    break;
                default:
                    result.put("status", "error");
                    result.put("message", "알 수 없는 신고 타입입니다: " + type);
                    return ResponseEntity.badRequest().body(result);
            }

            if (userIdToBlock == null) {
                result.put("status", "error");
                result.put("message", "작성자를 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(result);
            }

            // 4. 서비스 호출: 사용자 정지 및 신고 상태 완료 처리
            adminService.blockUser(userIdToBlock, banTime);
            adminService.updateReportStatus(reportId, "COMPLETED");

            result.put("status", 1);
            result.put("message", "사용자가 정지되었으며, 신고가 처리 완료되었습니다.");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "정지 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/backfill-chat-rooms")
    public ResponseEntity<Map<String, Object>> backfillChatRooms() {
        Map<String, Object> response = new HashMap<>();
        try {
            int createdCount = recruitmentService.backfillChatRooms();
            response.put("status", 1);
            response.put("message", createdCount + "개의 채팅방이 성공적으로 생성되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", 0);
            response.put("message", "채팅방 생성 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

}