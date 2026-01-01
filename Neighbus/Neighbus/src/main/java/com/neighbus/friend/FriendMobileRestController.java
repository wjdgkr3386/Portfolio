package com.neighbus.friend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.neighbus.account.AccountDTO;
import com.neighbus.chat.ChatMapper;
import com.neighbus.chat.ChatMessageDTO;
import com.neighbus.chat.ChatRoomDTO;

@RestController
@RequestMapping("/api/mobile/friend")
public class FriendMobileRestController {

    private final FriendMapper friendMapper;
    private final FriendService friendService;
    private final ChatMapper chatMapper;

    public FriendMobileRestController(
            FriendMapper friendMapper,
            FriendService friendService,
            ChatMapper chatMapper
    ) {
        this.friendMapper = friendMapper;
        this.friendService = friendService;
        this.chatMapper = chatMapper;
    }

    /**
     * 친구 목록 + 친구 요청 목록
     */
    @GetMapping("/list")
    public Map<String, Object> friendList(
            @AuthenticationPrincipal AccountDTO user
    ) {
    	System.out.println("FriendMobileRestController - friendList");
        Map<String, Object> result = new HashMap<>();

        String myUuid = friendMapper.getMyUuid(user.getId());
        List<AccountDTO> friendList = friendMapper.getMyFriendList(user.getId());
        List<AccountDTO> requestList = friendMapper.getFriendRequests(user.getId());

        result.put("myUuid", myUuid);
        result.put("friends", friendList);
        result.put("requests", requestList);

        return result;
    }

    /**
     * 친구 요청
     */
    @PostMapping("/request")
    public Map<String, Object> requestFriend(
            @AuthenticationPrincipal AccountDTO user,
            @RequestBody Map<String, String> body
    ) {
        String uuid = body.get("uuid");
        int result = friendService.friendRequest(user, uuid);

        return Map.of("result", result);
    }

    /**
     * 친구 수락
     */
    @PostMapping("/accept")
    public Map<String, Object> acceptFriend(
            @AuthenticationPrincipal AccountDTO user,
            @RequestBody Map<String, Integer> body
    ) {
        int friendId = body.get("friendId");
        int result = friendService.addFriend(user, friendId);

        return Map.of("result", result);
    }

    /**
     * 친구 거절
     */
    @PostMapping("/refuse")
    public Map<String, Object> refuseFriend(
            @AuthenticationPrincipal AccountDTO user,
            @RequestBody Map<String, Integer> body
    ) {
        int friendId = body.get("friendId");
        int result = friendService.refuseFriend(user, friendId);

        return Map.of("result", result);
    }

    /**
     * 친구 삭제
     */
    @PostMapping("/delete")
    public Map<String, Object> deleteFriend(
            @AuthenticationPrincipal AccountDTO user,
            @RequestBody Map<String, Integer> body
    ) {
        int friendId = body.get("friendId");
        int result = friendService.deleteFriend(user, friendId);

        return Map.of("result", result);
    }

    /**
     * 친구 1:1 채팅방 생성 / 입장
     */
    @PostMapping("/chat/room")
    public Map<String, Object> getOrCreateChatRoom(
            @AuthenticationPrincipal AccountDTO user,
            @RequestBody Map<String, Integer> body
    ) {
    	System.out.println("FriendMobileRestController - getOrCreateChatRoom");
    	System.out.println(user);
    	System.out.println(body);
    	
        int myId = user.getId();
        int friendId = body.get("friendId");

        int minId = Math.min(myId, friendId);
        int maxId = Math.max(myId, friendId);
        String roomId = minId + "_" + maxId;

        ChatRoomDTO room = chatMapper.findByRoomId(roomId);

        if (room == null) {
            ChatRoomDTO newRoom = new ChatRoomDTO();
            newRoom.setRoomId(roomId);
            newRoom.setRoomName("친구 채팅");
            newRoom.setLinkedRecruitmentId(null);
            newRoom.setUser1Id(minId);
            newRoom.setUser2Id(maxId);

            chatMapper.insertRoom(newRoom);
        }

        List<ChatMessageDTO> history =
                chatMapper.findMessagesByRoomId(roomId);

        Map<String, Object> result = new HashMap<>();
        result.put("roomId", roomId);
        result.put("history", history);
        result.put("myId", user.getId());

        return result;
    }
}
