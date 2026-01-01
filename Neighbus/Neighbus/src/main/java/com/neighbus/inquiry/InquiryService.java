package com.neighbus.inquiry; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InquiryService {

    private final InquiryMapper inquiryMapper;

    @Autowired
    public InquiryService(InquiryMapper inquiryMapper) {
        this.inquiryMapper = inquiryMapper;
    }

    public int registerInquiry(InquiryDTO dto, Integer currentUserId) {
        if (dto.getTitle() == null || dto.getContent() == null || currentUserId == null) {
            return 0; 
        }
        dto.setWriterId(currentUserId);
        return inquiryMapper.insertInquiry(dto);
    }
    
    public List<Map<String, Object>> getAllInquiries() {
        return inquiryMapper.selectAllInquiries();
    }

    public Map<String, Object> getInquiryById(int id) {
        Map<String, Object> inquiry = inquiryMapper.selectInquiryById(id);
        if (inquiry != null) {
            List<Map<String, Object>> comments = inquiryMapper.selectInquiryCommentByInquiryId(id);
            inquiry.put("comments", comments);
        }
        return inquiry;
    }

    public int updateInquiryState(int id, int state) {
        return inquiryMapper.updateInquiryState(id, state);
    }

    @Transactional
    public int addAnswer(int inquiryId, String content, int adminId) {
        // 1. 답변 댓글 추가
        Map<String, Object> params = new HashMap<>();
        params.put("inquiryId", inquiryId);
        params.put("content", content);
        params.put("writerId", adminId);
        inquiryMapper.insertInquiryComment(params);

        // 2. 문의 상태를 '답변 완료'(state=2)로 변경
        return inquiryMapper.updateInquiryState(inquiryId, 2);
    }

    @Transactional
    public int deleteInquiry(int id) {
        // 1. 먼저 문의에 달린 댓글들을 삭제
        inquiryMapper.deleteInquiryComments(id);

        // 2. 문의 삭제
        return inquiryMapper.deleteInquiry(id);
    }

    public List<Map<String, Object>> getInquiriesByWriterId(int writerId) {
        return inquiryMapper.selectInquiriesByWriterId(writerId);
    }
}