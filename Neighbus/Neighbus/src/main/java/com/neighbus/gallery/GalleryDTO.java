package com.neighbus.gallery;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class GalleryDTO {

	private String username;	//유저 아이디
	private String title;		//갤러리 게시글 제목
	private String content;		//갤러리 게시글 내용
	private int writer;			//작성자 ID
	private int galleryId;		//게시글 ID
	private int clubId;			//동아리 ID
	private List<MultipartFile> fileList = new ArrayList<MultipartFile>(); 	//받은 이미지 배열
	private List<String> fileNameList = new ArrayList<String>();			//받은 이미지 이름 배열
	private List<String> existingPathList = new ArrayList<String>();		//수정하기 전에 있던 이미지 경로
	private List<String> deletedExistingPathList = new ArrayList<String>();	//수정하기 전에 있던 이미지 경로 중 삭제할 이미지 경로
	private List<Integer> existingIds = new ArrayList<Integer>();
	
	
	//페이징
	private int searchCnt;		//갤러리 게시글 검색 개수
	private int selectPageNo;	//선택된 페이지 번호
	private int beginPageNo;	//시작 페이지 번호
	private int endPageNo;		//끝 페이지 번호
	private int beginRowNo;		//시작 행 번호
	private int endRowNo;		//시작 행 번호
	private int rowCnt;			//한 페이지에 보여질 행의 개수

	//검색
	private String keyword;		//검색 키워드
	private int id;				//로그인된 아이디
	//ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
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
	public int getGalleryId() {
		return galleryId;
	}
	public void setGalleryId(int galleryId) {
		this.galleryId = galleryId;
	}
	public int getClubId() {
		return clubId;
	}
	public void setClubId(int clubId) {
		this.clubId = clubId;
	}
	public List<MultipartFile> getFileList() {
		return fileList;
	}
	public void setFileList(List<MultipartFile> fileList) {
		this.fileList = fileList;
	}
	public List<String> getFileNameList() {
		return fileNameList;
	}
	public void setFileNameList(List<String> fileNameList) {
		this.fileNameList = fileNameList;
	}
	public List<String> getExistingPathList() {
		return existingPathList;
	}
	public void setExistingPathList(List<String> existingPathList) {
		this.existingPathList = existingPathList;
	}
	public List<String> getDeletedExistingPathList() {
		return deletedExistingPathList;
	}
	public void setDeletedExistingPathList(List<String> deletedExistingPathList) {
		this.deletedExistingPathList = deletedExistingPathList;
	}
	public List<Integer> getExistingIds() {
		return existingIds;
	}
	public void setExistingIds(List<Integer> existingIds) {
		this.existingIds = existingIds;
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
	public int getRowCnt() {
		return rowCnt;
	}
	public void setRowCnt(int rowCnt) {
		this.rowCnt = rowCnt;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "GalleryDTO [username=" + username + ", title=" + title + ", content=" + content + ", writer=" + writer
				+ ", galleryId=" + galleryId + ", clubId=" + clubId + ", fileList=" + fileList + ", fileNameList="
				+ fileNameList + ", existingPathList=" + existingPathList + ", deletedExistingPathList="
				+ deletedExistingPathList + ", searchCnt=" + searchCnt + ", selectPageNo=" + selectPageNo
				+ ", beginPageNo=" + beginPageNo + ", endPageNo=" + endPageNo + ", beginRowNo=" + beginRowNo
				+ ", endRowNo=" + endRowNo + ", rowCnt=" + rowCnt + ", keyword=" + keyword + ", id=" + id + "]";
	}
	
}
