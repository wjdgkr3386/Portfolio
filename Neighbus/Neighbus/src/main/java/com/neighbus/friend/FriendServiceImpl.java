package com.neighbus.friend;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neighbus.account.AccountDTO;
import com.neighbus.alarm.NotificationService;

@Transactional
@Service
public class FriendServiceImpl implements FriendService{

	@Autowired
	FriendMapper friendMapper;
	
	@Autowired
	NotificationService notificationService;
	
	public static final int FRIEND_SUCCESS = 1;                 // 요청 또는 처리 성공
	public static final int FRIEND_NOT_FOUND = -1;              // 친구 요청 대상의 UUID에 해당하는 사용자가 존재하지 않음
	public static final int FRIEND_ADD_SELF_FORBIDDEN = -2;     // 자기 자신에게 친구 추가를 시도함
	public static final int FRIEND_REQUEST_PENDING = -3;        // 이미 친구 요청이 대기 중인 상태 (요청을 보냈거나 받았거나)
	public static final int FRIEND_REQUEST_NOT_FOUND = -4;      // 친구 요청 레코드가 존재하지 않아 수락/거절/취소 불가
	public static final int FRIEND_ALREADY_EXISTS = -5;         // 이미 친구 관계임 (삭제 실패 시 사용)
	public static final int FRIEND_FAILED = -99;                // 친구 추가/수락/삭제 과정 중 데이터베이스 등 일반적인 오류 발생
	
    //친구 요청
	public int friendRequest(AccountDTO user, String friendUuid) {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("myId", user.getId());
		map.put("myUuid", user.getUserUuid());
		map.put("friendUuid", friendUuid);
		
		try {
			//존재하는 UUID인지 확인
			Integer friendId = friendMapper.getIdByUuid(friendUuid);
			if(friendId == null) return FRIEND_NOT_FOUND;

			//UUID가 나인지 확인
			if(friendMapper.checkMyUuid(map)>0) return FRIEND_ADD_SELF_FORBIDDEN;
			//이미 친추 요청을 했는지 확인
			if(friendMapper.checkFriendRequestByUuid(map)>0) return FRIEND_REQUEST_PENDING;
			//이미 친구인지 확인
			if(friendMapper.checkFriend(map)>0) return FRIEND_ALREADY_EXISTS;
			//친구 요청
			friendMapper.friendRequest(map);
			
			// 알림 전송
			notificationService.send(friendId, "FRIEND_REQUEST", user.getNickname() + "님이 친구 요청을 보냈습니다.", "/friend/list");
			
			return FRIEND_SUCCESS;
		}catch(Exception e) {
			System.out.println(e);
			return FRIEND_FAILED;
		}
	}

	//친구 수락
	public int addFriend(AccountDTO user, int friendId) {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("myId", user.getId());
		map.put("friendId", friendId);

		System.out.println("myId: "+ user.getId());
		System.out.println("friendId: "+ friendId);
		
		try {
			//이미 친추 요청을 했는지 확인
			if(friendMapper.checkFriendRequest(map)<1) return FRIEND_REQUEST_NOT_FOUND;
			//친구 요청 상태 변경 (수락)
			friendMapper.acceptFriendRequestState(map);
			friendMapper.addFriend(map);
			return FRIEND_SUCCESS;
		}catch(Exception e) {
			System.out.println(e);
			return FRIEND_FAILED;
		}
	}
	
	//친구 거절
	public int refuseFriend(AccountDTO user, int friendId) {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("myId", user.getId());
		map.put("friendId", friendId);

		try {
			//이미 친추 요청을 했는지 확인
			if(friendMapper.checkFriendRequest(map)<1) return FRIEND_REQUEST_NOT_FOUND;
			//친구 요청 상태 변경 (거절)
			friendMapper.refuseFriendRequestState(map);
			return FRIEND_SUCCESS;
		}catch(Exception e) {
			System.out.println(e);
			return FRIEND_FAILED;
		}
	}

	public int deleteFriend(AccountDTO user, int friendId) {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("myId", user.getId());
		map.put("friendId", friendId);

		try {
			//친구, 상태 삭제
			friendMapper.deleteFriendState(map);
			friendMapper.deleteFriend(map);
			return FRIEND_SUCCESS;
		}catch(Exception e) {
			System.out.println(e);
			return FRIEND_FAILED;
		}
	}
	
	
}
