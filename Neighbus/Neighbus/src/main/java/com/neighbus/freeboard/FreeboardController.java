package com.neighbus.freeboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.neighbus.Util;
import com.neighbus.account.AccountDTO;
import com.neighbus.club.ClubMapper;

@Controller
@RequestMapping("/freeboard")
public class FreeboardController {

    @Autowired
    private FreeboardService freeboardService;
    @Autowired
    private ClubMapper clubMapper;
    @Autowired
    private FreeboardMapper freeboardMapper;
    
    @GetMapping({"", "/"})
    public String freeboardForm() {
    	return "redirect:/freeboard/list";
    }

    // -----------------------------------------------------------------
    // 게시글 목록, 작성 폼, 작성 처리 (기존 로직 유지)
    // -----------------------------------------------------------------
    @GetMapping("/list")
    public ModelAndView list(
		FreeboardDTO freeboardDTO,
		@RequestParam(value = "keyword", required = false) String keyword,
		@AuthenticationPrincipal AccountDTO user
	) {
        System.out.println("FreeboardController - list");
        ModelAndView mav = new ModelAndView();
        freeboardDTO.setUserId(user.getId());
        try {
        	if(keyword != null) { freeboardDTO.setKeyword(keyword); }

            int searchCnt = freeboardMapper.searchCnt(freeboardDTO); // 검색 개수
            
            Map<String, Integer> pagingMap = Util.searchUtil(searchCnt, freeboardDTO.getSelectPageNo(), 10);
            freeboardDTO.setSearchCnt(searchCnt);
            freeboardDTO.setSelectPageNo(pagingMap.get("selectPageNo"));
            freeboardDTO.setRowCnt(pagingMap.get("rowCnt"));
            freeboardDTO.setBeginPageNo(pagingMap.get("beginPageNo"));
            freeboardDTO.setEndPageNo(pagingMap.get("endPageNo"));
            freeboardDTO.setBeginRowNo(pagingMap.get("beginRowNo"));
            freeboardDTO.setEndRowNo(pagingMap.get("endRowNo"));
            
            List<Map<String,Object>> posts = freeboardService.selectPostListWithPaging(freeboardDTO);
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("id", user.getId());
            List<Map<String,Object>> myClubList = clubMapper.getMyClub(map);
            
          
            mav.addObject("freeboardDTO", freeboardDTO);
            mav.addObject("posts", posts);
            mav.addObject("myClubList", myClubList);
            mav.addObject("pagingMap", pagingMap);
            mav.addObject("keyword", keyword);
        } catch(Exception e) {
            System.out.println(e);
        }
		mav.setViewName("freeboard/postList");
        return mav;
    }

    @GetMapping("/write")
    public String write(
        @AuthenticationPrincipal AccountDTO accountDTO, 
    	Model model
    ) {
    	System.out.println("FreeboardController - write");
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", accountDTO.getId());
        List<Map<String,Object>> myClubList = clubMapper.getMyClub(map);
        model.addAttribute("myClubList", myClubList);
    	return "freeboard/write";
    }

    @ResponseBody
    @PostMapping("/write")
    public int write(
        @AuthenticationPrincipal AccountDTO accountDTO, 
    	FreeboardDTO freeboardDTO
    ) {
    	System.out.println("FreeboardController - write");
    	int cnt=0;
    	try {
	    	freeboardDTO.setWriter(accountDTO.getId());
	    	freeboardService.postInsert(freeboardDTO);
	    	cnt=1;
    	}catch(Exception e) {
    		System.out.println(e);
    	}
    	return cnt;
    }

    // -----------------------------------------------------------------
    // 1. 게시글 상세 및 댓글 목록 조회
    // -----------------------------------------------------------------
    @GetMapping("/{id}")
    public String postDetail(
        @PathVariable("id") int id, // 🚨 수정: 매개변수 이름 명시
        Model model,
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
    	System.out.println("FreeboardController - postDetail");
        FreeboardDTO post = freeboardService.selectPostDetail(id);
        if (post == null) {
            return "redirect:/freeboard/list"; 
        }
        
        int currentUserId = 0;
        if (accountDTO != null) {
            currentUserId = accountDTO.getId(); // AccountDTO의 getId() 호출
        }

        Map<String, Object> reactionDataMap = new HashMap<String,Object>();
        reactionDataMap.put("userId", accountDTO.getId());
        reactionDataMap.put("freeboardId", id);
        Map<String, Object> reaction  = freeboardMapper.selectReaction(reactionDataMap);
        if (reaction == null) {
            reaction = new HashMap<>();
            reaction.put("likeCount", 0);
            reaction.put("dislikeCount", 0);
            reaction.put("userReaction", null);
        }
        
        List<CommentDTO> comments = freeboardService.getCommentList(id);

        model.addAttribute("reaction", reaction);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentForm", new CommentDTO());
        model.addAttribute("currentUserId", currentUserId);
        
        return "freeboard/postDetail"; 
    }

    // -----------------------------------------------------------------
    // 2. 댓글 등록 처리 (API Endpoint)
    // -----------------------------------------------------------------
    @PostMapping("/comment")
    @ResponseBody
    public ResponseEntity<String> registerComment(
        @RequestBody CommentDTO commentDTO,
        @AuthenticationPrincipal AccountDTO accountDTO 
    ) {
        if (accountDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        commentDTO.setWriter(accountDTO.getId()); 
        if (commentDTO.getParent() == null) {
        	commentDTO.setParent(0);
        }
        
        boolean success = freeboardService.registerComment(commentDTO);
        
        if (success) {
            return ResponseEntity.ok("댓글이 등록되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 등록에 실패했습니다.");
        }
    }

    // -----------------------------------------------------------------
    // 3. 댓글 삭제 처리 (API Endpoint)
    // -----------------------------------------------------------------
    @DeleteMapping("/comment/{id}")
    @ResponseBody
    public ResponseEntity<String> removeComment(
        @PathVariable("id") int id, // 🚨 수정: 매개변수 이름 명시
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
        if (accountDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        // Service에서 댓글 ID와 유저 ID를 확인하여 권한 체크 후 삭제하는 로직 수행
        boolean success = freeboardService.removeComment(id, accountDTO.getId());
        
        if (success) {
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } else {
            // NOT_FOUND 대신, 권한 부족일 경우 FORBIDDEN을 반환할 수 있음
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("댓글 삭제에 실패했거나 권한이 없습니다.");
        }
    }

    // -----------------------------------------------------------------
    // 4. 게시글 수정 및 삭제
    // -----------------------------------------------------------------

    // 게시글 수정 폼
    @GetMapping("/edit/{id}")
    public String editPostForm(
        @PathVariable("id") int id, // 🚨 수정: 매개변수 이름 명시
        Model model, 
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
    	System.out.println("FreeboardController - editPostForm");
        if (accountDTO == null) {
            return "redirect:/account/login";
        }

        FreeboardDTO post = freeboardService.selectPostDetail(id);
        if (post == null || post.getWriter() != accountDTO.getId()) {
            // 🚨 개선: 권한 없음 메시지를 추가하여 사용자에게 피드백 제공
            return "redirect:/freeboard/" + id + "?error=permission"; 
        }

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", accountDTO.getId());
        List<Map<String,Object>> myClubList = clubMapper.getMyClub(map);
        model.addAttribute("myClubList", myClubList);
        model.addAttribute("post", post);
        return "freeboard/write";
    }

    // 게시글 수정 처리
    @ResponseBody
    @PostMapping("/edit/{id}")
    public int updatePost(
        @PathVariable("id") int id, // 🚨 수정: 매개변수 이름 명시
        FreeboardDTO freeboardDTO, 
        @AuthenticationPrincipal AccountDTO accountDTO
    ) {
    	System.out.println("FreeboardController - updatePost");
    	Map<String,Object> map = new HashMap<String,Object>();
    	int cnt = 0;
    	try {
	        freeboardDTO.setId(id);
	        freeboardService.updatePost(freeboardDTO, accountDTO.getId());
	        cnt = 1;
    	}catch(Exception e) {
    		System.out.println(e);
    	}
        return cnt;
    }

    // 게시글 삭제 처리 (GET 요청 대신 POST/DELETE 요청을 권장하지만, 현재 GET 유지)
    @GetMapping("/delete/{id}") 
    public String deletePost(
        @PathVariable("id") int id, // 🚨 수정: 매개변수 이름 명시
        @AuthenticationPrincipal AccountDTO accountDTO, 
        RedirectAttributes redirectAttributes
    ) {
        if (accountDTO == null) {
            return "redirect:/account/login";
        }

        boolean success = freeboardService.deletePost(id, accountDTO.getId());

        if (success) {
            redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "게시글 삭제에 실패했거나 권한이 없습니다."); // 에러 메시지 변경
        }
        return "redirect:/freeboard";
    }

    
    
    
}