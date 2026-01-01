package com.neighbus.club;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neighbus.account.AccountDTO;
import com.neighbus.util.PagingDTO;

@RestController
@RequestMapping("/api/club")
public class ClubRestController {

    @Autowired
    private ClubService clubService;

    @GetMapping("/checkName")
    public ResponseEntity<Map<String, Boolean>> checkClubName(@RequestParam("clubName") String clubName) {
        boolean isDuplicate = clubService.isClubNameDuplicate(clubName);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        return ResponseEntity.ok(response);
    }
    
    // 필터 검색
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchClubList(
            ClubDTO clubDTO,
            @AuthenticationPrincipal AccountDTO user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            PagingDTO<ClubDTO> result = clubService.getClubsWithPaging(clubDTO);
            response.put("clubs", result.getList());       // 클럽 목록
            response.put("pagingMap", result.getPagingMap()); // 페이징 정보
            
            if (user != null) {
                response.put("userGrade", user.getGrade());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
