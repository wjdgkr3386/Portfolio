package com.neighbus.notice;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface NoticeMapper {
    // 전체 공지사항 조회
    List<Map<String, Object>> selectAllNotices();

    // 공지사항 상세 조회
    Map<String, Object> selectNoticeById(int id);

    // 공지사항 등록
    int insertNotice(NoticeDto dto);

    // 공지사항 수정
    int updateNotice(NoticeDto dto);

    // 공지사항 삭제
    int deleteNotice(int id);
}
