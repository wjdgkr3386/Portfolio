package com.neighbus.chat;

import java.time.LocalDateTime;

public class ChatMessageDTO {
	
	private Long messageId;
    private String roomId;
    private String sender;
    private String message;
    private String messageType; // ENUM 대신 String 저장
    private LocalDateTime createdAt;
    private String recipientUsername;
    private String senderNickname;
    private String senderImage;
	public Long getMessageId() {
		return messageId;
	}
	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
    public String getRecipientUsername() {
        return recipientUsername;
    }
    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }
    public String getSenderNickname() {
		return senderNickname;
	}
	public void setSenderNickname(String senderNickname) {
		this.senderNickname = senderNickname;
	}
	public String getSenderImage() {
		return senderImage;
	}
	public void setSenderImage(String senderImage) {
		this.senderImage = senderImage;
	}
    
    
    
    
}
