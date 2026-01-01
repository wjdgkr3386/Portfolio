package com.neighbus.gallery;

import java.util.List;
import java.util.Map;

public interface GalleryService {

	void insertGallery(GalleryDTO galleryDTO) throws Exception;
	void updateGallery(GalleryDTO galleryDTO) throws Exception;
	List<Map<String ,Object>> getGalleryList(GalleryDTO galleryDTO) throws Exception;
	void insertComment(Map<String ,Object> map) throws Exception;
	void updateViewCount(int id) throws Exception;
	
	void deleteGalleryById(int galleryId);
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