package com.neighbus.freeboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.Util;
import com.neighbus.account.AccountDTO;
import com.neighbus.club.ClubMapper;


@RestController
@RequestMapping("/api/mobile/freeboard")
public class FreeboardMobileRestController {

	private final FreeboardService freeboardService;
    private final FreeboardMapper freeboardMapper;
    private final ClubMapper clubMapper;

    public FreeboardMobileRestController(FreeboardService freeboardService, FreeboardMapper freeboardMapper, ClubMapper clubMapper) {
        this.freeboardService = freeboardService;
        this.freeboardMapper = freeboardMapper;
        this.clubMapper = clubMapper;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
        FreeboardDTO freeboardDTO,
        @AuthenticationPrincipal AccountDTO user
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 사용자 정보 설정
            freeboardDTO.setUserId(user.getId());

            // 2. 페이징 및 검색 처리
            int searchCnt = freeboardMapper.searchCnt(freeboardDTO);
            Map<String, Integer> pagingMap = Util.searchUtil(searchCnt, freeboardDTO.getSelectPageNo(), 10);
            
            // DTO에 페이징 정보 세팅
            freeboardDTO.setSearchCnt(searchCnt);
            freeboardDTO.setSelectPageNo(pagingMap.get("selectPageNo"));
            freeboardDTO.setRowCnt(pagingMap.get("rowCnt"));
            freeboardDTO.setBeginPageNo(pagingMap.get("beginPageNo"));
            freeboardDTO.setEndPageNo(pagingMap.get("endPageNo"));
            freeboardDTO.setBeginRowNo(pagingMap.get("beginRowNo"));
            freeboardDTO.setEndRowNo(pagingMap.get("endRowNo"));
            
            // 3. 데이터 조회
            List<Map<String, Object>> posts = freeboardService.selectPostListWithPaging(freeboardDTO);
            
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("id", user.getId());
            List<Map<String,Object>> myClubList = clubMapper.getMyClub(map);
            
            // 4. JSON 응답 데이터 구성
            response.put("success", true);
            response.put("posts", posts);
            response.put("paging", pagingMap);
            response.put("myClubList", myClubList);
            response.put("freeboardDTO", freeboardDTO); // 필요 시 검색 정보 포함

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "데이터를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> postDetail(
        @PathVariable("id") int id,
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
        System.out.println("FreeboardRestController - postDetail");
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 게시글 상세 정보 조회
            FreeboardDTO post = freeboardService.selectPostDetail(id);
            if (post == null) {
                response.put("success", false);
                response.put("message", "해당 게시글을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 2. 현재 사용자 ID 처리
            int currentUserId = (accountDTO != null) ? accountDTO.getId() : 0;

            // 3. 반응(좋아요/싫어요) 데이터 조회
            Map<String, Object> reactionDataMap = new HashMap<>();
            reactionDataMap.put("userId", currentUserId);
            reactionDataMap.put("freeboardId", id);
            
            Map<String, Object> reaction = freeboardMapper.selectReaction(reactionDataMap);
            if (reaction == null) {
                reaction = new HashMap<>();
                reaction.put("likeCount", 0);
                reaction.put("dislikeCount", 0);
                reaction.put("userReaction", null);
            }

            // 4. 댓글 목록 조회
            List<CommentDTO> comments = freeboardService.getCommentList(id);

            // 5. JSON 응답 구성
            response.put("success", true);
            response.put("post", post);
            response.put("reaction", reaction);
            response.put("comments", comments);
            response.put("currentUserId", currentUserId);
            
            
            Util.print(comments);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/write")
    public ResponseEntity<?> write(
            @AuthenticationPrincipal AccountDTO accountDTO, 
            @RequestBody FreeboardDTO freeboardDTO
    ) {
        System.out.println("FreeboardRestController - write");
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 작성자 설정
            freeboardDTO.setWriter(accountDTO.getId());
            
            // 2. 게시글 저장
            freeboardService.postInsert(freeboardDTO);
            
            // 3. 성공 응답
            response.put("success", true);
            response.put("message", "게시글이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            // 4. 실패 응답
            response.put("success", false);
            response.put("message", "게시글 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // 댓글 등록
    @PostMapping("/comment")
    public ResponseEntity<?> registerComment(
        @RequestBody CommentDTO commentDTO,
        @AuthenticationPrincipal AccountDTO accountDTO 
    ) {
        Map<String, Object> response = new HashMap<>();
        
        if (accountDTO == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        commentDTO.setWriter(accountDTO.getId()); 
        if (commentDTO.getParent() == null) {
            commentDTO.setParent(0);
        }
        
        boolean success = freeboardService.registerComment(commentDTO);
        
        if (success) {
            response.put("success", true);
            response.put("message", "댓글이 등록되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "댓글 등록에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 댓글 삭제
    @DeleteMapping("/comment/{id}")
    public ResponseEntity<?> removeComment(
        @PathVariable("id") int id,
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
        Map<String, Object> response = new HashMap<>();

        if (accountDTO == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        boolean success = freeboardService.removeComment(id, accountDTO.getId());
        
        if (success) {
            response.put("success", true);
            response.put("message", "댓글이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "삭제 실패: 권한이 없거나 이미 삭제된 댓글입니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
    }
    
    //좋아요 삭제
	@DeleteMapping("/reaction/delete")
	public Map<String, Object> deleteReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardRestController - deleteReaction");
		request.put("userId", user.getId());
		return freeboardService.deleteReaction(request);
	}

	//좋아요 수정
	@PutMapping("/reaction/update")
	public Map<String, Object> updateReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardRestController - updateReaction");
		request.put("userId", user.getId());
		return freeboardService.updateReaction(request);
	}
	
	//좋아요 입력
	@PostMapping("/reaction/insert")
	public Map<String, Object> insertReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardRestController - insertReaction");
		request.put("userId", user.getId());
		return freeboardService.insertReaction(request);
	}
	
	// 좋아요 조회
    @GetMapping("/reaction/select/{freeboardId}")
    public ResponseEntity<Map<String, Object>> getReaction(
            @PathVariable int freeboardId,
            @AuthenticationPrincipal AccountDTO accountDTO
    ) {
        int userId = 0;
        if (accountDTO != null) {
            userId = accountDTO.getId();
        }

        Map<String, Object> reactionDataMap = new HashMap<>();
        reactionDataMap.put("userId", userId);
        reactionDataMap.put("freeboardId", freeboardId);

        Map<String, Object> reaction = freeboardMapper.selectReaction(reactionDataMap);
        if (reaction == null) {
            reaction = new HashMap<>();
            reaction.put("likeCount", 0);
            reaction.put("dislikeCount", 0);
            reaction.put("userReaction", null);
        }
        return ResponseEntity.ok(reaction);
    }
}
