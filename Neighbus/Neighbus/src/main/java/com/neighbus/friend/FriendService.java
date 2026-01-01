package com.neighbus.friend;

import com.neighbus.account.AccountDTO;

public interface FriendService {

	//친구 요청
	public int friendRequest(AccountDTO user, String friendUuid);
	//친구 추가
	public int addFriend(AccountDTO user, int friendId);
	//친구 거절
	public int refuseFriend(AccountDTO user, int friendId);
	//친구 삭제
	public int deleteFriend(AccountDTO user, int friendId);
	
	
	
}
