package com.neighbus.chat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.neighbus.account.AccountDTO;
import com.neighbus.recruitment.RecruitmentDTO;
import com.neighbus.recruitment.RecruitmentService;

@Controller
@RequestMapping("/chat")
public class RoomController {

    private final ChatMapper chatMapper;
    private final RecruitmentService recruitmentService;

    @Autowired
    public RoomController(ChatMapper chatMapper, RecruitmentService recruitmentService) {
        this.chatMapper = chatMapper;
        this.recruitmentService = recruitmentService;
    }

    @GetMapping("/room")
    public String rooms(Model model, @AuthenticationPrincipal AccountDTO user) {
        // 1. 로그인한 유저의 username(아이디) 가져오기
        String username = user.getUsername();
        
        // 2. 해당 유저가 가입한 채팅방만 조회 (Mapper 메서드 변경)
        List<ChatRoomDTO> chatRooms = chatMapper.findRoomsByUsername(username);
        
        model.addAttribute("chatrooms", chatRooms);
        return "chat/room"; 
    }

    // 2. 모든 채팅방 목록 반환 (API)
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoomDTO> room() {
        return chatMapper.findAllRooms();
    }

 // RoomController.java의 createRoom 메서드만 교체하세요.

    // 3. 채팅방 생성 (모집글 전용)
    @PostMapping("/room")
    @ResponseBody
    public ChatRoomDTO createRoom(@RequestParam("roomId") String roomId, // 여기서 roomId는 모집글 ID(숫자형 문자열)
                                  @RequestParam("name") String name) {
    	System.out.println(">>> [디버깅] createRoom 호출됨! ID: " + roomId);
        // 중복 체크
        ChatRoomDTO existingRoom = chatMapper.findByRoomId(roomId);
        if (existingRoom != null) {
            return existingRoom;
        }
        
        // 2. 방 생성 객체 설정
        ChatRoomDTO newRoom = new ChatRoomDTO();
        newRoom.setRoomId(roomId);   // 예: "15"     
        newRoom.setRoomName(name);
        
        // ★ 핵심 추가: 모집글 ID를 정수형으로 변환하여 연결 고리 설정
        // 친구 채팅방 생성은 이 메서드가 아니라 FriendController에서 별도로 user1Id, user2Id를 세팅해야 함
        try {
            // 공백 제거(.trim()) 추가 (혹시 모를 공백 방지)
            int id = Integer.parseInt(roomId.trim()); 
            newRoom.setLinkedRecruitmentId(id);
            
            // ★ [확인용 로그] 콘솔창을 확인해주세요!
            System.out.println("========================================");
            System.out.println("1. 받은 Room ID: [" + roomId + "]");
            System.out.println("2. 변환된 정수 ID: " + id);
            System.out.println("3. DTO에 저장된 값: " + newRoom.getLinkedRecruitmentId());
            System.out.println("========================================");
            
        } catch (NumberFormatException e) {
            System.out.println("★ 에러: 숫자 변환 실패! (NULL로 저장됨)");
            newRoom.setLinkedRecruitmentId(null);
        }
        
        chatMapper.insertRoom(newRoom);
        return newRoom;
    }

    // 4. 채팅방 입장 화면
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable("roomId") String roomId, 
            @AuthenticationPrincipal AccountDTO accountDTO) {
        
        if (accountDTO == null) {
            // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
            return "redirect:/account/login";
        }
        
        int recruitmentId = Integer.parseInt(roomId);
        RecruitmentDTO recruitment = recruitmentService.findById(recruitmentId);
        
        if (recruitment == null) {
            // 모집글이 없는 경우
            return "redirect:/"; // 혹은 에러 페이지
        }
        
        // 해당 모집에 가입했는지 확인
        boolean isMember = recruitmentService.isMember(recruitmentId, accountDTO.getId());
        
        if (!isMember) {
            // 가입하지 않은 경우
            model.addAttribute("message", "가입한 모임의 채팅방에만 입장할 수 있습니다.");
            model.addAttribute("searchUrl", "/");
            return "admin/message"; 
        }
        // 과거 채팅 내역 가져오기
        List<ChatMessageDTO> messageHistory = chatMapper.findMessagesByRoomId(roomId);
        model.addAttribute("messageHistory", messageHistory); // 모델에 담기

        model.addAttribute("roomId", roomId);
        model.addAttribute("recruitment", recruitment);
        model.addAttribute("user", accountDTO.getUsername());
        
        return "chat/roomdetail";
    }
}
