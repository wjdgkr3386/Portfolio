package com.neighbus.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping(value="/notice")
public class NoticeController {

	private final NoticeService noticeService;

	@Autowired
	public NoticeController(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	@GetMapping(value={"/",""})
	public String noticeForm() {
		System.out.println("NoticeController - noticeForm");
		return "notice/notice";
	}
	@GetMapping(value="/{id}")
	public String noticeDetail(@PathVariable("id") int id, Model model) {
		System.out.println("NoticeController - noticeDetail: " + id);

		Map<String, Object> notice = noticeService.getNoticeById(id);

		if (notice == null) {
			// 공지사항이 없으면 목록으로 리다이렉트
			return "redirect:/notice";
		}

		model.addAttribute("notice", notice);
		return "notice/noticeDetail";
	}
}
