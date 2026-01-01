package com.neighbus.gallery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.neighbus.Util;
import com.neighbus.account.AccountDTO;
import com.neighbus.s3.S3UploadService;

@RequestMapping("/gallery/api")
@RestController
public class GalleryRestController {

	@Autowired
	GalleryService galleryService;
	@Autowired
	GalleryMapper galleryMapper;
	
	@Autowired
	S3UploadService s3UploadService;
	
	@PostMapping(value="/insertGallery")
	public Map<String, Object> insertGallery(
		@ModelAttribute GalleryDTO galleryDTO,
		@AuthenticationPrincipal AccountDTO user
	){
		System.out.println("GalleryRestController - insertGallery");
		Map<String ,Object> response = new HashMap<String, Object>();
		List<MultipartFile> fileList = galleryDTO.getFileList();
		List<String> fileNameList = new ArrayList<String>();
		galleryDTO.setWriter(user.getId());
		int status = 0;

		try {
			// 이미지 저장
			for(MultipartFile file : fileList) {
				String key = Util.s3Key();
				String imgUrl = s3UploadService.upload(key, file);
				fileNameList.add(imgUrl);
			}
			galleryDTO.setFileNameList(fileNameList);
			
			galleryService.insertGallery(galleryDTO);
			status = 1;
		}catch(Exception e) {
			System.out.println(e);
			for(String fileName : fileNameList) {
				s3UploadService.delete(fileName);
			}
			status = -1;
		}

		response.put("status", status);
		return response;
	}
	

	@PostMapping(value="/updateGallery")
	public Map<String, Object> updateGallery(
		@ModelAttribute GalleryDTO galleryDTO,
		@AuthenticationPrincipal AccountDTO user
	){
		System.out.println("GalleryRestController - updateGallery");
		galleryDTO.setWriter(user.getId());
		int status=0;
		Map<String ,Object> response = new HashMap<String, Object>();
		
		try {
			galleryService.updateGallery(galleryDTO);
			status = 1;
		}catch(Exception e) {
			System.out.println(e);
			status = -1;
		}

		response.put("status", status);
		
		return response;
	}
	
    @DeleteMapping("/delete/{galleryId}")
    public Map<String, Object> deleteGallery(
    		@PathVariable("galleryId") int galleryId,
            @AuthenticationPrincipal AccountDTO user,
            GalleryDTO galleryDTO
    ) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String,Object>> galleryImageList = galleryMapper.getGalleryImageById(galleryId);
        try {
            galleryService.deleteGalleryById(galleryId);
            for(Map<String,Object> imageMap : galleryImageList) {
            	String imgURL = (String) imageMap.get("IMG");
            	if(imgURL != null && imgURL.length()>44) {
            		String key = imgURL.substring(44);
            		s3UploadService.delete(key);
            	}
            }
            result.put("status", 1);
        } catch (Exception e) {
            result.put("status", 0);
            result.put("message", "삭제 실패");
        }

        return result;
    }
    
	
	@DeleteMapping("/deleteReaction")
	public Map<String, Object> deleteReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("ServiceRestController - deleteReaction");
		request.put("userId", user.getId());
		return galleryService.deleteReaction(request);
	}

	@PutMapping("/updateReaction")
	public Map<String, Object> updateReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("ServiceRestController - updateReaction");
		request.put("userId", user.getId());
		return galleryService.updateReaction(request);
	}
	
	@PostMapping("/insertReaction")
	public Map<String, Object> insertReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("ServiceRestController - insertReaction");
		request.put("userId", user.getId());
		return galleryService.insertReaction(request);
	}
}