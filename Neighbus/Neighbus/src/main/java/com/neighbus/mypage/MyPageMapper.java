package com.neighbus.mypage;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.neighbus.account.AccountDTO; // 회원 DTO 재사용

@Mapper
public interface MyPageMapper {

    // 내 정보 불러오기 (회원 DTO 재사용)
	Map<String, Object> getMyInfo(String username);
    
    // 내가 쓴 글 목록
    List<Map<String, Object>> getMyPosts(String username);
    
    // 내가 쓴 댓글 목록
    List<Map<String, Object>> getMyComments(String username);
    
    // 내가 누른 좋아요 수
    int getMyLikesCount(String username);
    
    void addFriend(Map<String ,Object> map);

    int checkFriend(Map<String, Object> map);
    int checkUser(Map<String, Object> map);
    
    List<Map<String, Object>> getFriendState(int id);

    void insertFriend(Map<String,Object> map);
    void updateFriendStateAccept(Map<String,Object> map);
    void updateFriendStateReject(Map<String,Object> map);

    void updateProfile(Map<String, Object> updateData);
    void updateProfileImage(Map<String, Object> updateData);
    void delMyUser(AccountDTO accountDTO);

    // 회원 탈퇴를 위한 데이터 삭제
    void delMyUserInquiryComments(Integer userId);
    void delMyUserInquiries(Integer userId);
    void delMyUserGalleryComments(Integer userId);
    void delMyUserFreeboardComments(Integer userId);
    void delMyUserGalleries(Integer userId);
    void delMyUserFreeboards(Integer userId);
    void delMyUserFriends(Integer userId);
    void delMyUserFriendState(Integer userId);
    void delMyUserInquiryCommentsByWriter(Integer userId);
    void delMyUserClubs(Integer userId);
    void delMyUserNotices(Integer userId);
}
