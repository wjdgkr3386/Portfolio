package com.neighbus.friend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.neighbus.account.AccountDTO;
import com.neighbus.chat.ChatMapper;
import com.neighbus.chat.ChatMessageDTO;
import com.neighbus.chat.ChatRoomDTO;

@Controller
@RequestMapping(value = "/friend")
public class FriendController {

    @Autowired
    FriendMapper friendMapper;
    @Autowired
    FriendService friendService;
    @Autowired
    ChatMapper chatMapper;
    

    @GetMapping("/list")
    public String friendList(Model model, @AuthenticationPrincipal AccountDTO user) {
        String myUuid = friendMapper.getMyUuid(user.getId());
        model.addAttribute("myUuid", myUuid);

        List<AccountDTO> friendList = friendMapper.getMyFriendList(user.getId());
        model.addAttribute("friendList", friendList);
        
        List<AccountDTO> requestList = friendMapper.getFriendRequests(user.getId());
        model.addAttribute("requestList", requestList);

        return "friend/friendlist";
    }

    // 친구 요청
    @ResponseBody
    @PostMapping("/request")
    public int requestFriend(@AuthenticationPrincipal AccountDTO user, @RequestParam("uuid") String uuid) {
        return friendService.friendRequest(user, uuid);
    }
    
    // 친구 수락
    @ResponseBody
    @PostMapping("/accept")
    public int acceptFriend(@AuthenticationPrincipal AccountDTO user, @RequestParam("friendId") int friendId) {
        return friendService.addFriend(user, friendId);
    }

    // 친구 거절
    @ResponseBody
    @PostMapping("/refuse")
    public int refuseFriend(@AuthenticationPrincipal AccountDTO user, @RequestParam("friendId") int friendId) {
        return friendService.refuseFriend(user, friendId);
    }
    
    // 친구 삭제
    @ResponseBody
    @PostMapping("/delete")
    public int deleteFriend(@AuthenticationPrincipal AccountDTO user, @RequestParam("friendId") int friendId) {
        return friendService.deleteFriend(user, friendId);
    }
    
    // ★ [핵심 수정] 채팅방 생성/입장 로직
    @ResponseBody
    @PostMapping("/chat/room")
    public Map<String, Object> getOrCreateChatRoom(
            @AuthenticationPrincipal AccountDTO user,
            @RequestParam("friendId") int friendId
    ) {
        Map<String, Object> result = new HashMap<>();
        
        int myId = user.getId();
        
        // 1. 방 ID 생성 규칙: "작은ID_큰ID" (예: "3_5")
        // 중간에 언더바(_)를 넣어야 ID 구분이 확실해집니다.
        int minId = Math.min(myId, friendId);
        int maxId = Math.max(myId, friendId);
        String roomId = minId + "_" + maxId;
        
        // 2. 방이 DB에 있는지 확인
        ChatRoomDTO room = chatMapper.findByRoomId(roomId);
        
        // 3. 없으면 방 생성 (최초 1회)
        if (room == null) {
            ChatRoomDTO newRoom = new ChatRoomDTO();
            newRoom.setRoomId(roomId);
            newRoom.setRoomName("친구 채팅"); // 필요하면 "OO님과 대화" 처럼 수정 가능
            
            // ★ [중요] DB 스키마 변경에 따른 추가 데이터 세팅
            // 친구 채팅이므로 모집글 ID는 비우고(null), 친구 ID 두 개를 채워줍니다.
            newRoom.setLinkedRecruitmentId(null); 
            newRoom.setUser1Id(minId);
            newRoom.setUser2Id(maxId);
            
            chatMapper.insertRoom(newRoom);
        }
        
        // 4. 기존 대화 내역 가져오기
        List<ChatMessageDTO> history = chatMapper.findMessagesByRoomId(roomId);
        
        result.put("roomId", roomId);
        result.put("history", history);
        result.put("myId", user.getUsername()); 
        
        return result;
    }
}