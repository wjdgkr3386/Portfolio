package com.neighbus.freeboard;

import java.util.List;
import java.util.Map;

public interface FreeboardService {

    /** 게시글 작성 */
    void postInsert(FreeboardDTO freeboardDTO);
    
    /** 게시글 목록 조회 */
    List<FreeboardDTO> selectPostList();

    /** 게시글 목록 조회 (페이징) */
    List<Map<String,Object>> selectPostListWithPaging(FreeboardDTO freeboardDTO);

    /** 게시글 상세 조회 (조회수 증가 포함) */
    FreeboardDTO selectPostDetail(int id);
    
    // ==========================================================
    // 댓글 관련 메서드 추가
    // ==========================================================
    
    // 댓글 등록 (INSERT)
    boolean registerComment(CommentDTO commentDTO);
    
    // 댓글 삭제 (DELETE)
    boolean removeComment(int id, int userId);
    
    // 댓글 목록 조회 (SELECT)
    List<CommentDTO> getCommentList(int freeboardId); 
    
    /** 게시글 수정 */
    boolean updatePost(FreeboardDTO freeboardDTO, int userId);

    /** 게시글 삭제 */
    boolean deletePost(int id, int userId);
    
    
    // ===============================================================
    //좋아요 관련 기능
    // ===============================================================
    
	// 반응 추가
	Map<String, Object> insertReaction(Map<String, Object> request);
	
	// 반응 삭제
	Map<String, Object> deleteReaction(Map<String, Object> request);
	
	// 반응 변경 (좋아요 ↔ 싫어요)
	Map<String, Object> updateReaction(Map<String, Object> request);
}