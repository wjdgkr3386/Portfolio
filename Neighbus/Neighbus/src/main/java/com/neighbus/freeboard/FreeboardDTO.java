package com.neighbus.freeboard;

import java.time.LocalDateTime;

public class FreeboardDTO {
    private int id; // 게시글 id
    private String title;
    private String content;
    private int writer; // 작성자 id
    private LocalDateTime createdAt;
    private int viewCount;
    private String writerNickname;
    private int clubId; // 동아리 id
    private String clubName; // 동아리 이름
    private String image; // 이미지 경로
    
    
    // 페이징 관련 필드
    private int boardAllCnt;      // 게시글 전체 개수
    private int searchCnt;      // 게시글 전체 개수
    private int selectPageNo;		// 선택된 페이지 번호
    private int rowCnt;		       // 한번에 보여될 행의 개수
    private int beginPageNo;       // 보여줄 시작 페이지 번호
    private int endPageNo;         // 보여줄 끝 페이지 번호
    private int beginRowNo;        // 보여줄 시작 행 번호
    private int endRowNo;          // 보여줄 끝 행 번호

    // 검색
    private String keyword;        // 검색 키워드
    private int userId;
    private int selectClubId;
    private Integer prev;
    private Integer next;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getWriter() {
		return writer;
	}
	public void setWriter(int writer) {
		this.writer = writer;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public int getViewCount() {
		return viewCount;
	}
	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	public String getWriterNickname() {
		return writerNickname;
	}
	public void setWriterNickname(String writerNickname) {
		this.writerNickname = writerNickname;
	}
	public int getClubId() {
		return clubId;
	}
	public void setClubId(int clubId) {
		this.clubId = clubId;
	}
	public String getClubName() {
		return clubName;
	}
	public void setClubName(String clubName) {
		this.clubName = clubName;
	}
	public int getBoardAllCnt() {
		return boardAllCnt;
	}
	public void setBoardAllCnt(int boardAllCnt) {
		this.boardAllCnt = boardAllCnt;
	}
	public int getSearchCnt() {
		return searchCnt;
	}
	public void setSearchCnt(int searchCnt) {
		this.searchCnt = searchCnt;
	}
	public int getSelectPageNo() {
		return selectPageNo;
	}
	public void setSelectPageNo(int selectPageNo) {
		this.selectPageNo = selectPageNo;
	}
	public int getRowCnt() {
		return rowCnt;
	}
	public void setRowCnt(int rowCnt) {
		this.rowCnt = rowCnt;
	}
	public int getBeginPageNo() {
		return beginPageNo;
	}
	public void setBeginPageNo(int beginPageNo) {
		this.beginPageNo = beginPageNo;
	}
	public int getEndPageNo() {
		return endPageNo;
	}
	public void setEndPageNo(int endPageNo) {
		this.endPageNo = endPageNo;
	}
	public int getBeginRowNo() {
		return beginRowNo;
	}
	public void setBeginRowNo(int beginRowNo) {
		this.beginRowNo = beginRowNo;
	}
	public int getEndRowNo() {
		return endRowNo;
	}
	public void setEndRowNo(int endRowNo) {
		this.endRowNo = endRowNo;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getSelectClubId() {
		return selectClubId;
	}
	public void setSelectClubId(int selectClubId) {
		this.selectClubId = selectClubId;
	}
	public Integer getPrev() {
		return prev;
	}
	public void setPrev(Integer prev) {
		this.prev = prev;
	}
	public Integer getNext() {
		return next;
	}
	public void setNext(Integer next) {
		this.next = next;
	}
	
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	@Override
	public String toString() {
		return "FreeboardDTO [id=" + id + ", title=" + title + ", content=" + content + ", writer=" + writer
				+ ", createdAt=" + createdAt + ", viewCount=" + viewCount + ", writerNickname=" + writerNickname
				+ ", clubId=" + clubId + ", clubName=" + clubName + ", image=" + image + ", boardAllCnt=" + boardAllCnt
				+ ", searchCnt=" + searchCnt + ", selectPageNo=" + selectPageNo + ", rowCnt=" + rowCnt
				+ ", beginPageNo=" + beginPageNo + ", endPageNo=" + endPageNo + ", beginRowNo=" + beginRowNo
				+ ", endRowNo=" + endRowNo + ", keyword=" + keyword + ", userId=" + userId + ", selectClubId="
				+ selectClubId + ", prev=" + prev + ", next=" + next + "]";
	}

    
    
    
}
