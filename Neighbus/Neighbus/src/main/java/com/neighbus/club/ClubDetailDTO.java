package com.neighbus.club;

import com.neighbus.account.AccountDTO;


public class ClubDetailDTO {
    private ClubDTO club;
    private boolean isLoggedIn;
    private boolean isMember;

    public ClubDetailDTO(ClubDTO club, AccountDTO accountDTO) {
        this.club = club;
        this.isLoggedIn = (accountDTO != null);
        this.isMember = false; // 기본값은 false
    }

	public ClubDTO getClub() {
		return club;
	}

	public void setClub(ClubDTO club) {
		this.club = club;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public boolean isMember() {
		return isMember;
	}

	public void setMember(boolean isMember) {
		this.isMember = isMember;
	}

	@Override
	public String toString() {
		return "ClubDetailDTO [club=" + club + ", isLoggedIn=" + isLoggedIn + ", isMember=" + isMember + "]";
	}
    
    
}
