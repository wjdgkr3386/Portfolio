package com.neighbus.inquiry; 

import com.neighbus.inquiry.InquiryDTO; 
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface InquiryMapper {
    int insertInquiry(InquiryDTO dto);

    List<Map<String, Object>> selectAllInquiries();

    Map<String, Object> selectInquiryById(int id);

    int updateInquiryState(@Param("id") int id, @Param("state") int state);

    int deleteInquiry(int id);

    // 문의 댓글 삭제
    int deleteInquiryComments(@Param("inquiryId") int inquiryId);

    // 답변(댓글) 추가
    int insertInquiryComment(Map<String, Object> params);

    // 특정 문의에 대한 답변(댓글) 조회
    List<Map<String, Object>> selectInquiryCommentByInquiryId(int inquiryId);

    // 사용자가 작성한 문의 목록 조회
    List<Map<String, Object>> selectInquiriesByWriterId(int writerId);
}