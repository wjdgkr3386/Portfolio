package com.neighbus.friend;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import com.neighbus.account.AccountDTO; // import 추가

@Mapper
public interface FriendMapper {
	
	//내 UUID 조회
	public String getMyUuid(int id);
	//UUID가 존재하는지 확인
	public int checkUuid(String uuid);
	//UUID가 나인지 확인
	public int checkMyUuid(Map<String,Object> map);
	//이미 친추 요청을 했는지 확인
	public int checkFriendRequestByUuid(Map<String,Object> map);
	//이미 친추 요청을 했는지 확인
	public int checkFriendRequest(Map<String,Object> map);
	//이미 친구인지 확인
	public int checkFriend(Map<String,Object> map);
	//친구 요청
	public void friendRequest(Map<String,Object> map);
	//친구 추가
	public void addFriend(Map<String,Object> map);
	//친구 요청 상태 변경
	public void acceptFriendRequestState(Map<String,Object> map);
	//친구 요청 상태 변경
	public void refuseFriendRequestState(Map<String,Object> map);
	//친구 상태 삭제
	public void deleteFriendState(Map<String, Object> map);
	//친구 삭제
	public void deleteFriend(Map<String, Object> map);
	// 인터페이스 안에 메서드 추가
	public List<AccountDTO> getFriendRequests(int id);	
	//내 친구 리스트 조회 (반환 타입 변경)
	public List<AccountDTO> getMyFriendList(int id);
	
	// UUID로 ID 찾기
	public Integer getIdByUuid(String uuid);
}