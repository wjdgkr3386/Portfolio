package com.neighbus.gallery;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GalleryMapper {

	// 갤러리 테이블에 저장
	int insertGallery(GalleryDTO galleryDTO);
	int insertGalleryImage(GalleryDTO galleryDTO);
	int getGalleryMaxId(GalleryDTO galleryDTO);
	// 갤러리 업에이트
	void updateGallery(GalleryDTO galleryDTO);
	void deleteGalleryImage(GalleryDTO galleryDTO);
	//갤러리 삭제
	void deleteGalleryById(int galleryId);
	
	//입력된 갤러리 정보 가져오기
	List<Map<String, Object>> getGalleryList(GalleryDTO galleryDTO);
	List<Map<String, Object>> getGalleryImageById(int galleryId);
	
	//단일 게시판 정보 가져오기
	Map<String, Object> getGalleryById(@Param("id") int id);

	//페이징 처리
	int searchCnt(GalleryDTO galleryDTO);
	
	void insertComment(Map<String ,Object> map);
	
	void updateViewCount(@Param("id") int id);
	
    //좋아요 저장, 삭제, 수정, 조회
    void insertReaction(Map<String, Object> request);
	void deleteReaction(Map<String, Object> request);
	void updateReaction(Map<String, Object> request);
    Map<String, Object> selectReaction(Map<String, Object> map);
    
    // 내가 쓴 갤러리 게시글의 이미지 리스트 조회
    List<String> selectGalleryImagesByWriter(int userId);
}