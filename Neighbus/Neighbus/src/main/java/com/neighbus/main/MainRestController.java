package com.neighbus.main;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.club.ClubMapper;


@RestController
@RequestMapping("/api/main")
public class MainRestController {

	@Autowired
	ClubMapper clubMapper;
	
	@PostMapping("/filterRegion")
	public Map<String,Object> filterRegion(
		SearchDTO searchDTO
	) {
		System.out.println("MainRestController - filterRegion");
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("newClubList", clubMapper.getNewClub(searchDTO));
		map.put("popularClubList", clubMapper.getPopularClub(searchDTO));
		map.put("searchDTO", searchDTO);
		return map;
	}
}
