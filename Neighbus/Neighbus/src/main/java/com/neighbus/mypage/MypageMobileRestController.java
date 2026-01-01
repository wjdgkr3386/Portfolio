package com.neighbus.mypage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.neighbus.Util;
import com.neighbus.account.AccountDTO;
import com.neighbus.account.AccountMapper;
import com.neighbus.account.AccountService;
import com.neighbus.club.ClubMapper;
import com.neighbus.recruitment.RecruitmentService;
import com.neighbus.s3.S3UploadService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/mobile/mypage")
public class MypageMobileRestController {

	@Autowired
	private MyPageService myPageService;
	@Autowired
	private MyPageMapper myPageMapper;
	@Autowired
	private AccountService accountService;
	@Autowired
	private AccountMapper accountMapper;
	@Autowired
	private ClubMapper clubMapper;
	@Autowired
	private RecruitmentService recruitmentService;
	@Autowired
	private S3UploadService s3UploadService;

	/**
	 * 마이페이지 정보 조회
	 */
	@GetMapping("/info")
	public ResponseEntity<Map<String, Object>> getMyPageInfo(@AuthenticationPrincipal AccountDTO loginUser) {
		if (loginUser == null) {
			return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
		}

		String username = loginUser.getUsername();

		Map<String, Object> response = new HashMap<>();
		
		// 1. 내 정보
		Map<String, Object> myInfo = myPageService.getMyPageInfo(username);
		response.put("myInfo", myInfo);

		// 2. 내가 쓴 게시글
		response.put("myPosts", myPageService.getMyPosts(username));

		// 3. 내가 쓴 댓글
		response.put("myComments", myPageService.getMyComments(username));

		// 4. 친구 상태
		response.put("friendState", myPageMapper.getFriendState(loginUser.getId()));

		// 5. 내 동아리 리스트
		response.put("myClubs", clubMapper.getMyClubs(loginUser.getId()));

		// 6. 내 모임 리스트
		response.put("recruitmentList", recruitmentService.getRecruitmentsByMyClubs(loginUser.getId()));

		// 7. 주소 데이터 (프로필 수정용)
		response.put("provinceList", accountMapper.getProvince());
		response.put("regionList", accountMapper.getCity());

		return ResponseEntity.ok(response);
	}

	/**
	 * 친구 추가 요청
	 */
	@PostMapping("/friend/add")
	public ResponseEntity<Map<String, Object>> addFriend(
			@AuthenticationPrincipal AccountDTO loginUser,
			@RequestBody Map<String, String> request) {
		
		String friendCode = request.get("friendCode");
		Map<String, Object> response = new HashMap<>();

		if (loginUser.getUserUuid().equals(friendCode)) {
			response.put("result", -2);
			response.put("message", "자기 자신에게 친구 요청을 보낼 수 없습니다.");
			return ResponseEntity.ok(response);
		}

		int result = myPageService.addFriend(loginUser.getId(), friendCode);

		if (result == -1) {
			response.put("result", -1);
			response.put("message", "해당 친구 코드를 가진 유저가 존재하지 않습니다.");
		} else if (result == 1) {
			response.put("result", 1);
			response.put("message", "친구 요청에 성공했습니다.");
		} else {
			response.put("result", 0);
			response.put("message", "친구 요청 처리 중 오류가 발생했습니다.");
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * 친구 요청 수락/거절
	 */
	@PostMapping("/friend/handle")
	public ResponseEntity<Map<String, Object>> handleFriendRequest(
			@AuthenticationPrincipal AccountDTO loginUser,
			@RequestBody Map<String, Integer> request) {
		
		int action = request.get("action"); // 1: 수락, 2: 거절
		int sender = request.get("sender");

		Map<String, Object> map = new HashMap<>();
		map.put("id", loginUser.getId());
		map.put("sender", sender);

		Map<String, Object> response = new HashMap<>();

		if (action == 1) {
			myPageService.friendAccept(map);
			response.put("result", 1);
			response.put("message", "친구 요청을 수락했습니다.");
		} else if (action == 2) {
			myPageService.friendReject(map);
			response.put("result", 1);
			response.put("message", "친구 요청을 거절했습니다.");
		} else {
			response.put("result", 0);
			response.put("message", "잘못된 요청입니다.");
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * 프로필 수정 (닉네임, 지역)
	 */
	@PostMapping("/profile/update")
	public ResponseEntity<Map<String, Object>> updateProfile(
			@AuthenticationPrincipal AccountDTO loginUser,
			@RequestBody Map<String, Object> request) {
		
		Map<String, Object> response = new HashMap<>();

		if (loginUser == null) {
			response.put("result", 0);
			response.put("message", "로그인이 필요합니다.");
			return ResponseEntity.status(401).body(response);
		}

		try {
			Map<String, Object> updateData = new HashMap<>();
			updateData.put("id", loginUser.getId());
			updateData.put("nickname", request.get("nickname"));
			updateData.put("province", request.get("province"));
			updateData.put("city", request.get("city"));

			myPageService.updateProfile(updateData);

			response.put("result", 1);
			response.put("message", "프로필이 성공적으로 수정되었습니다.");
		} catch (Exception e) {
			response.put("result", 0);
			response.put("message", "프로필 수정 중 오류가 발생했습니다.");
			e.printStackTrace();
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * 프로필 이미지 업로드
	 */
	@PostMapping("/profile/uploadImage")
	public ResponseEntity<Map<String, Object>> uploadProfileImage(
			@AuthenticationPrincipal AccountDTO loginUser,
			@RequestParam("profileImage") MultipartFile profileImage) {
		
		Map<String, Object> response = new HashMap<>();

		if (loginUser == null) {
			response.put("result", 0);
			response.put("message", "로그인이 필요합니다.");
			return ResponseEntity.status(401).body(response);
		}

		try {
			if (profileImage.isEmpty()) {
				response.put("result", 0);
				response.put("message", "이미지를 선택해주세요.");
				return ResponseEntity.ok(response);
			}

			String key = loginUser.getImage();
			key = key.equals("/img/profile/default-profile.png") ? Util.s3Key() : key;
			String imageUrl = s3UploadService.upload(key, profileImage);

			// DB 업데이트
			Map<String, Object> updateData = new HashMap<>();
			updateData.put("id", loginUser.getId());
			updateData.put("image", imageUrl);

			myPageService.updateProfileImage(updateData);

			response.put("result", 1);
			response.put("message", "프로필 이미지가 성공적으로 변경되었습니다.");
			response.put("imageUrl", imageUrl);
		} catch (IOException e) {
			response.put("result", 0);
			response.put("message", "프로필 이미지 업로드 중 오류가 발생했습니다.");
			e.printStackTrace();
		} catch (Exception e) {
			response.put("result", 0);
			response.put("message", "프로필 이미지 업데이트 중 오류가 발생했습니다.");
			e.printStackTrace();
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * 비밀번호 변경
	 */
	@PostMapping("/password/update")
	public ResponseEntity<Map<String, Object>> updatePassword(
			@AuthenticationPrincipal AccountDTO accountDTO,
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestBody Map<String, String> requestBody) {
		
		Map<String, Object> result = new HashMap<>();

		String currentPassword = requestBody.get("currentPassword");
		String newPassword = requestBody.get("password");

		Map<String, Object> map = new HashMap<>();
		map.put("id", accountDTO.getId());
		map.put("password", newPassword);
		map.put("currentPassword", currentPassword);
		map.put("myPassword", accountDTO.getPassword());

		int status = accountService.updatePwd(map);

		if (status == 1) {
			// 비밀번호 변경 성공 시 로그아웃
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null) {
				new SecurityContextLogoutHandler().logout(request, response, authentication);
			}
			result.put("result", 1);
			result.put("message", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
		} else {
			result.put("result", 0);
			result.put("message", "현재 비밀번호가 일치하지 않습니다.");
		}

		return ResponseEntity.ok(result);
	}

	/**
	 * 회원 탈퇴
	 */
	@PostMapping("/delete")
	public ResponseEntity<Map<String, Object>> deleteAccount(
			HttpServletRequest request,
			HttpServletResponse response,
			@AuthenticationPrincipal AccountDTO accountDTO) {
		
		Map<String, Object> result = new HashMap<>();

		if (accountDTO != null) {
			try {
				// 1. DB에서 회원 정보 삭제
				myPageService.delMyUser(accountDTO);

				// 2. 강제 로그아웃
				new SecurityContextLogoutHandler().logout(request, response,
						SecurityContextHolder.getContext().getAuthentication());

				result.put("result", 1);
				result.put("message", "회원 탈퇴가 완료되었습니다.");
			} catch (Exception e) {
				result.put("result", 0);
				result.put("message", "회원 탈퇴 처리 중 오류가 발생했습니다.");
				e.printStackTrace();
			}
		} else {
			result.put("result", 0);
			result.put("message", "로그인이 필요합니다.");
		}

		return ResponseEntity.ok(result);
	}

	/**
	 * 내 UUID 조회
	 */
	@GetMapping("/uuid")
	public ResponseEntity<Map<String, Object>> getMyUuid(@AuthenticationPrincipal AccountDTO loginUser) {
		Map<String, Object> response = new HashMap<>();
		
		if (loginUser == null) {
			response.put("result", 0);
			response.put("message", "로그인이 필요합니다.");
			return ResponseEntity.status(401).body(response);
		}

		response.put("result", 1);
		response.put("uuid", loginUser.getUserUuid());
		
		return ResponseEntity.ok(response);
	}
}