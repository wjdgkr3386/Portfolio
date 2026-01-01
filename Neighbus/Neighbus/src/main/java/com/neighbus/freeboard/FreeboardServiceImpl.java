package com.neighbus.freeboard;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neighbus.alarm.NotificationService;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class FreeboardServiceImpl implements FreeboardService {

    @Autowired
    private FreeboardMapper freeboardMapper;
    @Autowired
    private NotificationService notificationService;
    
    

    public FreeboardServiceImpl(FreeboardMapper freeboardMapper, NotificationService notificationService) {
		super();
		this.freeboardMapper = freeboardMapper;
		this.notificationService = notificationService;
	}

	/**
     * 게시글 작성
     */
    @Override
    public void postInsert(FreeboardDTO freeboardDTO) {
        System.out.println("FreeboardServiceImpl - insertPost");
        freeboardMapper.postInsert(freeboardDTO);
    }

    /**
     * 게시글 목록 조회
     */
    @Override
    public List<FreeboardDTO> selectPostList() {
        System.out.println("FreeboardServiceImpl - selectPostList");
        return freeboardMapper.selectPostList();
    }

    /**
     * 게시글 목록 조회 (페이징)
     */
    @Override
    public List<Map<String,Object>> selectPostListWithPaging(FreeboardDTO freeboardDTO) {
        System.out.println("FreeboardServiceImpl - selectPostListWithPaging");
        return freeboardMapper.selectPostListWithPaging(freeboardDTO);
    }

    /**
     * 게시글 상세 조회 및 조회수 증가
     */
    @Override
    public FreeboardDTO selectPostDetail(int id) {
        System.out.println("FreeboardServiceImpl - selectPostDetail");
        
        // 1. 조회수 증가 (Mapper에 구현되어 있어야 함)
        freeboardMapper.incrementViewCount(id);
        
        // 2. 상세 정보 조회
        return freeboardMapper.selectPostDetail(id);
    }

    // ==========================================================
    // 댓글 관련 메서드 구현
    // ==========================================================

    @Override
    public boolean registerComment(CommentDTO commentDTO) {
        // 1. 댓글 삽입 실행 (결과를 int로 받음)
    	// 1. 댓글 삽입 실행
        int result = freeboardMapper.insertComment(commentDTO);
        
        // 🚨추가: result 값 확인
        System.out.println("DEBUG: insertComment Result Value: " + result);

        // 2. 성공 시(1개 이상 삽입) 알림 발송 로직 실행
        if (result > 0) {
            System.out.println("DEBUG: Notification Logic Initiated."); // 🚨추가
            sendCommentNotification(commentDTO);
        } else {
            System.out.println("DEBUG: Notification Skipped (Result <= 0)."); // 🚨추가
        }
        
        // 3. 결과 반환
        return result > 0;
    }

    private void sendCommentNotification(CommentDTO commentDTO) {
        try {
            System.out.println("DEBUG: Entered sendCommentNotification method.");
            
            // 게시글 정보 가져오기
            FreeboardDTO board = freeboardMapper.selectPostDetail(commentDTO.getFreeboard()); 

            if (board == null) {
                System.err.println("DEBUG ERROR: 게시글 없음! ID: " + commentDTO.getFreeboard());
                return;
            }

            int postOwnerId = board.getWriter(); 
            int commenterId = commentDTO.getWriter(); 
            
            System.out.println("DEBUG: 작성자(" + postOwnerId + ") vs 댓글단사람(" + commenterId + ")");

            // 1. 자기 자신의 글에 댓글 단 경우 알림 안 보냄
            if (postOwnerId == commenterId) {
                System.out.println("DEBUG: 본인 게시글이라 알림 스킵");
                return;
            }

            // 2. 알림 내용 만들기
            String content = "새로운 댓글: " + commentDTO.getContent();
            // DB 컬럼 길이(255자) 넘지 않게 자르기 (안전장치)
            if (content.length() > 50) { 
                content = content.substring(0, 50) + "...";
            }
            
            String url = "/freeboard/" + commentDTO.getFreeboard();

            // 알림 전송 (이 부분이 빠져 있었음)
            notificationService.send(postOwnerId, "댓글등록", content, url);
            
            System.out.println("DEBUG: 알림 전송 요청 완료 (Service.send 호출됨)");

        } catch (Exception e) {
            System.err.println("알림 전송 중 에러: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean removeComment(int id, int userId) {
        CommentDTO comment = freeboardMapper.selectCommentById(id);
        if (comment != null && comment.getWriter() == userId) {
            return freeboardMapper.deleteComment(id) > 0;
        }
        return false;
    }

    @Override
    public List<CommentDTO> getCommentList(int freeboardId) {
        // 댓글 목록 조회
        return freeboardMapper.selectCommentList(freeboardId);
    }

    @Override
    public boolean updatePost(FreeboardDTO freeboardDTO, int userId) {
        FreeboardDTO post = freeboardMapper.selectPostDetail(freeboardDTO.getId());
        if (post != null && post.getWriter() == userId) {
            freeboardMapper.updatePost(freeboardDTO);
            return true;
        }
        return false;
    }

    @Override
    public boolean deletePost(int id, int userId) {
        FreeboardDTO post = freeboardMapper.selectPostDetail(id);
        if (post != null && post.getWriter() == userId) {
            freeboardMapper.deletePost(id);
            return true;
        }
        return false;
    }

    @Override
	public Map<String, Object> insertReaction(Map<String, Object> request) {
    	System.out.println("FreeboardServiceImpl - insertReaction");
		freeboardMapper.insertReaction(request);
		return freeboardMapper.selectReaction(request);
	}
	
	@Override
	public Map<String, Object> deleteReaction(Map<String, Object> request) {
    	System.out.println("FreeboardServiceImpl - deleteReaction");
		freeboardMapper.deleteReaction(request);
		return freeboardMapper.selectReaction(request);
	}
	
	@Override
	public Map<String, Object> updateReaction(Map<String, Object> request) {
    	System.out.println("FreeboardServiceImpl - updateReaction");
		freeboardMapper.updateReaction(request);
		return freeboardMapper.selectReaction(request);
	}
    
}