package com.neighbus.club;

import java.time.LocalDateTime;

public class ClubMemberDTO {
	private int clubId; // 동아리ID (club_id, FK)
	private int userId; // 유저ID (user_id, FK)
	private LocalDateTime createdAt; // 가입일 (created_at)



	public ClubMemberDTO() {
	}

	public int getClubId() {
		return clubId;
	}

	public void setClubId(int clubId) {
		this.clubId = clubId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "ClubMemberDTO [clubId=" + clubId + ", userId=" + userId + ", createdAt=" + createdAt + "]";
	}

	
}
