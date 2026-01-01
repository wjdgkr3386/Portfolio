package com.neighbus.freeboard;

import java.time.LocalDateTime;

public class CommentDTO {
	private int id;             // 댓글 고유 ID
    private int freeboard;      // 게시글 ID (FK: freeboard)
    private Integer parent;     // 부모 댓글 ID (대댓글용. NULL 가능하므로 Integer)
    private int writer;         // 작성자 ID (FK: users)
    private String content;     // 댓글 내용
    private LocalDateTime createdAt; // 작성일 (created_at)
    private String writerUsername; // 작성자 로그인ID
    private String nickname;	//작성자 닉네임
    private String image;       //작성자 이미지
    
	public CommentDTO(int id, int freeboard, Integer parent, int writer, String content, LocalDateTime createdAt,
			String writerUsername) {
		super();
		this.id = id;
		this.freeboard = freeboard;
		this.parent = parent;
		this.writer = writer;
		this.content = content;
		this.createdAt = createdAt;
		this.writerUsername = writerUsername;
	}
	public CommentDTO() {}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFreeboard() {
		return freeboard;
	}

	public void setFreeboard(int freeboard) {
		this.freeboard = freeboard;
	}

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

	public int getWriter() {
		return writer;
	}

	public void setWriter(int writer) {
		this.writer = writer;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getWriterUsername() {
		return writerUsername;
	}

	public void setWriterUsername(String writerUsername) {
		this.writerUsername = writerUsername;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return "CommentDTO [id=" + id + ", freeboard=" + freeboard + ", parent=" + parent + ", writer=" + writer
				+ ", content=" + content + ", createdAt=" + createdAt + ", writerUsername=" + writerUsername
				+ ", nickname=" + nickname + ", image=" + image + "]";
	}
	
	
}
