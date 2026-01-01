package com.neighbus.freeboard;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.account.AccountDTO;

@RequestMapping("/freeboard/api")
@RestController
public class FreeboardRestController {
	@Autowired
	FreeboardService freeboardService;
	
	@DeleteMapping("/deleteReaction")
	public Map<String, Object> deleteReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardRestController - deleteReaction");
		request.put("userId", user.getId());
		return freeboardService.deleteReaction(request);
	}

	@PutMapping("/updateReaction")
	public Map<String, Object> updateReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardRestController - updateReaction");
		request.put("userId", user.getId());
		return freeboardService.updateReaction(request);
	}
	
	@PostMapping("/insertReaction")
	public Map<String, Object> insertReaction(
		@RequestBody Map<String, Object> request,
		@AuthenticationPrincipal AccountDTO user
	) {
		System.out.println("FreeboardRestController - insertReaction");
		request.put("userId", user.getId());
		return freeboardService.insertReaction(request);
	}
}
