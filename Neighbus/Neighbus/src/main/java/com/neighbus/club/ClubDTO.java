package com.neighbus.club;

import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;

public class ClubDTO {
	private int ClubId; // club_memeber ID
	private int Id;
	private int writeId; // 동아리 생성자
	private String writerNickname; // 동아리 생성자 닉네임
	private String clubName; // 동아리이름 (club_name)
	private int city; // 소속 지역 (city, FK: 지역ID)
	private String cityName; // 지역 이름
	private String cityImg; // 동아리 이미지 이름
	private int provinceId; // 소속 지역 (city, FK: 지역ID)
	private String provinceName; // 지역 이름
	private String clubInfo; // 동아리 상세정보
	private LocalDateTime createdAt; // 생성일 (created_at)
	private String clubImg; // DB에 저장된 이미지 파일명
	private Integer category; // 카테고리 ID
	private String categoryName; // 카테고리 ID
	private int memberCount; //클럽 멤버 수

	// 이미지 업로드를 위한 필드
	private MultipartFile clubImage; // 업로드된 이미지 파일
	private String clubImageName; // 저장된 이미지 파일명

	// 페이징 처리를 위한 변수 추가
	private int selectPageNo = 1;
	private int rowCnt = 10;
	private int searchCnt;
	private int beginPageNo;
	private int endPageNo;
	private int beginRowNo;
	private int endRowNo;
	private String keyword;
	
	public int getClubId() {
		return ClubId;
	}
	public void setClubId(int clubId) {
		ClubId = clubId;
	}
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public int getWriteId() {
		return writeId;
	}
	public void setWriteId(int writeId) {
		this.writeId = writeId;
	}
	public String getWriterNickname() {
		return writerNickname;
	}
	public void setWriterNickname(String writerNickname) {
		this.writerNickname = writerNickname;
	}
	public String getClubName() {
		return clubName;
	}
	public void setClubName(String clubName) {
		this.clubName = clubName;
	}
	public int getCity() {
		return city;
	}
	public void setCity(int city) {
		this.city = city;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getCityImg() {
		return cityImg;
	}
	public void setCityImg(String cityImg) {
		this.cityImg = cityImg;
	}
	public int getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public String getClubInfo() {
		return clubInfo;
	}
	public void setClubInfo(String clubInfo) {
		this.clubInfo = clubInfo;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public String getClubImg() {
		return clubImg;
	}
	public void setClubImg(String clubImg) {
		this.clubImg = clubImg;
	}
	public Integer getCategory() {
		return category;
	}
	public void setCategory(Integer category) {
		this.category = category;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public int getMemberCount() {
		return memberCount;
	}
	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}
	public MultipartFile getClubImage() {
		return clubImage;
	}
	public void setClubImage(MultipartFile clubImage) {
		this.clubImage = clubImage;
	}
	public String getClubImageName() {
		return clubImageName;
	}
	public void setClubImageName(String clubImageName) {
		this.clubImageName = clubImageName;
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
	public int getSearchCnt() {
		return searchCnt;
	}
	public void setSearchCnt(int searchCnt) {
		this.searchCnt = searchCnt;
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
	@Override
	public String toString() {
		return "ClubDTO [ClubId=" + ClubId + ", Id=" + Id + ", writeId=" + writeId + ", writerNickname="
				+ writerNickname + ", clubName=" + clubName + ", city=" + city + ", cityName=" + cityName + ", cityImg="
				+ cityImg + ", provinceId=" + provinceId + ", provinceName=" + provinceName + ", clubInfo=" + clubInfo
				+ ", createdAt=" + createdAt + ", clubImg=" + clubImg + ", category=" + category + ", categoryName="
				+ categoryName + ", memberCount=" + memberCount + ", clubImage=" + clubImage + ", clubImageName="
				+ clubImageName + ", selectPageNo=" + selectPageNo + ", rowCnt=" + rowCnt + ", searchCnt=" + searchCnt
				+ ", beginPageNo=" + beginPageNo + ", endPageNo=" + endPageNo + ", beginRowNo=" + beginRowNo
				+ ", endRowNo=" + endRowNo + ", keyword=" + keyword + "]";
	}
	
}