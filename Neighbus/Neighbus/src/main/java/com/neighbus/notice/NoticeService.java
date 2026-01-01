package com.neighbus.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NoticeService {

    private final NoticeMapper noticeMapper;

    @Autowired
    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    /**
     * 전체 공지사항 조회
     */
    public List<Map<String, Object>> getAllNotices() {
        return noticeMapper.selectAllNotices();
    }

    /**
     * 공지사항 상세 조회
     */
    public Map<String, Object> getNoticeById(int id) {
        return noticeMapper.selectNoticeById(id);
    }

    /**
     * 공지사항 등록
     */
    public int registerNotice(NoticeDto dto) {
        if (dto.getTitle() == null || dto.getContent() == null || dto.getWriter() == null) {
            return 0;
        }
        return noticeMapper.insertNotice(dto);
    }

    /**
     * 공지사항 수정
     */
    public int updateNotice(NoticeDto dto) {
        if (dto.getId() == null || dto.getTitle() == null || dto.getContent() == null) {
            return 0;
        }
        return noticeMapper.updateNotice(dto);
    }

    /**
     * 공지사항 삭제
     */
    public int deleteNotice(int id) {
        return noticeMapper.deleteNotice(id);
    }
}
