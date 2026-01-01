package com.neighbus.freeboard;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FreeboardMapper {
	// 게시글 작성 (insertPost)
    // FreeboardDTO를 받아 처리하며, keyProperty="id" 설정에 따라 FreeboardDTO 객체의 id 필드가 업데이트됩니다.
    void postInsert(FreeboardDTO freeboardDTO);

    // 게시글 목록 조회 (selectPostList)
    // FreeboardDTO 리스트를 반환합니다.
    List<FreeboardDTO> selectPostList();

    // 게시글 목록 조회 (페이징)
    List<Map<String,Object>> selectPostListWithPaging(FreeboardDTO freeboardDTO);

    // 게시글 검색 개수 조회
    int searchCnt(FreeboardDTO freeboardDTO);
    
    // 게시글 상세 조회 (selectPostDetail)
    // 게시글 ID(int)를 받아 FreeboardDTO 객체를 반환합니다.
    FreeboardDTO selectPostDetail(@Param("id") int id);
    
    // 조회수 증가 (incrementViewCount)
    // 게시글 ID(int)를 받아 조회수를 1 증가시킵니다.
    void incrementViewCount(@Param("id") int id);
    
    // 댓글 생성 
    @Insert("INSERT INTO freeboard_comments (freeboard, parent, writer, content, created_at) " +
            "VALUES (#{freeboard}, #{parent}, #{writer}, #{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(CommentDTO commentDTO);
    // 삭제
    @Delete("DELETE FROM freeboard_comments WHERE id = #{id}")
    int deleteComment(@Param("id") int id);
    // 게시판 댓글 리스트
    List<CommentDTO> selectCommentList(@Param("freeboardId") int freeboardId);
    // ID로 댓글 조회
    CommentDTO selectCommentById(@Param("id") int id);
    
    // 게시글 수정
    void updatePost(FreeboardDTO freeboardDTO);

    // 게시글 삭제
    @Delete("DELETE FROM freeboards WHERE id = #{id}")
    void deletePost(@Param("id") int id);
    
    //좋아요 저장, 삭제, 수정, 조회
    void insertReaction(Map<String, Object> request);
	void deleteReaction(Map<String, Object> request);
	void updateReaction(Map<String, Object> request);
    Map<String, Object> selectReaction(Map<String, Object> map);
}
