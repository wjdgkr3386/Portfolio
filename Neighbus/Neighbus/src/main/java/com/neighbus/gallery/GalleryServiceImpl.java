package com.neighbus.gallery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.neighbus.Util;
import com.neighbus.s3.S3UploadService;

@Service
@Transactional
public class GalleryServiceImpl implements GalleryService {

	@Autowired
	GalleryMapper galleryMapper;
	@Autowired
	S3UploadService s3UploadService;
	
	public void insertGallery(GalleryDTO galleryDTO) {
		System.out.println("GalleryServiceImpl - insertGallery");
		if(galleryMapper.insertGallery(galleryDTO)>0) {
			int galleryId = galleryMapper.getGalleryMaxId(galleryDTO);
			galleryDTO.setGalleryId(galleryId);
			galleryMapper.insertGalleryImage(galleryDTO);
		}
	}

	public void updateGallery(GalleryDTO galleryDTO) {
	    System.out.println("GalleryServiceImpl - updateGallery");
	    
	    try {
	    	// ★ 0. 유지할 이미지 ID 목록(existingIds)을 기반으로 삭제할 이미지 경로 계산
	        List<Map<String, Object>> currentImages = galleryMapper.getGalleryImageById(galleryDTO.getGalleryId());
	        List<Integer> keepIds = galleryDTO.getExistingIds();
	        if (keepIds == null) keepIds = new ArrayList<>();
	        
	        List<String> pathsToDelete = new ArrayList<>();
	        
	        if (currentImages != null) {
	            for (Map<String, Object> img : currentImages) {
	                Integer id = (Integer) img.get("ID");
	                String path = (String) img.get("IMG");
	                
	                // 유지할 목록에 없는 ID라면 삭제 대상에 추가
	                if (!keepIds.contains(id)) {
	                    pathsToDelete.add(path);
	                }
	            }
	        }
	        
	        // 기존 로직과 호환되도록 deletedExistingPathList에 설정
	        galleryDTO.setDeletedExistingPathList(pathsToDelete);
	    	
	        // 1. 삭제할 이미지가 있다면 S3에서 제거
	        if (galleryDTO.getDeletedExistingPathList() != null && !galleryDTO.getDeletedExistingPathList().isEmpty()) {
	            for (String path : galleryDTO.getDeletedExistingPathList()) {
	                String key = path.substring(path.indexOf("images/"));
	                s3UploadService.delete(key);
	            }
	            // DB에서 해당 경로들 삭제
	            galleryMapper.deleteGalleryImage(galleryDTO);
	        }

	        // 2. 신규 파일이 있다면 S3 업로드 및 DB 추가
	        if (galleryDTO.getFileList() != null && !galleryDTO.getFileList().isEmpty()) {
	            List<String> newFileNameList = new ArrayList<>();
	            for (MultipartFile file : galleryDTO.getFileList()) {
	                if (!file.isEmpty()) {
	                    String key = Util.s3Key();
	                    String imgUrl = s3UploadService.upload(key, file);
	                    newFileNameList.add(imgUrl);
	                }
	            }
	            galleryDTO.setFileNameList(newFileNameList);
	            galleryMapper.insertGalleryImage(galleryDTO);
	        }

	        // 3. 기본 게시글 정보 업데이트
	        galleryMapper.updateGallery(galleryDTO);

	    } catch (Exception e) {
	        System.err.println("갤러리 수정 중 오류 발생: " + e.getMessage());
	        // 필요 시 RuntimeException을 던져 @Transactional이 롤백되도록 처리
	        throw new RuntimeException(e);
	    }
	}
	
	//갤러리 정보와 갤러리 이미지 정보 가져오기
	public List<Map<String ,Object>> getGalleryList(GalleryDTO galleryDTO){
		System.out.println("GalleryServiceImpl - getGalleryList");
		return galleryMapper.getGalleryList(galleryDTO);
	}
	
	public void insertComment(Map<String ,Object> map) {
		System.out.println("GalleryServiceImpl - insertComment");
		galleryMapper.insertComment(map);
	}
	public void updateViewCount(int id) {
		galleryMapper.updateViewCount(id);
	}
	
	public void deleteGalleryById(int galleryId) {
		galleryMapper.deleteGalleryById(galleryId);
	}
	
    @Override
	public Map<String, Object> insertReaction(Map<String, Object> request) {
    	System.out.println("GalleryServiceImpl - insertReaction");
    	galleryMapper.insertReaction(request);
		return galleryMapper.selectReaction(request);
	}
	
	@Override
	public Map<String, Object> deleteReaction(Map<String, Object> request) {
    	System.out.println("GalleryServiceImpl - deleteReaction");
    	galleryMapper.deleteReaction(request);
		return galleryMapper.selectReaction(request);
	}
	
	@Override
	public Map<String, Object> updateReaction(Map<String, Object> request) {
    	System.out.println("GalleryServiceImpl - updateReaction");
    	galleryMapper.updateReaction(request);
		return galleryMapper.selectReaction(request);
	}
}