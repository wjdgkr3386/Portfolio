package com.neighbus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neighbus.admin.AdminMapper;
import com.neighbus.recruitment.RecruitmentMapper; // [1] 새로 추가된 import

@Component
public class Scheduler {

	@Autowired
    AdminMapper adminMapper;
	
	@Autowired
	RecruitmentMapper recruitmentMapper; // [2] Mapper 의존성 주입 추가

	// 1. 기존 기능: 매일 0시(자정)에 정지된 유저 해제
	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void unblockUser() {
		adminMapper.unblockUser();
		System.out.println("[스케줄러] 정지 기간이 지난 회원의 정지를 해제했습니다.");
	}
	
	// 2. [★추가된 기능] 1분마다 실행되어 시간이 지난 모임을 마감 처리
	// (테스트용으로 1분마다 돌립니다. 나중에 1시간 간격 등으로 바꿀 수 있습니다.)
	@Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
	public void closeExpiredRecruitments() {
		int count = recruitmentMapper.updateExpiredRecruitments();
		
		// 변경된 모임이 있을 때만 로그 출력
		if (count > 0) {
			System.out.println("[스케줄러] 만남 시간이 지난 모임 " + count + "개를 '마감(CLOSED)' 처리했습니다.");
		}
	}
}