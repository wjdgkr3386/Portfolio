package com.neighbus.gallery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.neighbus.Util;
import com.neighbus.account.AccountDTO;
import com.neighbus.club.ClubMapper;
import com.neighbus.s3.S3UploadService;

@Controller
@RequestMapping(value="/gallery")
public class GalleryController {

	@Autowired
	GalleryMapper galleryMapper;
	@Autowired
	GalleryService galleryService;
	@Autowired
	ClubMapper clubMapper;
	@Autowired
	S3UploadService s3UploadService;

	@GetMapping(value={"/",""})
	public ModelAndView galleryForm(
		GalleryDTO galleryDTO,
		@RequestParam(value = "keyword", required = false) String keyword,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("GalleryController - galleryForm");
        ModelAndView mav = new ModelAndView();
        galleryDTO.setId(user.getId());

		try {
			if(keyword != null) { galleryDTO.setKeyword(keyword); }

            int searchCnt = galleryMapper.searchCnt(galleryDTO); // 검색 개수
            
			Map<String, Integer> pagingMap = Util.searchUtil(searchCnt, galleryDTO.getSelectPageNo(), 6);
			galleryDTO.setSearchCnt(searchCnt);
			galleryDTO.setSelectPageNo(pagingMap.get("selectPageNo"));
			galleryDTO.setRowCnt(pagingMap.get("rowCnt"));
			galleryDTO.setBeginPageNo(pagingMap.get("beginPageNo"));
			galleryDTO.setEndPageNo(pagingMap.get("endPageNo"));
			galleryDTO.setBeginRowNo(pagingMap.get("beginRowNo"));
			galleryDTO.setEndRowNo(pagingMap.get("endRowNo"));

			List<Map<String ,Object>> galleryMapList = galleryService.getGalleryList(galleryDTO);

			for (Map<String, Object> galleryMap : galleryMapList) {
			    galleryMap.put("CONTENT", Util.convertAngleBracketsString((String) galleryMap.get("CONTENT"), "<br>"));
			    galleryMap.put("TITLE", Util.convertAngleBracketsString((String) galleryMap.get("TITLE"), "<br>"));
			}
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("id", user.getId());
            List<Map<String,Object>> myClubList = clubMapper.getMyClub(map);

			mav.addObject("galleryDTO", galleryDTO);
			mav.addObject("myClubList", myClubList);
			mav.addObject("pagingMap", pagingMap);
			mav.addObject("galleryMapList", galleryMapList);
			mav.addObject("keyword", keyword);

		}catch(Exception e) {
			System.out.println(e);
		}
		mav.setViewName("gallery/gallery");
        return mav;
	}

	@GetMapping(value="/write")
	public String writeForm(
			@AuthenticationPrincipal AccountDTO user,
	        Model model
	) {
		System.out.println("GalleryController - writeForm");
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", user.getId());
        List<Map<String,Object>> myClubList = clubMapper.getMyClub(map);
        model.addAttribute("post", new GalleryDTO());
        model.addAttribute("myClubList", myClubList);
		model.addAttribute("isEdit", false);
		return "gallery/write";
	}
	
	@GetMapping(value="/edit/{id}")
	public String edit(
			@PathVariable("id") int galleryId,
			@AuthenticationPrincipal AccountDTO user,
			Model model
	) {
		Map<String, Object> galleryMap = galleryMapper.getGalleryById(galleryId);
		model.addAttribute("galleryMap", galleryMap);
		model.addAttribute("isEdit", true);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", user.getId());
        List<Map<String,Object>> myClubList = clubMapper.getMyClub(map);
        model.addAttribute("myClubList", myClubList);
		return "gallery/write";
	}
	
	@GetMapping(value="/detail/{id}")
	public String detail(
		@PathVariable("id") int galleryId,
		@AuthenticationPrincipal AccountDTO user,
		Model model
	) {
		System.out.println("GalleryController - detail:"+galleryId);
		int userId = user.getId();
		Map<String, Object> galleryMap = galleryMapper.getGalleryById(galleryId);
		try {
			galleryService.updateViewCount(galleryId);
		}catch(Exception e) {
			System.out.println(e);
		}
		if(galleryMap == null || galleryMap.isEmpty()) {
			return "gallery/error";
		}
		
		
        Map<String, Object> reactionDataMap = new HashMap<String,Object>();
        reactionDataMap.put("userId", userId);
        reactionDataMap.put("galleryId", galleryId);
        Map<String, Object> reaction  = galleryMapper.selectReaction(reactionDataMap);
        if (reaction == null) {
            reaction = new HashMap<>();
            reaction.put("likeCount", 0);
            reaction.put("dislikeCount", 0);
            reaction.put("userReaction", null);
        }

		model.addAttribute("userId", userId);
		model.addAttribute("reaction", reaction);
		model.addAttribute("galleryMap", galleryMap);
		return "gallery/detail";
	}
	
	@PostMapping(value="/insertComment/{id}")
	public String insertComment(
		@AuthenticationPrincipal AccountDTO user,
		@PathVariable(value="id") int id,
		@RequestParam(value = "parent", required = false) Integer parent,
	    @RequestParam("comment") String comment
	) {
		System.out.println("GalleryController - insertComment:"+id);
		Map<String ,Object> map = new HashMap<String ,Object>();
		map.put("gallery_id", id);
		map.put("user_id", user.getId());
		map.put("parent", parent==null?0:parent);
		map.put("comment", comment);
		try {
			galleryService.insertComment(map);
		}catch(Exception e) {
			System.out.println(e);
		}
		return "redirect:/gallery/detail/" + id;
	}

	@GetMapping("/delete/{id}")
	public String deleteGallery(
		@PathVariable("id") int id,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("GalleryController - deleteGallery:" + id);
		if (user == null) {
			return "redirect:/account/login";
		}

		try {
			// 권한 체크: 작성자인지 확인
			Map<String, Object> galleryMap = galleryMapper.getGalleryById(id);
			if (galleryMap != null) {
				Integer writerId = (Integer) galleryMap.get("WRITER_ID");
				if (writerId != null && writerId == user.getId()) {
					// S3 이미지 삭제
					List<Map<String, Object>> galleryImageList = galleryMapper.getGalleryImageById(id);
					for (Map<String, Object> imageMap : galleryImageList) {
						String imgURL = (String) imageMap.get("IMG");
						if (imgURL != null && imgURL.length() > 44) {
							String key = imgURL.substring(44);
							s3UploadService.delete(key);
						}
					}
					// 갤러리 삭제
					galleryService.deleteGalleryById(id);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return "redirect:/gallery";
	}

}
