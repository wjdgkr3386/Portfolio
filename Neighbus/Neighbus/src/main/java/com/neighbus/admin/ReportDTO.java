package com.neighbus.admin;

import java.time.LocalDateTime;

public class ReportDTO {
    private int id;
    private String type;
    private int reporterId;
    private Long targetId;
    private String reason;
    private LocalDateTime createdAt;
    private String status;
    private String reporterName; // 조인해서 가져올 신고자 이름
    private String targetUrl; // 신고 대상 컨텐츠의 URL
    private String targetAuthorName; // 신고 대상의 작성자 이름
    private String parentType; // 댓글의 부모 타입 (FREEBOARD 또는 GALLERY)
    private Long parentId; // 댓글의 부모 ID (게시글 또는 갤러리 ID)

    // Getters
    public int getId() {
        return id;
    }


    public String getType() {
        return type;
    }

    public int getReporterId() {
        return reporterId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public String getReporterName() {
        return reporterName;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setReporterId(int reporterId) {
        this.reporterId = reporterId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getTargetAuthorName() {
        return targetAuthorName;
    }

    public void setTargetAuthorName(String targetAuthorName) {
        this.targetAuthorName = targetAuthorName;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}